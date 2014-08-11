package com.code44.finance.ui.categories;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.code44.finance.R;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.db.model.Category;
import com.code44.finance.data.providers.CategoriesProvider;
import com.code44.finance.ui.ModelEditFragment;
import com.code44.finance.ui.SelectColorFragment;

public class CategoryEditFragment extends ModelEditFragment<Category> implements View.OnClickListener, SelectColorFragment.OnColorSelectedListener {
    private static final String FRAGMENT_SELECT_COLOR = CategoryEditFragment.class.getName() + ".FRAGMENT_SELECT_COLOR";

    private EditText title_ET;
    private Button color_B;

    public static CategoryEditFragment newInstance(String categoryServerId) {
        final Bundle args = makeArgs(categoryServerId);

        final CategoryEditFragment fragment = new CategoryEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        title_ET = (EditText) view.findViewById(R.id.title_ET);
        color_B = (Button) view.findViewById(R.id.color_B);

        // Setup
        color_B.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        SelectColorFragment.setListenerIfVisible(getChildFragmentManager(), FRAGMENT_SELECT_COLOR, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SelectColorFragment.removeListenerIfVisible(getChildFragmentManager(), FRAGMENT_SELECT_COLOR);
    }

    @Override
    public boolean onSave(Context context, Category model) {
        boolean canSave = true;

        if (TextUtils.isEmpty(model.getTitle())) {
            canSave = false;
            // TODO Show error
        }

        if (canSave) {
            DataStore.insert().model(model).into(CategoriesProvider.uriCategories());
        }

        return canSave;
    }

    @Override
    protected void ensureModelUpdated(Category model) {
        model.setTitle(title_ET.getText().toString());
    }

    @Override
    protected CursorLoader getModelCursorLoader(String modelServerId) {
        return Tables.Categories.getQuery(null).asCursorLoader(CategoriesProvider.uriCategory(modelServerId));
    }

    @Override
    protected Category getModelFrom(Cursor cursor) {
        return Category.from(cursor);
    }

    @Override
    protected void onModelLoaded(Category model) {
        title_ET.setText(model.getTitle());

        final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        circleDrawable.setColorFilter(model.getColor(), PorterDuff.Mode.SRC_ATOP);
        circleDrawable.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.text_subhead), getResources().getDimensionPixelSize(R.dimen.text_subhead));

        final SpannableStringBuilder ssb = new SpannableStringBuilder(" ");
        ssb.setSpan(new ImageSpan(circleDrawable), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        color_B.setText(ssb);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_B:
                SelectColorFragment.show(getChildFragmentManager(), FRAGMENT_SELECT_COLOR, model.getColor(), this);
                break;
        }
    }

    @Override
    public void onColorSelected(int color) {
        model.setColor(color);
        onModelLoaded(model);
    }
}
