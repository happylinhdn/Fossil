package com.fossil.vn.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;

import com.fossil.vn.R;
import com.fossil.vn.common.Constants;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.history.HistoryFragment;

import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends TemplateActivity {
    BroadcastReceiver dataUpdateEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.DATA_UPDATE_EVENT)) {
                if (loadDataEvent != null) loadDataEvent.needLoadData();
            }
        }
    };

    public LoadDataEvent loadDataEvent;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fixed startActivity when open from icon-IMPORTANT
        if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        this.setActionBarTitle(false, true, "New Record");
        hideMenuButton(true);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        RecordFragment fragment = new RecordFragment();
        tx.replace(R.id.activity_template_frame, fragment);
        tx.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DATA_UPDATE_EVENT);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataUpdateEvent, intentFilter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(dataUpdateEvent);
    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    public void startTimeTrack() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if(loadDataEvent != null) loadDataEvent.updateTimeTick();
            }
        }, 0, 1000);
    }

    public void stopTimeTrack() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public interface LoadDataEvent {
        void needLoadData();

        void updateTimeTick();
    }
}
