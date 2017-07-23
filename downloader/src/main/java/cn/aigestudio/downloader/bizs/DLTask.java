package cn.aigestudio.downloader.bizs;


import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import cn.aigestudio.downloader.MLog;

import static cn.aigestudio.downloader.bizs.DLCons.Base.DEFAULT_TIMEOUT;
import static cn.aigestudio.downloader.bizs.DLCons.Base.LENGTH_PER_THREAD;
import static cn.aigestudio.downloader.bizs.DLCons.Base.MAX_REDIRECTS;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_MOVED_PERM;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_MOVED_TEMP;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_NOT_MODIFIED;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_OK;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_PARTIAL;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_SEE_OTHER;
import static cn.aigestudio.downloader.bizs.DLCons.Code.HTTP_TEMP_REDIRECT;

class DLTask implements Runnable, IDLThreadListener {
    private static final String TAG = DLTask.class.getSimpleName();

    private DLInfo info;
    private Context context;

    private int totalProgress;
    private int count;
    private int onErrorTime;
    private long lastTime = System.currentTimeMillis();

    DLTask(Context context, DLInfo info) {
        this.info = info;
        this.context = context;
        this.totalProgress = info.getCurrentBytes();
        if (DLDBManager.getInstance(context).queryTaskInfoByFileName(info.getFileName()) == null || !info.isResume())
            DLDBManager.getInstance(context).insertTaskInfo(info);
    }

