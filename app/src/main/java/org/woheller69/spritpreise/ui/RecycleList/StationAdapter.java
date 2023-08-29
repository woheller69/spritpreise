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
    private int selected = -1;

//Adapter for Stations recycler view
    StationAdapter(List<Station> stationList, Context context) {
        this.context = context;
        this.stationList = stationList;
    }


    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_station, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StationViewHolder holder, int position) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


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
            holder.isOpen.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_local_gas_station_green_24dp, null));
        }
        else  {
            holder.isOpen.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_local_gas_station_red_24dp, null));
        }

        holder.name.setText(stationList.get(position).getBrand());

        if (position == selected) holder.itemView.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_highlight,null));
        else holder.itemView.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_transparent,null));

    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public void setSelected(int position) {
        int oldSelected = selected;
        selected = position;
        notifyItemChanged(oldSelected);
        notifyItemChanged(selected);
    }

    public int getPosUUID(String id) {

        for (int i=0;i<stationList.size();i++){
            if (stationList.get(i).getUuid().equals(id)) return i;
        }
        return 0;
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

