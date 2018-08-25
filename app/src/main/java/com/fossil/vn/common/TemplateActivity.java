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
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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

import com.fossil.vn.InitActivity;
import com.fossil.vn.history.HistoryFragment;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.R;

import java.util.List;

public abstract class TemplateActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    final int REQUEST_LOCATION = 4;
    final int REQUEST_LOCATION_SETTING = 8;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

    protected LinearLayout normalView, initView;
    protected NavigationView nvwMainView;
    protected Toolbar tlrMainToolBar;
    protected DrawerLayout dltMainDrawer;
    protected ImageButton ibmDrawer, ibmBack, ibmHome;
    protected TextView txtTitle;
    protected LinearLayout titleOut;
    public FragmentBackListener fragmentBackListener = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        normalView = findViewById(R.id.ll_normal_view);
        initView = findViewById(R.id.ll_init_request);

        nvwMainView = findViewById(R.id.activity_template_navview);
        tlrMainToolBar = findViewById(R.id.activity_template_toolbar);
        dltMainDrawer = findViewById(R.id.activity_template_drawer);
        this.initActionBar();

        this.registerListenerBase();
        this.setOrientation();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onBackPressed() {
        Utils.hideSoftKeyboard(this);
        // cancell all requests
        //APIRepo.Factory.cancelAllRequests();

        if (dltMainDrawer.isDrawerOpen(GravityCompat.START)) {
            dltMainDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentBackListener != null) {
                fragmentBackListener.onFragmentBackPressed();
                fragmentBackListener = null;
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
        ibmDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.openDrawer(Gravity.LEFT);
                Utils.hideSoftKeyboard(activity);
            }
        });
        ibmBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TemplateActivity.this.onBackPressed();
            }
        });
        ibmHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TemplateActivity.this.goToHomePage();
            }
        });
    }

    protected void initActionBar() {
        setSupportActionBar(tlrMainToolBar);
        tlrMainToolBar.setNavigationIcon(null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_custom);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        ibmBack = findViewById(R.id.actionbar_backbutton);
        ibmHome = findViewById(R.id.actionbar_homebutton);
        ibmDrawer = findViewById(R.id.actionbar_drawerbutton);
        txtTitle = findViewById(R.id.actionbar_title);
        titleOut = findViewById(R.id.actionbar_titleOuter);
    }

    public String setActionBarTitle(boolean hideBackButton, boolean hideHomeButton, int titleID) {
        String title = getString(titleID);
        setActionBarTitle(hideBackButton, hideHomeButton, title);
        return title;
    }

    public String setActionBarTitle(boolean hideBackButton, boolean hideHomeButton, String title) {
        if (hideBackButton) {
            ibmBack.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleOut.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            ibmBack.setVisibility(View.VISIBLE);
        }
        if (hideHomeButton) {
            ibmHome.setVisibility(View.GONE);
        } else {
            ibmHome.setVisibility(View.VISIBLE);
        }
        txtTitle.setText(title);
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

    protected void goToHomePage() {
        if (this instanceof HistoryActivity && this.fragmentBackListener instanceof HistoryFragment) {
            return;
        }
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
    }

    private void renderNavigationMenus() {
        nvwMainView.setVerticalScrollBarEnabled(false);
        final TextView txtViewCurrency = nvwMainView.getHeaderView(0).findViewById(R.id.nav_header_currency);
        final ImageView imgViewCountry = nvwMainView.getHeaderView(0).findViewById(R.id.nav_header_country);
        final TextView txtViewLang = nvwMainView.getHeaderView(0).findViewById(R.id.nav_header_lang);

        updateViews();
    }

    private View setSingleNavigationMenu(final int viewID, int imgID, int mainTextID, boolean hasDivider) {
        View viwCustom = nvwMainView.getHeaderView(0).findViewById(viewID);
        ImageView icon = (ImageView) viwCustom.findViewById(R.id.item_menu_icon);
        icon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), imgID));
        TextView title = (TextView) viwCustom.findViewById(R.id.item_menu_title);
        title.setText(getString(mainTextID));

        if (!hasDivider) {
            viwCustom.findViewById(R.id.item_menudrawer_divider).setVisibility(View.GONE);
        }
        return viwCustom;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderNavigationMenus();
        refreshUI();
        boolean isReady = requestPermission();
        initCurrentState(isReady);
    }

    public interface FragmentBackListener {
        boolean onFragmentBackPressed();
    }

    public void hideMenuButton(boolean hide) {
        ibmDrawer.setVisibility(hide ? View.GONE : View.VISIBLE);
        ibmHome.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    private void refreshUI() {
    }

    private void updateViews() {

    }

    protected void refreshCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> stackedFragments = fragmentManager.getFragments();
        stackedFragments.get(stackedFragments.size() - 1).onStart();
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
}