    @Override
    public synchronized void onProgress(int progress) {
        totalProgress += progress;

        if (onErrorTime < 1) {
            info.setStatus(DLInfo.LOADING);
        }
        info.setCurrentBytes(totalProgress);
        DLDBManager.getInstance(context).updateTaskInfo(info);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            MLog.LogD(TAG, totalProgress + "");
            if (info.hasListener) info.getListener().onProgress(info);
            lastTime = currentTime;
        }

    }

    @Override
    public synchronized void onStop(DLThreadInfo threadInfo) {
        if (null == threadInfo) {
            DLManager.getInstance(context).removeDLTask(info.getFileName());
            //DLDBManager.getInstance(context).deleteTaskInfo(info.baseUrl);
            if (info.hasListener) {
                info.getListener().onProgress(info);
                info.getListener().onStop(info);
            }
            return;
        }
        DLDBManager.getInstance(context).updateThreadInfo(threadInfo);
        count++;
        if (count >= info.threads.size()) {
            MLog.LogD(TAG, "All the threads was stopped.");
            info.setCurrentBytes(totalProgress);
            DLManager.getInstance(context).addStopTask(info).removeDLTask(info.getFileName());
            info.setStatus(DLInfo.STOP);
            DLDBManager.getInstance(context).updateTaskInfo(info);
            count = 0;
            if (info.hasListener) info.getListener().onStop(info);
        }
    }

    @Override
    public synchronized void onFinish(DLThreadInfo threadInfo) {

        DLManager.getInstance(context).addDLTask();
        info.setStatus(DLInfo.SUCCESS);
        DLDBManager.getInstance(context).updateTaskInfo(info);
        if (null == threadInfo) {
            DLManager.getInstance(context).removeDLTask(info.getFileName());
            //DLDBManager.getInstance(context).deleteTaskInfo(info.baseUrl);
            if (info.hasListener) {
                info.getListener().onProgress(info);
                info.getListener().onFinish(info);
            }
            return;
        }
        info.removeDLThread(threadInfo);
        DLDBManager.getInstance(context).deleteThreadInfo(threadInfo.id);
        if (info.threads.isEmpty()) {
            MLog.LogD(TAG, "Task was finished.");
            DLManager.getInstance(context).removeDLTask(info.getFileName());
            //DLDBManager.getInstance(context).deleteTaskInfo(info.getFileName());
            if (info.hasListener) {
                info.getListener().onProgress(info);
                info.getListener().onFinish(info);
            }

        }
    }

    @Override
    public void onError(int code, String msg, DLThreadInfo threadInfo) {

        DLDBManager.getInstance(context).updateThreadInfo(threadInfo);
        if (onErrorTime >= 1) {
            return;
        }
        onErrorTime++;
        info.setStatus(DLInfo.FAILURE);
        info.setError(code);
        info.setErrorTime(info.getErrorTime() + 1);
        DLDBManager.getInstance(context).updateTaskInfo(info);
        DLManager.getInstance(context).dlStop(info);
        DLManager.getInstance(context).removeDLTask(info.getFileName());
        if (msg != null) {
            if (info.hasListener) info.getListener().onError(code, msg, info);
        }

    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (info.getRedirect() < MAX_REDIRECTS) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(info.getRealUrl()).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);
                addRequestHeaders(conn);

                final int code = conn.getResponseCode();
                int time = 0;
                switch (code) {
                    case HTTP_OK:
                    case HTTP_PARTIAL:
                        dlInit(conn, code);
                        return;
                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                    case HTTP_NOT_MODIFIED:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("Location");
                        if (TextUtils.isEmpty(location))
                            throw new DLException(
                                    "Can not obtain real url from location in header.");
                        info.setRealUrl(location);
                        info.setRedirect(time++);
                        continue;
                    default:
                        if (info.hasListener)
                            info.getListener().onError(code, conn.getResponseMessage(), info);
                        info.setStatus(DLInfo.FAILURE);
                        info.setError(code);
                        info.setErrorTime(info.getErrorTime() + 1);
                        DLDBManager.getInstance(context).updateTaskInfo(info);
                        DLManager.getInstance(context).removeDLTask(info.getFileName());
                        return;
                }
            } catch (Exception e) {
                if (info.hasListener) try {
                    info.setError(conn.getResponseCode());
                    info.getListener().onError(conn.getResponseCode(), e.toString(), info);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    info.setError(DLError.ERROR_OPEN_CONNECT);
                    info.getListener().onError(DLError.ERROR_OPEN_CONNECT, e1.toString(), info);
                }
                info.setStatus(DLInfo.FAILURE);
                info.setErrorTime(info.getErrorTime() + 1);
                DLDBManager.getInstance(context).updateTaskInfo(info);
                DLManager.getInstance(context).removeDLTask(info.getFileName());
                return;
            } finally {
                if (null != conn) conn.disconnect();
            }
        }
        throw new RuntimeException("Too many redirects");
    }

    private void dlInit(HttpURLConnection conn, int code) throws Exception {

        if (!readResponseHeaders(conn)) {

            // DLDBManager.getInstance(context).updateTaskInfo(info);
            DLManager.getInstance(context).reDownload(info);
            return;
        }
        String errorMsg = DLUtil.createFile(info.getDirPath(), info.getFileName());
        if (errorMsg != null) {
            throw new DLException(errorMsg);
        }

        info.setFile(new File(info.getDirPath(), info.getFileName()));
        if (info.getFile().exists() && info.getFile().length() == info.getTotalBytes()) {
        }
        info.setStatus(DLInfo.STARTED);
        DLDBManager.getInstance(context).updateTaskInfo(info);
        if (info.hasListener) {
            info.getListener().onStart(info);
        }
        switch (code) {
            case HTTP_OK:
            case HTTP_PARTIAL:
                if (info.getTotalBytes() <= 0) {
                    dlData(conn);
                    break;
                }
                if (info.isResume()) {
                    if (!info.threads.isEmpty()) {
                        for (DLThreadInfo threadInfo : info.threads) {
                            DLManager.getInstance(context)
                                    .addDLThread(new DLThread(threadInfo, info, this));
                        }
                        break;
                    }
                }

                dlDispatch();
                break;
        }
    }

    private void dlDispatch() {
        int threadSize;
        int threadLength = LENGTH_PER_THREAD;
        if (info.getTotalBytes() <= LENGTH_PER_THREAD) {
            threadSize = 2;
            threadLength = info.getTotalBytes() / threadSize;
        } else {
            threadSize = info.getTotalBytes() / LENGTH_PER_THREAD;
        }
        int remainder = info.getTotalBytes() % threadLength;
        for (int i = 0; i < threadSize; i++) {
            int start = i * threadLength;
            int end = start + threadLength - 1;
            if (i == threadSize - 1) {
                end = start + threadLength + remainder - 1;
            }
            DLThreadInfo threadInfo =
                    new DLThreadInfo(UUID.randomUUID().toString(), info.getRealUrl(), start, end);
            info.addDLThread(threadInfo);
            DLDBManager.getInstance(context).insertThreadInfo(threadInfo);
            DLManager.getInstance(context).addDLThread(new DLThread(threadInfo, info, this));
        }
    }

    private void dlData(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getInputStream();
        FileOutputStream fos = new FileOutputStream(info.getFile());
        byte[] b = new byte[4096];
        int len;
        while (!info.isStop() && (len = is.read(b)) != -1) {
            fos.write(b, 0, len);
            onProgress(len);
        }
        if (!info.isStop()) {
            onFinish(null);
        } else {
            onStop(null);
        }
        fos.close();
        is.close();
    }

    private void addRequestHeaders(HttpURLConnection conn) {
        for (DLHeader header : info.getRequestHeaders()) {
            conn.addRequestProperty(header.key, header.value);
        }
    }

    private boolean readResponseHeaders(HttpURLConnection conn) {

        info.setDisposition(conn.getHeaderField("Content-Disposition"));
        info.setLocation(conn.getHeaderField("Content-Location"));
        info.setLastModify(conn.getHeaderField("Last-modified"));
        info.seteTag(conn.getHeaderField("Etag"));
        info.setMimeType(DLUtil.normalizeMimeType(conn.getContentType()));

        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        if (TextUtils.isEmpty(transferEncoding)) {
            try {
                info.setTotalBytes(Integer.parseInt(conn.getHeaderField("Content-Length")));
            } catch (NumberFormatException e) {
                info.setTotalBytes(-1);
            }
        } else {
            info.setTotalBytes(-1);
        }
        if (info.getTotalBytes() == -1 && (TextUtils.isEmpty(transferEncoding) ||
                !transferEncoding.equalsIgnoreCase("chunked")))
            throw new RuntimeException("Can not obtain size of download file.");
        if (TextUtils.isEmpty(info.getFileName()))
            info.setFileName(DLUtil.obtainFileName(info.getRealUrl(), info.getDisposition(), info.getLocation()));

        if (info.isResume()) {
            DLInfo dlInfo = DLDBManager.getInstance(context).queryTaskInfoByFileName(info.getFileName());
            if (dlInfo != null) {
                if (dlInfo.geteTag() != null && info.geteTag() != null) {
                    if (!dlInfo.geteTag().equals(info.geteTag())) {
                        return false;
                    }
                } else {
                    if (dlInfo.getLastModify() != null)
                        if (!dlInfo.getLastModify().equals(info.getLastModify())) {
                            return false;
                        }
                }
            }

        }
        return true;
    }
}