package com.mydownload;

import android.content.Context;

import java.util.List;

import com.mydownload.main.DLHeader;
import com.mydownload.main.DLInfo;
import com.mydownload.main.DLManager;
import com.mydownload.interfaces.IDListener;

/**
 * Created by lin on 2017/3/27.
 */
public class DownloadSelf {
    private static final String TAG = DownloadSelf.class.getSimpleName();
    private static DownloadSelf downloader = null;
    private static Context mContext;
    DLManager downloadManager;
    public static DownloadSelf getInstance(Context context) {
        if (downloader == null) {
            downloader = new DownloadSelf(context);
            mContext = context;
        }
        return downloader;
    }
    private DownloadSelf(Context context) {
        if (downloadManager == null) {
            downloadManager = DLManager.getInstance(context);
        }
    }
    public void doStartDownload(String downloadUrl, boolean showNotify , IDListener callBack, String fileName , String dir , String notifyTile , List<DLHeader> headers ,String key) {

        downloadManager.dlStart(downloadUrl,
                dir,
                fileName, headers, callBack , notifyTile ,showNotify ,key);
    }

    public DLInfo  getDownloadInfoByKey(String key){
        try {
            DLInfo downloadInfo = downloadManager.getDLInfoByKey(key);
            if (downloadInfo!=null){
                return downloadInfo;
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    public void stopDownload(String url){
        DLInfo dlInfo = downloadManager.getDLInfoByUrl(url);
        downloadManager.dlStop(dlInfo);

    }
}
