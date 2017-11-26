package com.linlif.mydownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;


import com.mydownload.main.DLDBManager;
import com.mydownload.main.DLInfo;
import com.mydownload.sys.DownloadBySy;
import com.mydownload.sys.DownloadRecord;
import com.mydownload.sys.Downloads;

import java.util.List;

public class Receiver extends BroadcastReceiver {
    public Receiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || "".equals(action)) {
            return;
        }

        if(action.equals("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED")) {    //下载通知栏点击

        } else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) { //网络状态变化
            List<DLInfo> infos = DLDBManager.getInstance(context).queryAllTaskInfo();
            List<DownloadRecord> downloadRecord = DownloadBySy.getInstance(context).queryDownloadRecords();
            //TODO 恢复下载


        }else if (action.equals(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                ){//系统下载完成
            final long downloadId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadRecord record = DownloadBySy.getInstance(context).queryDownloadRecord(downloadId);

            if (record == null){
                return;
            }
            int status = record.getStatus();
            //TODO:下载失败也有会进这个广播
            if(Downloads.Impl.isStatusInformational(status)){
                //从准备开始到下载中
            }else if (Downloads.Impl.isStatusSuccess(status)){
                //下载成功
            }else if (Downloads.Impl.isStatusClientError(status)){
                //客户端错误4xx-5xx
            }else if (Downloads.Impl.isStatusServerError(status)){
                //服务端错误,TODO:重试条件？
            }
        }


    }


}
