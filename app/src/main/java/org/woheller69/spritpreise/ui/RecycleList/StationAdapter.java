package org.woheller69.spritpreise.ui.RecycleList;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.ui.Help.StringFormatUtils;

import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

//**
// * Created by yonjuni on 02.01.17.
// * Adapter for the horizontal listView for course of the day.
// */import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<Station> stationList;
    private Context context;
    private TextView recyclerViewHeader;
    private RecyclerView recyclerView;
    private ImageView fav;

//Adapter for Stations recycler view
    StationAdapter(List<Station> stationList, Context context, TextView recyclerViewHeader, RecyclerView recyclerView) {
        this.context = context;
        this.stationList = stationList;
        this.recyclerViewHeader=recyclerViewHeader;
        this.recyclerView=recyclerView;
    }


    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_station, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StationViewHolder holder, int position) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        if (stationList !=null && stationList.size()!=0 && stationList.get(0)!=null) {
            long time = stationList.get(0).getTimestamp();
            long zoneseconds = TimeZone.getDefault().getOffset(Instant.now().toEpochMilli()) / 1000L;
            long updateTime = ((time + zoneseconds) * 1000);
            recyclerViewHeader.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_stations_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
        }

        if (prefManager.getBoolean("prefBrands", false)) {  //if preferred brands are defined
            String[] brands = prefManager.getString("prefBrandsString", "").split(","); //read comma separated list
            for (String brand : brands) {
                if (stationList.get(position).getBrand().toLowerCase().contains(brand.toLowerCase().trim())) {
                    holder.fav.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        if (stationList.get(position).getDiesel()>0){
            holder.diesel.setText(StringFormatUtils.formatPrice(context, "D: ",stationList.get(position).getDiesel()," €"));
        }   else holder.diesel.setVisibility(View.GONE);
        if (stationList.get(position).getE5()>0){
            holder.e5.setText( StringFormatUtils.formatPrice(context, "E5: ",stationList.get(position).getE5()," €"));
        }   else holder.e5.setVisibility(View.GONE);
        if (stationList.get(position).getE10()>0){
            holder.e10.setText(StringFormatUtils.formatPrice(context, "E10: ",stationList.get(position).getE10()," €"));
        }   else holder.e10.setVisibility(View.GONE);
        holder.dist.setText(stationList.get(position).getDistance()+" km");
        holder.address.setText((stationList.get(position).getAddress1()+", "+stationList.get(position).getAddress2()).toUpperCase());
        if (stationList.get(position).isOpen()) {
            holder.isOpen.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_door_open_24px, null));
        }
        else  {
            holder.isOpen.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_door_closed_24px, null));
        }

        holder.name.setText(stationList.get(position).getBrand());
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    class StationViewHolder extends RecyclerView.ViewHolder {
        TextView e5;
        TextView diesel;
        TextView e10;
        TextView name;
        TextView dist;
        ImageView isOpen;
        TextView address;
        ImageView fav;

        StationViewHolder(View itemView) {
            super(itemView);

            e5 = itemView.findViewById(R.id.station_e5);
            diesel = itemView.findViewById(R.id.station_diesel);
            e10 = itemView.findViewById(R.id.station_e10);
            name = itemView.findViewById(R.id.station_brand);
            dist = itemView.findViewById(R.id.station_dist);
            isOpen = itemView.findViewById(R.id.station_isOpen);
            address = itemView.findViewById(R.id.station_address);
            fav = itemView.findViewById(R.id.station_fav);

        }
    }
}

