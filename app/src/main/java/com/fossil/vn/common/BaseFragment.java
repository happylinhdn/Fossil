package com.fossil.vn.common;

import android.support.v4.app.Fragment;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fossil.vn.R;

/**
 * Created by user on 31/3/2017.
 */

public abstract class BaseFragment extends Fragment {

    public String title;
    public String customTag = "";

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            refreshUI();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (true && !enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.clear_stack_exit);
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI();
    }

    public void onUserLogEvent(boolean isLogin) {
        refreshUI();
    }

    public abstract void refreshUI();
}
