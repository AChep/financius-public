package com.code44.finance.ui.transactions.presenters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.code44.finance.R;
import com.code44.finance.api.currencies.CurrenciesApi;
import com.code44.finance.api.currencies.ExchangeRateRequest;
import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.common.utils.Strings;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.ui.CalculatorActivity;
import com.code44.finance.ui.accounts.AccountsActivity;
import com.code44.finance.ui.categories.CategoriesActivity;
import com.code44.finance.ui.common.BaseActivity;
import com.code44.finance.ui.common.ModelListActivity;
import com.code44.finance.ui.common.Presenter;
import com.code44.finance.ui.dialogs.DatePickerDialog;
import com.code44.finance.ui.dialogs.TimePickerDialog;
import com.code44.finance.ui.tags.TagsActivity;
import com.code44.finance.ui.transactions.autocomplete.AutoCompleteAdapter;
import com.code44.finance.ui.transactions.autocomplete.AutoCompleteInput;
import com.code44.finance.ui.transactions.autocomplete.AutoCompleteResult;
import com.code44.finance.ui.transactions.autocomplete.TransactionAutoComplete;
import com.code44.finance.ui.transactions.autocomplete.smart.SmartTransactionAutoComplete;
import com.code44.finance.utils.EventBus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class TransactionPresenter extends Presenter implements TransactionAutoComplete.TransactionAutoCompleteListener, NotePresenter.Callbacks, View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_AMOUNT = 1;
    private static final int REQUEST_ACCOUNT_FROM = 2;
    private static final int REQUEST_ACCOUNT_TO = 3;
    private static final int REQUEST_CATEGORY = 4;
    private static final int REQUEST_TAGS = 5;
    private static final int REQUEST_DATE = 6;
    private static final int REQUEST_TIME = 7;
    private static final int REQUEST_EXCHANGE_RATE = 8;
    private static final int REQUEST_AMOUNT_TO = 9;

    private static final String STATE_TRANSACTION_EDIT_DATA = "STATE_TRANSACTION_EDIT_DATA";

    private static final boolean LOG_AUTO_COMPLETE = true;

    private final BaseActivity activity;
    private final EventBus eventBus;
    private final Executor autoCompleteExecutor;
    private final CurrenciesApi currenciesApi;
    private final OnTransactionUpdatedListener listener;

    private final TransactionEditData transactionEditData;

    private final TransactionTypePresenter transactionTypeViewController;
    private final AmountPresenter amountViewController;
    private final DateTimePresenter dateTimeViewController;
    private final AccountsPresenter accountsViewController;
    private final CategoryPresenter categoryViewController;
    private final TagsPresenter tagsViewController;
    private final NotePresenter noteViewController;
    private final TransactionStatePresenter transactionStateViewController;
    private final FlagsPresenter flagsViewController;

    private AutoCompleteAdapter<?> currentAutoCompleteAdapter;
    private boolean isUpdated = false;
    private boolean isAutoCompleteUpdateQueued = false;

    public TransactionPresenter(BaseActivity activity, String transactionId, Bundle savedInstanceState, EventBus eventBus, Executor autoCompleteExecutor, Currency mainCurrency, CurrenciesApi currenciesApi, OnTransactionUpdatedListener listener) {
        this.activity = activity;
        this.eventBus = eventBus;
        this.autoCompleteExecutor = autoCompleteExecutor;
        this.currenciesApi = currenciesApi;
        this.listener = listener;

        if (savedInstanceState == null) {
            transactionEditData = new TransactionEditData();
        } else {
            transactionEditData = savedInstanceState.getParcelable(STATE_TRANSACTION_EDIT_DATA);
        }

        transactionTypeViewController = new TransactionTypePresenter(activity, this);
        amountViewController = new AmountPresenter(activity, this, this, mainCurrency);
        dateTimeViewController = new DateTimePresenter(activity, this, this);
        accountsViewController = new AccountsPresenter(activity, this, this);
        categoryViewController = new CategoryPresenter(activity, this, this);
        tagsViewController = new TagsPresenter(activity, this, this);
        noteViewController = new NotePresenter(activity, this, this);
        transactionStateViewController = new TransactionStatePresenter(activity, this);
        flagsViewController = new FlagsPresenter(activity, this);

        if (Strings.isEmpty(transactionId) || transactionId.equals("0")) {
            if (savedInstanceState == null) {
                CalculatorActivity.start(activity, REQUEST_AMOUNT, 0);
            }

            requestAutoComplete();
        }
    }

    @Override public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (!isUpdated) {
            update(false);
        } else if (isAutoCompleteUpdateQueued) {
            update(true);
        }
    }

    @Override public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TRANSACTION_EDIT_DATA, transactionEditData);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transactionTypeImageButton:
                toggleTransactionType();
                break;
            case R.id.amountButton:
                CalculatorActivity.start(activity, REQUEST_AMOUNT, transactionEditData.getAmount());
                break;
            case R.id.exchangeRateButton:
                CalculatorActivity.start(activity, REQUEST_EXCHANGE_RATE, transactionEditData.getExchangeRate());
                break;
            case R.id.amountToButton:
                CalculatorActivity.start(activity, REQUEST_AMOUNT_TO, Math.round(transactionEditData.getAmount() * transactionEditData.getExchangeRate()));
                break;
            case R.id.dateButton:
                DatePickerDialog.show(activity.getSupportFragmentManager(), REQUEST_DATE, transactionEditData.getDate());
                break;
            case R.id.timeButton:
                TimePickerDialog.show(activity.getSupportFragmentManager(), REQUEST_TIME, transactionEditData.getDate());
                break;
            case R.id.accountFromButton: {
                final boolean showPopup = !transactionEditData.isAccountFromSet();
                if (showPopup) {
                    currentAutoCompleteAdapter = accountsViewController.showAutoComplete(currentAutoCompleteAdapter, transactionEditData.getAutoCompleteResult(), new AutoCompleteAdapter.OnAutoCompleteItemClickListener<Account>() {
                        @Override public void onAutoCompleteItemClick(Account item) {
                            transactionEditData.setAccountFrom(item);
                            requestAutoComplete();
                        }
                    }, v);
                }

                if (currentAutoCompleteAdapter == null) {
                    AccountsActivity.startSelect(activity, REQUEST_ACCOUNT_FROM);
                }
                break;
            }

            case R.id.accountToButton: {
                final boolean showPopup = !transactionEditData.isAccountFromSet();
                if (showPopup) {
                    currentAutoCompleteAdapter = accountsViewController.showAutoComplete(currentAutoCompleteAdapter, transactionEditData.getAutoCompleteResult(), new AutoCompleteAdapter.OnAutoCompleteItemClickListener<Account>() {
                        @Override public void onAutoCompleteItemClick(Account item) {
                            transactionEditData.setAccountTo(item);
                            requestAutoComplete();
                        }
                    }, v);
                }

                if (currentAutoCompleteAdapter == null) {
                    AccountsActivity.startSelect(activity, REQUEST_ACCOUNT_TO);
                }
                break;
            }

            case R.id.categoryButton: {
                final boolean showPopup = !transactionEditData.isCategorySet();
                if (showPopup) {
                    currentAutoCompleteAdapter = categoryViewController.showAutoComplete(currentAutoCompleteAdapter, transactionEditData.getAutoCompleteResult(), new AutoCompleteAdapter.OnAutoCompleteItemClickListener<Category>() {
                        @Override public void onAutoCompleteItemClick(Category item) {
                            transactionEditData.setCategory(item);
                            requestAutoComplete();
                        }
                    }, v);
                }

                if (currentAutoCompleteAdapter == null) {
                    CategoriesActivity.startSelect(activity, REQUEST_CATEGORY, transactionEditData.getTransactionType());
                }
                break;
            }

            case R.id.tagsButton: {
                final boolean showPopup = !transactionEditData.isTagsSet();
                if (showPopup) {
                    currentAutoCompleteAdapter = tagsViewController.showAutoComplete(currentAutoCompleteAdapter, transactionEditData.getAutoCompleteResult(), new AutoCompleteAdapter.OnAutoCompleteItemClickListener<List<Tag>>() {
                        @Override public void onAutoCompleteItemClick(List<Tag> item) {
                            transactionEditData.setTags(item);
                            requestAutoComplete();
                        }
                    }, v);
                }

                if (currentAutoCompleteAdapter == null) {
                    TagsActivity.startMultiSelect(activity, REQUEST_TAGS, transactionEditData.getTags() != null ? transactionEditData.getTags() : Collections.<Tag>emptyList());
                }
                break;
            }
        }
    }

    @Override public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.amountButton:
                transactionEditData.setAmount(0L);
                requestAutoComplete();
                return true;
            case R.id.exchangeRateButton:
                transactionEditData.setExchangeRate(1.0);
                requestAutoComplete();
                return true;
            case R.id.amountToButton:
                transactionEditData.setAmount(0L);
                requestAutoComplete();
                return true;
            case R.id.dateButton:
            case R.id.timeButton:
                transactionEditData.setDate(System.currentTimeMillis());
                requestAutoComplete();
                return true;
            case R.id.accountFromButton:
                transactionEditData.setAccountFrom(null);
                requestAutoComplete();
                return true;
            case R.id.accountToButton:
                transactionEditData.setAccountTo(null);
                requestAutoComplete();
                return true;
            case R.id.categoryButton:
                transactionEditData.setCategory(null);
                requestAutoComplete();
                return true;
            case R.id.tagsButton:
                transactionEditData.setTags(null);
                requestAutoComplete();
                return true;
        }
        return false;
    }

    @Override public void onNoteUpdated(String note) {
        transactionEditData.setNote(note);
        requestAutoComplete();
    }

    @Override public void onNoteFocusFained() {
        if (!transactionEditData.isNoteSet()) {
            transactionEditData.setNote(null);
            updateNote(transactionEditData.getNote());
            requestAutoComplete();
        }
    }

    @Override public void onTransactionAutoComplete(AutoCompleteResult result) {
        transactionEditData.setAutoCompleteResult(result);
        update(true);
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.confirmedCheckBox:
                boolean canBeConfirmed = true;
                if (!transactionEditData.validateAmount(amountViewController)) {
                    canBeConfirmed = false;
                }

                if (!transactionEditData.validateAccountFrom(accountsViewController)) {
                    canBeConfirmed = false;
                }

                if (!transactionEditData.validateAccountTo(accountsViewController)) {
                    canBeConfirmed = false;
                }

                transactionEditData.setTransactionState(canBeConfirmed && isChecked ? TransactionState.Confirmed : TransactionState.Pending);
                requestAutoComplete();
                break;
            case R.id.includeInReportsCheckBox:
                transactionEditData.setIncludeInReports(isChecked);
                requestAutoComplete();
                break;
        }
    }

    @Subscribe public void onDateSet(DatePickerDialog.DateSelected dateSelected) {
        final long date = new DateTime(transactionEditData.getDate())
                .withYear(dateSelected.getYear())
                .withMonthOfYear(dateSelected.getMonthOfYear())
                .withDayOfMonth(dateSelected.getDayOfMonth())
                .getMillis();
        transactionEditData.setDate(date);
        requestAutoComplete();
    }

    @Subscribe public void onTimeSet(TimePickerDialog.TimeSelected timeSelected) {
        final long date = new DateTime(transactionEditData.getDate())
                .withHourOfDay(timeSelected.getHourOfDay())
                .withMinuteOfHour(timeSelected.getMinute())
                .getMillis();
        transactionEditData.setDate(date);
        requestAutoComplete();
    }

    @Subscribe public void onExchangeRateUpdated(ExchangeRateRequest request) {
        if (!request.isError() && transactionEditData.getAccountFrom() != null && transactionEditData.getAccountTo() != null && transactionEditData.getAccountFrom().getCurrency().getCode().equals(request.getFromCode()) && transactionEditData.getAccountTo().getCurrency().getCode().equals(request.getToCode())) {
            transactionEditData.setExchangeRate(request.getCurrency().getExchangeRate());
            requestAutoComplete();
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_AMOUNT:
                    transactionEditData.setAmount(data.getLongExtra(CalculatorActivity.RESULT_EXTRA_RESULT, 0));
                    requestAutoComplete();
                    break;
                case REQUEST_EXCHANGE_RATE:
                    transactionEditData.setExchangeRate(data.getDoubleExtra(CalculatorActivity.RESULT_EXTRA_RAW_RESULT, 1.0));
                    requestAutoComplete();
                    break;
                case REQUEST_AMOUNT_TO:
                    final long newAmount = Math.round(data.getLongExtra(CalculatorActivity.RESULT_EXTRA_RESULT, 0) / transactionEditData.getExchangeRate());
                    transactionEditData.setAmount(newAmount);
                    refreshExchangeRate();
                    requestAutoComplete();
                    break;
                case REQUEST_ACCOUNT_FROM:
                    transactionEditData.setAccountFrom(ModelListActivity.<Account>getModelExtra(data));
                    refreshExchangeRate();
                    requestAutoComplete();
                    break;
                case REQUEST_ACCOUNT_TO:
                    transactionEditData.setAccountTo(ModelListActivity.<Account>getModelExtra(data));
                    refreshExchangeRate();
                    requestAutoComplete();
                    break;
                case REQUEST_CATEGORY:
                    transactionEditData.setCategory(ModelListActivity.<Category>getModelExtra(data));
                    requestAutoComplete();
                    break;
                case REQUEST_TAGS:
                    transactionEditData.setTags(ModelListActivity.<Tag>getModelsExtra(data));
                    requestAutoComplete();
                    break;
            }
        }
    }

    public boolean save() {
        DataStore.insert().values(transactionEditData.getModel().asValues()).into(activity, TransactionsProvider.uriTransactions());
        return true;
    }

    public void setStoredTransaction(Transaction transaction) {
        transactionEditData.setStoredTransaction(transaction);
        update(false);
    }

    private void update(boolean isAutoComplete) {
        if (!isResumed()) {
            if (isAutoComplete) {
                isAutoCompleteUpdateQueued = true;
            }
            return;
        }
        isUpdated = true;
        isAutoCompleteUpdateQueued = false;

        updateTransactionType(transactionEditData.getTransactionType());
        updateAmount(transactionEditData.getAmount());
        updateExchangeRate(transactionEditData.getExchangeRate());
        updateDate(transactionEditData.getDate());
        updateAccountFrom(transactionEditData.getAccountFrom());
        updateAccountTo(transactionEditData.getAccountTo());
        updateCategory(transactionEditData.getCategory());
        updateTags(transactionEditData.getTags());
        updateTransactionState(transactionEditData.getTransactionState());
        updateIncludeInReports(transactionEditData.getIncludeInReports());

        if (!isAutoComplete || !noteViewController.hasFocus()) {
            updateNote(transactionEditData.getNote());
        }

        listener.onTransactionUpdated(transactionEditData);
    }

    private void requestAutoComplete() {
        hideAutoCompleteItems();

        if (transactionEditData.getStoredTransaction() != null) {
            update(true);
            return;
        }

        final AutoCompleteInput.Builder input = AutoCompleteInput.build(transactionEditData.getTransactionType());
        input.setDate(transactionEditData.getDate());

        if (transactionEditData.isAmountSet()) {
            input.setAmount(transactionEditData.getAmount());
        }

        if (transactionEditData.isAccountFromSet()) {
            input.setAccountFrom(transactionEditData.getAccountFrom());
        }

        if (transactionEditData.isAccountToSet()) {
            input.setAccountTo(transactionEditData.getAccountTo());
        }

        if (transactionEditData.isCategorySet()) {
            input.setCategory(transactionEditData.getCategory());
        }

        if (transactionEditData.isTagsSet()) {
            input.setTags(transactionEditData.getTags());
        }

        if (transactionEditData.isNoteSet()) {
            input.setNote(transactionEditData.getNote());
        }

        new SmartTransactionAutoComplete(activity, autoCompleteExecutor, this, input.build(), LOG_AUTO_COMPLETE).execute();
    }

    private void toggleTransactionType() {
        TransactionType transactionType;
        switch (transactionEditData.getTransactionType()) {
            case Expense:
                transactionType = TransactionType.Income;
                break;
            case Income:
                transactionType = TransactionType.Transfer;
                break;
            case Transfer:
                transactionType = TransactionType.Expense;
                break;
            default:
                throw new IllegalArgumentException("TransactionType " + transactionEditData.getTransactionType() + " is not supported.");
        }

        if (currentAutoCompleteAdapter != null) {
            currentAutoCompleteAdapter.hide();
            currentAutoCompleteAdapter = null;
        }

        transactionEditData.setTransactionType(transactionType);
        requestAutoComplete();
    }

    private void refreshExchangeRate() {
        switch (transactionEditData.getTransactionType()) {
            case Expense:
            case Income:
                transactionEditData.setExchangeRate(1.0);
                requestAutoComplete();
                break;
            case Transfer:
                if (transactionEditData.getAccountFrom() != null && transactionEditData.getAccountTo() != null) {
                    final Currency currencyFrom = transactionEditData.getAccountFrom().getCurrency();
                    final Currency currencyTo = transactionEditData.getAccountTo().getCurrency();
                    if (currencyFrom.isDefault() || currencyTo.isDefault()) {
                        if (currencyFrom.isDefault()) {
                            transactionEditData.setExchangeRate(1.0 / currencyTo.getExchangeRate());
                        } else {
                            transactionEditData.setExchangeRate(currencyFrom.getExchangeRate());
                        }
                        requestAutoComplete();
                    } else {
                        currenciesApi.getExchangeRate(transactionEditData.getAccountFrom().getCurrency().getCode(), transactionEditData.getAccountTo().getCurrency().getCode());
                    }
                }
                break;
        }
    }

    private void updateTransactionType(TransactionType transactionType) {
        transactionTypeViewController.setTransactionType(transactionType);
        amountViewController.setTransactionType(transactionType);
        accountsViewController.setTransactionType(transactionType);
        categoryViewController.setTransactionType(transactionType);
        noteViewController.setTransactionType(transactionType);
    }

    private void updateAmount(long amount) {
        amountViewController.setAmount(amount);
    }

    private void updateExchangeRate(double exchangeRate) {
        amountViewController.setExchangeRate(exchangeRate);
    }

    private void updateDate(long date) {
        dateTimeViewController.setDateTime(date);
        dateTimeViewController.isSetByUser(transactionEditData.isDateSet());
    }

    private void updateAccountFrom(Account account) {
        accountsViewController.setAccountFrom(account);
        amountViewController.setAccountFrom(account);
    }

    private void updateAccountTo(Account account) {
        accountsViewController.setAccountTo(account);
        amountViewController.setAccountTo(account);
    }

    private void updateCategory(Category category) {
        categoryViewController.setCategory(category);
    }

    private void updateTags(List<Tag> tags) {
        tagsViewController.setTags(tags);
    }

    private void updateNote(String note) {
        noteViewController.setNote(note);
        noteViewController.setIsSetByUser(transactionEditData.isNoteSet());
    }

    private void updateTransactionState(TransactionState transactionState) {
        transactionStateViewController.setTransactionState(transactionState);
    }

    private void updateIncludeInReports(boolean includeInReports) {
        flagsViewController.setIncludeInReports(includeInReports);
    }

    private void hideAutoCompleteItems() {
        if (currentAutoCompleteAdapter != null) {
            currentAutoCompleteAdapter.hide();
            currentAutoCompleteAdapter = null;
        }
    }

    public static interface OnTransactionUpdatedListener {
        public void onTransactionUpdated(TransactionEditData transactionEditData);
    }
}
