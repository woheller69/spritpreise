package org.woheller69.spritpreise.ui.RecycleList;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.CourseOfDayViewHolder> {

    private List<Station> stationList;
    private Context context;
    private TextView recyclerViewHeader;
    private RecyclerView recyclerView;


    StationAdapter(List<Station> stationList, Context context, TextView recyclerViewHeader, RecyclerView recyclerView) {
        this.context = context;
        this.stationList = stationList;
        this.recyclerViewHeader=recyclerViewHeader;
        this.recyclerView=recyclerView;
    }


    @Override
    public CourseOfDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_station, parent, false);
        return new CourseOfDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseOfDayViewHolder holder, int position) {

        if (stationList !=null && stationList.size()!=0 && stationList.get(0)!=null) {
            long time = stationList.get(0).getTimestamp();
            long zoneseconds = TimeZone.getDefault().getOffset(Instant.now().toEpochMilli()) / 1000L;
            long updateTime = ((time + zoneseconds) * 1000);
            recyclerViewHeader.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_details_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
        }

        holder.diesel.setText("D: " + stationList.get(position).getDiesel());
        holder.e5.setText( "E5: " + stationList.get(position).getE5());
        holder.e10.setText("E10: " + stationList.get(position).getE10());
        holder.dist.setText(stationList.get(position).getDistance()+" km");
        holder.address.setText((stationList.get(position).getAddress1()+", "+stationList.get(position).getAddress2()).toUpperCase());
        if (stationList.get(position).isOpen()) {
            holder.isOpen.setText("Geöffnet");
            holder.isOpen.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_green,null));
        }
        else  {
            holder.isOpen.setText("Geschlossen");
            holder.isOpen.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_lightred,null));
        }

        holder.name.setText(stationList.get(position).getBrand());
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    class CourseOfDayViewHolder extends RecyclerView.ViewHolder {
        TextView e5;
        TextView diesel;
        TextView e10;
        TextView name;
        TextView dist;
        TextView isOpen;
        TextView address;

        CourseOfDayViewHolder(View itemView) {
            super(itemView);

            e5 = itemView.findViewById(R.id.station_e5);
            diesel = itemView.findViewById(R.id.station_diesel);
            e10 = itemView.findViewById(R.id.station_e10);
            name = itemView.findViewById(R.id.station_brand);
            dist = itemView.findViewById(R.id.station_dist);
            isOpen = itemView.findViewById(R.id.station_isOpen);
            address = itemView.findViewById(R.id.station_address);

        }
    }
}

