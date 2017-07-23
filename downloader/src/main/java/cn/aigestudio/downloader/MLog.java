package cn.aigestudio.downloader;

import android.util.Log;


public class MLog {
    public static boolean LOG = false;
    public static void LogE(String tag,String msg){
        if(LOG){
            Log.e(tag,msg);
        }
    }
    public static void LogD(String tag,String msg){
        Log.d(tag,msg);

    }
}
