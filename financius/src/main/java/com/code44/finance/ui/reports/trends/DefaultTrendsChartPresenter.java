package com.code44.finance.ui.reports.trends;

import com.code44.finance.common.model.TransactionState;
import com.code44.finance.common.model.TransactionType;
import com.code44.finance.data.model.Currency;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.ui.reports.AmountGroups;

import lecho.lib.hellocharts.model.Line;

public class DefaultTrendsChartPresenter extends TrendsChartPresenter {
    public DefaultTrendsChartPresenter(TrendsChartView trendsChartView, Currency mainCurrency) {
        super(trendsChartView, mainCurrency);
    }

    @Override protected AmountGroups.AmountCalculator[] getTransactionValidators() {
        return new AmountGroups.AmountCalculator[]{new AmountGroups.AmountCalculator() {
            @Override public boolean isTransactionValid(Transaction transaction) {
                return transaction.includeInReports() && transaction.getTransactionType() != TransactionType.Transfer && transaction.getTransactionState() == TransactionState.Confirmed;
            }
        }};
    }

    @Override protected void onLineCreated(AmountGroups.AmountCalculator amountCalculator, Line line) {
    }
}
