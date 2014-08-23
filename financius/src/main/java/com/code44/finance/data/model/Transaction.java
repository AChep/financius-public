package com.code44.finance.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.code44.finance.backend.endpoint.transactions.model.TransactionEntity;
import com.code44.finance.common.model.CategoryType;
import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.utils.Preconditions;
import com.code44.finance.data.db.Column;
import com.code44.finance.data.db.Tables;

public class Transaction extends BaseModel<TransactionEntity> {
    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    private Account accountFrom;
    private Account accountTo;
    private Category category;
    private long date;
    private long amount;
    private double exchangeRate;
    private String note;
    private TransactionState transactionState;
    private boolean includeInReports;

    public Transaction() {
        super();
        setAccountFrom(Account.getSystem());
        setAccountTo(Account.getSystem());
        setCategory(Category.getExpense());
        setDate(System.currentTimeMillis());
        setAmount(0);
        setExchangeRate(1.0);
        setNote(null);
        setTransactionState(TransactionState.CONFIRMED);
        setIncludeInReports(true);
    }

    public Transaction(Parcel in) {
        super(in);
    }

    public static Transaction from(Cursor cursor) {
        final Transaction transaction = new Transaction();
        if (cursor.getCount() > 0) {
            transaction.updateFrom(cursor, null);
        }
        return transaction;
    }

    public static Transaction from(TransactionEntity entity, Account accountFrom, Account accountTo, Category category) {
        final Transaction transaction = new Transaction();
        transaction.setAccountFrom(accountFrom);
        transaction.setAccountTo(accountTo);
        transaction.setCategory(category);
        transaction.updateFrom(entity);
        return transaction;
    }

    @Override protected Column getLocalIdColumn() {
        return Tables.Transactions.LOCAL_ID;
    }

    @Override protected Column getIdColumn() {
        return Tables.Transactions.ID;
    }

    @Override protected Column getModelStateColumn() {
        return Tables.Transactions.MODEL_STATE;
    }

    @Override protected Column getSyncStateColumn() {
        return Tables.Transactions.SYNC_STATE;
    }

    @Override protected void toValues(ContentValues values) {
        values.put(Tables.Transactions.ACCOUNT_FROM_ID.getName(), accountFrom.getId());
        values.put(Tables.Transactions.ACCOUNT_TO_ID.getName(), accountTo.getId());
        values.put(Tables.Transactions.CATEGORY_ID.getName(), category.getId());
        values.put(Tables.Transactions.DATE.getName(), date);
        values.put(Tables.Transactions.AMOUNT.getName(), amount);
        values.put(Tables.Transactions.EXCHANGE_RATE.getName(), exchangeRate);
        values.put(Tables.Transactions.NOTE.getName(), note);
        values.put(Tables.Transactions.STATE.getName(), transactionState.asInt());
        values.put(Tables.Transactions.INCLUDE_IN_REPORTS.getName(), includeInReports);
    }

    @Override protected void toParcel(Parcel parcel) {
        parcel.writeParcelable(accountFrom, 0);
        parcel.writeParcelable(accountTo, 0);
        parcel.writeParcelable(category, 0);
        parcel.writeLong(date);
        parcel.writeLong(amount);
        parcel.writeDouble(exchangeRate);
        parcel.writeString(note);
        parcel.writeInt(transactionState.asInt());
        parcel.writeInt(includeInReports ? 1 : 0);
    }

    @Override protected void toEntity(TransactionEntity entity) {
        entity.setAccountFromId(accountFrom.getId());
        entity.setAccountToId(accountTo.getId());
        entity.setCategoryId(category.getId());
        entity.setDate(date);
        entity.setAmount(amount);
        entity.setExchangeRate(exchangeRate);
        entity.setNote(note);
        entity.setTransactionState(transactionState.toString());
        entity.setIncludeInReports(includeInReports);
    }

    @Override protected TransactionEntity createEntity() {
        return new TransactionEntity();
    }

