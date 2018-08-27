package com.fossil.vn.common;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fossil.vn.history.HistoryFragment;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.R;
import com.fossil.vn.record.RecordActivity;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;
import com.fossil.vn.service.TrackingService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public abstract class TemplateActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    final int REQUEST_LOCATION = 4;
    final int REQUEST_LOCATION_SETTING = 8;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

    protected LinearLayout normalView, initView;
    protected NavigationView nvwMainView;
//    protected Toolbar tlrMainToolBar;
    protected DrawerLayout dltMainDrawer;
    public FragmentBackListener fragmentBackListener = null;
    public RecordSession lastRecord = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        normalView = findViewById(R.id.ll_normal_view);
        initView = findViewById(R.id.ll_init_request);

        nvwMainView = findViewById(R.id.activity_template_navview);
//        tlrMainToolBar = findViewById(R.id.activity_template_toolbar);
        dltMainDrawer = findViewById(R.id.activity_template_drawer);
//        this.initActionBar();

        this.registerListenerBase();
        this.setOrientation();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onBackPressed() {
        Utils.hideSoftKeyboard(this);

        if (dltMainDrawer.isDrawerOpen(GravityCompat.START)) {
            dltMainDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentBackListener != null) {
                if (fragmentBackListener.onFragmentBackPressed()) {
                    fragmentBackListener = null;
                }
            } else {
                int count = getSupportFragmentManager().getBackStackEntryCount();

                if (count == 0) {
                    super.onBackPressed();
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack();
                    fragmentManager.executePendingTransactions();
                    List<Fragment> fragments = fragmentManager.getFragments();
                    Utils.resetFragmentTitle(fragments, this, false);
                }
            }
        }
    }

    private void registerListenerBase() {
        nvwMainView.setNavigationItemSelectedListener(this);
        nvwMainView.setVerticalScrollBarEnabled(false);
        final Activity activity = this;
    }

    public String setActionBarTitle(boolean hideBackButton, boolean hideHomeButton, String title) {
        return title;
    }

    private void setOrientation() {
        boolean isTablet = false;
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void displaySelectedScreen(Fragment fragment, int animId) {
        String customTag = "";
        if (fragment instanceof BaseFragment) {
            customTag = ((BaseFragment) fragment).customTag;
        }
        if (fragment != null) {
            fragmentBackListener = null;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (animId == 1) {
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            } else if (animId == 2) {
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
            }
            ft.add(R.id.activity_template_frame, fragment, customTag);
            FragmentManager fragmentManager = getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments != null) {
                for (Fragment fragmentTemp : fragments) {
                    if (fragmentTemp != null && fragmentTemp.isVisible()) {
                        if (customTag.equals("EDIT PROFILE")) {
                            ft.remove(fragmentTemp);
                        } else {
                            ft.hide(fragmentTemp);
                        }
                    }
                }
            }

            ft.addToBackStack(customTag);
            ft.commit();
        }
        dltMainDrawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onResume() {
        super.onResume();
        lastRecord = null;
        // Check to redirect to Record fragment
        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getApplicationContext());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        lastRecord = recordSession;
                        if (recordSession != null && !recordSession.isFinished()) {
                            moveToRecordActivity();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        refreshUI();
        boolean isReady = requestPermission();
        initCurrentState(isReady);
    }

    public void startRecord() {
        Intent intent = new Intent(getApplicationContext(), TrackingService.class);
        startService(intent);
        lastRecord = null;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getApplicationContext());
                RecordSession recordSession = new RecordSession(Utils.getCurrent(), new ArrayList<Node>(), false);
                recordSessionRepository.updateOrCreateRecord(recordSession);
                lastRecord = recordSession;

                Intent intentStart = new Intent();
                intentStart.setAction(Constants.START_EVENT);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentStart);
            }
        }, 100);


    }

    public void pauseRecord() {
        Intent intent = new Intent();
        intent.setAction(Constants.PAUSE_EVENT);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);
    }

    public void resumeRecord() {
        Intent intentResume = new Intent(getApplicationContext(), TrackingService.class);
        startService(intentResume);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                Intent intent = new Intent();
                intent.setAction(Constants.RESUME_EVENT);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }, 100);


    }

    public void stopRecord() {
        // Stop service , update record with isFinished = true
        Intent intent = new Intent();
        intent.setAction(Constants.STOP_EVENT);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);

//        Intent intentStop = new Intent(getApplicationContext(), TrackingService.class);
//        stopService(intentStop);

        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getApplicationContext());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        if (recordSession == null) {
                            return;
                        }
                        recordSession.setFinished(true);
                        recordSessionRepository.updateOrCreateRecord(recordSession);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.DATA_UPDATE_EVENT));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    public interface FragmentBackListener {
        boolean onFragmentBackPressed();
    }

    public void hideMenuButton(boolean hide) {
    }

    private void refreshUI() {
    }

    private boolean requestPermission() {
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this)
                        .setMessage("You need to allow access to GPS location")
                        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(TemplateActivity.this, Constants.PERMISSIONS_LOCATION,
                                        REQUEST_LOCATION);
                            }
                        });
                adb.create().show();
                return false;
            }


            ActivityCompat.requestPermissions(this, Constants.PERMISSIONS_LOCATION, REQUEST_LOCATION);
            return false;
        }

        boolean isGpsEnable = Utils.isLocationEnable(this);
        if (!isGpsEnable) {
            requestGPSLocationSetting();
            return false;
        }
        return true;
    }

    private void requestGPSLocationSetting() {
        Toast.makeText(this, "Need to request GPS Location Permission", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SETTING);
    }

    /**
     * Called when startActivityForResult() call is completed. The result of
     * activation could be success of failure, mostly depending on user okaying
     * this app's request to administer the device.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION_SETTING:
                if (!Utils.isLocationEnable(this)) {
                    requestGPSLocationSetting();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(TemplateActivity.this, "TrackMe needs to access GPS permission", Toast.LENGTH_SHORT).show();
                    initCurrentState(false);
                }
                return;
            }
        }
    }

    private void initCurrentState(boolean isReady) {
        initView.setVisibility(isReady ? View.GONE : View.VISIBLE);
        normalView.setVisibility(isReady ? View.VISIBLE : View.GONE);
    }

    private void moveToRecordActivity() {
        if (this instanceof RecordActivity) {

        } else {
            Intent intent = new Intent(this, RecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
