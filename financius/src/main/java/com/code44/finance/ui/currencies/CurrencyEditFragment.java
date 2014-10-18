package com.code44.finance.ui.currencies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.api.currencies.CurrenciesApi;
import com.code44.finance.api.currencies.ExchangeRateRequest;
import com.code44.finance.common.model.DecimalSeparator;
import com.code44.finance.common.model.GroupSeparator;
import com.code44.finance.common.model.SymbolPosition;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.providers.CurrenciesProvider;
import com.code44.finance.qualifiers.Main;
import com.code44.finance.ui.ModelEditFragment;
import com.code44.finance.utils.MoneyFormatter;
import com.squareup.otto.Subscribe;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;

public class CurrencyEditFragment extends ModelEditFragment<Currency> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int LOADER_CURRENCIES = 1;

    @Inject CurrenciesApi currenciesApi;
    @Inject @Main Currency mainCurrency;

    private SmoothProgressBar loading_SPB;
    private AutoCompleteTextView code_ET;
    private Button thousandsSeparator_B;
    private Button decimalSeparator_B;
    private Button decimalsCount_B;
    private TextView code_TV;
    private TextView symbol_TV;
    private EditText symbol_ET;
    private Button symbolPosition_B;
    private EditText exchangeRate_ET;
    private ListPopupWindow listPopupWindow_LPW;
    private View mainCurrencyContainer_V;
    private View exchangeRateContainer_V;
    private CheckBox isDefault_CB;
    private Set<String> existingCurrencyCodes = new HashSet<>();

    public CurrencyEditFragment() {
    }

    public static CurrencyEditFragment newInstance(String currencyServerId) {
        final Bundle args = makeArgs(currencyServerId);

        final CurrencyEditFragment fragment = new CurrencyEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_currency_edit, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get view
        loading_SPB = (SmoothProgressBar) view.findViewById(R.id.loading_SPB);
        code_ET = (AutoCompleteTextView) view.findViewById(R.id.code_ET);
        thousandsSeparator_B = (Button) view.findViewById(R.id.thousandsSeparator_B);
        decimalSeparator_B = (Button) view.findViewById(R.id.decimalSeparator_B);
        decimalsCount_B = (Button) view.findViewById(R.id.decimalsCount_B);
        code_TV = (TextView) view.findViewById(R.id.code_TV);
        symbol_TV = (TextView) view.findViewById(R.id.symbol_TV);
        symbol_ET = (EditText) view.findViewById(R.id.symbol_ET);
        symbolPosition_B = (Button) view.findViewById(R.id.symbolPosition_B);
        exchangeRate_ET = (EditText) view.findViewById(R.id.exchangeRate_ET);
        mainCurrencyContainer_V = view.findViewById(R.id.mainCurrencyContainer_V);
        exchangeRateContainer_V = view.findViewById(R.id.exchangeRateContainer_V);
        isDefault_CB = (CheckBox) view.findViewById(R.id.isDefault_CB);
        final TextView currentMainCurrency_TV = (TextView) view.findViewById(R.id.currentMainCurrency_TV);
        final ImageButton refreshRate_B = (ImageButton) view.findViewById(R.id.refreshRate_B);

        // Setup
        prepareCurrenciesAutoComplete();
        currentMainCurrency_TV.setText(getString(R.string.f_current_main_currency_is_x, mainCurrency.getCode()));
        decimalsCount_B.setOnClickListener(this);
        thousandsSeparator_B.setOnClickListener(this);
        decimalSeparator_B.setOnClickListener(this);
        symbolPosition_B.setOnClickListener(this);
        isDefault_CB.setOnCheckedChangeListener(this);
        refreshRate_B.setOnClickListener(this);
        symbol_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //noinspection ConstantConditions
                model.setSymbol(symbol_ET.getText().toString());
                updateFormatView();

                updateSymbolTitlePosition();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        code_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCodeTitlePosition();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Loader
        getLoaderManager().initLoader(LOADER_CURRENCIES, null, this);
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

    @Override public boolean onSave(Context context, Currency model) {
        boolean canSave = true;

        if (TextUtils.isEmpty(model.getCode()) || model.getCode().length() != 3 || model.getCode().equals(mainCurrency.getCode())) {
            canSave = false;
            // TODO Show error
        }

        if (canSave) {
            DataStore.insert().values(model.asValues()).into(context, CurrenciesProvider.uriCurrencies());
        }

        return canSave;
    }

    @Override protected void ensureModelUpdated(Currency model) {
        model.setCode(code_ET.getText().toString());
        model.setSymbol(symbol_ET.getText().toString());
        double exchangeRate;
        try {
            exchangeRate = Double.parseDouble(exchangeRate_ET.getText().toString());
        } catch (Exception e) {
            exchangeRate = 1.0;
        }
        model.setExchangeRate(exchangeRate);
    }

    @Override protected Currency getModelFrom(Cursor cursor) {
        return Currency.from(cursor);
    }

    @Override protected void onModelLoaded(Currency model) {
        symbol_ET.setText(model.getSymbol());
        code_ET.setText(model.getCode());
        thousandsSeparator_B.setText(getGroupSeparatorExplanation(model.getGroupSeparator()));
        decimalSeparator_B.setText(getDecimalSeparatorExplanation(model.getDecimalSeparator()));
        decimalsCount_B.setText(String.valueOf(model.getDecimalCount()));
        exchangeRate_ET.setText(String.valueOf(model.getExchangeRate()));
        isDefault_CB.setChecked(model.isDefault());
        updateFormatView();
        updateCodeTitlePosition();
        updateSymbolTitlePosition();

        code_ET.setEnabled(isNewModel());
        mainCurrencyContainer_V.setVisibility(mainCurrency.getId().equals(model.getId()) ? View.GONE : View.VISIBLE);
        exchangeRateContainer_V.setVisibility(model.isDefault() ? View.GONE : View.VISIBLE);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_CURRENCIES) {
            return Tables.Currencies.getQuery().asCursorLoader(getActivity(), CurrenciesProvider.uriCurrencies());
        }
        return super.onCreateLoader(id, args);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_CURRENCIES) {
            existingCurrencyCodes.clear();
            if (data.moveToFirst()) {
                do {
                    existingCurrencyCodes.add(data.getString(0));
                } while (data.moveToNext());
            }
            return;
        }
        super.onLoadFinished(loader, data);
    }

    @Override protected CursorLoader getModelCursorLoader(Context context, String modelServerId) {
        return Tables.Currencies.getQuery().asCursorLoader(context, CurrenciesProvider.uriCurrency(modelServerId));
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refreshRate_B: {
                ensureModelUpdated(model);
                final String code = model.getCode();
                if (!TextUtils.isEmpty(code) && code.length() == 3) {
                    currenciesApi.updateExchangeRate(code, mainCurrency.getCode());
                    setRefreshing(true);
                }
                break;
            }
            case R.id.symbolPosition_B: {
                final String[] values = new String[SymbolPosition.values().length];
                int index = 0;
                for (SymbolPosition symbolPosition : SymbolPosition.values()) {
                    values[index++] = getSymbolPositionExplanation(symbolPosition);
                }
                final ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, values);
                final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        listPopupWindow_LPW.dismiss();
                        listPopupWindow_LPW = null;
                        model.setSymbolPosition(SymbolPosition.values()[position]);
                        ensureModelUpdated(model);
                        onModelLoaded(model);
                    }
                };
                showPopupList(view, adapter, itemClickListener);
                break;
            }

            case R.id.thousandsSeparator_B: {
                final String[] values = new String[GroupSeparator.values().length];
                int index = 0;
                for (GroupSeparator groupSeparator : GroupSeparator.values()) {
                    values[index++] = getGroupSeparatorExplanation(groupSeparator);
                }
                final ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, values);
                final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        listPopupWindow_LPW.dismiss();
                        listPopupWindow_LPW = null;
                        model.setGroupSeparator(GroupSeparator.values()[position]);
                        ensureModelUpdated(model);
                        onModelLoaded(model);
                    }
                };
                showPopupList(view, adapter, itemClickListener);
                break;
            }

            case R.id.decimalSeparator_B: {
                final String[] values = new String[DecimalSeparator.values().length];
                int index = 0;
                for (DecimalSeparator decimalSeparator : DecimalSeparator.values()) {
                    values[index++] = getDecimalSeparatorExplanation(decimalSeparator);
                }
                final ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, values);
                final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        listPopupWindow_LPW.dismiss();
                        listPopupWindow_LPW = null;
                        model.setDecimalSeparator(DecimalSeparator.values()[position]);
                        ensureModelUpdated(model);
                        onModelLoaded(model);
                    }
                };
                showPopupList(view, adapter, itemClickListener);
                break;
            }

            case R.id.decimalsCount_B: {
                final ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, new String[]{"0", "1", "2"});
                final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        listPopupWindow_LPW.dismiss();
                        listPopupWindow_LPW = null;
                        model.setDecimalCount(position);
                        ensureModelUpdated(model);
                        onModelLoaded(model);
                    }
                };
                showPopupList(view, adapter, itemClickListener);
                break;
            }
        }
    }

    @Override public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        model.setDefault(isChecked);
        onModelLoaded(model);
    }

    @Subscribe public void onRefreshFinished(final ExchangeRateRequest request) {
        if (model.getCode().equals(request.getFromCode())) {
            loading_SPB.post(new Runnable() {
                @Override public void run() {
                    setRefreshing(false);
                    model.setExchangeRate(request.getCurrency().getExchangeRate());
                    onModelLoaded(model);
                }
            });
        }
    }

    private void prepareCurrenciesAutoComplete() {
        // Build currencies set
        final Set<java.util.Currency> currencySet = new HashSet<>();
        final Locale[] locales = Locale.getAvailableLocales();
        for (Locale loc : locales) {
            try {
                currencySet.add(java.util.Currency.getInstance(loc));
            } catch (Exception exc) {
                // Locale not found
            }
        }

        // Build currencies codes array
        final String[] currencies = new String[currencySet.size()];
        int i = 0;
        for (java.util.Currency currency : currencySet) {
            currencies[i++] = currency.getCurrencyCode();
        }

        // Prepare auto complete view
        final ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, currencies);
        code_ET.setAdapter(autoCompleteAdapter);
        code_ET.setThreshold(0);
        code_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //noinspection ConstantConditions
                checkForCurrencyDuplicate(code_ET.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkForCurrencyDuplicate(String code) {
        if (isCurrencyExists(code) && isNewModel()) {
            code_ET.setError(getString(R.string.l_currency_exists));
        } else {
            code_ET.setError(null);
        }
    }

    private boolean isCurrencyExists(String code) {
        return existingCurrencyCodes.contains(code.toUpperCase());
    }

    private void updateFormatView() {
        symbolPosition_B.setText(MoneyFormatter.format(model, 100000, false));
    }

    private void showPopupList(View anchorView, ListAdapter adapter, AdapterView.OnItemClickListener itemClickListener) {
        listPopupWindow_LPW = new ListPopupWindow(getActivity());
        listPopupWindow_LPW.setModal(true);
        // listPopupWindow_LPW.setListSelector(getResources().getDrawable(R.drawable.btn_borderless));
        listPopupWindow_LPW.setAdapter(adapter);
        listPopupWindow_LPW.setOnItemClickListener(itemClickListener);
        listPopupWindow_LPW.setAnchorView(anchorView);
        listPopupWindow_LPW.show();
    }

    private void updateCodeTitlePosition() {
        if (TextUtils.isEmpty(code_ET.getText())) {
            code_TV.animate().translationY(code_ET.getBaseline() + (code_TV.getHeight() - code_TV.getBaseline())).setDuration(100).start();
        } else {
            code_TV.animate().translationY(0).setDuration(100).start();
        }
    }

    private void updateSymbolTitlePosition() {
        if (TextUtils.isEmpty(symbol_ET.getText())) {
            symbol_TV.animate().translationY(symbol_ET.getBaseline() + (symbol_TV.getHeight() - symbol_TV.getBaseline())).setDuration(100).start();
        } else {
            symbol_TV.animate().translationY(0).setDuration(100).start();
        }
    }

    private String getDecimalSeparatorExplanation(DecimalSeparator decimalSeparator) {
        switch (decimalSeparator) {
            case Dot:
                return getString(R.string.dot);
            case Comma:
                return getString(R.string.comma);
            case Space:
                return getString(R.string.space);
        }
        return null;
    }

    private String getGroupSeparatorExplanation(GroupSeparator groupSeparator) {
        switch (groupSeparator) {
            case None:
                return getString(R.string.none);
            case Dot:
                return getString(R.string.dot);
            case Comma:
                return getString(R.string.comma);
            case Space:
                return getString(R.string.space);
        }
        return null;
    }

    private String getSymbolPositionExplanation(SymbolPosition symbolPosition) {
        switch (symbolPosition) {
            case CloseRight:
                return getString(R.string.close_right);
            case FarRight:
                return getString(R.string.far_right);
            case CloseLeft:
                return getString(R.string.close_left);
            case FarLeft:
                return getString(R.string.far_left);
        }
        return null;
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshing) {
            loading_SPB.setVisibility(View.VISIBLE);
            loading_SPB.progressiveStart();
        } else {
            loading_SPB.progressiveStop();
            loading_SPB.setSmoothProgressDrawableCallbacks(new SmoothProgressDrawable.Callbacks() {
                @Override public void onStop() {
                    loading_SPB.setSmoothProgressDrawableCallbacks(null);
                    loading_SPB.setVisibility(View.GONE);
                }

                @Override public void onStart() {

                }
            });
        }
    }
}
