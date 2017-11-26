package com.mydownload.interfaces;

import com.mydownload.main.DLInfo;

public interface IDListener {

    void onPrepare(DLInfo info);

    void onStart(DLInfo info);

    void onProgress(DLInfo info);

    void onStop(DLInfo info);

    void onFinish(DLInfo info);

    void onError(int error, String msg ,DLInfo info);

    void onCancel(DLInfo info);
}