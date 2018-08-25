package com.fossil.vn.common;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.TemplateActivity;

import java.util.List;

public class Utils {
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
}
