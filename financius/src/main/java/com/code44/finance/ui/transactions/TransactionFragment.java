package com.code44.finance.ui.transactions;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.common.model.CategoryType;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Account;
import com.code44.finance.data.model.Category;
import com.code44.finance.data.model.Transaction;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.qualifiers.Expense;
import com.code44.finance.ui.ModelFragment;
import com.code44.finance.utils.MoneyFormatter;

import org.joda.time.DateTime;

import javax.inject.Inject;

public class TransactionFragment extends ModelFragment<Transaction> {
    @Inject @Expense Category expenseCategory;
    @Inject Account systemAccount;

    private TextView amount_TV;
    private TextView date_TV;
    private TextView category_TV;
    private TextView account_TV;

    public static TransactionFragment newInstance(String transactionServerId) {
        final Bundle args = makeArgs(transactionServerId);

        final TransactionFragment fragment = new TransactionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        amount_TV = (TextView) view.findViewById(R.id.amount_TV);
    }

    @Override protected CursorLoader getModelCursorLoader(Context context, String modelServerId) {
        return Tables.Transactions.getQuery().asCursorLoader(context, TransactionsProvider.uriTransaction(modelServerId));
    }

    @Override protected Transaction getModelFrom(Cursor cursor) {
        final Transaction transaction = Transaction.from(cursor);
        if (transaction.getAccountFrom() == null) {
            transaction.setAccountFrom(systemAccount);
        }

        if (transaction.getAccountTo() == null) {
            transaction.setAccountTo(systemAccount);
        }

        if (transaction.getCategory() == null) {
            transaction.setCategory(expenseCategory);
        }

        return transaction;
    }

    @Override protected void onModelLoaded(Transaction transaction) {
        final Category category = transaction.getCategory();
        final DateTime date = new DateTime(transaction.getDate());
//        holder.color_IV.setColorFilter(category.getColor());
//        holder.weekday_TV.setText(date.dayOfWeek().getAsShortText());
//        holder.day_TV.setText(date.dayOfMonth().getAsShortText());
//        holder.title_TV.setText(category.getTitle());
//        holder.subtitle_TV.setText(transaction.getNote());
        amount_TV.setText(MoneyFormatter.format(transaction));

        if (category.getCategoryType() == CategoryType.EXPENSE) {
//            holder.account_TV.setText(transaction.getAccountFrom().getTitle());
//            amount_TV.setTextColor(expenseColor);
        } else if (category.getCategoryType() == CategoryType.INCOME) {
//            holder.account_TV.setText(transaction.getAccountTo().getTitle());
//            amount_TV.setTextColor(incomeColor);
        } else {
//            holder.account_TV.setText(transaction.getAccountFrom().getTitle() + " > " + transaction.getAccountTo().getTitle());
//            amount_TV.setTextColor(transferColor);
        }
    }

    @Override protected Uri getDeleteUri() {
        return TransactionsProvider.uriTransactions();
    }

    @Override protected Pair<String, String[]> getDeleteSelection() {
        return Pair.create(Tables.Transactions.ID + "=?", new String[]{modelId});
    }

    @Override protected void startModelEdit(Context context, String modelServerId) {
        TransactionEditActivity.start(context, modelServerId);
    }
}
