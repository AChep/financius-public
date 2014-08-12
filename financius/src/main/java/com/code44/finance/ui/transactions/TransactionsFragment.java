package com.code44.finance.ui.transactions;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.code44.finance.R;
import com.code44.finance.adapters.BaseModelsAdapter;
import com.code44.finance.adapters.TransactionsAdapter;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.db.model.BaseModel;
import com.code44.finance.data.db.model.Transaction;
import com.code44.finance.data.providers.TransactionsProvider;
import com.code44.finance.ui.ModelListFragment;

public class TransactionsFragment extends ModelListFragment {
    public static TransactionsFragment newInstance() {
        final Bundle args = makeArgs(Mode.VIEW);

        final TransactionsFragment fragment = new TransactionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override protected BaseModelsAdapter createAdapter(Context context) {
        return new TransactionsAdapter(context);
    }

    @Override protected CursorLoader getModelsCursorLoader(Context context) {
        return Tables.Transactions.getQuery().asCursorLoader(context, TransactionsProvider.uriTransactions());
    }

    @Override protected BaseModel modelFrom(Cursor cursor) {
        return Transaction.from(cursor);
    }

    @Override protected void onModelClick(Context context, View view, int position, String modelServerId, BaseModel model) {
        // TODO Open TransactionActivity
    }

    @Override protected void startModelEdit(Context context, String modelServerId) {
        // TODO Open TransactionEditActivity
    }

    @Override public String getTitle() {
        return getString(R.string.transactions);
    }
}
