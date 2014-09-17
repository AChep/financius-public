package com.code44.finance.modules;

import com.code44.finance.ui.CalculatorActivity;
import com.code44.finance.ui.CalculatorFragment;
import com.code44.finance.ui.GoogleApiFragment;
import com.code44.finance.ui.MainActivity;
import com.code44.finance.ui.NavigationFragment;
import com.code44.finance.ui.accounts.AccountActivity;
import com.code44.finance.ui.accounts.AccountEditActivity;
import com.code44.finance.ui.accounts.AccountEditFragment;
import com.code44.finance.ui.accounts.AccountFragment;
import com.code44.finance.ui.accounts.AccountsActivity;
import com.code44.finance.ui.accounts.AccountsFragment;
import com.code44.finance.ui.categories.CategoriesActivity;
import com.code44.finance.ui.categories.CategoriesFragment;
import com.code44.finance.ui.categories.CategoryActivity;
import com.code44.finance.ui.categories.CategoryEditActivity;
import com.code44.finance.ui.categories.CategoryEditFragment;
import com.code44.finance.ui.categories.CategoryFragment;
import com.code44.finance.ui.currencies.CurrenciesActivity;
import com.code44.finance.ui.currencies.CurrenciesFragment;
import com.code44.finance.ui.currencies.CurrencyActivity;
import com.code44.finance.ui.currencies.CurrencyEditActivity;
import com.code44.finance.ui.currencies.CurrencyEditFragment;
import com.code44.finance.ui.currencies.CurrencyFragment;
import com.code44.finance.ui.dialogs.DeleteDialogFragment;
import com.code44.finance.ui.dialogs.ListDialogFragment;
import com.code44.finance.ui.overview.OverviewFragment;
import com.code44.finance.ui.settings.SettingsActivity;
import com.code44.finance.ui.settings.data.DataActivity;
import com.code44.finance.ui.settings.data.DataFragment;
import com.code44.finance.ui.settings.data.ExportActivity;
import com.code44.finance.ui.settings.data.FileExportFragment;
import com.code44.finance.ui.settings.data.FileImportFragment;
import com.code44.finance.ui.settings.data.ImportActivity;
import com.code44.finance.ui.tags.TagActivity;
import com.code44.finance.ui.tags.TagEditActivity;
import com.code44.finance.ui.tags.TagEditFragment;
import com.code44.finance.ui.tags.TagFragment;
import com.code44.finance.ui.tags.TagsActivity;
import com.code44.finance.ui.tags.TagsFragment;
import com.code44.finance.ui.transactions.TransactionActivity;
import com.code44.finance.ui.transactions.TransactionEditActivity;
import com.code44.finance.ui.transactions.TransactionEditFragment;
import com.code44.finance.ui.transactions.TransactionFragment;
import com.code44.finance.ui.transactions.TransactionsFragment;

import dagger.Module;

@Module(
        library = true,
        complete = false,
        injects = {
                MainActivity.class,
                CurrenciesActivity.class,
                CurrencyActivity.class,
                CurrencyEditActivity.class,
                AccountsActivity.class,
                AccountActivity.class,
                AccountEditActivity.class,
                TransactionActivity.class,
                TransactionEditActivity.class,
                CategoriesActivity.class,
                CategoryActivity.class,
                CategoryEditActivity.class,
                TagsActivity.class,
                TagActivity.class,
                TagEditActivity.class,
                SettingsActivity.class,
                DataActivity.class,
                CalculatorActivity.class,
                ExportActivity.class,
                ImportActivity.class,

                NavigationFragment.class,
                OverviewFragment.class,
                CurrenciesFragment.class,
                CurrencyFragment.class,
                CurrencyEditFragment.class,
                AccountsFragment.class,
                AccountFragment.class,
                AccountEditFragment.class,
                TransactionsFragment.class,
                TransactionFragment.class,
                TransactionEditFragment.class,
                CategoriesFragment.class,
                CategoryFragment.class,
                CategoryEditFragment.class,
                TagsFragment.class,
                TagFragment.class,
                TagEditFragment.class,
                DeleteDialogFragment.class,
                ListDialogFragment.class,
                CalculatorFragment.class,
                GoogleApiFragment.class,
                FileExportFragment.class,
                FileImportFragment.class,
                DataFragment.class
        }
)
public class UIModule {
}
