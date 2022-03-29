package org.woheller69.spritpreise.ui.RecycleList;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.database.CurrentWeatherData;
import org.woheller69.spritpreise.database.Forecast;
import org.woheller69.spritpreise.database.PFASQLiteHelper;
import org.woheller69.spritpreise.database.WeekForecast;
import org.woheller69.spritpreise.ui.Help.StringFormatUtils;
import org.woheller69.spritpreise.ui.UiResourceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.woheller69.spritpreise.weather_api.IApiToDatabaseConversion;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
    private static final String TAG = "Forecast_Adapter";

    private int[] dataSetTypes;
    private List<Forecast> courseDayList;
    private float[][] forecastData;

    private Context context;

    private CurrentWeatherData currentWeatherDataList;

    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int DAY = 2;


    public CityAdapter(CurrentWeatherData currentWeatherDataList, int[] dataSetTypes, Context context) {
        this.currentWeatherDataList = currentWeatherDataList;
        this.dataSetTypes = dataSetTypes;
        this.context = context;

        PFASQLiteHelper database = PFASQLiteHelper.getInstance(context.getApplicationContext());

        List<Forecast> forecasts = database.getForecastsByCityId(currentWeatherDataList.getCity_id());
        List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());

        updateForecastData(forecasts);

    }

    // function update 3-hour or 1-hour forecast list
    public void updateForecastData(List<Forecast> forecasts) {

        courseDayList = new ArrayList<>();

        long threehoursago = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        long onehourago = System.currentTimeMillis() - (1 * 60 * 60 * 1000);

        if (forecasts.size() >= 48) {  //2day 1-hour forecast
                for (Forecast f : forecasts) {
                    if (f.getForecastTime() >= onehourago) {
                        courseDayList.add(f);
                    }
                }
        } else if (forecasts.size() == 40) {  //5day 3-hour forecast
                for (Forecast f : forecasts) {
                    if (f.getForecastTime() >= threehoursago) {
                        courseDayList.add(f);
                    }
                }
            }
            notifyDataSetChanged();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    public class OverViewHolder extends ViewHolder {
        TextView temperature;
        ImageView weather;
        TextView sun;

        OverViewHolder(View v) {
            super(v);
            this.temperature = v.findViewById(R.id.card_overview_temperature);
            this.weather = v.findViewById(R.id.card_overview_weather_image);
            this.sun=v.findViewById(R.id.card_overview_sunrise_sunset);
        }
    }

    public class DetailViewHolder extends ViewHolder {
        TextView humidity;
        TextView pressure;
        TextView windspeed;
        TextView rain60min;
        TextView rain60minLegend;
        TextView time;
        ImageView winddirection;

        DetailViewHolder(View v) {
            super(v);
            this.humidity = v.findViewById(R.id.card_details_humidity_value);
            this.pressure = v.findViewById(R.id.card_details_pressure_value);
            this.windspeed = v.findViewById(R.id.card_details_wind_speed_value);
            this.rain60min = v.findViewById(R.id.card_details_rain60min_value);
            this.rain60minLegend=v.findViewById(R.id.card_details_legend_rain60min);
            this.winddirection =v.findViewById((R.id.card_details_wind_direction_value));
            this.time=v.findViewById(R.id.card_details_title);
        }
    }



    public class DayViewHolder extends ViewHolder {
        RecyclerView recyclerView;
        TextView recyclerViewHeader;

        DayViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_course_day);
            recyclerView.setHasFixedSize(true);
            recyclerViewHeader=v.findViewById(R.id.recycler_view_header);
        }
    }


    public class ErrorViewHolder extends ViewHolder {
        ErrorViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == OVERVIEW) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_overview, viewGroup, false);

            return new OverViewHolder(v);

        } else if (viewType == DETAILS) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_details, viewGroup, false);
            return new DetailViewHolder(v);

        }  else if (viewType == DAY) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_day, viewGroup, false);
            return new DayViewHolder(v);

        }  else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_error, viewGroup, false);
            return new ErrorViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        boolean isDay = currentWeatherDataList.isDay(context);

        if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;

            //correct for timezone differences
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            long riseTime = (currentWeatherDataList.getTimeSunrise() + zoneseconds) * 1000;
            long setTime = (currentWeatherDataList.getTimeSunset() + zoneseconds) * 1000;
            if (riseTime==zoneseconds*1000 || setTime==zoneseconds*1000) holder.sun.setText("\u2600\u25b2 --:--" + " \u25bc --:--" );
            else  {
                holder.sun.setText("\u2600\u25b2 " + StringFormatUtils.formatTimeWithoutZone(context, riseTime) + " \u25bc " + StringFormatUtils.formatTimeWithoutZone(context, setTime));
            }

            setImage(currentWeatherDataList.getWeatherID(), holder.weather, isDay);

            holder.temperature.setText(StringFormatUtils.formatTemperature(context, currentWeatherDataList.getTemperatureCurrent()));

        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;

            long time = currentWeatherDataList.getTimestamp();
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            long updateTime = ((time + zoneseconds) * 1000);

            holder.time.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_details_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
            holder.humidity.setText(StringFormatUtils.formatInt(currentWeatherDataList.getHumidity(), context.getString(R.string.units_rh)));
            holder.pressure.setText(StringFormatUtils.formatDecimal(currentWeatherDataList.getPressure(), context.getString(R.string.units_hPa)));
            holder.windspeed.setText(StringFormatUtils.formatWindSpeed(context, currentWeatherDataList.getWindSpeed()));
            holder.windspeed.setBackground(StringFormatUtils.colorWindSpeed(context, currentWeatherDataList.getWindSpeed()));
            holder.winddirection.setRotation(currentWeatherDataList.getWindDirection());

            if (currentWeatherDataList.getRain60min()!=null && currentWeatherDataList.getRain60min().length()==12){
                holder.rain60min.setText(currentWeatherDataList.getRain60min().substring(0,3)+"\u2009"+currentWeatherDataList.getRain60min().substring(3,6)+"\u2009"+currentWeatherDataList.getRain60min().substring(6,9)+"\u2009"+currentWeatherDataList.getRain60min().substring(9));
            } else {
                holder.rain60min.setText(R.string.error_no_rain60min_data);
            }
            holder.rain60minLegend.setText("( "+context.getResources().getString(R.string.units_mm_h)+String.format(Locale.getDefault(),": □ %.1f ▤ <%.1f ▦ <%.1f ■ >=%.1f )",0.0,0.5,2.5,2.5));

        }  else if (viewHolder.getItemViewType() == DAY) {

            DayViewHolder holder = (DayViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            StationAdapter adapter = new StationAdapter(courseDayList, context, holder.recyclerViewHeader, holder.recyclerView);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);
        }
        //No update for error needed
    }

    public void setImage(int value, ImageView imageView, boolean isDay) {
        imageView.setImageResource(UiResourceProvider.getImageResourceForWeatherCategory(value, isDay));
    }

    //this method fixes the problem that OpenWeatherMap will show a rain symbol for the whole day even if weather during day is great and there are just a few drops of rain during night
    public static boolean checkSun(Context context, int cityId, long forecastTimeNoon) {
        PFASQLiteHelper dbHelper = PFASQLiteHelper.getInstance(context);
        List<Forecast> forecastList = dbHelper.getForecastsByCityId(cityId);
        long extend=0;
        boolean sun=false;
        //iterate over FCs 5h before and 5h past forecast time of the weekforecast (which should usually be noon)
        if (!forecastList.isEmpty() && forecastList.get(0).getForecastTime()>forecastTimeNoon) extend = 10800000;  // if it is already afternoon iterate 3h more, this happens on current day only
        for (Forecast fc : forecastList) {
            if ((fc.getForecastTime() >= forecastTimeNoon-18000000) && (fc.getForecastTime() <= forecastTimeNoon+18000000+extend)) {
                if (fc.getWeatherID() <= IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal()) sun = true;  //if weather better or equal broken clouds in one interval there is at least some sun during day.
            }
        }
        return sun;
    }
    //this method fixes the problem that OpenWeatherMap will show a rain symbol for the whole day even if weather during day is great and there are just a few drops of rain during night
    public static Integer getCorrectedWeatherID(Context context, int cityId, long forecastTimeNoon) {
        PFASQLiteHelper dbHelper = PFASQLiteHelper.getInstance(context);
        List<Forecast> forecastList = dbHelper.getForecastsByCityId(cityId);
        long extend=0;
        int category=0;
        //iterate over FCs 5h before and 5h past forecast time of the weekforecast (which should usually be noon)
        if (!forecastList.isEmpty() && forecastList.get(0).getForecastTime()>forecastTimeNoon) extend = 10800000;  // if it is already afternoon iterate 3h more, this happens on current day only
        for (Forecast fc : forecastList) {
            if ((fc.getForecastTime() >= forecastTimeNoon - 18000000) && (fc.getForecastTime() <= forecastTimeNoon + 18000000+extend)) {
                if (fc.getWeatherID() > category) {
                    category = fc.getWeatherID();  //find worst weather
                }
            }
        }
        //if worst is overcast clouds set category to broken clouds because fix is only used if checkSun=true, i.e. at least one interval with sun
        if (category==IApiToDatabaseConversion.WeatherCategories.OVERCAST_CLOUDS.getNumVal()) category=IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal();
        if (category>IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal() || category==0) category=1000; //do not change weather if condition is worse than broken clouds or forecastList empty
        return category;
    }


    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }
}