    @Override protected void fromParcel(Parcel parcel) {
        setAccountFrom((Account) parcel.readParcelable(Account.class.getClassLoader()));
        setAccountTo((Account) parcel.readParcelable(Account.class.getClassLoader()));
        setCategory((Category) parcel.readParcelable(Category.class.getClassLoader()));
        setDate(parcel.readLong());
        setAmount(parcel.readLong());
        setExchangeRate(parcel.readDouble());
        setNote(parcel.readString());
        setTransactionState(TransactionState.fromInt(parcel.readInt()));
        setIncludeInReports(parcel.readInt() != 0);
    }

    @Override protected void fromCursor(Cursor cursor, String columnPrefixTable) {
        int index;

        // Account from
        final Account accountFrom = Account.fromAccountFrom(cursor);
        accountFrom.setLocalId(0);
        setAccountFrom(accountFrom);

        // Account to
        final Account accountTo = Account.fromAccountTo(cursor);
        accountTo.setLocalId(0);
        setAccountTo(accountTo);

        // Category
        final Category category = Category.from(cursor);
        category.setLocalId(0);
        setCategory(category);

        // Date
        index = cursor.getColumnIndex(Tables.Transactions.DATE.getName(columnPrefixTable));
        if (index >= 0) {
            setDate(cursor.getLong(index));
        }

        // Amount
        index = cursor.getColumnIndex(Tables.Transactions.AMOUNT.getName(columnPrefixTable));
        if (index >= 0) {
            setAmount(cursor.getLong(index));
        }

        // Exchange rate
        index = cursor.getColumnIndex(Tables.Transactions.EXCHANGE_RATE.getName(columnPrefixTable));
        if (index >= 0) {
            setExchangeRate(cursor.getDouble(index));
        }

        // Note
        index = cursor.getColumnIndex(Tables.Transactions.NOTE.getName(columnPrefixTable));
        if (index >= 0) {
            setNote(cursor.getString(index));
        }

        // Transaction state
        index = cursor.getColumnIndex(Tables.Transactions.STATE.getName(columnPrefixTable));
        if (index >= 0) {
            setTransactionState(TransactionState.fromInt(cursor.getInt(index)));
        }

        // Include in reports
        index = cursor.getColumnIndex(Tables.Transactions.INCLUDE_IN_REPORTS.getName(columnPrefixTable));
        if (index >= 0) {
            setIncludeInReports(cursor.getInt(index) != 0);
        }
    }

    @Override protected void fromEntity(TransactionEntity entity) {
        setDate(entity.getDate());
        setAmount(entity.getAmount());
        setExchangeRate(entity.getExchangeRate());
        setNote(entity.getNote());
        setTransactionState(TransactionState.valueOf(entity.getTransactionState()));
        setIncludeInReports(entity.getIncludeInReports());
    }

    @Override public void checkValues() throws IllegalStateException {
        super.checkValues();
        Preconditions.checkNotNull(accountFrom, "AccountFrom cannot be null.");
        Preconditions.checkNotNull(accountTo, "AccountTo cannot be null.");
        Preconditions.checkNotNull(category, "Category cannot be null.");
        Preconditions.checkNotNull(transactionState, "Transaction state cannot be null.");

        if (accountFrom == accountTo && transactionState == TransactionState.CONFIRMED) {
            throw new IllegalStateException("AccountFrom cannot be equal to AccountTo.");
        }

        if (category.getCategoryType() == CategoryType.EXPENSE && accountFrom == Account.getSystem() && transactionState == TransactionState.CONFIRMED) {
            throw new IllegalStateException("Account from cannot be system account.");
        }

        if (category.getCategoryType() == CategoryType.INCOME && accountTo == Account.getSystem() && transactionState == TransactionState.CONFIRMED) {
            throw new IllegalStateException("Account to cannot be system account.");
        }

        if (Double.compare(exchangeRate, 0) < 0) {
            throw new IllegalStateException("Exchange rate must be > 0.");
        }
    }

    public Account getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(Account accountFrom) {
        this.accountFrom = accountFrom;
    }

    public Account getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(Account accountTo) {
        this.accountTo = accountTo;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

    public boolean includeInReports() {
        return includeInReports;
    }

    public void setIncludeInReports(boolean includeInReports) {
        this.includeInReports = includeInReports;
    }
}
