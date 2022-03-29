package org.woheller69.spritpreise.weather_api.open_weather_map;

import android.content.Context;

import androidx.preference.PreferenceManager;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.http.HttpRequestType;
import org.woheller69.spritpreise.http.IHttpRequest;
import org.woheller69.spritpreise.http.VolleyHttpRequest;
import org.woheller69.spritpreise.preferences.AppPreferencesManager;
import org.woheller69.spritpreise.weather_api.IHttpRequestForForecast;

/**
 * This class provides the functionality for making and processing HTTP requests to the
 * OpenWeatherMap to retrieve the latest weather data for all stored cities.
 */
public class OwmHttpRequestForForecast  implements IHttpRequestForForecast {

    /**
     * Member variables.
     */
    private Context context;

    /**
     * @param context The context to use.
     */
    public OwmHttpRequestForForecast(Context context) {
        this.context = context;
    }

    /**
     * @see IHttpRequestForForecast#perform(float, float)
     */
    @Override
    public void perform(float lat, float lon) {
        IHttpRequest httpRequest = new VolleyHttpRequest(context);
        final String URL = getUrlForQueryingForecast(context, lat, lon);
        httpRequest.make(URL, HttpRequestType.GET, new ProcessOwmForecastRequest(context));
    }

    protected String getUrlForQueryingForecast(Context context, float lat, float lon) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        return String.format(
                "%sforecast?lat=%s&lon=%s&units=metric&appid=%s",
                BuildConfig.BASE_URL,
                lat,
                lon,
                prefManager.getOWMApiKey(context)
        );
    }
}
