package com.fossil.vn.common;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import com.fossil.vn.history.HistoryFragment;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.R;

import java.util.List;

public abstract class TemplateActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    /*
    private void goToRegisterPage() {
        if (this instanceof RegisterActivity)
            return;
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
    }

    protected void goToSignInPage() {
        if (this instanceof SignInActivity)
            return;
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivityForResult(intent, 101);
    }

    private void goToBookingPage() {
        if (this instanceof OrderMainActivity)
            return;
        Intent intent = new Intent(this, OrderMainActivity.class);
        this.startActivity(intent);
    }

    private void goToFaqsPage(){
        if (this instanceof FaqsActivity)
            return;
        Intent intent = new Intent(this, FaqsActivity.class);
        this.startActivity(intent);
    }

    public void goToAboutUsPage(){
        if (this instanceof AboutUsActivity)
            return;
        Intent intent = new Intent(this, AboutUsActivity.class);
        this.startActivity(intent);
    }

    public void goToContactUsPage(){
        if (this instanceof ContactUsActivity)
            return;
        Intent intent = new Intent(this, ContactUsActivity.class);
        this.startActivity(intent);
    }

    private void goToProfileBasedPage(int mode) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("mode", mode);
        this.startActivity(intent);
    }

    public void goToNotificationPage(){
        if (this instanceof NotificationActivity)
            return;
        Intent intent = new Intent(this, NotificationActivity.class);
        this.startActivity(intent);
    }*/

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

    private void signOut() {

    }

    @Override
    public void onResume() {
        super.onResume();
        renderNavigationMenus();
        refreshUI();
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
        /*View view = this.setSingleNavigationMenu(R.id.navbar_header_home, R.drawable.ic_home, R.string.title_Home, true);
        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dltMainDrawer.closeDrawer(GravityCompat.START);
                        goToHomePage();
                    }
                }
        );
        view = this.setSingleNavigationMenu(R.id.navbar_header_contact, R.drawable.ic_contact, R.string.title_ContactUs, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToContactUsPage();
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_mybooking, R.drawable.ic_my_booking, R.string.title_MyBookings, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToBookingPage();
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_myprofile, R.drawable.ic_my_profile, R.string.title_MyProfile, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToProfileBasedPage(0);
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_changepass, R.drawable.ic_password, R.string.ChangePassword, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToProfileBasedPage(1);
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_signout, R.drawable.ic_sign_out, R.string.SignOut, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                signOut();
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_faq, R.drawable.ic_faq, R.string.title_FAQs, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToFaqsPage();
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_about, R.drawable.ic_eb, R.string.title_AboutEasybook, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToAboutUsPage();
            }
        });
        view = this.setSingleNavigationMenu(R.id.navbar_header_notification, R.drawable.ic_notif, R.string.title_Notifications, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dltMainDrawer.closeDrawer(GravityCompat.START);
                goToNotificationPage();
            }
        });*/
    }

    protected void refreshCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> stackedFragments = fragmentManager.getFragments();
        stackedFragments.get(stackedFragments.size() - 1).onStart();
    }

}
