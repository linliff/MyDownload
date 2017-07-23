package cn.aigestudio.downloader.bizs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static cn.aigestudio.downloader.bizs.DLCons.DBCons.TB_THREAD;
import static cn.aigestudio.downloader.bizs.DLCons.DBCons.TB_THREAD_END;
import static cn.aigestudio.downloader.bizs.DLCons.DBCons.TB_THREAD_ID;
import static cn.aigestudio.downloader.bizs.DLCons.DBCons.TB_THREAD_START;
import static cn.aigestudio.downloader.bizs.DLCons.DBCons.TB_THREAD_URL_REAL;

class ThreadDAO implements IThreadDAO {

    private Context context;
    public ThreadDAO(Context context) {
        this.context = context;
    }

    private final Object mLock = new Object();


    @Override
    public void insertThreadInfo(DLThreadInfo info) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("INSERT INTO " + TB_THREAD + "(" +
                                TB_THREAD_URL_REAL + ", " +
                                TB_THREAD_START + ", " +
                                TB_THREAD_END + ", " +
                                TB_THREAD_ID + ") VALUES (?,?,?,?)",
                        new Object[]{info.realUrl, info.start, info.end, info.id});
            } catch (Exception e) {
                Log.d("db", "thread insertThreadInfo ---  db exception");
                e.printStackTrace();
            } finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public void deleteThreadInfo(String id) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("DELETE FROM " + TB_THREAD + " WHERE " + TB_THREAD_ID + "=?", new String[]{id});
            } catch (Exception e) {
                Log.d("db", "thread deleteTaskInfo ---  db exception");
                e.printStackTrace();
            } finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public void deleteAllThreadInfo(String url) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("DELETE FROM " + TB_THREAD + " WHERE " + TB_THREAD_URL_REAL + "=?",
                        new String[]{url});
            }catch (Exception e) {
                Log.d("db", "thread deleteAllThreadInfo ---  db exception");
                e.printStackTrace();
            } finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public void updateThreadInfo(DLThreadInfo info) {
        synchronized (mLock) {
            SQLiteDatabase db = null;
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                db.execSQL("UPDATE " + TB_THREAD + " SET " +
                        TB_THREAD_START + "=? WHERE " +
                        TB_THREAD_URL_REAL + "=? AND " +
                        TB_THREAD_ID + "=?", new Object[]{info.start, info.realUrl, info.id});
            } catch (Exception e) {
                Log.d("db", "thread updateThreadInfo ---  db exception");
                e.printStackTrace();
            } finally {
                DLDBConManager.getInstance(context).closeDatabase();
            }
        }
    }

    @Override
    public DLThreadInfo queryThreadInfo(String id) {
        DLThreadInfo info = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        TB_THREAD_URL_REAL + ", " +
                        TB_THREAD_START + ", " +
                        TB_THREAD_END + " FROM " +
                        TB_THREAD + " WHERE " +
                        TB_THREAD_ID + "=?", new String[]{id});
                if (c.moveToFirst())
                    info = new DLThreadInfo(id, c.getString(0), c.getInt(1), c.getInt(2));
            }catch (Exception e) {
                Log.d("db", "thread queryThreadInfo ---  db exception");
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
    public List<DLThreadInfo> queryAllThreadInfo(String url) {
        List<DLThreadInfo> info = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mLock) {
            try {
                db = DLDBConManager.getInstance(context).openDatabase();
                c = db.rawQuery("SELECT " +
                        TB_THREAD_URL_REAL + ", " +
                        TB_THREAD_START + ", " +
                        TB_THREAD_END + ", " +
                        TB_THREAD_ID + " FROM " +
                        TB_THREAD + " WHERE " +
                        TB_THREAD_URL_REAL + "=?", new String[]{url});
                while (c.moveToNext())
                    info.add(new DLThreadInfo(c.getString(3), c.getString(0), c.getInt(1), c.getInt(2)));
            }catch (Exception e) {
                Log.d("db", "thread queryAllThreadInfo ---  db exception");
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
}