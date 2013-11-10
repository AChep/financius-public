package com.code44.finance.ui.transactions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.code44.finance.API;
import com.code44.finance.R;
import com.code44.finance.db.Tables;
import com.code44.finance.db.model.Account;
import com.code44.finance.db.model.Category;
import com.code44.finance.providers.AccountsProvider;
import com.code44.finance.providers.CategoriesProvider;
import com.code44.finance.providers.TransactionsProvider;
import com.code44.finance.services.AbstractService;
import com.code44.finance.services.CurrenciesRestService;
import com.code44.finance.ui.ItemEditFragment;
import com.code44.finance.ui.accounts.AccountListActivity;
import com.code44.finance.ui.accounts.AccountListFragment;
import com.code44.finance.ui.categories.CategoryListActivity;
import com.code44.finance.ui.categories.CategoryListFragment;
import com.code44.finance.ui.dialogs.DateTimeDialog;
import com.code44.finance.utils.AmountUtils;
import com.code44.finance.utils.AnimUtils;
import com.code44.finance.utils.CurrenciesHelper;
import com.code44.finance.utils.TransactionAutoHelper;
import de.greenrobot.event.EventBus;

public class TransactionEditFragment extends ItemEditFragment implements View.OnClickListener, DateTimeDialog.DialogCallbacks
{
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_ACCOUNT_FROM_ID = "STATE_ACCOUNT_FROM_ID";
    private static final String STATE_ACCOUNT_FROM_TITLE = "STATE_ACCOUNT_FROM_TITLE";
    private static final String STATE_ACCOUNT_FROM_CURRENCY_ID = "STATE_ACCOUNT_FROM_CURRENCY_ID";
    private static final String STATE_ACCOUNT_FROM_CURRENCY_CODE = "STATE_ACCOUNT_FROM_CURRENCY_CODE";
    private static final String STATE_ACCOUNT_FROM_CURRENCY_EXCHANGE_RATE = "STATE_ACCOUNT_FROM_CURRENCY_EXCHANGE_RATE";
    private static final String STATE_ACCOUNT_TO_ID = "STATE_ACCOUNT_TO_ID";
    private static final String STATE_ACCOUNT_TO_TITLE = "STATE_ACCOUNT_TO_TITLE";
    private static final String STATE_ACCOUNT_TO_CURRENCY_ID = "STATE_ACCOUNT_TO_CURRENCY_ID";
    private static final String STATE_ACCOUNT_TO_CURRENCY_CODE = "STATE_ACCOUNT_TO_CURRENCY_CODE";
    private static final String STATE_ACCOUNT_TO_CURRENCY_EXCHANGE_RATE = "STATE_ACCOUNT_TO_CURRENCY_EXCHANGE_RATE";
    private static final String STATE_EXCHANGE_RATE = "STATE_EXCHANGE_RATE";
    private static final String STATE_CATEGORY_ID = "STATE_CATEGORY_ID";
    private static final String STATE_CATEGORY_TITLE = "STATE_CATEGORY_TITLE";
    private static final String STATE_CATEGORY_COLOR = "STATE_CATEGORY_COLOR";
    private static final String STATE_CATEGORY_TYPE = "STATE_CATEGORY_TYPE";
    private static final String STATE_EXPENSE_CATEGORY_ID = "STATE_EXPENSE_CATEGORY_ID";
    private static final String STATE_EXPENSE_CATEGORY_TITLE = "STATE_EXPENSE_CATEGORY_TITLE";
    private static final String STATE_EXPENSE_CATEGORY_COLOR = "STATE_EXPENSE_CATEGORY_COLOR";
    private static final String STATE_INCOME_CATEGORY_ID = "STATE_INCOME_CATEGORY_ID";
    private static final String STATE_INCOME_CATEGORY_TITLE = "STATE_INCOME_CATEGORY_TITLE";
    private static final String STATE_INCOME_CATEGORY_COLOR = "STATE_INCOME_CATEGORY_COLOR";
    private static final String STATE_AMOUNT = "STATE_AMOUNT";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_STATE = "STATE_STATE";
    private static final String STATE_SHOW_IN_TOTALS = "STATE_SHOW_IN_TOTALS";
    private static final String STATE_USER_SET_ACCOUNT_FROM = "STATE_USER_SET_ACCOUNT_FROM";
    private static final String STATE_USER_SET_ACCOUNT_TO = "STATE_USER_SET_ACCOUNT_TO";
    private static final String STATE_USER_SET_CATEGORY_INCOME = "STATE_USER_SET_CATEGORY_INCOME";
    private static final String STATE_USER_SET_CATEGORY_EXPENSE = "STATE_USER_SET_CATEGORY_EXPENSE";
    private static final String STATE_AUTO_TRANSACTION_SET_FOR_TYPE = "STATE_AUTO_TRANSACTION_SET_FOR_TYPE";
    // -----------------------------------------------------------------------------------------------------------------
    private static final String FRAGMENT_DATE_TIME = "FRAGMENT_DATE_TIME";
    // -----------------------------------------------------------------------------------------------------------------
    private static final int REQUEST_DATE = 1;
    private static final int REQUEST_TIME = 2;
    private static final int REQUEST_ACCOUNT_FROM = 3;
    private static final int REQUEST_ACCOUNT_TO = 4;
    private static final int REQUEST_EXCHANGE_RATE = 5;
    private static final int REQUEST_CATEGORY = 6;
    private static final int REQUEST_AMOUNT = 7;
    private static final int REQUEST_AMOUNT_TO = 8;
    // -----------------------------------------------------------------------------------------------------------------
    private static final int LOADER_ACCOUNTS = 1;
    private static final int LOADER_CATEGORIES = 2;
    private static final int LOADER_TRANSACTIONS = 3;
    // -----------------------------------------------------------------------------------------------------------------
    private final TransactionAutoHelper transactionAutoHelper = TransactionAutoHelper.getInstance();
    // -----------------------------------------------------------------------------------------------------------------
    private RadioButton expense_B;
    private RadioButton income_B;
    private RadioButton transfer_B;
    private Button date_B;
    private Button time_B;
    private Button accountFrom_B;
    private View accountsSeparator_V;
    private Button accountTo_B;
    private View currenciesContainer_V;
    private TextView currencies_TV;
    private Button exchangeRate_B;
    private View categoryContainer_V;
    private Button category_B;
    private View color_V;
    private Button amount_B;
    private View amountsSeparator_V;
    private Button amountTo_B;
    private EditText note_ET;
    private CheckBox confirmed_CB;
    private ImageButton confirmedInfo_B;
    private View confirmedInfo_V;
    private CheckBox showInTotals_CB;
    private ImageButton showInTotalsInfo_B;
    private View showInTotalsInfo_V;
    // -----------------------------------------------------------------------------------------------------------------
    private long date;
    private long accountFromId;
    private String accountFromTitle;
    private long accountFromCurrencyId;
    private String accountFromCurrencyCode;
    private double accountFromCurrencyExchangeRate;
    private long accountToId;
    private String accountToTitle;
    private long accountToCurrencyId;
    private String accountToCurrencyCode;
    private double accountToCurrencyExchangeRate;
    private long categoryId;
    private String categoryTitle;
    private int categoryColor;
    private int categoryType = -1;
    private long expenseCategoryId;
    private String expenseCategoryTitle;
    private int expenseCategoryColor;
    private long incomeCategoryId;
    private String incomeCategoryTitle;
    private int incomeCategoryColor;
    private boolean userSetAccountFrom;
    private boolean userSetAccountTo;
    private boolean userSetCategoryIncome;
    private boolean userSetCategoryExpense;
    private boolean autoTransactionSetForType = false;
    private boolean doingAutoTransaction = false;

