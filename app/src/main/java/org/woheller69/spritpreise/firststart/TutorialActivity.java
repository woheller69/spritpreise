package org.woheller69.spritpreise.firststart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.CityGasPricesActivity;
import org.woheller69.spritpreise.activities.SettingsActivity;

import org.woheller69.spritpreise.preferences.AppPreferencesManager;
import org.woheller69.spritpreise.ui.util.ThemeUtils;


/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 *
 * @author Karola Marky
 * @version 20161214
 */

public class TutorialActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext, btnRegister;
    private AppPreferencesManager prefManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_tutorial);
        ThemeUtils.setStatusBarAppearance(this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnRegister = (Button) findViewById(R.id.btn_register);


        // layouts of all welcome sliders
        // add few more layouts if you want
        if (prefManager.isApiKeyMissing()) {
            layouts = new int[]{
                    R.layout.tutorial_slide1,
                    R.layout.tutorial_slide2,
                    R.layout.tutorial_slide3,
                    R.layout.tutorial_slide4};
        } else {
            layouts = new int[]{
                    R.layout.tutorial_slide1,
                    R.layout.tutorial_slide2,
                    R.layout.tutorial_slide3};  //do not show slide for API key registration if app has built in API key
        }
        // adding bottom dots
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);



        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    if (prefManager.isApiKeyMissing()) launchSettings();
                    else launchHomeScreen();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://creativecommons.tankerkoenig.de")));
            }
        });

    }


    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("\u2022");
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(TutorialActivity.this, CityGasPricesActivity.class));
        finish();
    }

    private void launchSettings() {
        startActivity(new Intent(TutorialActivity.this, SettingsActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewPagerPageChangeListener.onPageSelected(viewPager.getCurrentItem());
    }


    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.okay));
                if (BuildConfig.DEFAULT_API_KEY.equals(BuildConfig.UNPATCHED_API_KEY)) btnRegister.setVisibility(View.VISIBLE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnRegister.setVisibility((View.INVISIBLE));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View view = layoutInflater.inflate(layouts[position], container, false);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
