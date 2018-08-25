package com.fossil.vn.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.fossil.vn.R;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.history.HistoryFragment;

public class RecordActivity extends TemplateActivity {

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
}
