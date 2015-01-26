package com.code44.finance.ui.accounts.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.data.model.CurrencyFormat;
import com.code44.finance.ui.common.activities.BaseActivity;
import com.code44.finance.ui.common.presenters.ActivityPresenter;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.utils.interval.CurrentInterval;

import javax.inject.Inject;

public class AccountActivity extends BaseActivity {
    @Inject CurrencyFormat mainCurrencyFormat;
    @Inject CurrentInterval currentInterval;

    public static void start(Context context, String accountId) {
        final Intent intent = makeIntentForActivity(context, AccountActivity.class);
        AccountActivityPresenter.addExtras(intent, accountId);
        startActivity(context, intent);
    }

    @Override protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_account);
    }

    @Override protected ActivityPresenter onCreateActivityPresenter() {
//   TODO     return new AccountActivityPresenter(getEventBus(), mainCurrencyFormat, currentInterval);
        return null;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.Account;
    }
}
