package com.code44.finance.ui.tags;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;

import com.code44.finance.R;
import com.code44.finance.ui.ModelActivity;
import com.code44.finance.ui.ModelFragment;

public class TagActivity extends ModelActivity {
    public static void start(Context context, String tagServerId) {
        final Intent intent = makeIntent(context, TagActivity.class, tagServerId);
        start(context, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    protected int getActionBarTitleResId() {
        return R.string.tag;
    }

    @Override
    protected ModelFragment createModelFragment(String modelServerId) {
        return TagFragment.newInstance(modelServerId);
    }
}
