package com.code44.finance.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.code44.finance.R;
import com.code44.finance.data.model.Model;

import java.util.List;
import java.util.Set;

public abstract class ModelListActivityOld extends DrawerActivity implements ModelListFragment.OnModelSelectedListener, ModelListFragment.OnModelsSelectedListener {
    public static final String RESULT_EXTRA_MODEL = "RESULT_EXTRA_MODEL";
    public static final String RESULT_EXTRA_MODELS = "RESULT_EXTRA_MODELS";

    private static final String EXTRA_MODE = ModelListActivityOld.class.getName() + ".EXTRA_MODE";
    private static final String EXTRA_SELECTED_MODELS = ModelListActivityOld.class.getName() + ".EXTRA_SELECTED_MODELS";

    protected ModelListFragment.Mode mode;
    protected Parcelable[] selectedModels;

    public static Intent makeIntentView(Context context, Class<? extends ModelListActivityOld> activityClass) {
        final Intent intent = makeIntentForActivity(context, activityClass);
        intent.putExtra(EXTRA_MODE, ModelListFragment.Mode.VIEW);
        return intent;
    }

    public static Intent makeIntentSelect(Context context, Class<? extends ModelListActivityOld> activityClass) {
        final Intent intent = makeIntentForActivity(context, activityClass);
        intent.putExtra(EXTRA_MODE, ModelListFragment.Mode.SELECT);
        return intent;
    }

    public static Intent makeIntentMultiSelect(Context context, Class<? extends ModelListActivityOld> activityClass, List<? extends Model> selectedModels) {
        final Intent intent = makeIntentForActivity(context, activityClass);
        intent.putExtra(EXTRA_MODE, ModelListFragment.Mode.MULTI_SELECT);
        final Parcelable[] parcelables = new Parcelable[selectedModels.size()];
        int index = 0;
        for (Model model : selectedModels) {
            parcelables[index++] = model;
        }
        intent.putExtra(EXTRA_SELECTED_MODELS, parcelables);
        return intent;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readExtras();
        final int containerId = inflateActivity();

        // Setup ActionBar
        if (mode == ModelListFragment.Mode.SELECT) {
            getSupportActionBar().setTitle(R.string.select);
        }

        final boolean addFragmentHere = containerId != 0;
        if (addFragmentHere && savedInstanceState == null) {
            ModelListFragment fragment = createModelsFragment(mode, selectedModels);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().add(containerId, fragment).commit();
            }
        }
    }

    @Override public void onModelSelected(Model model) {
        final Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_MODEL, model);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override public void onModelsSelected(Set<Model> models) {
        final Intent data = new Intent();
        final Parcelable[] parcelables = new Parcelable[models.size()];
        int index = 0;
        for (Model model : models) {
            parcelables[index++] = model;
        }
        data.putExtra(RESULT_EXTRA_MODELS, parcelables);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override public void onModelsSelectCanceled() {
        finish();
    }

    protected int inflateActivity() {
        setContentView(R.layout.activity_simple);
        return R.id.content;
    }

    protected void readExtras() {
        mode = (ModelListFragment.Mode) getIntent().getSerializableExtra(EXTRA_MODE);
        selectedModels = getIntent().getParcelableArrayExtra(EXTRA_SELECTED_MODELS);
        if (mode == null) {
            throw new IllegalStateException("Activity " + ((Object) this).getClass().getName() + " must be created with Intent containing " + EXTRA_MODE + " with values from " + ModelListFragment.Mode.class.getName());
        }
    }

    protected abstract ModelListFragment createModelsFragment(ModelListFragment.Mode mode, Parcelable[] selectedModels);
}
