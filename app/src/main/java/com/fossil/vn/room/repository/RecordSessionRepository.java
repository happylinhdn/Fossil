package com.fossil.vn.room.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.fossil.vn.common.Node;
import com.fossil.vn.room.dao.RecordSessionDao;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.roomdb.MyDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RecordSessionRepository {
    private static RecordSessionRepository INSTANCE;
    public static RecordSessionRepository getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (RecordSessionRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RecordSessionRepository(context);

                }
            }
        }
        return INSTANCE;
    }

    private RecordSessionDao mRecordSessionDao;

    private RecordSessionRepository(Context context) {
        MyDatabase db = MyDatabase.getDatabase(context);
        mRecordSessionDao = db.recordSessionDao();
    }

    public void updateOrCreateRecord(RecordSession recordSession) {
        AgentAsyncTask task = new AgentAsyncTask();
        task.execute(recordSession);
    }

    public Single<RecordSession> getLastRecord() {
        Single<RecordSession> results = mRecordSessionDao.getLastRecord();
        return results;
    }

    public Flowable<List<RecordSession>> getAllRecord() {
        Flowable<List<RecordSession>> results = null;
        results = mRecordSessionDao.getAllRecord();
        return results;
    }

    class AgentAsyncTask extends AsyncTask<RecordSession, Void, Void> {

        @Override
        protected Void doInBackground(RecordSession... data) {
            mRecordSessionDao.insertRecord(data);
            return null;
        }
    }

    public interface LoadCallback {
        public void onLoaded();
    }
}
