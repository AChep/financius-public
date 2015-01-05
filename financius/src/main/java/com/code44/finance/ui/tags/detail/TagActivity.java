package com.code44.finance.ui.tags.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.ui.common.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;

public class TagActivity extends BaseActivity {
    public static void start(Context context, String tagId) {
        final Intent intent = makeIntentForActivity(context, TagActivity.class);
        TagActivityPresenter.addExtras(intent, tagId);
        startActivity(context, intent);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_tag);
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.Tag;
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new TagActivityPresenter(getEventBus());
    }
}
