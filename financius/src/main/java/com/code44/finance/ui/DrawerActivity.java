package com.code44.finance.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.code44.finance.R;
import com.code44.finance.adapters.NavigationAdapter;
import com.code44.finance.ui.accounts.AccountsActivity;
import com.code44.finance.ui.overview.OverviewActivity;
import com.code44.finance.ui.reports.CategoriesReportActivity;
import com.code44.finance.ui.transactions.TransactionsActivity;

public abstract class DrawerActivity extends BaseActivity implements NavigationFragment.NavigationListener {
    private static final int DRAWER_LAUNCH_DELAY = 250;

    private final Handler handler = new Handler();

    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;

    private NavigationFragment navigationFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_drawer);
    }

    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof NavigationFragment) {
            navigationFragment = (NavigationFragment) fragment;
            navigationFragment.setSelected(getNavigationScreen());
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (navigationFragment != null) {
            navigationFragment.setSelected(getNavigationScreen());
        }
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START) || drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override public void setContentView(int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID, (ViewGroup) findViewById(R.id.content), true);

        // Get views
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        // Setup
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, getToolbar(), R.string.open_navigation, R.string.close_navigation);
        drawerLayout.setDrawerListener(drawerToggle);
        setupToolbar();
    }

    @Override public void onNavigationItemSelected(NavigationAdapter.NavigationScreen navigationScreen) {
        if (isSameNavigationScreen(navigationScreen)) {
            drawerLayout.closeDrawers();
            return;
        }

        final Intent intent;
        switch (navigationScreen) {
            case User:
                intent = null;
                break;
            case Overview:
                intent = OverviewActivity.makeIntent(this);
                break;
            case Accounts:
                intent = AccountsActivity.makeIntentView(this);
                break;
            case Transactions:
                intent = TransactionsActivity.makeIntentView(this);
                break;
            case Reports:
                intent = CategoriesReportActivity.makeIntent(this);
                break;
            default:
                intent = null;
                break;
        }

        drawerLayout.closeDrawers();

        final boolean finish = !(this instanceof OverviewActivity);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                startActivity(DrawerActivity.this, intent);
                if (finish) {
                    finish();
                }
            }
        }, DRAWER_LAUNCH_DELAY);
    }

    protected abstract NavigationAdapter.NavigationScreen getNavigationScreen();

    private boolean isSameNavigationScreen(NavigationAdapter.NavigationScreen navigationScreen) {
        switch (navigationScreen) {
            case User:
                break;
            case Overview:
                return this instanceof OverviewActivity;
            case Accounts:
                return this instanceof AccountsActivity;
            case Transactions:
                return this instanceof TransactionsActivity;
            case Reports:
                return this instanceof CategoriesReportActivity;
        }
        return false;
    }
}
