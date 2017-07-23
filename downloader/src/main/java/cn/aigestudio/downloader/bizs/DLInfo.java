package cn.aigestudio.downloader.bizs;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.aigestudio.downloader.interfaces.IDListener;

public class DLInfo {
    public final static int STARTED =1;
    public final static int FAILURE=2;
    public final static int LOADING =3;
    public final static int SUCCESS=4;
    public final static int STOP=5;

    private String key;
    private long id;
    private int totalBytes;
    private int currentBytes;
    private String fileName;
    private String dirPath;
    private String baseUrl;
    private String realUrl;
    private String lastModify;
    private int status;
    private String notifyTitle;
    private int redirect;
    private boolean isResume;
    private boolean showModify;
    private boolean isStop;
    private String mimeType;
    private String eTag;
    private String disposition;
    private String location;
    private List<DLHeader> requestHeaders;
    final List<DLThreadInfo> threads;
    private IDListener listener;
    private File file;
    private int error;
    private int errorTime;
    boolean hasListener;

    DLInfo() {
        threads = new ArrayList<>();
    }

    synchronized void addDLThread(DLThreadInfo info) {
        threads.add(info);
    }

    synchronized void removeDLThread(DLThreadInfo info) {
        threads.remove(info);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public int getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(int currentBytes) {
        this.currentBytes = currentBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRealUrl() {
        return realUrl;
    }

    public void setRealUrl(String realUrl) {
        this.realUrl = realUrl;
    }

    public String getLastModify() {
        return lastModify;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNotifyTitle() {
        return notifyTitle;
    }

    public void setNotifyTitle(String notifyTitle) {
        this.notifyTitle = notifyTitle;
    }

    public int getRedirect() {
        return redirect;
    }

    public void setRedirect(int redirect) {
        this.redirect = redirect;
    }

    public boolean isResume() {
        return isResume;
    }

    public void setResume(boolean resume) {
        isResume = resume;
    }

    public boolean isShowModify() {
        return showModify;
    }

    public void setShowModify(boolean showModify) {
        this.showModify = showModify;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<DLHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(List<DLHeader> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public IDListener getListener() {
        return listener;
    }

    public void setListener(IDListener listener) {
        this.listener = listener;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(int errorTime) {
        this.errorTime = errorTime;
    }

    @Override
    public String toString() {
        return "dlInfo{" +
                "key='" + key + '\'' +
                ", id=" + id +
                ", totalBytes='" + totalBytes + '\'' +
                ", currentBytes='" + currentBytes + '\'' +
                ", fileName='" + fileName + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", dirPath=" + dirPath +
                ", lastModify=" + lastModify +
                ", realUrl=" + realUrl +
                ", notifyTitle=" + notifyTitle +
                ", status=" + status +
                ", redirect=" + redirect +
                ", isResume=" + isResume +
                ", showModify=" + showModify +
                ", isStop=" + isStop +
                ", etag=" + eTag +
                ", errorTime=" + errorTime+

                '}';
    }
}