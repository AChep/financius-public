package com.code44.finance.ui.currencies;

import android.content.Context;

import com.code44.finance.R;
import com.code44.finance.ui.ModelEditActivity;
import com.code44.finance.ui.ModelFragment;

public class CurrencyEditActivity extends ModelEditActivity {
    public static void start(Context context, String currencyServerId) {
        startActivity(context, makeIntent(context, CurrencyEditActivity.class, currencyServerId));
    }

    @Override
    protected int getActionBarTitleResId() {
        return R.string.currency;
    }

    @Override
    protected ModelFragment createModelFragment(String modelServerId) {
        return CurrencyEditFragment.newInstance(modelServerId);
    }
}
