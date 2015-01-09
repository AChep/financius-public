package com.code44.finance.ui.currencies.detail;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.api.currencies.CurrenciesApi;
import com.code44.finance.api.currencies.ExchangeRateRequest;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.providers.AccountsProvider;
import com.code44.finance.data.providers.CurrenciesProvider;
import com.code44.finance.ui.common.BaseActivity;
import com.code44.finance.ui.common.presenters.ModelActivityPresenter;
import com.code44.finance.ui.currencies.edit.CurrencyAccountsAdapter;
import com.code44.finance.ui.currencies.edit.CurrencyEditActivity;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.MoneyFormatter;
import com.squareup.otto.Subscribe;

class CurrencyActivityPresenter extends ModelActivityPresenter<Currency> implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int LOADER_ACCOUNTS = 1;

    private final CurrenciesApi currenciesApi;
    private final Currency mainCurrency;

    private TextView codeTextView;
    private TextView formatTextView;
    private TextView exchangeRateTextView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CurrencyAccountsAdapter adapter;

    protected CurrencyActivityPresenter(EventBus eventBus, CurrenciesApi currenciesApi, Currency mainCurrency) {
        super(eventBus);
        this.currenciesApi = currenciesApi;
        this.mainCurrency = mainCurrency;
    }

    @Override public void onActivityCreated(BaseActivity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);

        // Get views
        codeTextView = findView(activity, R.id.codeTextView);
        formatTextView = findView(activity, R.id.formatTextView);
        exchangeRateTextView = findView(activity, R.id.exchangeRateTextView);
        swipeRefreshLayout = findView(activity, R.id.swipeRefreshLayout);
        final ImageView refreshRateButton = findView(activity, R.id.refreshRateButton);
        final ListView listView = findView(activity, R.id.listView);

        // Setup
        refreshRateButton.setVisibility(!mainCurrency.getId().equals(getModelId()) ? View.VISIBLE : View.GONE);
        refreshRateButton.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        adapter = new CurrencyAccountsAdapter(activity);
        listView.setAdapter(adapter);
    }

    @Override public void onActivityResumed(BaseActivity activity) {
        super.onActivityResumed(activity);
        getEventBus().register(this);
    }

    @Override public void onActivityPaused(BaseActivity activity) {
        super.onActivityPaused(activity);
        getEventBus().unregister(this);
    }

    @Override public boolean onActivityPrepareOptionsMenu(BaseActivity activity, Menu menu) {
        super.onActivityPrepareOptionsMenu(activity, menu);
        menu.findItem(R.id.action_delete).setVisible(!mainCurrency.getId().equals(getModelId()));
        return true;
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ACCOUNTS) {
            return Tables.Accounts.getQuery().asCursorLoader(getActivity(), AccountsProvider.uriAccounts());
        }
        return super.onCreateLoader(id, args);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ACCOUNTS) {
            adapter.swapCursor(data);
            return;
        }
        super.onLoadFinished(loader, data);
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ACCOUNTS) {
            adapter.swapCursor(null);
            return;
        }
        super.onLoaderReset(loader);
    }

    @Override protected CursorLoader getModelCursorLoader(Context context, String modelId) {
        return Tables.Currencies.getQuery().asCursorLoader(context, CurrenciesProvider.uriCurrency(modelId));
    }

    @Override protected Currency getModelFrom(Cursor cursor) {
        return Currency.from(cursor);
    }

    @Override protected void onModelLoaded(Currency currency) {
        adapter.setCurrency(currency);
        codeTextView.setText(currency.getCode());
        if (currency.isDefault()) {
            exchangeRateTextView.setText(R.string.main_currency);
        } else {
            exchangeRateTextView.setText(String.valueOf(currency.getExchangeRate()));
        }
        formatTextView.setText(MoneyFormatter.format(currency, 100000));

        getActivity().supportInvalidateOptionsMenu();

        // Loader
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ACCOUNTS, null, this);
    }

    @Override protected void startModelEdit(Context context, String modelId) {
        CurrencyEditActivity.start(context, modelId);
    }

    @Override protected Uri getDeleteUri() {
        return CurrenciesProvider.uriCurrencies();
    }

    @Override protected Pair<String, String[]> getDeleteSelection(String modelId) {
        return Pair.create(Tables.Currencies.ID + "=?", new String[]{String.valueOf(modelId)});
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refreshRateButton:
                onRefresh();
                break;
        }
    }

    @Override public void onRefresh() {
        currenciesApi.updateExchangeRate(getStoredModel().getCode(), mainCurrency.getCode());
        setRefreshing(true);
    }

    @Subscribe public void onRefreshFinished(ExchangeRateRequest request) {
        if (getStoredModel() != null && getStoredModel().getCode().equals(request.getFromCode())) {
            setRefreshing(false);
        }
    }

    private void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }
}
