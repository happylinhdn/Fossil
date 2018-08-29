package com.fossil.vn.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.fossil.vn.common.Constants;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(context.getApplicationContext());
            recordSessionRepository.getLastRecord()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<RecordSession>() {
                        @Override
                        public void accept(RecordSession recordSession) {
                            if (recordSession != null && !recordSession.isFinished()) {
                                context.startService(new Intent(context, TrackingService.class));
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.removeCallbacks(this);
                                        Intent intentStart = new Intent();
                                        intentStart.setAction(Constants.START_EVENT);
                                        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intentStart);
                                    }
                                }, 100);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {

                        }
                    });
        }
    }
}
