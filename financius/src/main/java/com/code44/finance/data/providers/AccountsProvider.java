package com.code44.finance.data.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.code44.finance.R;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.Query;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.db.model.Account;
import com.code44.finance.data.db.model.BaseModel;
import com.code44.finance.data.db.model.Category;
import com.code44.finance.data.db.model.Transaction;
import com.code44.finance.utils.IOUtils;

import java.util.List;
import java.util.Map;

public class AccountsProvider extends BaseModelProvider {
    private static final String EXTRA_BALANCE_DELTA = "balance_delta";

    public static Uri uriAccounts() {
        return uriModels(AccountsProvider.class, Tables.Accounts.TABLE_NAME);
    }

    public static Uri uriAccount(long accountId) {
        return uriModel(AccountsProvider.class, Tables.Accounts.TABLE_NAME, accountId);
    }

    @Override
    protected String getModelTable() {
        return Tables.Accounts.TABLE_NAME;
    }

    @Override
    protected String getQueryTables() {
        return getModelTable() + " inner join " + Tables.Currencies.TABLE_NAME + " on " + Tables.Currencies.ID.getNameWithTable() + "=" + Tables.Accounts.CURRENCY_ID.getName();
    }

    @Override
    protected void onBeforeInsertItem(Uri uri, ContentValues values, Map<String, Object> outExtras) {
        super.onBeforeInsertItem(uri, values, outExtras);

        final long currentBalance = getCurrentBalance(values);
        final long newBalance = values.getAsLong(Tables.Accounts.BALANCE.getName());
        outExtras.put(EXTRA_BALANCE_DELTA, newBalance - currentBalance);
        values.remove(Tables.Accounts.BALANCE.getName());
    }

    @Override
    protected void onAfterInsertItem(Uri uri, ContentValues values, long id, Map<String, Object> extras) {
        super.onAfterInsertItem(uri, values, id, extras);

        final Account account = new Account();
        account.setId(id);

        long balanceDelta = (long) extras.get(EXTRA_BALANCE_DELTA);
        final Transaction transaction = createBalanceTransaction(account, balanceDelta);
        if (transaction != null) {
            DataStore.insert().model(transaction).into(TransactionsProvider.uriTransactions());
        }
    }

    @Override
    protected void onBeforeDeleteItems(Uri uri, String selection, String[] selectionArgs, BaseModel.ItemState itemState, Map<String, Object> outExtras) {
        super.onBeforeDeleteItems(uri, selection, selectionArgs, itemState, outExtras);

        final List<Long> affectedIds = getIdList(Tables.Accounts.TABLE_NAME, selection, selectionArgs);
        outExtras.put("affectedIds", affectedIds);
    }

    @Override
    protected void onAfterDeleteItems(Uri uri, String selection, String[] selectionArgs, BaseModel.ItemState itemState, Map<String, Object> extras) {
        super.onAfterDeleteItems(uri, selection, selectionArgs, itemState, extras);

        //noinspection unchecked
        final List<Long> affectedIds = (List<Long>) extras.get("affectedIds");
        if (affectedIds.size() > 0) {
            final Uri transactionsUri = uriForDeleteFromItemState(TransactionsProvider.uriTransactions(), itemState);

            Query query = Query.get().selectionInClause(Tables.Transactions.ACCOUNT_FROM_ID.getName(), affectedIds);
            getContext().getContentResolver().delete(transactionsUri, query.getSelection(), query.getSelectionArgs());

            query = Query.get().selectionInClause(Tables.Transactions.ACCOUNT_TO_ID.getName(), affectedIds);
            getContext().getContentResolver().delete(transactionsUri, query.getSelection(), query.getSelectionArgs());
        }
    }

    private long getCurrentBalance(ContentValues values) {
        final Long accountId = values.getAsLong(Tables.Accounts.ID.getName());
        if (accountId == null || accountId == 0) {
            return 0;
        }

        final Cursor cursor = Query.get()
                .projection(Tables.Accounts.BALANCE.getName())
                .selection(Tables.Accounts.ID + "=?", String.valueOf(accountId))
                .from(database, Tables.Accounts.TABLE_NAME)
                .execute();
        final long balance = cursor.getLong(cursor.getColumnIndex(Tables.Accounts.BALANCE.getName()));
        IOUtils.closeQuietly(cursor);
        return balance;
    }

    private Transaction createBalanceTransaction(Account account, long balanceDelta) {
        Transaction transaction = null;

        if (balanceDelta > 0) {
            transaction = new Transaction();
            transaction.setAccountTo(account);
            transaction.setCategory(Category.getIncome());
        } else if (balanceDelta < 0) {
            transaction = new Transaction();
            transaction.setAccountFrom(account);
            transaction.setCategory(Category.getExpense());
        }

        if (transaction != null) {
            transaction.setAmount(Math.abs(balanceDelta));
            transaction.setDate(System.currentTimeMillis());
            transaction.setNote(getContext().getString(R.string.account_balance_update));
        }

        return transaction;
    }
}
