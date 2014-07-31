package com.code44.finance.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.code44.finance.R;
import com.code44.finance.common.model.CategoryType;
import com.code44.finance.ui.ModelListActivity;
import com.code44.finance.ui.categories.CategoriesFragment;

public class CategoriesPagerAdapter extends FragmentPagerAdapter {
    private final Context context;

    public CategoriesPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return CategoriesFragment.newInstance(ModelListActivity.Mode.VIEW, CategoryType.EXPENSE);
        } else {
            return CategoriesFragment.newInstance(ModelListActivity.Mode.VIEW, CategoryType.INCOME);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.expense);
        } else {
            return context.getString(R.string.income);
        }
    }
}
