package com.mydownload.main;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.mydownload.MLog;
import com.mydownload.interfaces.IDListener;


public final class DLManager {
    private static final String TAG = DLManager.class.getSimpleName();
    private static final boolean DEBUG_DOWNLOAD = true;
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int POOL_SIZE = CORES + 1;
    private static final int POOL_SIZE_MAX = CORES * 2 + 1;

    private static final BlockingQueue<Runnable> POOL_QUEUE_TASK = new LinkedBlockingQueue<>(56);
    private static final BlockingQueue<Runnable> POOL_QUEUE_THREAD = new LinkedBlockingQueue<>(256);

    private static final ThreadFactory TASK_FACTORY = new ThreadFactory() {
        private final AtomicInteger COUNT = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "DLTask #" + COUNT.getAndIncrement());
        }
    };

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger COUNT = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "DLThread #" + COUNT.getAndIncrement());
        }
    };

    private static final ExecutorService POOL_TASK = new ThreadPoolExecutor(POOL_SIZE,
            POOL_SIZE_MAX, 3, TimeUnit.SECONDS, POOL_QUEUE_TASK, TASK_FACTORY);
    private static final ExecutorService POOL_Thread = new ThreadPoolExecutor(POOL_SIZE * 5,
            POOL_SIZE_MAX * 5, 1, TimeUnit.SECONDS, POOL_QUEUE_THREAD, THREAD_FACTORY);

    private static final ConcurrentHashMap<String, DLInfo> TASK_DLING = new ConcurrentHashMap<>();
    private static final List<DLInfo> TASK_PREPARE = Collections.synchronizedList(new ArrayList<DLInfo>());
    private static final ConcurrentHashMap<String, DLInfo> TASK_STOPPED = new ConcurrentHashMap<>();

    private static DLManager sManager;
    private static DLDBManager dldbManager;
    private static List<DLInfo> downloadInfoList;

    private Context context;

    private int maxTask = 10;

    private DLManager(Context context) {
        this.context = context;
    }

    public static DLManager getInstance(Context context) {
        if (null == sManager) {
            sManager = new DLManager(context);
            dldbManager = DLDBManager.getInstance(context);

        }
        if (downloadInfoList == null) {
            downloadInfoList = new ArrayList<DLInfo>();
        }
        return sManager;
    }

    /**
     * 设置并发下载任务最大值
     * The max task of DLManager.
     *
     * @param maxTask ...
     * @return ...
     */
    public DLManager setMaxTask(int maxTask) {
        this.maxTask = maxTask;
        return sManager;
    }


    /**
     * 开始一个下载任务
     * Start a download task.
     *
     * @param url      文件下载地址
     *                 Download url.
     * @param dir      文件下载后保存的目录地址，该值为空时会默认使用应用的文件缓存目录作为保存目录地址
     *                 The directory of download file. This parameter can be null, in this case we
     *                 will use cache dir of app for download path.
     * @param name     文件名，文件名需要包括文件扩展名，类似“AigeStudio.apk”的格式。该值可为空，为空时将由程
     *                 序决定文件名。
     *                 Name of download file, include extension like "AigeStudio.apk". This
     *                 parameter can be null, in this case the file name will be decided by program.
     * @param headers  请求头参数
     *                 Request header of http.
     * @param listener 下载监听器
     *                 Listener of download task.
     */
    public void dlStart(String url, String dir, String name, List<DLHeader> headers, IDListener listener , String notifyTitle , boolean showNotify ,String key) {
        boolean hasListener = listener != null;

        if (TextUtils.isEmpty(url)) {

            return;
        }
        if (TASK_DLING.containsKey(name)) {
        } else {
            DLInfo info;
            if (TASK_STOPPED.containsKey(name)) {
                info = TASK_STOPPED.remove(name);
            } else {
                info = dldbManager.queryTaskInfoByFileName(name);
                if (null != info) {
                    MLog.LogE(TAG, "Resume task from database.");
                    info.threads.clear();
                    info.threads.addAll(DLDBManager.getInstance(context).queryAllThreadInfo(url));
                }
            }
            if (null == info) {
                MLog.LogE(TAG, "New task will be start.");
                info = new DLInfo();
                info.setBaseUrl(url);
                info.setRealUrl(url);
                try {
                    info.setId(Integer.valueOf(key).intValue());
                }catch (Exception e){
                }
                if (TextUtils.isEmpty(dir)) {
                    dir = context.getCacheDir().getAbsolutePath();
                }
                info.setDirPath(dir);
                info.setFileName(name);
            } else {
                info.setResume(true);
                for (DLThreadInfo threadInfo : info.threads) {
                    threadInfo.isStop = false;
                }
            }
            info.setKey(key);
            info.setRedirect(0);
            info.setNotifyTitle(notifyTitle);
            info.setShowModify(showNotify);
            info.hasListener = hasListener;
            info.setRequestHeaders(DLUtil.initRequestHeaders(headers, info));
            info.setListener(listener);
            if (TASK_DLING.size() >= maxTask) {
                TASK_PREPARE.add(info);
            } else {
                MLog.LogE(TAG, "Prepare download from " + info.getBaseUrl());
                if (info.hasListener){
                    listener.onPrepare(info);
                }
                TASK_DLING.put(name, info);
                POOL_TASK.execute(new DLTask(context, info));
            }
        }
    }


    public void resumeDownload(DLInfo downloadInfo, IDListener callBack) {
        if (TASK_DLING.containsKey(downloadInfo.getFileName())) {
                MLog.LogE(TAG, "resumeDownload -- is downloading--  " );
            return;

        }
        //恢复暂停的下载
        if (TASK_STOPPED.containsKey(downloadInfo.getFileName())) {
            MLog.LogE(TAG, "Resume task from memory.");
            TASK_STOPPED.remove(downloadInfo.getFileName());
        } else {
            if (null != downloadInfo) {
                MLog.LogE(TAG, "Resume task from database.");
                downloadInfo.threads.clear();
                downloadInfo.threads.addAll(DLDBManager.getInstance(context).queryAllThreadInfo(downloadInfo.getRealUrl()));

            }
        }
        downloadInfo.setResume(true);
        for (DLThreadInfo threadInfo : downloadInfo.threads) {
            threadInfo.isStop = false;
        }
        downloadInfo.setRedirect(0);
        downloadInfo.setListener(callBack);
        downloadInfo.hasListener = callBack != null;
        if (TASK_DLING.size() >= maxTask) {
            MLog.LogE(TAG, "Downloading urls is out of range.");
            TASK_PREPARE.add(downloadInfo);
        } else {
            MLog.LogE(TAG, "resume download from " + downloadInfo.getRealUrl());
            MLog.LogE(TAG, "" + downloadInfo.getFileName());
            TASK_DLING.put(downloadInfo.getFileName(), downloadInfo);
            if (downloadInfo.hasListener){
                callBack.onPrepare(downloadInfo);
            }
            POOL_TASK.execute(new DLTask(context, downloadInfo));
        }


    }



    public void reDownload(DLInfo info){
        if (info.threads != null){
            info.threads.clear();
        }
        File file = new File(info.getDirPath(), info.getFileName());
        if (file.exists()) file.delete();

        dldbManager.deleteAllThreadInfo(info.getRealUrl());
        if (TASK_DLING.size() >= maxTask) {
            TASK_PREPARE.add(info);
        } else {
            TASK_DLING.put(info.getFileName(), info);
            List<DLHeader> headers = info.getRequestHeaders();
            info.setRequestHeaders(DLUtil.initRequestHeaders(headers, info));
            info.setResume(false);
            info.setRedirect(0);
            if (info.hasListener) {
                info.getListener().onPrepare(info);
            }
            POOL_TASK.execute(new DLTask(context, info));
        }
    }


    /**
     * 根据info暂停一个下载任务
     * Stop a download task according to url.
     *
     * @param info 文件下载信息
     *            DLInfo info.
     */
    public void dlStop(DLInfo info) {
        if (TASK_DLING.containsKey(info.getFileName())) {
            DLInfo info2 = TASK_DLING.get(info.getFileName());
            info2.setStop(true);
            if (!info2.threads.isEmpty()) {
                for (DLThreadInfo threadInfo : info2.threads) {
                    threadInfo.isStop = true;
                }
            }
        }

    }

    /**
     * 根据dlInfo取消一个下载任务
     * Cancel a download task according to url.
     *
     * @param dlInfo 文件信息
     *            DLInfo dlInfo.
     */
    public void dlCancel(DLInfo dlInfo) {
        if (dlInfo != null){
            String fileName = dlInfo.getFileName();
            String url = dlInfo.getRealUrl();
            if(dlInfo.getFileName()==null)return;
            dlStop(dlInfo);
            if (TASK_DLING.containsKey(fileName)) {
                TASK_DLING.remove(fileName);
            }else if (TASK_STOPPED.containsKey(fileName)){
                TASK_STOPPED.remove(fileName);
            }

            if (dlInfo.threads != null){
                dlInfo.threads.clear();
            }
            File file = new File(dlInfo.getDirPath(), dlInfo.getFileName());
            if (file.exists()) file.delete();

            dldbManager.deleteTaskInfo(fileName);
            dldbManager.deleteAllThreadInfo(url);
            if (dlInfo.hasListener){
                dlInfo.getListener().onCancel(dlInfo);
            }
        }

    }

    public DLInfo getDLInfoByUrl(String url) {
        return dldbManager.queryTaskInfoByUrl(url);
    }

    public DLInfo getDLInfoByKey(String key) {
        return dldbManager.queryTaskInfoByKey(key);
    }



    @Deprecated
    public DLDBManager getDLDBManager() {
        return DLDBManager.getInstance(context);
    }

    synchronized DLManager removeDLTask(String name) {
        if (TASK_DLING.containsKey(name)){
            TASK_DLING.remove(name);

        }
        return sManager;
    }

    synchronized DLManager addDLTask() {
        if (!TASK_PREPARE.isEmpty()) {
            //POOL_TASK.execute(new DLTask(context, TASK_PREPARE.remove(0) ));
        }
        return sManager;
    }

    synchronized DLManager addStopTask(DLInfo info) {
        TASK_STOPPED.put(info.getFileName(), info);
        return sManager;
    }

    synchronized DLManager addDLThread(DLThread thread) {
        POOL_Thread.execute(thread);
        return sManager;
    }


    public boolean hasDownloadingTask(){
        int count = TASK_DLING.size();
        return count == 0 ? false : true;

    }


    public boolean isDownloading(String name) {
        if(TASK_DLING.containsKey(name)) {
            if (DEBUG_DOWNLOAD) {
                MLog.LogE(TAG,  "isDownloading --- info: "   );

            }
            return true;
        }
        return false;
    }

    public List<DLInfo> getDownloadInfoList(){
        List<DLInfo> downloadInfoList = dldbManager.queryAllTaskInfo();
        return downloadInfoList;
    }

}