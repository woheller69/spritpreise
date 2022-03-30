package org.woheller69.spritpreise.weather_api.open_weather_map;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.spritpreise.database.Forecast;
import org.woheller69.spritpreise.weather_api.IDataExtractor;

import java.util.Date;
import java.util.TimeZone;

/**
 * This is a concrete implementation for extracting weather data that was retrieved by
 * OpenWeatherMap.
 */
public class OwmDataExtractor implements IDataExtractor {

    /**
     * @see IDataExtractor#wasCityFound(String)
     */
    @Override
    public boolean wasCityFound(String data) {
        try {
            JSONObject json = new JSONObject(data);
            return json.has("ok") && (json.getBoolean("ok"));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param data The data that contains the information to instantiate a CurrentWeatherData
     *             object.
     *             If data for a single city were requested, the response string can be
     *             passed as an argument.
     *             If data for multiple cities were requested, make sure to pass only one item
     *             of the response list at a time!
     * @return Returns an instance of CurrentWeatherData of the information could be extracted
     * successfully or null in case there was some error while parsing the response (which is not
     * too good because that means that the response of OpenWeatherMap was not well-formed).
     */




    /**
     * @see IDataExtractor#extractForecast(String)
     */
    @Override
    public Forecast extractForecast(String data) {
        try {

            Forecast forecast = new Forecast();
            TimeZone tz = TimeZone.getDefault();
            Date now = new Date();
            double offsetFromUtc = tz.getOffset(now.getTime()) / 3600.0;
            forecast.setTimestamp((long) ((System.currentTimeMillis() + offsetFromUtc)/ 1000));

            JSONObject json = new JSONObject(data);

            if (json.has("diesel") && !json.isNull("diesel")) forecast.setTemperature((float) json.getDouble("diesel"));
            Log.d("Extract Diesel", String.valueOf(json.getDouble("diesel")));
            if (json.has("e5") && !json.isNull("e5")) forecast.setHumidity((float) json.getDouble("e5"));
            if (json.has("e10") && !json.isNull("e10")) forecast.setPressure((float) json.getDouble("e10"));
            forecast.setCity_name(json.getString("brand"));
            if (json.getString("brand").equals("")) forecast.setCity_name(json.getString("name"));

            forecast.setPrecipitation((float) json.getDouble("postCode"));
            return forecast;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
