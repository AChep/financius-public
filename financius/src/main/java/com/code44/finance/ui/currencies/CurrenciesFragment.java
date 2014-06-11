package com.code44.finance.ui.currencies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.code44.finance.R;
import com.code44.finance.adapters.BaseModelsAdapter;
import com.code44.finance.adapters.CurrenciesAdapter;
import com.code44.finance.db.Tables;
import com.code44.finance.providers.CurrenciesProvider;
import com.code44.finance.ui.ModelListFragment;
import com.code44.finance.utils.GeneralPrefs;

public class CurrenciesFragment extends ModelListFragment implements CompoundButton.OnCheckedChangeListener {
    public static CurrenciesFragment newInstance() {
        final Bundle args = makeArgs();

        final CurrenciesFragment fragment = new CurrenciesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_currencies, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        final View separator_V = view.findViewById(R.id.separator_V);
        final Switch autoUpdateCurrencies_S = (Switch) view.findViewById(R.id.autoUpdateCurrencies_S);

        // Setup
        autoUpdateCurrencies_S.setChecked(GeneralPrefs.get().isAutoUpdateCurrencies());
        autoUpdateCurrencies_S.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.currencies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_rates:
                refreshRates();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void startModelActivity(Context context, View expandFrom, long modelId) {
        CurrencyActivity.start(context, expandFrom, modelId);
    }

    @Override
    protected void startModelEditActivity(Context context, View expandFrom, long modelId) {
        CurrencyEditActivity.start(context, expandFrom, modelId);
    }

    @Override
    protected BaseModelsAdapter createAdapter(Context context) {
        return new CurrenciesAdapter(context);
    }

    @Override
    protected Uri getUri() {
        return CurrenciesProvider.uriCurrencies();
    }

    @Override
    protected String getSortOrder() {
        return Tables.Currencies.IS_DEFAULT + " desc, " + Tables.Currencies.CODE;
    }

    private void refreshRates() {
        // TODO Refresh rates
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        GeneralPrefs.get().setAutoUpdateCurrencies(isChecked);
        if (isChecked) {
            refreshRates();
        }
    }
}