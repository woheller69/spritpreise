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

    private Context context;



    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int DAY = 2;


    public CityAdapter(int cityID, int[] dataSetTypes, Context context) {

        this.dataSetTypes = dataSetTypes;
        this.context = context;

        PFASQLiteHelper database = PFASQLiteHelper.getInstance(context.getApplicationContext());

        List<Forecast> forecasts = database.getForecastsByCityId(cityID);

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

        }  else  {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_day, viewGroup, false);
            return new DayViewHolder(v);

        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;
            if (courseDayList!=null && courseDayList.size()!=0 && courseDayList.get(0)!=null) {
                holder.temperature.setText(StringFormatUtils.formatTemperature(context, courseDayList.get(0).getTemperature()));
            }

        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;
            if (courseDayList!=null && courseDayList.size()!=0 && courseDayList.get(0)!=null) {
                long time = courseDayList.get(0).getTimestamp();
                int zoneseconds = 9600;
                long updateTime = ((time + zoneseconds) * 1000);

                holder.time.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_details_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
                holder.humidity.setText(StringFormatUtils.formatInt(courseDayList.get(0).getHumidity(), context.getString(R.string.units_rh)));
                holder.pressure.setText(StringFormatUtils.formatDecimal(courseDayList.get(0).getTemperature(), context.getString(R.string.units_hPa)));
            }
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

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }
}