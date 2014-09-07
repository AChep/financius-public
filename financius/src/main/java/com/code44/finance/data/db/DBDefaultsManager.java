package com.code44.finance.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.TextUtils;

import com.code44.finance.R;
import com.code44.finance.common.model.AccountOwner;
import com.code44.finance.common.model.CategoryOwner;
import com.code44.finance.common.model.CategoryType;
import com.code44.finance.common.utils.Preconditions;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Currency;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class DBDefaultsManager {
    private final Context context;
    private final SQLiteDatabase database;

    public DBDefaultsManager(Context context, SQLiteDatabase database) {
        Preconditions.checkNotNull(context, "Context cannot be null.");
        Preconditions.checkNotNull(database, "Database cannot be null.");

        this.context = context;
        this.database = database;
    }

    public void addDefaults() {
        final String mainCurrencyId = addCurrencies();
        addAccounts(mainCurrencyId);
        addCategories();
    }

    private String addCurrencies() {
        final Set<String> currencyCodes = new HashSet<>();
        final String mainCurrencyCode = getMainCurrencyCode();
        currencyCodes.add(mainCurrencyCode);

        // Popular currencies
        currencyCodes.add("USD");
        currencyCodes.add("EUR");
        currencyCodes.add("GBP");
        currencyCodes.add("CNY");
        currencyCodes.add("INR");
        currencyCodes.add("RUB");
        currencyCodes.add("JPY");

        // Create currencies
        String mainCurrencyId = "";
        for (String code : currencyCodes) {
            java.util.Currency javaCurrency = getCurrencyFromCode(code);
            if (javaCurrency != null) {
                Currency currency = new Currency();
                currency.setId(UUID.randomUUID().toString());
                currency.setCode(code);
                currency.setSymbol(javaCurrency.getSymbol());
                currency.setDecimalCount(javaCurrency.getDefaultFractionDigits());
                currency.setDefault(code.equals(mainCurrencyCode));
                database.insert(Tables.Currencies.TABLE_NAME, null, currency.asValues());

                if (currency.isDefault()) {
                    mainCurrencyId = currency.getId();
                }
            }
        }

        return mainCurrencyId;
    }

    private void addAccounts(String mainCurrencyId) {
        final Currency currency = new Currency();
        currency.setId(mainCurrencyId);

        final Account systemAccount = new Account();
        systemAccount.setCurrency(currency);
        systemAccount.setId(UUID.randomUUID().toString());
        systemAccount.setAccountOwner(AccountOwner.SYSTEM);

        database.insert(Tables.Accounts.TABLE_NAME, null, systemAccount.asValues());
    }

    private void addCategories() {
        final Category expenseCategory = new Category();
        expenseCategory.setLocalId(Category.EXPENSE_ID);
        expenseCategory.setId(UUID.randomUUID().toString());
        expenseCategory.setTitle(context.getString(R.string.expense));
        expenseCategory.setColor(context.getResources().getColor(R.color.text_negative));
        expenseCategory.setCategoryType(CategoryType.EXPENSE);
        expenseCategory.setCategoryOwner(CategoryOwner.SYSTEM);
        expenseCategory.setSortOrder(0);

        final Category incomeCategory = new Category();
        incomeCategory.setLocalId(Category.INCOME_ID);
        incomeCategory.setId(UUID.randomUUID().toString());
        incomeCategory.setTitle(context.getString(R.string.income));
        incomeCategory.setColor(context.getResources().getColor(R.color.text_positive));
        incomeCategory.setCategoryType(CategoryType.INCOME);
        incomeCategory.setCategoryOwner(CategoryOwner.SYSTEM);
        incomeCategory.setSortOrder(0);

        final Category transferCategory = new Category();
        transferCategory.setLocalId(Category.TRANSFER_ID);
        transferCategory.setId(UUID.randomUUID().toString());
        transferCategory.setTitle(context.getString(R.string.transfer));
        transferCategory.setColor(context.getResources().getColor(R.color.text_neutral));
        transferCategory.setCategoryType(CategoryType.TRANSFER);
        transferCategory.setCategoryOwner(CategoryOwner.SYSTEM);
        transferCategory.setSortOrder(0);

        database.insert(Tables.Categories.TABLE_NAME, null, expenseCategory.asValues());
        database.insert(Tables.Categories.TABLE_NAME, null, incomeCategory.asValues());
        database.insert(Tables.Categories.TABLE_NAME, null, transferCategory.asValues());

        insertCategories(context.getResources().getStringArray(R.array.expense_categories), context.getResources().getStringArray(R.array.expense_categories_colors), CategoryType.EXPENSE);
        insertCategories(context.getResources().getStringArray(R.array.income_categories), context.getResources().getStringArray(R.array.income_categories_colors), CategoryType.INCOME);
    }

    private String getMainCurrencyCode() {
        String code = null;
        try {
            code = java.util.Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        } catch (Exception ignored) {
        }

        if (TextUtils.isEmpty(code)) {
            code = "USD";
        }

        return code;
    }

    private java.util.Currency getCurrencyFromCode(String code) {
        try {
            return java.util.Currency.getInstance(code);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void insertCategories(String[] titles, String[] colors, CategoryType type) {
        int order = 0;
        for (String title : titles) {
            final Category category = new Category();
            category.setId(UUID.randomUUID().toString());
            category.setTitle(title);
            category.setColor(Color.parseColor(colors[order % colors.length]));
            category.setCategoryType(type);
            category.setCategoryOwner(CategoryOwner.USER);
            category.setSortOrder(order++);
            database.insert(Tables.Categories.TABLE_NAME, null, category.asValues());
        }
    }
}
