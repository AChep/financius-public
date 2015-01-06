package com.code44.finance.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import com.code44.finance.R;
import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.data.providers.AccountsProvider;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.qualifiers.Main;
import com.code44.finance.ui.DrawerActivity;
import com.code44.finance.ui.common.navigation.NavigationScreen;
import com.code44.finance.ui.reports.AmountGroups;
import com.code44.finance.ui.reports.categories.CategoriesReportData;
import com.code44.finance.ui.reports.trends.TrendsGraphView;
import com.code44.finance.ui.transactions.TransactionEditActivity;
import com.code44.finance.utils.BaseInterval;
import com.code44.finance.utils.CurrentInterval;
import com.code44.finance.utils.MoneyFormatter;
import com.code44.finance.utils.ThemeUtils;
import com.code44.finance.utils.analytics.Analytics;
import com.code44.finance.views.AccountsView;
import com.code44.finance.views.FabImageButton;
import com.squareup.otto.Subscribe;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;

public class OverviewActivity extends DrawerActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final int LOADER_TRANSACTIONS = 1;
    private static final int LOADER_ACCOUNTS = 2;

    @Inject CurrentInterval currentInterval;
    @Inject @Main Currency mainCurrency;

    private OverviewGraphView overviewGraphView;
    private AccountsView accountsView;
    private TrendsGraphView trendsGraphView;

    public static Intent makeIntent(Context context) {
        return makeIntentForActivity(context, OverviewActivity.class);
    }

    public static void start(Context context) {
        final Intent intent = makeIntent(context);
        startActivity(context, intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowDrawer(true);
        setShowDrawerToggle(true);
        setContentView(R.layout.activity_overview);

        // Get views
        final FabImageButton newTransactionView = (FabImageButton) findViewById(R.id.newTransaction);
        overviewGraphView = (OverviewGraphView) findViewById(R.id.overviewGraphView);
        accountsView = (AccountsView) findViewById(R.id.accounts);
        trendsGraphView = (TrendsGraphView) findViewById(R.id.trendsGraphView);

        // Setup
        newTransactionView.setOnClickListener(this);
        overviewGraphView.setOnClickListener(this);
        accountsView.setOnClickListener(this);
        trendsGraphView.setOnClickListener(this);

        // Loader
        getSupportLoaderManager().initLoader(LOADER_TRANSACTIONS, null, this);
        getSupportLoaderManager().initLoader(LOADER_ACCOUNTS, null, this);
    }

    @Override public void onResume() {
        super.onResume();
        getEventBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        getEventBus().unregister(this);
    }

    @Override protected NavigationScreen getNavigationScreen() {
        return NavigationScreen.Overview;
    }

    @Override protected Analytics.Screen getScreen() {
        return Analytics.Screen.Overview;
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_TRANSACTIONS:
                return Tables.Transactions
                        .getQuery()
                        .selection(" and " + Tables.Transactions.DATE + " between ? and ?", String.valueOf(currentInterval.getInterval().getStartMillis()), String.valueOf(currentInterval.getInterval().getEndMillis() - 1))
                        .clearSort()
                        .sortOrder(Tables.Transactions.DATE.getName())
                        .asCursorLoader(this, TransactionsProvider.uriTransactions());
            case LOADER_ACCOUNTS:
                return Tables.Accounts
                        .getQuery()
                        .selection(" and " + Tables.Accounts.INCLUDE_IN_TOTALS + "=?", "1")
                        .asCursorLoader(this, AccountsProvider.uriAccounts());
        }
        return null;
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_TRANSACTIONS:
                onTransactionsLoaded(cursor);
                break;
            case LOADER_ACCOUNTS:
                onAccountsLoaded(cursor);
                break;
        }
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newTransaction:
                TransactionEditActivity.start(this, null);
                break;
            case R.id.overviewGraphView:
                onNavigationItemSelected(NavigationScreen.Reports);
                break;
            case R.id.accounts:
                onNavigationItemSelected(NavigationScreen.Accounts);
                break;
            case R.id.trendsGraphView:
                onNavigationItemSelected(NavigationScreen.Transactions);
                break;
        }
    }

    @Subscribe public void onCurrentIntervalChanged(CurrentInterval currentInterval) {
        getSupportLoaderManager().restartLoader(LOADER_TRANSACTIONS, null, this);
        getSupportActionBar().setTitle(currentInterval.getTitle());
    }

    private void onTransactionsLoaded(Cursor cursor) {
        final CategoriesReportData categoriesReportData = new CategoriesReportData(this, cursor, mainCurrency, TransactionType.Expense);
        overviewGraphView.setPieChartData(categoriesReportData.getPieChartData());
        overviewGraphView.setTotalExpense(categoriesReportData.getPieChartData().getTotalValue());

        final AmountGroups.TransactionValidator transactionValidator = new AmountGroups.TransactionValidator() {
            @Override public boolean isTransactionValid(Transaction transaction) {
                return transaction.includeInReports() && transaction.getTransactionType() != TransactionType.Transfer && transaction.getTransactionState() == TransactionState.Confirmed;
            }
        };
        final AmountGroups amountGroups = new AmountGroups(currentInterval);
        final Line line = getLine(amountGroups.getGroups(cursor, mainCurrency, transactionValidator).values().iterator().next())
                .setColor(ThemeUtils.getColor(this, R.attr.textColorNegative))
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(true);

        final LineChartData lineChartData = new LineChartData(Arrays.asList(line));
        lineChartData.setAxisXBottom(getAxis());

        trendsGraphView.setLineGraphData(lineChartData);
    }

    private Line getLine(List<Long> amounts) {
        final List<PointValue> points = new ArrayList<>();
        int index = 0;
        for (Long amount : amounts) {
            points.add(new PointValue(index++, amount));
        }

        final int lineWidthDp = (int) (getResources().getDimension(R.dimen.report_trend_graph_width) / Resources.getSystem().getDisplayMetrics().density);
        return new Line(points)
                .setCubic(true)
                .setStrokeWidth(lineWidthDp)
                .setPointRadius(lineWidthDp)
                .setFormatter(new SimpleLineChartValueFormatter() {
                    @Override public int formatChartValue(char[] chars, PointValue pointValue) {
                        final char[] fullText = MoneyFormatter.format(mainCurrency, (long) pointValue.getY()).toCharArray();
                        final int size = Math.min(chars.length, fullText.length);
                        System.arraycopy(fullText, 0, chars, chars.length - size, size);
                        return size;
                    }
                })
                .setHasPoints(false);
    }

    private Axis getAxis() {
        final List<AxisValue> values = new ArrayList<>();
        final Period period = BaseInterval.getSubPeriod(currentInterval.getType(), currentInterval.getLength());
        Interval interval = new Interval(currentInterval.getInterval().getStart(), period);
        int index = 0;
        while (interval.overlaps(currentInterval.getInterval())) {
            values.add(new AxisValue(index++, BaseInterval.getSubTypeShortestTitle(interval, currentInterval.getType()).toCharArray()));
            interval = new Interval(interval.getEnd(), period);
        }

        return new Axis(values).setHasLines(true).setHasSeparationLine(true);
    }

    private void onAccountsLoaded(Cursor cursor) {
        final List<Account> accounts = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                accounts.add(Account.from(cursor));
            } while (cursor.moveToNext());
        }
        accountsView.setAccounts(accounts);
    }
}
