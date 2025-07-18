package org.woheller69.spritpreise.activities;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.view.MenuItem;


import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.preferences.AppPreferencesManager;

import static java.lang.Boolean.TRUE;

import java.util.List;

/**
 * Created by Chris on 04.07.2016.
 */
public class NavigationActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    // delay to launch nav drawer item, to allow close animation to play
    static final int NAVDRAWER_LAUNCH_DELAY = 250;
    public static boolean isVisible = false;

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    // Helper
    private Handler mHandler;
    protected AppPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper());
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));
        if (prefManager.showStarDialog(this)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.dialog_StarOnGitHub);
            alertDialogBuilder.setPositiveButton(getString(R.string.dialog_OK_button), (dialog, which) -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.GITHUB_URL)));
                prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
                prefManager.setAskForStar(false);
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.dialog_NO_button), (dialog, which) -> {
                prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
                prefManager.setAskForStar(false);
            });
            alertDialogBuilder.setNeutralButton(getString(R.string.dialog_Later_button), null);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getNavigationDrawerID()!=R.id.nav_gasprices)
            {
                Intent intent = new Intent(this, CityGasPricesActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }
    }

    protected int getNavigationDrawerID() {
        return 0;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        return goToNavigationItem(itemId);
    }

    protected boolean goToNavigationItem(final int itemId) {

        if (itemId == getNavigationDrawerID()) {
            // just close drawer because we are already in this activity
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // delay transition so the drawer can close
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawer(GravityCompat.START);

        selectNavigationItem(itemId);


        return true;
    }

    // set active navigation item
    private void selectNavigationItem(int itemId) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            boolean b = itemId == mNavigationView.getMenu().getItem(i).getItemId();
            mNavigationView.getMenu().getItem(i).setChecked(b);
        }
    }

    private void callDrawerItem(final int itemId) {

        Intent intent;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean("pref_DarkMode", false)==TRUE) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (itemId==R.id.nav_gasprices) {
            intent = new Intent(this, CityGasPricesActivity.class);
            startActivity(intent);
        }else if (itemId==R.id.nav_manage){
            intent = new Intent(this, ManageLocationsActivity.class);
            startActivity(intent);
        }else if (itemId==R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }else if(itemId==R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }else if (itemId==R.id.star_on_github){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(BuildConfig.GITHUB_URL)));
            prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
            prefManager.setAskForStar(false);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        selectNavigationItem(getNavigationDrawerID());

    }
    @Override
    protected void onResume() {
        super.onResume();
        isVisible=true;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean("pref_DarkMode", false)==TRUE) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible=false;
    }
}
