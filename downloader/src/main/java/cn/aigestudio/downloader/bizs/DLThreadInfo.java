package cn.aigestudio.downloader.bizs;

class DLThreadInfo {
    String id;
    String realUrl;
    int start, end;
    boolean isStop;

    DLThreadInfo(String id, String realUrl, int start, int end) {
        this.id = id;
        this.realUrl = realUrl;
        this.start = start;
        this.end = end;
    }
}