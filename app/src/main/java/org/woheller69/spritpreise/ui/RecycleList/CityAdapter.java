package org.woheller69.spritpreise.ui.RecycleList;

import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private int[] dataSetTypes;
    private List<Station> stationList;

    private Context context;



    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int STATIONS = 2;


    public CityAdapter(int cityID, int[] dataSetTypes, Context context) {

        this.dataSetTypes = dataSetTypes;
        this.context = context;

        SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());

        List<Station> stations = database.getStationsByCityId(cityID);

        updateStationsData(stations);

    }

    public void updateStationsData(List<Station> stations) {

        stationList = new ArrayList<>();
        stationList.addAll(stations);

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



    public class StationViewHolder extends ViewHolder {
        RecyclerView recyclerView;
        TextView recyclerViewHeader;

        StationViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_stations);
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
                    .inflate(R.layout.card_stations, viewGroup, false);
            return new StationViewHolder(v);

        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;


        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;


        }  else if (viewHolder.getItemViewType() == STATIONS) {

            StationViewHolder holder = (StationViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            holder.recyclerView.addItemDecoration(new DividerItemDecoration(holder.recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            StationAdapter adapter = new StationAdapter(stationList, context, holder.recyclerViewHeader, holder.recyclerView);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);
            holder.recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, holder.recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String loc = stationList.get(position).getLatitude() + "," + stationList.get(position).getLongitude();
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + loc + "?q=" + loc)));
                    } catch (ActivityNotFoundException ignored) {
                        Toast.makeText(context,R.string.error_no_map_app, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onLongItemClick(View view, int position) {

                }
            }));
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