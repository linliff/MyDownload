package cn.aigestudio.downloader.sys;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.aigestudio.downloader.bizs.DLHeader;
import cn.aigestudio.downloader.interfaces.SysListener;

public class DownloadBySy {

    private static final String TAG = DownloadBySy.class.getSimpleName();

    DownloadManager downloadManager;
    Context mContext;

    private DownloadBySy(Context context) {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            mContext = context;
        }
    }

    private static DownloadBySy mPackageDownloader = null;

    public static DownloadBySy getInstance(Context context) {
        if (mPackageDownloader == null) {
            mPackageDownloader = new DownloadBySy(context);
        }
        return mPackageDownloader;
    }


    public void doStartDownload(String downloadUrl, String title , boolean showNotify , String dir , String fileName , List<DLHeader> headers , SysListener callBack){

        DownloadManager.Request request = generateRequest(
                downloadUrl,
                dir,
                fileName,
                title,
                showNotify);
        for (int i = 0; i <headers.size() ; i++) {
            request.addRequestHeader(headers.get(i).key ,headers.get(i).value);
        }
        if (downloadManager != null) {
            long id = downloadManager.enqueue(request);
            callBack.onStart(id);
        }

    }

    private boolean isFolderCreate(String dir){
        File folder = new File(dir);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }
    private DownloadManager.Request generateRequest(String downloadUrl, String dir,String fileName, String title, boolean showNotify) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

        if (isFolderCreate(dir)) {
            try {
                request.setDestinationInExternalPublicDir("Juice/backup", fileName);
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
        request.setTitle(title);
        request.setDescription("Downloading");
        request.setShowRunningNotification(showNotify);
        return request;
    }


    public  DownloadRecord queryDownloadRecord(long downloadId){
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        DownloadRecord record = new DownloadRecord();
        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
                int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                if (filenameIndex == -1){
                    filenameIndex =cursor.getColumnIndex("_data");
                }
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
                if (idIndex != -1 && filenameIndex != -1 && statusIndex != -1 && uriIndex != -1) {
                    if (cursor.moveToFirst()) {
                        String downId = cursor.getString(idIndex);
                        String filename = cursor.getString(filenameIndex);
                        String status = cursor.getString(statusIndex);
                        String uri = cursor.getString(uriIndex);
                        record.set_id(Long.parseLong(downId));
                        record.setDownloadUrl(uri);
                        record.setLocal_filename(filename);
                        record.setStatus(Integer.parseInt(status));
                    }
                }
                cursor.close();
            }catch (Exception e){
                Log.e(TAG, "queryDownloadRecord Exception**" + e.toString());
            }
        }
        return record;
    }

    public  List<DownloadRecord> queryDownloadRecords(){
        List<DownloadRecord> records = new ArrayList<>();
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            DownloadRecord record;
            try {
	            int idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
	            int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
	            if (filenameIndex == -1){
	                filenameIndex =cursor.getColumnIndex("_data");
	            }
	            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
	            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
	            int error = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
	            if (idIndex != -1 && filenameIndex != -1 && statusIndex != -1 && uriIndex != -1) {
	                while (cursor.moveToNext()) {
	                    record = new DownloadRecord();
	                    String downId = cursor.getString(idIndex);
	                    String filename = cursor.getString(filenameIndex);
	                    String status = cursor.getString(statusIndex);
	                    String uri = cursor.getString(uriIndex);
	                    int errorMsg = cursor.getInt(error);
	                    record.set_id(Long.parseLong(downId));
	                    record.setDownloadUrl(uri);
	                    record.setErrorMsg(errorMsg+"");
	                    record.setLocal_filename(filename);
	                    record.setStatus(Integer.parseInt(status));
	                    records.add(record);
	                }
	            }
	            cursor.close();
            }catch (Exception e){
                Log.e(TAG, "queryDownloadRecord Exception**" + e.toString());
            }
        }
        return records;
    }
    public int removeDownload(long id){
        return downloadManager.remove(id);
    }

}
