package org.woheller69.spritpreise.weather_api;

import org.woheller69.spritpreise.database.CurrentWeatherData;
import org.woheller69.spritpreise.database.Forecast;
import org.woheller69.spritpreise.database.WeekForecast;
import org.woheller69.spritpreise.radius_search.RadiusSearchItem;

/**
 * This interface defines the frame of the functionality to extractCurrentWeatherData weather information from which
 * is returned by some API.
 */
public interface IDataExtractor {

    /**
     * Takes the response from the (web) server and checks whether the requested city was found.
     *
     * @param data The textual response from the server.
     * @return Returns true if the city was found or false otherwise (i. e. in case of 404).
     */
    boolean wasCityFound(String data);


    Forecast extractForecast(String data);

    /**
     * @param data0, data1, data2, data3, data4 contain the information to retrieve the rain for a minute within the next 60min.
     * @return Returns a string with a rain drop in case of rain or a - in case of no rain
     */


}
