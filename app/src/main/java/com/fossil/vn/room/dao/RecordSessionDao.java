package com.fossil.vn.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fossil.vn.room.entity.RecordSession;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface RecordSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(RecordSession... recordSessions);

    @Query("SELECT * FROM record_session ORDER BY _start_time DESC")
    Flowable<List<RecordSession>> getAllRecord();

    @Query("SELECT * FROM record_session ORDER BY _start_time DESC LIMIT 1")
    Single<RecordSession> getLastRecord();

}
