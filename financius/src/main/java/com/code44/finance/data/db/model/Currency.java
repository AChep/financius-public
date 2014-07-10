package com.code44.finance.data.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.code44.finance.App;
import com.code44.finance.R;
import com.code44.finance.data.Query;
import com.code44.finance.data.db.Column;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.providers.CurrenciesProvider;
import com.code44.finance.utils.IOUtils;

public class Currency extends BaseModel {
    public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {
        public Currency createFromParcel(Parcel in) {
            return new Currency(in);
        }

        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    private static Currency defaultCurrency;

    private String code;
    private String symbol;
    private SymbolPosition symbolPosition;
    private DecimalSeparator decimalSeparator;
    private GroupSeparator groupSeparator;
    private int decimalCount;
    private boolean isDefault;
    private double exchangeRate;

    public Currency() {
        super();
        setCode(null);
        setSymbol(null);
        setSymbolPosition(SymbolPosition.FAR_RIGHT);
        setDecimalSeparator(DecimalSeparator.DOT);
        setGroupSeparator(GroupSeparator.COMMA);
        setDecimalCount(2);
        setDefault(false);
        setExchangeRate(1.0);
    }

    public Currency(Parcel in) {
        super(in);
        setCode(in.readString());
        setSymbol(in.readString());
        setSymbolPosition(SymbolPosition.fromInt(in.readInt()));
        setDecimalSeparator(DecimalSeparator.fromSymbol(in.readString()));
        setGroupSeparator(GroupSeparator.fromSymbol(in.readString()));
        setDecimalCount(in.readInt());
        setDefault(in.readInt() != 0);
        setExchangeRate(in.readDouble());
    }

    public static Currency getDefault() {
        if (defaultCurrency == null) {
            final Cursor cursor = Query.create()
                    .projectionId(Tables.Currencies.ID)
                    .projection(Tables.Currencies.PROJECTION)
                    .selection(Tables.Currencies.IS_DEFAULT.getName() + "=?", "1")
                    .from(App.getAppContext(), CurrenciesProvider.uriCurrencies())
                    .execute();

            defaultCurrency = Currency.from(cursor);
            IOUtils.closeQuietly(cursor);
        }
        return defaultCurrency;
    }

    public static void updateDefaultCurrency(SQLiteDatabase db) {
        final Cursor cursor = Query.create()
                .projectionId(Tables.Currencies.ID)
                .projection(Tables.Currencies.PROJECTION)
                .selection(Tables.Currencies.IS_DEFAULT + "=?", "1")
                .from(db, Tables.Currencies.TABLE_NAME)
                .execute();

        defaultCurrency = Currency.from(cursor);
        IOUtils.closeQuietly(cursor);
    }

    public static Currency from(Cursor cursor) {
        final Currency currency = new Currency();
        if (cursor.getCount() > 0) {
            currency.updateFrom(cursor, null);
        }
        return currency;
    }

    public static Currency fromCurrencyFrom(Cursor cursor) {
        final Currency currency = new Currency();
        if (cursor.getCount() > 0) {
            currency.updateFrom(cursor, Tables.Currencies.TEMP_TABLE_NAME_FROM_CURRENCY);
        }
        return currency;
    }

    public static Currency fromCurrencyTo(Cursor cursor) {
        final Currency currency = new Currency();
        if (cursor.getCount() > 0) {
            currency.updateFrom(cursor, Tables.Currencies.TEMP_TABLE_NAME_TO_CURRENCY);
        }
        return currency;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(getCode());
        dest.writeString(getSymbol());
        dest.writeInt(getSymbolPosition().asInt());
        dest.writeString(getDecimalSeparator().symbol());
        dest.writeString(getGroupSeparator().symbol());
        dest.writeInt(getDecimalCount());
        dest.writeInt(isDefault() ? 1 : 0);
        dest.writeDouble(getExchangeRate());
    }

    @Override
    public void checkValues() throws IllegalStateException {
        super.checkValues();

        if (TextUtils.isEmpty(code)) {
            throw new IllegalStateException("Code cannot be empty.");
        }

        if (symbolPosition == null) {
            throw new IllegalStateException("SymbolPosition cannot be null.");
        }

        if (decimalSeparator == null) {
            throw new IllegalStateException("DecimalSeparator cannot be null.");
        }

        if (groupSeparator == null) {
            throw new IllegalStateException("GroupSeparator cannot be null.");
        }

        if (decimalCount < 0 || decimalCount > 2) {
            throw new IllegalStateException("Decimal count must be [0, 2]");
        }

        if (Double.compare(exchangeRate, 0.0) < 0) {
            throw new IllegalStateException("Exchange rate must be > 0");
        }
    }

    @Override
    protected Column getIdColumn() {
        return Tables.Currencies.ID;
    }

    @Override
    protected Column getServerIdColumn() {
        return Tables.Currencies.SERVER_ID;
    }

    @Override
    protected Column getItemStateColumn() {
        return Tables.Currencies.ITEM_STATE;
    }

    @Override
    protected Column getSyncStateColumn() {
        return Tables.Currencies.SYNC_STATE;
    }

    @Override
    public ContentValues asContentValues() {
        final ContentValues values = super.asContentValues();

        values.put(Tables.Currencies.CODE.getName(), code);
        values.put(Tables.Currencies.SYMBOL.getName(), symbol);
        values.put(Tables.Currencies.SYMBOL_POSITION.getName(), symbolPosition.asInt());
        values.put(Tables.Currencies.DECIMAL_SEPARATOR.getName(), decimalSeparator.symbol());
        values.put(Tables.Currencies.GROUP_SEPARATOR.getName(), groupSeparator.symbol());
        values.put(Tables.Currencies.DECIMAL_COUNT.getName(), decimalCount);
        values.put(Tables.Currencies.IS_DEFAULT.getName(), isDefault);
        values.put(Tables.Currencies.EXCHANGE_RATE.getName(), isDefault ? 1.0f : exchangeRate);

        return values;
    }

    @Override
    protected void updateFrom(Cursor cursor, String columnPrefixTable) {
        super.updateFrom(cursor, columnPrefixTable);

        int index;

        index = cursor.getColumnIndex(Tables.Currencies.CODE.getName(columnPrefixTable));
        if (index >= 0) {
            setCode(cursor.getString(index));
        }

        index = cursor.getColumnIndex(Tables.Currencies.SYMBOL.getName(columnPrefixTable));
        if (index >= 0) {
            setSymbol(cursor.getString(index));
        }

        index = cursor.getColumnIndex(Tables.Currencies.SYMBOL_POSITION.getName(columnPrefixTable));
        if (index >= 0) {
            setSymbolPosition(SymbolPosition.fromInt(cursor.getInt(index)));
        }

        index = cursor.getColumnIndex(Tables.Currencies.DECIMAL_SEPARATOR.getName(columnPrefixTable));
        if (index >= 0) {
            setDecimalSeparator(DecimalSeparator.fromSymbol(cursor.getString(index)));
        }

        index = cursor.getColumnIndex(Tables.Currencies.GROUP_SEPARATOR.getName(columnPrefixTable));
        if (index >= 0) {
            setGroupSeparator(GroupSeparator.fromSymbol(cursor.getString(index)));
        }

        index = cursor.getColumnIndex(Tables.Currencies.DECIMAL_COUNT.getName(columnPrefixTable));
        if (index >= 0) {
            setDecimalCount(cursor.getInt(index));
        }

        index = cursor.getColumnIndex(Tables.Currencies.IS_DEFAULT.getName(columnPrefixTable));
        if (index >= 0) {
            setDefault(cursor.getInt(index) != 0);
        }

        index = cursor.getColumnIndex(Tables.Currencies.EXCHANGE_RATE.getName(columnPrefixTable));
        if (index >= 0) {
            setExchangeRate(cursor.getDouble(index));
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        setServerId(code);
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolPosition getSymbolPosition() {
        return symbolPosition;
    }

    public void setSymbolPosition(SymbolPosition symbolPosition) {
        this.symbolPosition = symbolPosition;
    }

    public DecimalSeparator getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(DecimalSeparator decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public GroupSeparator getGroupSeparator() {
        return groupSeparator;
    }

    public void setGroupSeparator(GroupSeparator groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    public int getDecimalCount() {
        return decimalCount;
    }

    public void setDecimalCount(int decimalCount) {
        this.decimalCount = decimalCount;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public static enum DecimalSeparator {
        DOT("."),
        COMMA(","),
        SPACE(" ");

        private final String symbol;

        private DecimalSeparator(String symbol) {
            this.symbol = symbol;
        }

        public static DecimalSeparator fromSymbol(String symbol) {
            switch (symbol) {
                case ".":
                    return DOT;

                case ",":
                    return COMMA;

                case " ":
                    return SPACE;

                default:
                    throw new IllegalArgumentException("Symbol '" + symbol + "' is not supported.");
            }
        }

        public String symbol() {
            return symbol;
        }

        public String explanation(Context context) {
            final int resId;
            switch (this) {
                case DOT:
                    resId = R.string.dot;
                    break;

                case COMMA:
                    resId = R.string.comma;
                    break;

                case SPACE:
                    resId = R.string.space;
                    break;

                default:
                    throw new IllegalArgumentException(this + " explanation is not supported.");
            }
            return context.getString(resId);
        }
    }

    public static enum GroupSeparator {
        NONE(""),
        DOT("."),
        COMMA(","),
        SPACE(" ");

        private final String symbol;

        private GroupSeparator(String symbol) {
            this.symbol = symbol;
        }

        public static GroupSeparator fromSymbol(String symbol) {
            switch (symbol) {
                case "":
                    return NONE;

                case ".":
                    return DOT;

                case ",":
                    return COMMA;

                case " ":
                    return SPACE;

                default:
                    throw new IllegalArgumentException("Symbol '" + symbol + "' is not supported.");
            }
        }

        public String symbol() {
            return symbol;
        }

        public String explanation(Context context) {
            final int resId;
            switch (this) {
                case NONE:
                    resId = R.string.none;
                    break;

                case DOT:
                    resId = R.string.dot;
                    break;

                case COMMA:
                    resId = R.string.comma;
                    break;

                case SPACE:
                    resId = R.string.space;
                    break;

                default:
                    throw new IllegalArgumentException(this + " explanation is not supported.");
            }
            return context.getString(resId);
        }
    }

    public static enum SymbolPosition {
        CLOSE_RIGHT(SymbolPosition.VALUE_CLOSE_RIGHT),
        FAR_RIGHT(SymbolPosition.VALUE_FAR_RIGHT),
        CLOSE_LEFT(SymbolPosition.VALUE_CLOSE_LEFT),
        FAR_LEFT(SymbolPosition.VALUE_FAR_LEFT);

        private static final int VALUE_CLOSE_RIGHT = 1;
        private static final int VALUE_FAR_RIGHT = 2;
        private static final int VALUE_CLOSE_LEFT = 3;
        private static final int VALUE_FAR_LEFT = 4;

        private final int value;

        private SymbolPosition(int value) {
            this.value = value;
        }

        public static SymbolPosition fromInt(int value) {
            switch (value) {
                case VALUE_CLOSE_RIGHT:
                    return CLOSE_RIGHT;

                case VALUE_FAR_RIGHT:
                    return FAR_RIGHT;

                case VALUE_CLOSE_LEFT:
                    return CLOSE_LEFT;

                case VALUE_FAR_LEFT:
                    return FAR_LEFT;

                default:
                    throw new IllegalArgumentException("Value " + value + " is not supported.");
            }
        }

        public int asInt() {
            return value;
        }

        public String explanation(Context context) {
            final int resId;
            switch (this) {
                case CLOSE_RIGHT:
                    resId = R.string.close_right;
                    break;

                case FAR_RIGHT:
                    resId = R.string.far_right;
                    break;

                case CLOSE_LEFT:
                    resId = R.string.close_left;
                    break;

                case FAR_LEFT:
                    resId = R.string.far_left;
                    break;

                default:
                    throw new IllegalArgumentException(this + " explanation is not supported.");
            }
            return context.getString(resId);
        }
    }
}
