package cn.aigestudio.downloader.interfaces;

import cn.aigestudio.downloader.bizs.DLInfo;

public interface IDListener {

    void onPrepare(DLInfo info);

    void onStart(DLInfo info);

    void onProgress(DLInfo info);

    void onStop(DLInfo info);

    void onFinish(DLInfo info);

    void onError(int error, String msg ,DLInfo info);

    void onCancel(DLInfo info);
}