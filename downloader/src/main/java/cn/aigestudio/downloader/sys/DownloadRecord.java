package cn.aigestudio.downloader.sys;

public class DownloadRecord {

    private long _id;
    private String local_filename;
    private String title;
    private int status;
    private String downloadUrl;

    private String errorMsg;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getLocal_filename() {
        return local_filename;
    }

    public void setLocal_filename(String local_filename) {
        this.local_filename = local_filename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "DownloadRecordInfo{" +
                "_id=" + _id +
                ", local_filename='" + local_filename + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
