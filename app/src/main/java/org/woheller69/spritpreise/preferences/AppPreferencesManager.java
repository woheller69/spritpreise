package org.woheller69.spritpreise.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.R;
import static org.woheller69.preferences.Utils.getKey;

/**
 * This class provides access and methods for relevant preferences.
 */
public class AppPreferencesManager {


    /**
     * Member variables
     */
    SharedPreferences preferences;

    /**
     * Constructor.
     *
     * @param preferences Source for the preferences to use.
     */
    public AppPreferencesManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isFirstTimeLaunch() {
        return (preferences.getString("API_key_value", "").equals("") && BuildConfig.DEFAULT_API_KEY.equals(BuildConfig.UNPATCHED_API_KEY));
    }


    public String getTKApiKey(Context context){
        String prefValue = preferences.getString("API_key_value", "");
        if (prefValue.length()==36) return prefValue;  // if a valid key has been entered use it
        else if (BuildConfig.DEFAULT_API_KEY.equals(BuildConfig.UNPATCHED_API_KEY)){ // no key entered and build config not patched when compiling
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, context.getResources().getString(R.string.settings_title_API_key), Toast.LENGTH_LONG).show());
            return "";
        } else {
            return getKey(BuildConfig.DEFAULT_API_KEY);
        }
    }

    public boolean showStarDialog() {
        int versionCode = preferences.getInt("versionCode",BuildConfig.VERSION_CODE);
        boolean askForStar=preferences.getBoolean("askForStar",true);

        if (!isFirstTimeLaunch() && BuildConfig.VERSION_CODE>versionCode && askForStar){ //not at first start, only after upgrade and only if use has not yet given a star or has declined
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
         return true;
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
          return false;
        }
    }

    public void setAskForStar(boolean askForStar){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("askForStar", askForStar);
        editor.apply();
    }
}
