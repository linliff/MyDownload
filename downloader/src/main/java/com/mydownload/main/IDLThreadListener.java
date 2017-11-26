package com.mydownload.main;


interface IDLThreadListener {
    void onProgress(int progress);

    void onStop(DLThreadInfo threadInfo);

    void onFinish(DLThreadInfo threadInfo);

    void onError(int code , String msg,DLThreadInfo threadInfo);
}