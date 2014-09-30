package com.code44.finance.ui.settings.about;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.ui.BaseFragment;
import com.code44.finance.utils.ListTagHandler;

public class ChangeLogFragment extends BaseFragment {
    public static ChangeLogFragment newInstance() {
        return new ChangeLogFragment();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_log, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        final TextView changeLog_TV = (TextView) view.findViewById(R.id.changeLog_TV);

        // Setup
        changeLog_TV.setText(Html.fromHtml(getString(R.string.change_log_items), null, new ListTagHandler()));
    }
}
