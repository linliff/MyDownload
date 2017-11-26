package com.mydownload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Method;

import com.mydownload.main.DLInfo;

public class DownloadNotifier {

    public static final String NOTIFY_ID = "notify_id";

    public static final int STARTED = 1;
    public static final int LOADING = 3;
    public static final int FAILURE = 2;
    public static final int STOPED =5;
    public static final int SUCCESS = 4;


    private static final String TAG = DownloadNotifier.class.getSimpleName();

    private static NotificationManager mNotificationManager;
    private static Notification.Builder builder;

    private static NotificationManager getNotificationManager(Context context) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private static Notification.Builder getNotificatinBuilder(
            Context context) {
        if (builder == null) {
            builder = new Notification.Builder(context);
        }
        return builder;
    }

    public static void cancelNotify(Context context, DLInfo downloadInfo) {
        int id = (int) downloadInfo.getId();
        if(true) {

            StackTraceElement[] stackTrace = new Exception().getStackTrace();
            for(StackTraceElement e: stackTrace) {
                Log.d(TAG, e.getClassName() + " \t" + e.getMethodName() + " \t" + e.getLineNumber());
            }

        }

        getNotificationManager(context).cancel(id);
    }

    public static void notifyProgress(Context context, DLInfo downloadInfo) {
        Notification notify = null;

        int notifyId = (int)downloadInfo.getId();
        String notifyTitle = downloadInfo.getNotifyTitle();
        long total = downloadInfo.getTotalBytes();
        long current = downloadInfo.getCurrentBytes();
        int progress = total == 0 ? 0 : (int) (current * 100 / total);

		//TODO
        int icon = android.R.drawable.stat_sys_download;
        if(icon == 0){
            icon = android.R.drawable.stat_sys_download_done;
            if (icon == 0){
                icon = android.R.drawable.ic_media_ff;
                if (icon == 0) {
                    return;
                }
            }
        }
        Intent intent = new Intent("tri.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
        intent.putExtra(NOTIFY_ID,notifyId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            try{
                notify = buildNotificationHigher(context, downloadInfo, notifyTitle, progress, icon, pendingIntent);
            } catch(Throwable e){
                    e.printStackTrace();

                try{
                    notify = buildNotificationLower(context, downloadInfo, notifyTitle, progress, icon, pendingIntent);
                }catch (Throwable e1){
                }
            }

        } else {
            try{
                notify = buildNotificationLower(context, downloadInfo, notifyTitle, progress, icon, pendingIntent);
            }catch (Throwable e){
            }
        }

        if (notify == null) return;

//        notify.flags = notify.flags|Notification.FLAG_AUTO_CANCEL;

        getNotificationManager(context).notify(notifyId, notify);
    }

    private static Notification buildNotificationLower(Context context, DLInfo downloadInfo, String notifyTitle, int progress, int icon, PendingIntent pendingIntent) {
        Notification notify;
        notify = new Notification();
//        notify.flags = Notification.DEFAULT_ALL;
        notify.icon = icon;

//        if(Build.VERSION.SDK_INT >= 11){
//            notify.largeIcon = PMResManage.pmCreateBitmapByBase(new OPRes_IC_DOWNLOAD());
//        }
//        int layId = ResUtil.getRes(context, "fl_custom_notify", "layout", context.getPackageName());
        int layId = android.R.layout.two_line_list_item;
        if(layId == 0){
            return null;
        }


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layId);
        //remoteViews.setImageViewResource(ResUtil.getRes(context, "iv_icon", "id", context.getPackageName()), icon);
//        remoteViews.setTextViewText(ResUtil.getRes(context, "tv_notify_title", "id", context.getPackageName()), notifyTitle);
        remoteViews.setTextViewText(android.R.id.text1,notifyTitle);
        switch (downloadInfo.getStatus()) {
            case STARTED:
                remoteViews.setTextViewText(android.R.id.text2,"Download Connecting");
//                remoteViews.setTextViewText(ResUtil.getRes(context, "tv_notify_status", "id", context.getPackageName()), "Download Connecting");
                break;
            case LOADING:
                remoteViews.setTextViewText(android.R.id.text2, "Down Loading "
                        + progress
                        + "%");
//                remoteViews.setTextViewText(ResUtil.getRes(context, "tv_notify_status", "id", context.getPackageName()), "Down Loading");
//                remoteViews.setProgressBar(ResUtil.getRes(context, "progressBar", "id", context.getPackageName()), 100, progress, false);
                break;
            case FAILURE:
                remoteViews.setTextViewText(android.R.id.text2,"Download Failed");
//                remoteViews.setTextViewText(ResUtil.getRes(context, "tv_notify_status", "id", context.getPackageName()), "Download Failed");
                break;
            case SUCCESS:
                remoteViews.setTextViewText(android.R.id.text2,"Download Success");
//                remoteViews.setTextViewText(ResUtil.getRes(context, "tv_notify_status", "id", context.getPackageName()), "Download Success");
                break;
        }

        try {
            Method deprecatedMethod = notify.getClass().getMethod("setLatestEventInfo",
                    Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            deprecatedMethod.invoke(notify, context, notifyTitle, null, pendingIntent);
        } catch (Exception e) {
        }
        notify.contentView = remoteViews;
        return notify;
    }


    private static Notification buildNotificationHigher(Context context, DLInfo downloadInfo, String notifyTitle, int progress, int icon, PendingIntent pendingIntent) {
        Notification.Builder builder = getNotificatinBuilder(context);

        builder.setContentTitle(notifyTitle)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        switch (downloadInfo.getStatus()) {
            case STARTED:
                builder.setContentText("Download Connecting");
                break;
            case LOADING:
                builder.setProgress(100, progress, false);
                builder.setContentText("");
                break;
            case FAILURE:
                builder.setContentText("Download Failed");
                break;
            case SUCCESS:
                builder.setContentText("Download Success");
                break;
        }

        return builder.getNotification();
    }

}
