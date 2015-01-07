package com.code44.finance.ui.categories.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.data.model.Currency;
import com.code44.finance.qualifiers.Main;
import com.code44.finance.ui.common.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.CurrentInterval;
import com.code44.finance.utils.analytics.Analytics;

import javax.inject.Inject;

public class CategoryActivity extends BaseActivity {
    @Inject CurrentInterval currentInterval;
    @Inject @Main Currency mainCurrency;

    public static void start(Context context, String categoryId) {
        final Intent intent = makeIntentForActivity(context, CategoryActivity.class);
        CategoryActivityPresenter.addExtras(intent, categoryId);
        startActivity(context, intent);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_category);
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.Category;
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
        return new CategoryActivityPresenter(getEventBus(), currentInterval, mainCurrency);
    }
}
