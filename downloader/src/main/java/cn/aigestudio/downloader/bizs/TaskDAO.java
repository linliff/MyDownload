package cn.aigestudio.downloader.bizs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static cn.aigestudio.downloader.bizs.DLCons.DBCons;

public class TaskDAO implements ITaskDAO {

    private Context context;
    public TaskDAO(Context context) {
        this.context = context;
    }

    private final Object mLock = new Object();

    @Override
    public void insertTaskInfo(DLInfo info) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("INSERT INTO " + DBCons.TB_TASK + "(" +
                                DBCons.TB_TASK_FILE_NAME + ", " +
                                DBCons.TB_TASK_KEY + ", " +
                                DBCons.TB_TASK_URL_BASE + ", " +
                                DBCons.TB_TASK_URL_REAL + ", " +
                                DBCons.TB_TASK_DIR_PATH + ", " +
                                DBCons.TB_TASK_MIME_TYPE + ", " +
                                DBCons.TB_TASK_ETAG + ", " +
                                DBCons.TB_TASK_DISPOSITION + ", " +
                                DBCons.TB_TASK_LOCATION + ", " +
                                DBCons.TB_TASK_Last_Modified + ", " +
                                DBCons.TB_TASK_STATUS + ", " +
                                DBCons.TB_TASK_CURRENT_BYTES + ", " +
                                DBCons.TB_TASK_TOTAL_BYTES + "," +
                                DBCons.TB_TASK_ERROR + "," +
                                DBCons.TB_TASK_ERROR_TIME + "," +
                                DBCons.TB_TASK_NOTIFY + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{info.getFileName(), info.getKey(), info.getBaseUrl(), info.getRealUrl(), info.getDirPath(),
                                info.getMimeType(), info.geteTag(), info.getDisposition(), info.getLocation(), info.getLastModify(), info.getStatus(),
                                info.getCurrentBytes(), info.getTotalBytes(), info.getError(), info.getErrorTime(), info.isShowModify()});

            }catch (Exception e){
                Log.d("db", "task insertTaskInfo ---  db exception");
                e.printStackTrace();
            }finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }

        }
    }

    @Override
    public void deleteTaskInfo(String key) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("DELETE FROM " + DBCons.TB_TASK + " WHERE " + DBCons.TB_TASK_FILE_NAME + "=?",
                        new String[]{key});
            }catch (Exception e){
                Log.d("db", "task deleteTaskInfo ---  db exception");
                e.printStackTrace();
            }finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public void updateTaskInfo(DLInfo info) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("UPDATE " + DBCons.TB_TASK + " SET " +
                        DBCons.TB_TASK_DISPOSITION + "=?," +
                        DBCons.TB_TASK_URL_BASE + "=?," +
                        DBCons.TB_TASK_URL_REAL + "=?," +
                        DBCons.TB_TASK_LOCATION + "=?," +
                        DBCons.TB_TASK_ETAG + "=?, " +
                        DBCons.TB_TASK_Last_Modified + "=?," +
                        DBCons.TB_TASK_STATUS + "=?," +
                        DBCons.TB_TASK_MIME_TYPE + "=?," +
                        DBCons.TB_TASK_TOTAL_BYTES + "=?," +
                        DBCons.TB_TASK_FILE_NAME + "=?," +
                        DBCons.TB_TASK_ERROR + "=?," +
                        DBCons.TB_TASK_ERROR_TIME + "=?," +
                        DBCons.TB_TASK_CURRENT_BYTES + "=? WHERE " +
                        DBCons.TB_TASK_KEY + "=?", new Object[]{info.getDisposition(), info.getBaseUrl(), info.getRealUrl(), info.getLocation(), info.geteTag(), info.getLastModify(), info.getStatus(),
                        info.getMimeType(), info.getTotalBytes(), info.getFileName(), info.getError(), info.getErrorTime(), info.getCurrentBytes(), info.getKey()});
            }catch (Exception e){
                Log.d("db", "task updateTaskInfo ---  db exception");
                e.printStackTrace();
            }finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public DLInfo queryTaskInfoByUrl(String url) {
        DLInfo info = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        DBCons.TB_TASK_URL_BASE + ", " +
                        DBCons.TB_TASK_URL_REAL + ", " +
                        DBCons.TB_TASK_DIR_PATH + ", " +
                        DBCons.TB_TASK_FILE_NAME + ", " +
                        DBCons.TB_TASK_MIME_TYPE + ", " +
                        DBCons.TB_TASK_ETAG + ", " +
                        DBCons.TB_TASK_DISPOSITION + ", " +
                        DBCons.TB_TASK_LOCATION + ", " +
                        DBCons.TB_TASK_Last_Modified + ", " +
                        DBCons.TB_TASK_STATUS + ", " +
                        DBCons.TB_TASK_CURRENT_BYTES + ", " +
                        DBCons.TB_TASK_NOTIFY + ", " +
                        DBCons.TB_TASK_KEY + ", " +
                        DBCons.TB_TASK_ERROR + ", " +
                        DBCons.TB_TASK_ERROR_TIME + ", " +
                        DBCons.TB_TASK_TOTAL_BYTES + " FROM " +
                        DBCons.TB_TASK + " WHERE " +
                        DBCons.TB_TASK_URL_BASE + "=?", new String[]{url});
                if (c.moveToFirst()) {
                    info = new DLInfo();
                    info.setBaseUrl(c.getString(0));
                    info.setRealUrl(c.getString(1));
                    info.setDirPath(c.getString(2));
                    info.setFileName(c.getString(3));
                    info.setMimeType(c.getString(4));
                    info.seteTag(c.getString(5));
                    info.setDisposition(c.getString(6));
                    info.setLocation(c.getString(7));
                    info.setLastModify(c.getString(8));
                    info.setStatus(c.getInt(9));
                    info.setCurrentBytes(c.getInt(10));
                    info.setShowModify(c.getInt(11) == 1);
                    info.setKey(c.getString(12));
                    info.setError(c.getInt(13));
                    info.setErrorTime(c.getInt(14));
                    info.setTotalBytes(c.getInt(15));
                }
            }catch (Exception e) {
                Log.d("db", "task queryTaskInfoByUrl ---  db exception");
                e.printStackTrace();
            } finally {
                if(c != null) {
                    c.close();
                }
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
        return info;

    }

    @Override
    public DLInfo queryTaskInfoByKey(String key) {
        DLInfo info = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        DBCons.TB_TASK_URL_BASE + ", " +
                        DBCons.TB_TASK_URL_REAL + ", " +
                        DBCons.TB_TASK_DIR_PATH + ", " +
                        DBCons.TB_TASK_FILE_NAME + ", " +
                        DBCons.TB_TASK_MIME_TYPE + ", " +
                        DBCons.TB_TASK_ETAG + ", " +
                        DBCons.TB_TASK_DISPOSITION + ", " +
                        DBCons.TB_TASK_LOCATION + ", " +
                        DBCons.TB_TASK_Last_Modified + ", " +
                        DBCons.TB_TASK_STATUS + ", " +
                        DBCons.TB_TASK_CURRENT_BYTES + ", " +
                        DBCons.TB_TASK_NOTIFY + ", " +
                        DBCons.TB_TASK_KEY + ", " +
                        DBCons.TB_TASK_ERROR + ", " +
                        DBCons.TB_TASK_ERROR_TIME + ", " +
                        DBCons.TB_TASK_TOTAL_BYTES + " FROM " +
                        DBCons.TB_TASK + " WHERE " +
                        DBCons.TB_TASK_KEY + "=?", new String[]{key});
                if (c.moveToFirst()) {
                    info = new DLInfo();
                    info.setBaseUrl(c.getString(0));
                    info.setRealUrl(c.getString(1));
                    info.setDirPath(c.getString(2));
                    info.setFileName(c.getString(3));
                    info.setMimeType(c.getString(4));
                    info.seteTag(c.getString(5));
                    info.setDisposition(c.getString(6));
                    info.setLocation(c.getString(7));
                    info.setLastModify(c.getString(8));
                    info.setStatus(c.getInt(9));
                    info.setCurrentBytes(c.getInt(10));
                    info.setShowModify(c.getInt(11) == 1);
                    info.setKey(c.getString(12));
                    info.setError(c.getInt(13));
                    info.setErrorTime(c.getInt(14));
                    info.setTotalBytes(c.getInt(15));
                }
            }catch (Exception e) {
                    Log.d("db", "task queryTaskInfoByKey ---  db exception");
                    e.printStackTrace();
                } finally {
                    if(c != null) {
                        c.close();
                    }
                    DLDBConManager.getInstance(context).closeDatabase();
                }
            }
            return info;
    }

    @Override
    public DLInfo queryTaskInfoByFileName(String name) {
        DLInfo info = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        DBCons.TB_TASK_URL_BASE + ", " +
                        DBCons.TB_TASK_URL_REAL + ", " +
                        DBCons.TB_TASK_DIR_PATH + ", " +
                        DBCons.TB_TASK_FILE_NAME + ", " +
                        DBCons.TB_TASK_MIME_TYPE + ", " +
                        DBCons.TB_TASK_ETAG + ", " +
                        DBCons.TB_TASK_DISPOSITION + ", " +
                        DBCons.TB_TASK_LOCATION + ", " +
                        DBCons.TB_TASK_Last_Modified + ", " +
                        DBCons.TB_TASK_STATUS + ", " +
                        DBCons.TB_TASK_CURRENT_BYTES + ", " +
                        DBCons.TB_TASK_NOTIFY + ", " +
                        DBCons.TB_TASK_KEY + ", " +
                        DBCons.TB_TASK_ERROR + ", " +
                        DBCons.TB_TASK_ERROR_TIME + ", " +
                        DBCons.TB_TASK_TOTAL_BYTES + " FROM " +
                        DBCons.TB_TASK + " WHERE " +
                        DBCons.TB_TASK_FILE_NAME + "=?", new String[]{name});
                if (c.moveToFirst()) {
                    info = new DLInfo();
                    info.setBaseUrl(c.getString(0));
                    info.setRealUrl(c.getString(1));
                    info.setDirPath(c.getString(2));
                    info.setFileName(c.getString(3));
                    info.setMimeType(c.getString(4));
                    info.seteTag(c.getString(5));
                    info.setDisposition(c.getString(6));
                    info.setLocation(c.getString(7));
                    info.setLastModify(c.getString(8));
                    info.setStatus(c.getInt(9));
                    info.setCurrentBytes(c.getInt(10));
                    info.setShowModify(c.getInt(11) == 1);
                    info.setKey(c.getString(12));
                    info.setError(c.getInt(13));
                    info.setErrorTime(c.getInt(14));
                    info.setTotalBytes(c.getInt(15));
                }
            }catch (Exception e) {
                Log.d("db", "task queryTaskInfoByFileName ---  db exception");
                e.printStackTrace();
            } finally {
                if(c != null) {
                    c.close();
                }
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
        return info;
    }


    @Override
    public List<DLInfo> queryAllTaskInfo() {
        List<DLInfo> infos = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        DBCons.TB_TASK_URL_BASE + ", " +
                        DBCons.TB_TASK_URL_REAL + ", " +
                        DBCons.TB_TASK_DIR_PATH + ", " +
                        DBCons.TB_TASK_FILE_NAME + ", " +
                        DBCons.TB_TASK_MIME_TYPE + ", " +
                        DBCons.TB_TASK_ETAG + ", " +
                        DBCons.TB_TASK_DISPOSITION + ", " +
                        DBCons.TB_TASK_LOCATION + ", " +
                        DBCons.TB_TASK_Last_Modified + ", " +
                        DBCons.TB_TASK_STATUS + ", " +
                        DBCons.TB_TASK_CURRENT_BYTES + ", " +
                        DBCons.TB_TASK_TOTAL_BYTES + ", " +
                        DBCons.TB_TASK_KEY + ", " +
                        DBCons.TB_TASK_ERROR + ", " +
                        DBCons.TB_TASK_ERROR_TIME + ", " +
                        DBCons.TB_TASK_NOTIFY + " FROM " +
                        DBCons.TB_TASK, null);
                while (c.moveToNext()) {
                    DLInfo info = new DLInfo();
                    infos.add(info);
                    info.setBaseUrl(c.getString(0));
                    info.setRealUrl(c.getString(1));
                    info.setDirPath(c.getString(2));
                    info.setFileName(c.getString(3));
                    info.setMimeType(c.getString(4));
                    info.seteTag(c.getString(5));
                    info.setDisposition(c.getString(6));
                    info.setLocation(c.getString(7));
                    info.setLastModify(c.getString(8));
                    info.setStatus(c.getInt(9));
                    info.setCurrentBytes(c.getInt(10));
                    info.setTotalBytes(c.getInt(11));
                    info.setKey(c.getString(12));
                    info.setError(c.getInt(13));
                    info.setErrorTime(c.getInt(14));
                    info.setShowModify(c.getInt(15) == 1);
                }
            }catch (Exception e) {
                Log.d("db", "task queryAllTaskInfo ---  db exception");
                e.printStackTrace();
            } finally {
                if(c != null) {
                    c.close();
                }
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
        return infos;
    }
}