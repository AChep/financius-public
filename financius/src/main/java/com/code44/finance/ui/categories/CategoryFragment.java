package com.code44.finance.ui.categories;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.Query;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.db.model.Category;
import com.code44.finance.data.providers.CategoriesProvider;
import com.code44.finance.ui.ModelFragment;

public class CategoryFragment extends ModelFragment<Category> {
    private ImageView color_IV;
    private TextView title_TV;

    public static CategoryFragment newInstance(long categoryId) {
        final Bundle args = makeArgs(categoryId);

        final CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get views
        color_IV = (ImageView) view.findViewById(R.id.color_IV);
        title_TV = (TextView) view.findViewById(R.id.title_TV);
    }

    @Override
    protected Uri getUri(long modelId) {
        return CategoriesProvider.uriCategory(modelId);
    }

    @Override
    protected Query getQuery() {
        return Query.create()
                .projectionId(Tables.Categories.ID)
                .projection(Tables.Categories.PROJECTION);
    }

    @Override
    protected Category getModelFrom(Cursor cursor) {
        return Category.from(cursor);
    }

    @Override
    protected void onModelLoaded(Category model) {
        color_IV.setColorFilter(model.getColor());
        title_TV.setText(model.getTitle());
    }
}
