package org.woheller69.spritpreise.api.tankerkoenig;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.http.HttpRequestType;
import org.woheller69.spritpreise.http.IHttpRequest;
import org.woheller69.spritpreise.http.VolleyHttpRequest;
import org.woheller69.spritpreise.preferences.AppPreferencesManager;
import org.woheller69.spritpreise.api.IHttpRequestForStations;

/**
 * This class provides the functionality for making and processing HTTP requests to
 * Tankerkönig to retrieve the latest gas prices for all stored cities.
 */
public class TKHttpRequestForStations implements IHttpRequestForStations {

    /**
     * Member variables.
     */
    private Context context;

    /**
     * @param context The context to use.
     */
    public TKHttpRequestForStations(Context context) {
        this.context = context;
    }

    /**
     * @see IHttpRequestForStations#perform(float, float,int)
     */
    @Override
    public void perform(float lat, float lon, int cityId) {
        IHttpRequest httpRequest = new VolleyHttpRequest(context, cityId);
        final String URL = getUrlForQueryingStations(context, lat, lon);
        httpRequest.make(URL, HttpRequestType.GET, new TKProcessHttpRequest(context));
    }

    protected String getUrlForQueryingStations(Context context, float lat, float lon) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return String.format(
                "%slist.php?lat=%s&lng=%s&rad=%s&sort=%s&type=%s&apikey=%s",
                BuildConfig.BASE_URL,
                lat,
                lon,
                sharedPreferences.getString("pref_searchRadius","3"),
                sharedPreferences.getBoolean("pref_sort",false)&&(!sharedPreferences.getString("pref_type","all").equals("all")) ? "price" : "dist",
                sharedPreferences.getString("pref_type","all"),
                prefManager.getTKApiKey(context)
        );
    }
}
