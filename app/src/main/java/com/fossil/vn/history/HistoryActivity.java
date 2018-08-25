package com.fossil.vn.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.fossil.vn.R;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.record.RecordFragment;

public class HistoryActivity extends TemplateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fixed startActivity when open from icon-IMPORTANT
        if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        this.setActionBarTitle(true, true, "History Record");
        hideMenuButton(true);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        HistoryFragment fragment = new HistoryFragment();
        tx.replace(R.id.activity_template_frame, fragment);
        tx.commit();
    }

    public void openNewRecord() {
        Fragment fragment = new RecordFragment();

        super.displaySelectedScreen(fragment, 1);
    }
}
