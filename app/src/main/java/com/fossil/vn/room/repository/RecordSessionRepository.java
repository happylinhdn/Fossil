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
    /*public void searchTourPlacesByKeyword(String keyword, final ArrayList<DOPlace> bindingList, final LoadCallback callback) {
        Flowable<List<TourPlace>> results = null;
        if (TextUtils.isEmpty(keyword)) {
            results = mTourDao.searchTourPlacesByKeyword(InMem.doSettings.getCountry().toUpperCase());
        } else {
            results = mTourDao.searchTourPlacesByKeyword("%" +keyword + "%", InMem.doSettings.getCountry().toUpperCase());
        }
        results.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<TourPlace>>() {
                    @Override
                    public void accept(List<TourPlace> tourPlaces) throws Exception {
                        for (TourPlace place : tourPlaces) {
                            int placeId = place.getPlaceId();
                            String placeName = place.getPlaceName();
                            int subPlaceId = place.getSubPlaceId();
                            String subPlaceName = place.getSubPlaceName();
                            String countryCode = place.getCountryCode();
                            String countryName = place.getCountryName();
                            DOPlace doPlace = new DOPlace(placeId, placeName, subPlaceId, subPlaceName, countryCode, countryName);
                            bindingList.add(doPlace);
                        }
                        callback.onLoaded();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callback.onLoaded();
                    }
                });
    }*/

    public interface LoadCallback {
        public void onLoaded();
    }
}
