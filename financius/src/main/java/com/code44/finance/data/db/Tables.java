package com.code44.finance.data.db;

import android.provider.BaseColumns;

import com.code44.finance.common.model.ModelState;
import com.code44.finance.data.db.model.SyncState;

public final class Tables {
    public static final String SUFFIX_SERVER_ID = "server_id";
    public static final String SUFFIX_MODEL_STATE = "model_state";
    public static final String SUFFIX_SYNC_STATE = "sync_state";

    private Tables() {
    }

    private static Column getIdColumn(String tableName) {
        return new Column(tableName, BaseColumns._ID, Column.DataType.INTEGER_PRIMARY_KEY, null, false);
    }

    private static Column getServerIdColumn(String tableName) {
        return new Column(tableName, SUFFIX_SERVER_ID, Column.DataType.TEXT, null);
    }

    private static Column getModelStateColumn(String tableName) {
        return new Column(tableName, SUFFIX_MODEL_STATE, Column.DataType.INTEGER, String.valueOf(ModelState.NORMAL.asInt()));
    }

    private static Column getSyncStateColumn(String tableName) {
        return new Column(tableName, SUFFIX_SYNC_STATE, Column.DataType.INTEGER, String.valueOf(SyncState.NONE.asInt()));
    }

    private static String makeCreateScript(String table, Column... columns) {
        final StringBuilder sb = new StringBuilder("create table ");
        sb.append(table);
        sb.append(" (");

        if (columns != null) {
            for (int i = 0, size = columns.length; i < size; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(columns[i].getCreateScript());
            }
        }

        sb.append(");");

        return sb.toString();
    }

    public static final class Currencies {
        public static final String TABLE_NAME = "currencies";
        public static final String TEMP_TABLE_NAME_FROM_CURRENCY = "currencies_from";
        public static final String TEMP_TABLE_NAME_TO_CURRENCY = "currencies_to";

        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column SERVER_ID = getServerIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column CODE = new Column(TABLE_NAME, "code", Column.DataType.TEXT);
        public static final Column SYMBOL = new Column(TABLE_NAME, "symbol", Column.DataType.TEXT);
        public static final Column SYMBOL_POSITION = new Column(TABLE_NAME, "symbol_position", Column.DataType.INTEGER);
        public static final Column DECIMAL_SEPARATOR = new Column(TABLE_NAME, "decimal_separator", Column.DataType.TEXT);
        public static final Column GROUP_SEPARATOR = new Column(TABLE_NAME, "group_separator", Column.DataType.TEXT);
        public static final Column DECIMAL_COUNT = new Column(TABLE_NAME, "decimal_count", Column.DataType.INTEGER);
        public static final Column IS_DEFAULT = new Column(TABLE_NAME, "is_default", Column.DataType.BOOLEAN);
        public static final Column EXCHANGE_RATE = new Column(TABLE_NAME, "exchange_rate", Column.DataType.REAL);

        public static final String[] PROJECTION = {SERVER_ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                CODE.getName(), SYMBOL.getName(), SYMBOL_POSITION.getName(), DECIMAL_SEPARATOR.getName(),
                GROUP_SEPARATOR.getName(), DECIMAL_COUNT.getName(), IS_DEFAULT.getName(), EXCHANGE_RATE.getName()};

        public static final String[] PROJECTION_ACCOUNT_FROM = {SERVER_ID.getName(TEMP_TABLE_NAME_FROM_CURRENCY), MODEL_STATE.getName(TEMP_TABLE_NAME_FROM_CURRENCY), SYNC_STATE.getName(TEMP_TABLE_NAME_FROM_CURRENCY),
                CODE.getName(TEMP_TABLE_NAME_FROM_CURRENCY), SYMBOL.getName(TEMP_TABLE_NAME_FROM_CURRENCY), SYMBOL_POSITION.getName(TEMP_TABLE_NAME_FROM_CURRENCY), DECIMAL_SEPARATOR.getName(TEMP_TABLE_NAME_FROM_CURRENCY),
                GROUP_SEPARATOR.getName(TEMP_TABLE_NAME_FROM_CURRENCY), DECIMAL_COUNT.getName(TEMP_TABLE_NAME_FROM_CURRENCY), IS_DEFAULT.getName(TEMP_TABLE_NAME_FROM_CURRENCY), EXCHANGE_RATE.getName(TEMP_TABLE_NAME_FROM_CURRENCY)};

        public static final String[] PROJECTION_ACCOUNT_TO = {SERVER_ID.getName(TEMP_TABLE_NAME_TO_CURRENCY), MODEL_STATE.getName(TEMP_TABLE_NAME_TO_CURRENCY), SYNC_STATE.getName(TEMP_TABLE_NAME_TO_CURRENCY),
                CODE.getName(TEMP_TABLE_NAME_TO_CURRENCY), SYMBOL.getName(TEMP_TABLE_NAME_TO_CURRENCY), SYMBOL_POSITION.getName(TEMP_TABLE_NAME_TO_CURRENCY), DECIMAL_SEPARATOR.getName(TEMP_TABLE_NAME_TO_CURRENCY),
                GROUP_SEPARATOR.getName(TEMP_TABLE_NAME_TO_CURRENCY), DECIMAL_COUNT.getName(TEMP_TABLE_NAME_TO_CURRENCY), IS_DEFAULT.getName(TEMP_TABLE_NAME_TO_CURRENCY), EXCHANGE_RATE.getName(TEMP_TABLE_NAME_TO_CURRENCY)};

