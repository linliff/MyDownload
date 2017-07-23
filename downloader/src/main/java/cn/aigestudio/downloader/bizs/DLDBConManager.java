package cn.aigestudio.downloader.bizs;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class DLDBConManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DLDBConManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            synchronized (DLDBConManager.class) {
                if(instance == null) {
                    instance = new DLDBConManager();
                    mDatabaseHelper = helper;
                }
            }
        }
    }

    public static synchronized DLDBConManager getInstance(Context context) {
        if (instance == null) {
            initializeInstance(new DLDBHelper(context));
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }

        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            if(mDatabase != null) {
                mDatabase.close();
            }
        }
    }
}
