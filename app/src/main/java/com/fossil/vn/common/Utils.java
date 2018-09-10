package com.fossil.vn.common;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.fossil.vn.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static long getCurrentTime() {
        return Calendar.getInstance(Locale.US).getTimeInMillis();
    }

    public static void hideSoftKeyboard(Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),0);
    }

    public static void resetFragmentTitle(List<Fragment> fragments, TemplateActivity activity, boolean resetRightMenu) {
        if (activity == null)
            return;
        if (fragments != null) {
            for (Fragment fragmentTemp : fragments) {
                if (fragmentTemp != null && fragmentTemp.isVisible()) {
                    if(fragmentTemp instanceof BaseFragment) {
                        activity.setActionBarTitle(false, false, ((BaseFragment) fragmentTemp).title);
                    }
                }
            }
        }
        if (resetRightMenu) {
            ImageButton ibmDrawer = (ImageButton) activity.findViewById(R.id.actionbar_drawerbutton);
            ibmDrawer.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return (isGPSEnabled || isNetworkEnabled);
    }

    public static String getStringFromSecond(long seconds) {
        long value = seconds;
        long elapsedHours = value / Constants.HOURS_IN_SECOND;
        value = value % Constants.HOURS_IN_SECOND;
        long elapsedMins = value / Constants.MINUTES_IN_SECOND;
        value = value % Constants.MINUTES_IN_SECOND;


        return String.format("%02d:%02d:%02d", elapsedHours, elapsedMins, value);
    }
}
