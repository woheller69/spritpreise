package org.woheller69.spritpreise.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.ui.updater.IUpdateableCityUI;
import org.woheller69.spritpreise.ui.updater.ViewUpdater;
import org.woheller69.spritpreise.ui.viewPager.CityPagerAdapter;

import java.util.List;

public class CityGasPricesActivity extends NavigationActivity implements IUpdateableCityUI {
    private CityPagerAdapter pagerAdapter;

    private static MenuItem refreshActionButton;
    private MenuItem rainviewerButton;

    private int cityId = -1;
    private ViewPager viewPager;
    private TextView noCityText;

    @Override
    protected void onPause() {
        super.onPause();

        ViewUpdater.removeSubscriber(this);
        ViewUpdater.removeSubscriber(pagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initResources();

        SQLiteHelper db = SQLiteHelper.getInstance(this);
        if (db.getAllCitiesToWatch().isEmpty()) {
            // no cities selected.. don't show the viewPager - rather show a text that tells the user that no city was selected
            viewPager.setVisibility(View.GONE);
            noCityText.setVisibility(View.VISIBLE);

        } else {
            noCityText.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAdapter(pagerAdapter);
        }

        ViewUpdater.addSubscriber(this);
        ViewUpdater.addSubscriber(pagerAdapter);

        if (pagerAdapter.getCount()>0) {  //only if at least one city is watched
             //if pagerAdapter has item with current cityId go there, otherwise use cityId from current item
            if (pagerAdapter.getPosForCityID(cityId)==0) cityId=pagerAdapter.getCityIDForPos(viewPager.getCurrentItem());
            List <Station> stations = db.getStationsByCityId(cityId);

            long timestamp = 0;
            if (stations.size()!=0) timestamp= stations.get(0).getTimestamp();
            long systemTime = System.currentTimeMillis() / 1000;
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "15")) * 60);

            if (timestamp + updateInterval - systemTime <= 0) {
                CityPagerAdapter.refreshSingleData(getApplicationContext(),true, cityId); //only update current tab at start
                CityGasPricesActivity.startRefreshAnimation();
            }
        }
        viewPager.setCurrentItem(pagerAdapter.getPosForCityID(cityId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_gas_prices);
        overridePendingTransition(0, 0);

        initResources();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {

                //Update current tab if outside update interval, show animation
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SQLiteHelper database = SQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
                List <Station> stations = database.getStationsByCityId(pagerAdapter.getCityIDForPos(position));

                long timestamp = 0;
                if (stations.size()!=0) timestamp= stations.get(0).getTimestamp();
                long systemTime = System.currentTimeMillis() / 1000;
                long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "15")) * 60);

                if (timestamp + updateInterval - systemTime <= 0) {
                    CityPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(position));
                    CityGasPricesActivity.startRefreshAnimation();
                }
                viewPager.setNextFocusRightId(position);
                cityId=pagerAdapter.getCityIDForPos(viewPager.getCurrentItem());  //save current cityId for next resume
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("cityId")) cityId = intent.getIntExtra("cityId",-1);
    }

    private void initResources() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new CityPagerAdapter(this, getSupportFragmentManager());
        noCityText = findViewById(R.id.noCitySelectedText);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_gasprices;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_city_gas_prices, menu);

        final Menu m = menu;

        refreshActionButton = menu.findItem(R.id.menu_refresh);
        refreshActionButton.setActionView(R.layout.menu_refresh_action_view);
        refreshActionButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.performIdentifierAction(refreshActionButton.getItemId(), 0);
            }
        });
        /*
        rainviewerButton = menu.findItem(R.id.menu_rainviewer);
        rainviewerButton.setActionView(R.layout.menu_rainviewer_view);
        rainviewerButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.performIdentifierAction(rainviewerButton.getItemId(), 0);
            }
        });*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SQLiteHelper db = SQLiteHelper.getInstance(this);
 /*       if (id==R.id.menu_rainviewer) {
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                Intent intent = new Intent(this, RainViewerActivity.class);
                intent.putExtra("latitude", pagerAdapter.getLatForPos((viewPager.getCurrentItem())));
                intent.putExtra("longitude", pagerAdapter.getLonForPos((viewPager.getCurrentItem())));
                startActivity(intent);
            }
        }else*/ if (id==R.id.menu_refresh){
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                CityPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(viewPager.getCurrentItem()));
                CityGasPricesActivity.startRefreshAnimation();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void processUpdateStations(List<Station> stations, int cityID) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    public static void startRefreshAnimation(){
        {
            if(refreshActionButton !=null && refreshActionButton.getActionView() != null) {
                RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(500);
                rotate.setRepeatCount(5);
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        refreshActionButton.getActionView().setActivated(false);
                        refreshActionButton.getActionView().setEnabled(false);
                        refreshActionButton.getActionView().setClickable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        refreshActionButton.getActionView().setActivated(true);
                        refreshActionButton.getActionView().setEnabled(true);
                        refreshActionButton.getActionView().setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                refreshActionButton.getActionView().startAnimation(rotate);
            }
        }
    }
}

