package com.code44.finance.ui.settings.about;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;

import com.code44.finance.R;
import com.code44.finance.ui.common.BaseActivity;
import com.code44.finance.utils.analytics.Analytics;

public class AboutActivity extends BaseActivity {
    public static void start(Context context) {
        startActivity(context, makeIntentForActivity(context, AboutActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        getSupportActionBar().setTitle(R.string.about);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, AboutFragment.newInstance())
                    .commit();
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        return true;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.About;
    }
}