    public static TransactionEditFragment newInstance(long itemId)
    {
        TransactionEditFragment f = new TransactionEditFragment();
        f.setArguments(makeArgs(itemId));
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_transaction_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        expense_B = (RadioButton) view.findViewById(R.id.expense_B);
        income_B = (RadioButton) view.findViewById(R.id.income_B);
        transfer_B = (RadioButton) view.findViewById(R.id.transfer_B);
        date_B = (Button) view.findViewById(R.id.date_B);
        time_B = (Button) view.findViewById(R.id.time_B);
        accountFrom_B = (Button) view.findViewById(R.id.accountFrom_B);
        accountsSeparator_V = view.findViewById(R.id.accountsSeparator_TV);
        accountTo_B = (Button) view.findViewById(R.id.accountTo_B);
        currenciesContainer_V = view.findViewById(R.id.currenciesContainer_V);
        currencies_TV = (TextView) view.findViewById(R.id.currencies_TV);
        exchangeRate_B = (Button) view.findViewById(R.id.exchangeRate_B);
        categoryContainer_V = view.findViewById(R.id.categoryContainer_V);
        category_B = (Button) view.findViewById(R.id.category_B);
        color_V = view.findViewById(R.id.color_V);
        amount_B = (Button) view.findViewById(R.id.amount_B);
        amountsSeparator_V = view.findViewById(R.id.amountsSeparator_TV);
        amountTo_B = (Button) view.findViewById(R.id.amountTo_B);
        note_ET = (EditText) view.findViewById(R.id.note_ET);
        confirmed_CB = (CheckBox) view.findViewById(R.id.confirmed_CB);
        confirmedInfo_B = (ImageButton) view.findViewById(R.id.confirmedInfo_B);
        confirmedInfo_V = view.findViewById(R.id.confirmedInfo_TV);
        showInTotals_CB = (CheckBox) view.findViewById(R.id.showInTotals_CB);
        showInTotalsInfo_B = (ImageButton) view.findViewById(R.id.showInTotalsInfo_B);
        showInTotalsInfo_V = view.findViewById(R.id.showInTotalsInfo_TV);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Setup
        expense_B.setOnClickListener(this);
        income_B.setOnClickListener(this);
        transfer_B.setOnClickListener(this);
        date_B.setOnClickListener(this);
        time_B.setOnClickListener(this);
        accountFrom_B.setOnClickListener(this);
        accountsSeparator_V.setOnClickListener(this);
        accountTo_B.setOnClickListener(this);
        exchangeRate_B.setOnClickListener(this);
        category_B.setOnClickListener(this);
        amount_B.setOnClickListener(this);
        amountTo_B.setOnClickListener(this);
        confirmedInfo_B.setOnClickListener(this);
        showInTotalsInfo_B.setOnClickListener(this);

        // Restore date time dialog fragment
        final DateTimeDialog dateTime_F = (DateTimeDialog) getFragmentManager().findFragmentByTag(FRAGMENT_DATE_TIME);
        if (dateTime_F != null)
            dateTime_F.setListener(this);

        // Loaders
        if (itemId == 0)
        {
            getLoaderManager().initLoader(LOADER_ACCOUNTS, null, this);
            getLoaderManager().initLoader(LOADER_CATEGORIES, null, this);
            getLoaderManager().initLoader(LOADER_TRANSACTIONS, null, this);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Register events
        EventBus.getDefault().register(this, CurrenciesRestService.GetExchangeRateEvent.class);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Unregister events
        EventBus.getDefault().unregister(this, CurrenciesRestService.GetExchangeRateEvent.class);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Reset listener for date time dialog
        final DateTimeDialog dateTime_F = (DateTimeDialog) getFragmentManager().findFragmentByTag(FRAGMENT_DATE_TIME);
        if (dateTime_F != null)
            dateTime_F.setListener(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DATE, getDate());
        outState.putLong(STATE_ACCOUNT_FROM_ID, getAccountFromId());
        outState.putString(STATE_ACCOUNT_FROM_TITLE, getAccountFromTitle());
        outState.putLong(STATE_ACCOUNT_FROM_CURRENCY_ID, getAccountFromCurrencyId());
        outState.putString(STATE_ACCOUNT_FROM_CURRENCY_CODE, getAccountFromCurrencyCode());
        outState.putDouble(STATE_ACCOUNT_FROM_CURRENCY_EXCHANGE_RATE, getAccountFromCurrencyExchangeRate());
        outState.putLong(STATE_ACCOUNT_TO_ID, getAccountToId());
        outState.putString(STATE_ACCOUNT_TO_TITLE, getAccountToTitle());
        outState.putLong(STATE_ACCOUNT_TO_CURRENCY_ID, getAccountToCurrencyId());
        outState.putString(STATE_ACCOUNT_TO_CURRENCY_CODE, getAccountToCurrencyCode());
        outState.putDouble(STATE_ACCOUNT_TO_CURRENCY_EXCHANGE_RATE, getAccountToCurrencyExchangeRate());
        outState.putDouble(STATE_EXCHANGE_RATE, getExchangeRate());
        outState.putLong(STATE_CATEGORY_ID, getCategoryId());
        outState.putString(STATE_CATEGORY_TITLE, getCategoryTitle());
        outState.putInt(STATE_CATEGORY_COLOR, getCategoryColor());
        outState.putInt(STATE_CATEGORY_TYPE, getCategoryType());
        outState.putLong(STATE_EXPENSE_CATEGORY_ID, expenseCategoryId);
        outState.putString(STATE_EXPENSE_CATEGORY_TITLE, expenseCategoryTitle);
        outState.putInt(STATE_EXPENSE_CATEGORY_COLOR, expenseCategoryColor);
        outState.putLong(STATE_INCOME_CATEGORY_ID, incomeCategoryId);
        outState.putString(STATE_INCOME_CATEGORY_TITLE, incomeCategoryTitle);
        outState.putInt(STATE_INCOME_CATEGORY_COLOR, incomeCategoryColor);
        outState.putDouble(STATE_AMOUNT, getAmount());
        outState.putString(STATE_NOTE, getNote());
        outState.putInt(STATE_STATE, getState());
        outState.putBoolean(STATE_SHOW_IN_TOTALS, isShowInTotals());
        outState.putBoolean(STATE_USER_SET_ACCOUNT_FROM, userSetAccountFrom);
        outState.putBoolean(STATE_USER_SET_ACCOUNT_TO, userSetAccountTo);
        outState.putBoolean(STATE_USER_SET_CATEGORY_EXPENSE, userSetCategoryExpense);
        outState.putBoolean(STATE_USER_SET_CATEGORY_INCOME, userSetCategoryIncome);
        outState.putBoolean(STATE_AUTO_TRANSACTION_SET_FOR_TYPE, autoTransactionSetForType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_ACCOUNT_FROM:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    userSetAccountFrom = true;
                    setAccountFrom(data.getLongExtra(AccountListFragment.RESULT_EXTRA_ITEM_ID, 0), data.getStringExtra(AccountListFragment.RESULT_EXTRA_TITLE), data.getLongExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_ID, 0), data.getStringExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_CODE), data.getDoubleExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_EXCHANGE_RATE, 1.0));
                }
                break;
            }

            case REQUEST_ACCOUNT_TO:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    userSetAccountTo = true;
                    setAccountTo(data.getLongExtra(AccountListFragment.RESULT_EXTRA_ITEM_ID, 0), data.getStringExtra(AccountListFragment.RESULT_EXTRA_TITLE), data.getLongExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_ID, 0), data.getStringExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_CODE), data.getDoubleExtra(AccountListFragment.RESULT_EXTRA_CURRENCY_EXCHANGE_RATE, 1.0));
                }
                break;
            }

            case REQUEST_EXCHANGE_RATE:
            {
                if (resultCode == Activity.RESULT_OK)
                    setExchangeRate(data.getDoubleExtra(CalculatorActivity.RESULT_EXTRA_AMOUNT, 0));
                break;
            }

            case REQUEST_CATEGORY:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    if (getCategoryType() == Tables.Categories.Type.EXPENSE)
                        userSetCategoryExpense = true;
                    else if (getCategoryType() == Tables.Categories.Type.INCOME)
                        userSetCategoryIncome = true;
                    setCategory(data.getLongExtra(CategoryListFragment.RESULT_EXTRA_ITEM_ID, 0), data.getStringExtra(CategoryListFragment.RESULT_EXTRA_CATEGORY_TITLE), data.getIntExtra(CategoryListFragment.RESULT_EXTRA_CATEGORY_COLOR, 0));
                }
                break;
            }

            case REQUEST_AMOUNT:
            {
                if (resultCode == Activity.RESULT_OK)
                    setAmount(data.getDoubleExtra(CalculatorActivity.RESULT_EXTRA_AMOUNT, 0));
                break;
            }

            case REQUEST_AMOUNT_TO:
            {
                if (resultCode == Activity.RESULT_OK && getExchangeRate() != 0)
                    setAmount(data.getDoubleExtra(CalculatorActivity.RESULT_EXTRA_AMOUNT, 0) / getExchangeRate());
                break;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
    {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        switch (id)
        {
            case LOADER_ACCOUNTS:
                uri = AccountsProvider.uriAccounts(getActivity());
                projection = new String[]{Tables.Accounts.T_ID, Tables.Accounts.CURRENCY_ID, Tables.Accounts.TITLE, Tables.Currencies.EXCHANGE_RATE, Tables.Currencies.CODE};
                selection = Tables.Accounts.ORIGIN + "<>? and " + Tables.Accounts.DELETE_STATE + "=? and " + Tables.Accounts.SHOW_IN_SELECTION + "=?";
                selectionArgs = new String[]{String.valueOf(Tables.Categories.Origin.SYSTEM), String.valueOf(Tables.DeleteState.NONE), "1"};
                break;

            case LOADER_CATEGORIES:
                uri = CategoriesProvider.uriCategories(getActivity());
                selection = Tables.Categories.DELETE_STATE + "=? and " + Tables.Categories.LEVEL + ">?";
                selectionArgs = new String[]{String.valueOf(Tables.DeleteState.NONE), "0"};
                break;

            case LOADER_TRANSACTIONS:
                uri = TransactionsProvider.uriTransactions(getActivity());
                projection = new String[]{Tables.Transactions.T_ID, Tables.Transactions.SERVER_ID, Tables.Transactions.ACCOUNT_FROM_ID, Tables.Transactions.ACCOUNT_TO_ID, Tables.Transactions.TIMESTAMP, Tables.Transactions.DATE, Tables.Accounts.AccountFrom.S_TITLE,
                        Tables.Accounts.AccountTo.S_TITLE, Tables.Transactions.CATEGORY_ID, Tables.Categories.CategoriesChild.S_TITLE,
                        Tables.Categories.CategoriesChild.S_TYPE, Tables.Transactions.AMOUNT, Tables.Transactions.NOTE, Tables.Transactions.DELETE_STATE, Tables.Transactions.SYNC_STATE};
                selection = Tables.Transactions.STATE + "=? and " + Tables.Transactions.DELETE_STATE + "=? and " + Tables.Transactions.DATE + " between ? and ?";
                final long now = System.currentTimeMillis();
                selectionArgs = new String[]{String.valueOf(Tables.Transactions.State.CONFIRMED), String.valueOf(Tables.DeleteState.NONE), String.valueOf(now - (DateUtils.WEEK_IN_MILLIS * 12)), String.valueOf(now)};
                break;
        }

        if (uri != null)
            return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        else
            return super.onCreateLoader(id, bundle);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        switch (cursorLoader.getId())
        {
            case LOADER_ACCOUNTS:
                transactionAutoHelper.setAccounts(cursor);
                cursorLoader.abandon();
                doAutoComplete();
                return;

            case LOADER_CATEGORIES:
                transactionAutoHelper.setCategories(cursor);
                cursorLoader.abandon();
                doAutoComplete();
                return;

            case LOADER_TRANSACTIONS:
                transactionAutoHelper.setTransactions(cursor);
                cursorLoader.abandon();
                doAutoComplete();
                return;
        }
        super.onLoadFinished(cursorLoader, cursor);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.expense_B:
                setCategoryType(Tables.Categories.Type.EXPENSE);
                break;

            case R.id.income_B:
                setCategoryType(Tables.Categories.Type.INCOME);
                break;

            case R.id.transfer_B:
                setCategoryType(Tables.Categories.Type.TRANSFER);
                break;

            case R.id.date_B:
                DateTimeDialog.newDateDialogInstance(this, REQUEST_DATE, getDate()).show(getFragmentManager(), FRAGMENT_DATE_TIME);
                break;

            case R.id.time_B:
                DateTimeDialog.newTimeDialogInstance(this, REQUEST_TIME, getDate()).show(getFragmentManager(), FRAGMENT_DATE_TIME);
                break;

            case R.id.accountFrom_B:
                AccountListActivity.startListSelection(getActivity(), this, REQUEST_ACCOUNT_FROM);
                break;

            case R.id.accountsSeparator_TV:
                final long tempAccountId = getAccountFromId();
                final String tempAccountTitle = getAccountFromTitle();
                final long tempCurrencyId = getAccountFromCurrencyId();
                final String tempCurrencyCode = getAccountFromCurrencyCode();
                final double tempExchangeRate = getAccountFromCurrencyExchangeRate();
                setAccountFrom(getAccountToId(), getAccountToTitle(), getAccountToCurrencyId(), getAccountToCurrencyCode(), getAccountToCurrencyExchangeRate());
                setAccountTo(tempAccountId, tempAccountTitle, tempCurrencyId, tempCurrencyCode, tempExchangeRate);
                break;

            case R.id.accountTo_B:
                AccountListActivity.startListSelection(getActivity(), this, REQUEST_ACCOUNT_TO);
                break;

            case R.id.exchangeRate_B:
                showExchangeRateOptions();
                break;

            case R.id.category_B:
                CategoryListActivity.startListSelection(getActivity(), this, REQUEST_CATEGORY, categoryType);
                break;

            case R.id.amount_B:
                CalculatorActivity.startCalculator(this, REQUEST_AMOUNT, getAmount(), false, true);
                break;

            case R.id.confirmedInfo_B:
                confirmedInfo_V.setVisibility(confirmedInfo_V.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.showInTotalsInfo_B:
                showInTotalsInfo_V.setVisibility(showInTotalsInfo_V.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.amountTo_B:
                CalculatorActivity.startCalculator(this, REQUEST_AMOUNT_TO, getAmount() * getExchangeRate(), false, true);
                break;
        }
    }

    @Override
    public void onDateSelected(int requestCode, long date)
    {
        setDate(date);
    }

    @Override
    public boolean onSave(Context context, long itemId)
    {
        boolean isOK = true;

        // Check values
        if (getAccountFromId() == 0)
        {
            AnimUtils.shake(accountFrom_B);
            isOK = false;
        }

        if (getAccountToId() == 0)
        {
            AnimUtils.shake(accountTo_B);
            isOK = false;
        }

        if (getCategoryType() == Tables.Categories.Type.TRANSFER && getAccountFromId() == getAccountToId())
        {
            AnimUtils.shake(accountFrom_B);
            AnimUtils.shake(accountTo_B);
            isOK = false;
        }

        double exchangeRate = getExchangeRate();
        if (getCategoryType() == Tables.Categories.Type.TRANSFER && exchangeRate <= 0)
        {
            exchangeRate = 1.0;
        }

        if (getAmount() <= 0)
        {
            AnimUtils.shake(amount_B);
            isOK = false;
        }

        if (isOK)
        {
            if (this.itemId == 0)
                API.createTransaction(getActivity(), getAccountFromId(), getAccountToId(), getCategoryId(), getDate(), getAmount(), exchangeRate, getNote(), getState(), isShowInTotals());
            else
                API.updateTransaction(getActivity(), itemId, getAccountFromId(), getAccountToId(), getCategoryId(), getDate(), getAmount(), exchangeRate, getNote(), getState(), isShowInTotals());
        }

        return isOK;
    }

    @Override
    public boolean onDiscard()
    {
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(CurrenciesRestService.GetExchangeRateEvent event)
    {
        if (event.getState() == AbstractService.ServiceEvent.State.SUCCEEDED && event.getFromCode().equalsIgnoreCase(getAccountFromCurrencyCode()) && event.getToCode().equalsIgnoreCase(getAccountToCurrencyCode()))
            setExchangeRate(event.getExchangeRate());
    }

    @Override
    protected Loader<Cursor> createItemLoader(Context context, long itemId)
    {
        final Uri uri = TransactionsProvider.uriTransaction(getActivity(), itemId);
        final String[] projection = new String[]{
                Tables.Transactions.T_ID, Tables.Transactions.DATE, Tables.Transactions.AMOUNT, Tables.Transactions.NOTE, Tables.Transactions.STATE, Tables.Transactions.EXCHANGE_RATE, Tables.Transactions.SHOW_IN_TOTALS,
                Tables.Transactions.ACCOUNT_FROM_ID, Tables.Accounts.AccountFrom.S_TITLE, Tables.Accounts.AccountFrom.S_CURRENCY_ID, Tables.Currencies.CurrencyFrom.S_CODE, Tables.Currencies.CurrencyFrom.S_EXCHANGE_RATE,
                Tables.Transactions.ACCOUNT_TO_ID, Tables.Accounts.AccountTo.S_TITLE, Tables.Accounts.AccountTo.S_CURRENCY_ID, Tables.Currencies.CurrencyTo.S_CODE, Tables.Currencies.CurrencyTo.S_EXCHANGE_RATE,
                Tables.Transactions.CATEGORY_ID, Tables.Categories.CategoriesChild.S_TITLE, Tables.Categories.CategoriesChild.S_TYPE, Tables.Categories.CategoriesChild.S_COLOR};
        final String selection = null;
        final String[] selectionArgs = null;
        final String sortOrder = null;

        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected boolean bindItem(Cursor c, boolean isDataLoaded)
    {
        if (!isDataLoaded && c != null && c.moveToFirst())
        {
            setCategoryType(c.getInt(c.getColumnIndex(Tables.Categories.CategoriesChild.TYPE)));
            setDate(c.getLong(c.getColumnIndex(Tables.Transactions.DATE)));
            setAccountFrom(c.getLong(c.getColumnIndex(Tables.Transactions.ACCOUNT_FROM_ID)), c.getString(c.getColumnIndex(Tables.Accounts.AccountFrom.TITLE)), c.getLong(c.getColumnIndex(Tables.Accounts.AccountFrom.CURRENCY_ID)), c.getString(c.getColumnIndex(Tables.Currencies.CurrencyFrom.CODE)), c.getDouble(c.getColumnIndex(Tables.Currencies.CurrencyFrom.EXCHANGE_RATE)));
            setAccountTo(c.getLong(c.getColumnIndex(Tables.Transactions.ACCOUNT_TO_ID)), c.getString(c.getColumnIndex(Tables.Accounts.AccountTo.TITLE)), c.getLong(c.getColumnIndex(Tables.Accounts.AccountTo.CURRENCY_ID)), c.getString(c.getColumnIndex(Tables.Currencies.CurrencyTo.CODE)), c.getDouble(c.getColumnIndex(Tables.Currencies.CurrencyTo.EXCHANGE_RATE)));
            setExchangeRate(c.getDouble(c.getColumnIndex(Tables.Transactions.EXCHANGE_RATE)));
            setCategory(c.getLong(c.getColumnIndex(Tables.Transactions.CATEGORY_ID)), c.getString(c.getColumnIndex(Tables.Categories.CategoriesChild.TITLE)), c.getInt(c.getColumnIndex(Tables.Categories.CategoriesChild.COLOR)));
            setAmount(c.getDouble(c.getColumnIndex(Tables.Transactions.AMOUNT)));
            setNote(c.getString(c.getColumnIndex(Tables.Transactions.NOTE)));
            setState(c.getInt(c.getColumnIndex(Tables.Transactions.STATE)));
            setShowInTotals(c.getInt(c.getColumnIndex(Tables.Transactions.SHOW_IN_TOTALS)) != 0);
            return true;
        }

        return isDataLoaded;
    }

    @Override
    protected void restoreOrInit(long itemId, Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            setCategoryType(savedInstanceState.getInt(STATE_CATEGORY_TYPE));
            setDate(savedInstanceState.getLong(STATE_DATE));
            setAccountFrom(savedInstanceState.getLong(STATE_ACCOUNT_FROM_ID), savedInstanceState.getString(STATE_ACCOUNT_FROM_TITLE), savedInstanceState.getLong(STATE_ACCOUNT_FROM_CURRENCY_ID), savedInstanceState.getString(STATE_ACCOUNT_FROM_CURRENCY_CODE), savedInstanceState.getDouble(STATE_ACCOUNT_FROM_CURRENCY_EXCHANGE_RATE));
            setAccountTo(savedInstanceState.getLong(STATE_ACCOUNT_TO_ID), savedInstanceState.getString(STATE_ACCOUNT_TO_TITLE), savedInstanceState.getLong(STATE_ACCOUNT_TO_CURRENCY_ID), savedInstanceState.getString(STATE_ACCOUNT_TO_CURRENCY_CODE), savedInstanceState.getDouble(STATE_ACCOUNT_TO_CURRENCY_EXCHANGE_RATE));
            setExchangeRate(savedInstanceState.getDouble(STATE_EXCHANGE_RATE));
            setCategory(savedInstanceState.getLong(STATE_CATEGORY_ID), savedInstanceState.getString(STATE_CATEGORY_TITLE), savedInstanceState.getInt(STATE_CATEGORY_COLOR));
            expenseCategoryId = savedInstanceState.getLong(STATE_EXPENSE_CATEGORY_ID);
            expenseCategoryTitle = savedInstanceState.getString(STATE_EXPENSE_CATEGORY_TITLE);
            expenseCategoryColor = savedInstanceState.getInt(STATE_EXPENSE_CATEGORY_COLOR);
            incomeCategoryId = savedInstanceState.getLong(STATE_INCOME_CATEGORY_ID);
            incomeCategoryTitle = savedInstanceState.getString(STATE_INCOME_CATEGORY_TITLE);
            incomeCategoryColor = savedInstanceState.getInt(STATE_INCOME_CATEGORY_COLOR);
            setAmount(savedInstanceState.getDouble(STATE_AMOUNT));
            setNote(savedInstanceState.getString(STATE_NOTE));
            setState(savedInstanceState.getInt(STATE_STATE));
            setShowInTotals(savedInstanceState.getBoolean(STATE_SHOW_IN_TOTALS));
            userSetAccountFrom = savedInstanceState.getBoolean(STATE_USER_SET_ACCOUNT_FROM);
            userSetAccountTo = savedInstanceState.getBoolean(STATE_USER_SET_ACCOUNT_TO);
            userSetCategoryExpense = savedInstanceState.getBoolean(STATE_USER_SET_CATEGORY_EXPENSE);
            userSetCategoryIncome = savedInstanceState.getBoolean(STATE_USER_SET_CATEGORY_INCOME);
            autoTransactionSetForType = savedInstanceState.getBoolean(STATE_AUTO_TRANSACTION_SET_FOR_TYPE);
        }
        else if (itemId == 0)
        {
            setCategoryType(Tables.Categories.Type.EXPENSE);
            setDate(System.currentTimeMillis());
            setAccountFrom(0, null, 0, null, 1.0);
            setAccountTo(0, null, 0, null, 1.0);
            setExchangeRate(1.0);
            setCategory(Tables.Categories.IDs.EXPENSE_ID, null, 0);
            setAmount(0);
            setNote(null);
            setState(Tables.Transactions.State.CONFIRMED);
            setShowInTotals(true);
            userSetAccountFrom = false;
            userSetAccountTo = false;
            userSetCategoryExpense = false;
            userSetCategoryIncome = false;
            autoTransactionSetForType = false;

            // Loader
            getLoaderManager().initLoader(LOADER_ACCOUNTS, null, this);
            getLoaderManager().initLoader(LOADER_CATEGORIES, null, this);
            getLoaderManager().initLoader(LOADER_TRANSACTIONS, null, this);
        }
    }

    private void doAutoComplete()
    {
        if (autoTransactionSetForType || doingAutoTransaction || !transactionAutoHelper.isDataOk())
            return;

        doingAutoTransaction = true;

        // Category
        if ((getCategoryType() == Tables.Categories.Type.EXPENSE && !userSetCategoryExpense) || (getCategoryType() == Tables.Categories.Type.INCOME && !userSetCategoryIncome))
        {
            transactionAutoHelper.setCategoryId(0);
            final Category category = transactionAutoHelper.getCategory();
            if (category != null)
                setCategory(category.getId(), category.getTitle(), category.getColor());
        }

        // Account from
        if (!userSetAccountFrom && getCategoryType() != Tables.Categories.Type.INCOME)
        {
            transactionAutoHelper.setAccountFromId(0);
            final Account account = transactionAutoHelper.getAccount(TransactionAutoHelper.AccountType.FROM);
            if (account != null)
                setAccountFrom(account.getId(), account.getTitle(), account.getCurrency().getId(), account.getCurrency().getCode(), account.getCurrency().getExchangeRate());
        }

        // Account to
        if (!userSetAccountTo && getCategoryType() != Tables.Categories.Type.EXPENSE || getAccountFromId() == getAccountToId())
        {
            transactionAutoHelper.setAccountToId(0);
            final Account account = transactionAutoHelper.getAccount(TransactionAutoHelper.AccountType.TO);
            if (account != null)
                setAccountTo(account.getId(), account.getTitle(), account.getCurrency().getId(), account.getCurrency().getCode(), account.getCurrency().getExchangeRate());
        }

        autoTransactionSetForType = true;
        doingAutoTransaction = false;
    }

    private long getDate()
    {
        return date;
    }

    private void setDate(long date)
    {
        this.date = date;
        date_B.setText(DateUtils.formatDateTime(getActivity(), date, DateUtils.FORMAT_SHOW_DATE));
        time_B.setText(DateUtils.formatDateTime(getActivity(), date, DateUtils.FORMAT_SHOW_TIME));
        transactionAutoHelper.setDate(date);
        doAutoComplete();
    }

    private void setAccountFrom(long accountId, String title, long currencyId, String currencyCode, double exchangeRate)
    {
        if (accountId == Tables.Accounts.IDs.EXPENSE_ID || accountId == Tables.Accounts.IDs.INCOME_ID)
            title = null;

        this.accountFromId = accountId;
        this.accountFromTitle = title;
        this.accountFromCurrencyId = currencyId;
        this.accountFromCurrencyCode = currencyCode;
        this.accountFromCurrencyExchangeRate = exchangeRate;

        if (TextUtils.isEmpty(title))
        {
            accountFrom_B.setText(R.string.from_account);
            accountFrom_B.setTextColor(getResources().getColor(R.color.text_secondary));
        }
        else
        {
            accountFrom_B.setText(title);
            accountFrom_B.setTextColor(getResources().getColor(R.color.text_primary));
        }
        updateCurrency();

        transactionAutoHelper.setAccountFromId(getCategoryType() == Tables.Categories.Type.INCOME ? 0 : accountId);
        doAutoComplete();
    }

    private long getAccountFromId()
    {
        return getCategoryType() == Tables.Categories.Type.INCOME ? Tables.Categories.IDs.INCOME_ID : accountFromId;
    }

    private String getAccountFromTitle()
    {
        return accountFromTitle;
    }

    private long getAccountFromCurrencyId()
    {
        return accountFromCurrencyId;
    }

    private String getAccountFromCurrencyCode()
    {
        return accountFromCurrencyCode;
    }

    private double getAccountFromCurrencyExchangeRate()
    {
        return accountFromCurrencyExchangeRate;
    }

    private void setAccountTo(long accountId, String title, long currencyId, String currencyCode, double exchangeRate)
    {
        if (accountId == Tables.Accounts.IDs.EXPENSE_ID || accountId == Tables.Accounts.IDs.INCOME_ID)
            title = null;

        this.accountToId = accountId;
        this.accountToTitle = title;
        this.accountToCurrencyId = currencyId;
        this.accountToCurrencyCode = currencyCode;
        this.accountToCurrencyExchangeRate = exchangeRate;

        if (TextUtils.isEmpty(title))
        {
            accountTo_B.setText(R.string.to_account);
            accountTo_B.setTextColor(getResources().getColor(R.color.text_secondary));
        }
        else
        {
            accountTo_B.setText(title);
            accountTo_B.setTextColor(getResources().getColor(R.color.text_primary));
        }
        updateCurrency();

        transactionAutoHelper.setAccountToId(getCategoryType() == Tables.Categories.Type.EXPENSE ? 0 : accountId);
        doAutoComplete();
    }

    private long getAccountToId()
    {
        return getCategoryType() == Tables.Categories.Type.EXPENSE ? Tables.Categories.IDs.EXPENSE_ID : accountToId;
    }

    private String getAccountToTitle()
    {
        return accountToTitle;
    }

    private long getAccountToCurrencyId()
    {
        return accountToCurrencyId;
    }

    private String getAccountToCurrencyCode()
    {
        return accountToCurrencyCode;
    }

    private double getAccountToCurrencyExchangeRate()
    {
        return accountToCurrencyExchangeRate;
    }

    private double getExchangeRate()
    {
        return getCategoryType() == Tables.Categories.Type.TRANSFER ? Double.parseDouble(exchangeRate_B.getText().toString()) : 1.0;
    }

    private void setExchangeRate(double exchangeRate)
    {
        exchangeRate_B.setText(String.valueOf(exchangeRate));
        if (accountToCurrencyId > 0)
            amountTo_B.setText(AmountUtils.formatAmount(getActivity(), accountToCurrencyId, getAmount() * exchangeRate));
    }

    private void setCategory(long categoryId, String title, int color)
    {
        if (categoryId == Tables.Categories.IDs.EXPENSE_ID || categoryId == Tables.Categories.IDs.INCOME_ID || categoryId == Tables.Categories.IDs.TRANSFER_ID)
            title = null;

        this.categoryId = categoryId;
        this.categoryTitle = title;
        this.categoryColor = color;

        switch (categoryType)
        {
            case Tables.Categories.Type.EXPENSE:
                expenseCategoryId = categoryId;
                expenseCategoryTitle = title;
                expenseCategoryColor = color;
                break;

            case Tables.Categories.Type.INCOME:
                incomeCategoryId = categoryId;
                incomeCategoryTitle = title;
                incomeCategoryColor = color;
                break;
        }

        if (TextUtils.isEmpty(title))
        {
            category_B.setText(R.string.category);
            category_B.setTextColor(getResources().getColor(R.color.text_secondary));
            color_V.setBackgroundColor(getResources().getColor(R.color.f_light_darker2));
        }
        else
        {
            category_B.setText(title);
            category_B.setTextColor(getResources().getColor(R.color.text_primary));
            color_V.setBackgroundColor(color);
        }

        transactionAutoHelper.setCategoryId(categoryId);
        doAutoComplete();
    }

    private long getCategoryId()
    {
        switch (getCategoryType())
        {
            case Tables.Categories.Type.TRANSFER:
                return Tables.Categories.IDs.TRANSFER_ID;

            case Tables.Categories.Type.EXPENSE:
                return categoryId <= 0 ? Tables.Categories.IDs.EXPENSE_ID : categoryId;

            case Tables.Categories.Type.INCOME:
                return categoryId <= 0 ? Tables.Categories.IDs.INCOME_ID : categoryId;
        }

        return 0;
    }

    private String getCategoryTitle()
    {
        return categoryTitle;
    }

    private int getCategoryColor()
    {
        return categoryColor;
    }

    private int getCategoryType()
    {
        return categoryType;
    }

    private void setCategoryType(int categoryType)
    {
        if (this.categoryType == categoryType)
            return;
        this.categoryType = categoryType;
        switch (categoryType)
        {
            case Tables.Categories.Type.EXPENSE:
                expense_B.setChecked(true);
                break;

            case Tables.Categories.Type.INCOME:
                income_B.setChecked(true);
                break;

            case Tables.Categories.Type.TRANSFER:
                transfer_B.setChecked(true);
                break;
        }

        transactionAutoHelper.setCategoryId(0);
        switch (categoryType)
        {
            case Tables.Categories.Type.EXPENSE:
                accountsSeparator_V.setVisibility(View.GONE);
                accountTo_B.setVisibility(View.GONE);
                accountFrom_B.setVisibility(View.VISIBLE);
                categoryContainer_V.setVisibility(View.VISIBLE);
                setCategory(expenseCategoryId, expenseCategoryTitle, expenseCategoryColor);
                break;

            case Tables.Categories.Type.INCOME:
                accountFrom_B.setVisibility(View.GONE);
                accountsSeparator_V.setVisibility(View.GONE);
                accountTo_B.setVisibility(View.VISIBLE);
                categoryContainer_V.setVisibility(View.VISIBLE);
                setCategory(incomeCategoryId, incomeCategoryTitle, incomeCategoryColor);
                break;

            case Tables.Categories.Type.TRANSFER:
                accountFrom_B.setVisibility(View.VISIBLE);
                accountsSeparator_V.setVisibility(View.VISIBLE);
                accountTo_B.setVisibility(View.VISIBLE);
                categoryContainer_V.setVisibility(View.GONE);
                setCategory(Tables.Categories.IDs.TRANSFER_ID, getString(R.string.transfer), 0);
                break;
        }

        setAmount(AmountUtils.getAmount(amount_B.getText().toString()));
        updateCurrency();

        transactionAutoHelper.setAccountFromId(0);
        transactionAutoHelper.setAccountToId(0);
        transactionAutoHelper.setCategoryType(categoryType);
        autoTransactionSetForType = false;
        doAutoComplete();
    }

    private double getAmount()
    {
        return AmountUtils.getAmount(amount_B.getText().toString());
    }

    private void setAmount(double amount)
    {
        amount_B.setText(AmountUtils.formatAmount(amount));
        AmountUtils.setAmount(amount_B, amount, categoryType);
        if (accountToCurrencyId > 0)
            amountTo_B.setText(AmountUtils.formatAmount(getActivity(), accountToCurrencyId, getAmount() * getExchangeRate()));
    }

    private String getNote()
    {
        return note_ET.getText().toString();
    }

    private void setNote(String note)
    {
        note_ET.setText(note);
    }

    private int getState()
    {
        return confirmed_CB.isChecked() ? Tables.Transactions.State.CONFIRMED : Tables.Transactions.State.PENDING;
    }

    private void setState(int state)
    {
        confirmed_CB.setChecked(state == Tables.Transactions.State.CONFIRMED);
    }

    private boolean isShowInTotals()
    {
        return showInTotals_CB.isChecked();
    }

    private void setShowInTotals(boolean showInTotals)
    {
        showInTotals_CB.setChecked(showInTotals);
    }

    private void updateCurrency()
    {
        int visibility = View.GONE;
        double exchangeRate = 1.0;
        String currencies = null;
        switch (getCategoryType())
        {
            case Tables.Categories.Type.EXPENSE:
            case Tables.Categories.Type.INCOME:
                visibility = View.GONE;
                exchangeRate = 1.0;
                currencies = null;
                break;

            case Tables.Categories.Type.TRANSFER:
                visibility = getAccountFromCurrencyId() == getAccountToCurrencyId() || getAccountFromId() == 0 || getAccountFromId() == Tables.Accounts.IDs.INCOME_ID || getAccountToId() == 0 || getAccountToId() == Tables.Accounts.IDs.EXPENSE_ID ? View.GONE : View.VISIBLE;
                exchangeRate = getAccountFromCurrencyId() == CurrenciesHelper.getDefault(getActivity()).getMainCurrencyId() ? 1 / getAccountToCurrencyExchangeRate() : getAccountToCurrencyId() == CurrenciesHelper.getDefault(getActivity()).getMainCurrencyId() ? getAccountFromCurrencyExchangeRate() : 1.0;
                currencies = getAccountFromCurrencyCode() + " \u21E8 " + getAccountToCurrencyCode();
                break;
        }
        currenciesContainer_V.setVisibility(visibility);
        currencies_TV.setText(currencies);
        setExchangeRate(exchangeRate);
        amountTo_B.setVisibility(visibility);
        amountsSeparator_V.setVisibility(visibility);
    }

    private void showExchangeRateOptions()
    {
        final ListPopupWindow popup = new ListPopupWindow(getActivity());
        popup.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{getString(R.string.refresh_rate), getString(R.string.set)}));
        popup.setContentWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getActivity().getResources().getDisplayMetrics()));
        popup.setAnchorView(exchangeRate_B);
        popup.setModal(true);
        popup.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                popup.dismiss();
                if (position == 0)
                    API.getExchangeRate(getActivity(), getAccountFromCurrencyCode(), getAccountToCurrencyCode());
                else
                    CalculatorActivity.startCalculator(TransactionEditFragment.this, REQUEST_EXCHANGE_RATE, getExchangeRate(), false, false);
            }
        });
        popup.show();
    }
}