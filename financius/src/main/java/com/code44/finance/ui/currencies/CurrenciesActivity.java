package com.code44.finance.ui.currencies;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.code44.finance.R;
import com.code44.finance.api.currencies.CurrenciesApi;
import com.code44.finance.api.currencies.ExchangeRatesRequest;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.BaseModel;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.providers.CurrenciesProvider;
import com.code44.finance.qualifiers.Main;
import com.code44.finance.ui.common.BaseModelsAdapter;
import com.code44.finance.ui.common.ModelListActivity;
import com.code44.finance.utils.GeneralPrefs;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;

public class CurrenciesActivity extends ModelListActivity implements CompoundButton.OnCheckedChangeListener {
    private final List<Currency> currencies = new ArrayList<>();

    @Inject GeneralPrefs generalPrefs;
    @Inject CurrenciesApi currenciesApi;
    @Inject @Main Currency mainCurrency;

    private SmoothProgressBar loadingView;

    public static void start(Context context) {
        startActivity(context, makeViewIntent(context, CurrenciesActivity.class));
    }

    public static void startSelect(Fragment fragment, int requestCode) {
        startActivityForResult(fragment, makeSelectIntent(fragment.getActivity(), CurrenciesActivity.class), requestCode);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        // Get views
        loadingView = (SmoothProgressBar) findViewById(R.id.loading);
        final View settingsContainerView = findViewById(R.id.settingsContainer);
        final Switch autoUpdateCurrenciesView = (Switch) findViewById(R.id.autoUpdateCurrencies);

        // Setup
        autoUpdateCurrenciesView.setChecked(generalPrefs.isAutoUpdateCurrencies());
        autoUpdateCurrenciesView.setOnCheckedChangeListener(this);
        if (getMode() != Mode.VIEW) {
            settingsContainerView.setVisibility(View.GONE);
        }
    }

    @Override public void onResume() {
        super.onResume();
        setRefreshing(false);
        getEventBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        getEventBus().unregister(this);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        getMenuInflater().inflate(R.menu.currencies, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_rates:
                refreshRates();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected BaseModelsAdapter createAdapter() {
        return new CurrenciesAdapter(this);
    }

    @Override protected CursorLoader getModelsCursorLoader() {
        return Tables.Currencies.getQuery().asCursorLoader(this, CurrenciesProvider.uriCurrencies());
    }

    @Override protected BaseModel modelFrom(Cursor cursor) {
        return Currency.from(cursor);
    }

    @Override protected void onModelClick(View view, int position, String modelId, BaseModel model) {
        CurrencyActivity.start(this, modelId);
    }

    @Override protected void startModelEdit(String modelId) {
        CurrencyEditActivity.start(this, modelId);
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        generalPrefs.setAutoUpdateCurrencies(isChecked);
        if (isChecked) {
            refreshRates();
        }
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_MODELS) {
            currencies.clear();
            if (data.moveToFirst()) {
                do {
                    currencies.add(Currency.from(data));
                } while (data.moveToNext());
            }
        }
        super.onLoadFinished(loader, data);
    }

    @Subscribe public void onRefreshFinished(ExchangeRatesRequest request) {
        setRefreshing(false);
    }

    private void refreshRates() {
        final List<String> fromCodes = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.isDefault()) {
                fromCodes.add(currency.getCode());
            }
        }

        if (!fromCodes.isEmpty()) {
            currenciesApi.updateExchangeRates(fromCodes, mainCurrency.getCode());
            setRefreshing(true);
        }
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshing) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.progressiveStart();
        } else {
            loadingView.progressiveStop();
            loadingView.setSmoothProgressDrawableCallbacks(new SmoothProgressDrawable.Callbacks() {
                @Override public void onStop() {
                    loadingView.setSmoothProgressDrawableCallbacks(null);
                    loadingView.setVisibility(View.GONE);
                }

                @Override public void onStart() {

                }
            });
        }
    }
}
