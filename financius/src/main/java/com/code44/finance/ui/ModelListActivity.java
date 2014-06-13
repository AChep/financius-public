package com.code44.finance.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.data.db.model.BaseModel;

public abstract class ModelListActivity extends BaseActivity implements ModelListFragment.ModelListFragmentCallbacks {
    public static final String RESULT_EXTRA_MODEL_ID = "RESULT_EXTRA_MODEL_ID";
    public static final String RESULT_EXTRA_MODEL = "RESULT_EXTRA_MODEL";

    private static final String EXTRA_MODE = ModelListActivity.class.getName() + ".EXTRA_MODE";

    static final int MODE_VIEW = 1;
    static final int MODE_SELECT = 2;

    public static Intent makeIntentView(Context context, Class<? extends ModelListActivity> activityClass) {
        final Intent intent = makeIntent(context, activityClass);
        intent.putExtra(EXTRA_MODE, MODE_VIEW);
        return intent;
    }

    public static Intent makeIntentSelect(Context context, Class<? extends ModelListActivity> activityClass) {
        final Intent intent = makeIntent(context, activityClass);
        intent.putExtra(EXTRA_MODE, MODE_SELECT);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get extras
        final int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_VIEW);

        // Setup ActionBar
        if (mode == MODE_VIEW) {
            setActionBarTitle(getActionBarTitleResId());
        } else {
            setActionBarTitle(R.string.select);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, createModelsFragment(mode)).commit();
        }
    }

    @Override
    public void onModelSelected(long id, BaseModel model) {
        final Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_MODEL_ID, id);
        data.putExtra(RESULT_EXTRA_MODEL, model);
        setResult(RESULT_OK, data);
        finish();
    }

    protected abstract int getActionBarTitleResId();

    protected abstract ModelListFragment createModelsFragment(int mode);
}