        private Currencies() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, ID, SERVER_ID, MODEL_STATE, SYNC_STATE, CODE, SYMBOL,
                    SYMBOL_POSITION, DECIMAL_SEPARATOR, GROUP_SEPARATOR, DECIMAL_COUNT, IS_DEFAULT,
                    EXCHANGE_RATE);
        }
    }

    public static final class Accounts {
        public static final String TABLE_NAME = "accounts";
        public static final String TEMP_TABLE_NAME_FROM_ACCOUNT = "accounts_from";
        public static final String TEMP_TABLE_NAME_TO_ACCOUNT = "accounts_to";

        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column SERVER_ID = getServerIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column CURRENCY_ID = new Column(TABLE_NAME, "currency_id", Column.DataType.INTEGER);
        public static final Column TITLE = new Column(TABLE_NAME, "title", Column.DataType.TEXT);
        public static final Column NOTE = new Column(TABLE_NAME, "note", Column.DataType.TEXT);
        public static final Column BALANCE = new Column(TABLE_NAME, "balance", Column.DataType.INTEGER);
        public static final Column OWNER = new Column(TABLE_NAME, "owner", Column.DataType.INTEGER);

        public static final String[] PROJECTION = {SERVER_ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                CURRENCY_ID.getName(), TITLE.getName(), NOTE.getName(), BALANCE.getName(), OWNER.getName()};

        public static final String[] PROJECTION_ACCOUNT_FROM = {SERVER_ID.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), MODEL_STATE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                SYNC_STATE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), CURRENCY_ID.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                TITLE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), NOTE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT),
                BALANCE.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT), OWNER.getNameWithAs(TEMP_TABLE_NAME_FROM_ACCOUNT)};

        public static final String[] PROJECTION_ACCOUNT_TO = {SERVER_ID.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), MODEL_STATE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                SYNC_STATE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), CURRENCY_ID.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                TITLE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), NOTE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT),
                BALANCE.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT), OWNER.getNameWithAs(TEMP_TABLE_NAME_TO_ACCOUNT)};

        private Accounts() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, ID, SERVER_ID, MODEL_STATE, SYNC_STATE, CURRENCY_ID, TITLE, NOTE, BALANCE, OWNER);
        }
    }

    public static final class Categories {
        public static final String TABLE_NAME = "categories";

        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column SERVER_ID = getServerIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column TITLE = new Column(TABLE_NAME, "title", Column.DataType.TEXT);
        public static final Column COLOR = new Column(TABLE_NAME, "color", Column.DataType.INTEGER);
        public static final Column TYPE = new Column(TABLE_NAME, "type", Column.DataType.INTEGER);
        public static final Column OWNER = new Column(TABLE_NAME, "owner", Column.DataType.INTEGER);
        public static final Column SORT_ORDER = new Column(TABLE_NAME, "sort_order", Column.DataType.INTEGER);

        public static final String[] PROJECTION = {SERVER_ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                TITLE.getName(), COLOR.getName(), TYPE.getName(), OWNER.getName(), SORT_ORDER.getName()};

        private Categories() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, ID, SERVER_ID, MODEL_STATE, SYNC_STATE, TITLE, COLOR, TYPE, OWNER, SORT_ORDER);
        }
    }

    public static final class Transactions {
        public static final String TABLE_NAME = "transactions";

        public static final Column ID = getIdColumn(TABLE_NAME);
        public static final Column SERVER_ID = getServerIdColumn(TABLE_NAME);
        public static final Column MODEL_STATE = getModelStateColumn(TABLE_NAME);
        public static final Column SYNC_STATE = getSyncStateColumn(TABLE_NAME);
        public static final Column ACCOUNT_FROM_ID = new Column(TABLE_NAME, "account_from_id", Column.DataType.INTEGER);
        public static final Column ACCOUNT_TO_ID = new Column(TABLE_NAME, "account_to_id", Column.DataType.INTEGER);
        public static final Column CATEGORY_ID = new Column(TABLE_NAME, "category_id", Column.DataType.INTEGER);
        public static final Column DATE = new Column(TABLE_NAME, "date", Column.DataType.DATETIME);
        public static final Column AMOUNT = new Column(TABLE_NAME, "amount", Column.DataType.INTEGER);
        public static final Column EXCHANGE_RATE = new Column(TABLE_NAME, "exchange_rate", Column.DataType.REAL);
        public static final Column NOTE = new Column(TABLE_NAME, "note", Column.DataType.TEXT);
        public static final Column STATE = new Column(TABLE_NAME, "state", Column.DataType.INTEGER);

        public static final String[] PROJECTION = {SERVER_ID.getName(), MODEL_STATE.getName(), SYNC_STATE.getName(),
                ACCOUNT_FROM_ID.getName(), ACCOUNT_TO_ID.getName(), CATEGORY_ID.getName(),
                DATE.getName(), AMOUNT.getName(), EXCHANGE_RATE.getName(), NOTE.getName(), STATE.getName()};

        private Transactions() {
        }

        public static String createScript() {
            return makeCreateScript(TABLE_NAME, ID, SERVER_ID, MODEL_STATE, SYNC_STATE, ACCOUNT_FROM_ID,
                    ACCOUNT_TO_ID, CATEGORY_ID, DATE, AMOUNT, EXCHANGE_RATE, NOTE, STATE);
        }
    }
}
