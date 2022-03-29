package org.woheller69.spritpreise.ui.updater;

import org.woheller69.spritpreise.database.CurrentWeatherData;
import org.woheller69.spritpreise.database.Forecast;
import org.woheller69.spritpreise.database.WeekForecast;

import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */
public interface IUpdateableCityUI {
    void processNewCurrentWeatherData(CurrentWeatherData data);

    void processNewForecasts(List<Forecast> forecasts);

    void processNewWeekForecasts(List<WeekForecast> forecasts);
}
