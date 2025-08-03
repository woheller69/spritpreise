package org.woheller69.spritpreise.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.WindowInsetsController;
import android.widget.TextView;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.ui.util.ThemeUtils;

/**
 * Created by yonjuni on 15.06.16.
 */
public class AboutActivity extends NavigationActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ThemeUtils.setStatusBarAppearance(this);

        ((TextView) findViewById(R.id.githubURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.tankerkoenigURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.textFieldVersionName)).setText(BuildConfig.VERSION_NAME);

    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_about;
    }
}

