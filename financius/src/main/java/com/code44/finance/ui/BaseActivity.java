package com.code44.finance.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.code44.finance.R;
import com.code44.finance.ui.settings.SettingsActivity;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.GeneralPrefs;
import com.code44.finance.utils.IntervalHelper;
import com.code44.finance.utils.ToolbarHelper;

public class BaseActivity extends Activity {
    protected final EventBus eventBus = EventBus.get();
    protected final IntervalHelper intervalHelper = IntervalHelper.get();
    protected final GeneralPrefs generalPrefs = GeneralPrefs.get();

    protected ToolbarHelper toolbarHelper;

    protected static Intent makeIntent(Context context, Class activityClass) {
        return new Intent(context, activityClass);
    }

    protected static void start(Context context, Intent intent) {
        context.startActivity(intent);
    }

    protected static void startForResult(Fragment fragment, Intent intent, int requestCode) {
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Init
        toolbarHelper = new ToolbarHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toolbarHelper.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.start(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    protected IntervalHelper getIntervalHelper() {
        return intervalHelper;
    }

    protected GeneralPrefs getGeneralPrefs() {
        return generalPrefs;
    }
}