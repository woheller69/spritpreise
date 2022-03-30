package org.woheller69.spritpreise.ui.RecycleList;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.database.Forecast;
import org.woheller69.spritpreise.database.PFASQLiteHelper;
import org.woheller69.spritpreise.ui.Help.StringFormatUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

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

    public void updateForecastData(List<Forecast> forecasts) {

        courseDayList = new ArrayList<>();
        courseDayList.addAll(forecasts);

            notifyDataSetChanged();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    public class OverViewHolder extends ViewHolder {
        //TextView temperature;
        //TextView sun;

        OverViewHolder(View v) {
            super(v);
            //this.temperature = v.findViewById(R.id.card_overview_temperature);
            //this.sun=v.findViewById(R.id.card_overview_sunrise_sunset);
        }
    }

    public class DetailViewHolder extends ViewHolder {


        DetailViewHolder(View v) {
            super(v);

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


        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;


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