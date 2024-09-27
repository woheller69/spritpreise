package org.woheller69.spritpreise.ui.RecycleList;

import static org.woheller69.spritpreise.database.SQLiteHelper.getWidgetCityID;
import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.CityGasPricesActivity;
import org.woheller69.spritpreise.database.CityToWatch;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.ui.Help.StringFormatUtils;
import org.woheller69.spritpreise.ui.viewPager.CityPagerAdapter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final int[] dataSetTypes;
    private List<Station> stationList;
    private final int cityID;
    private final Context context;
    private LocationManager locationManager;
    private static LocationListener myPositionListenerGPS;

    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int STATIONS = 2;

//Adapter for CityFragment
    public CityAdapter(int cityID, int[] dataSetTypes, Context context) {

        this.dataSetTypes = dataSetTypes;
        this.context = context;
        this.cityID = cityID;

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

        OverViewHolder(View v) {
            super(v);
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
        MapView map;

        StationViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_stations);
            recyclerView.setHasFixedSize(false);
            recyclerViewHeader=v.findViewById(R.id.recycler_view_header);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!recyclerView.canScrollVertically(-1)){
                        recyclerView.setOnTouchListener(new OnSwipeDownListener(context) {
                            public void onSwipeDown() {
                                CityPagerAdapter.refreshSingleData(context,true,cityID);
                                CityGasPricesActivity.startRefreshAnimation();
                            }
                        });
                    }else recyclerView.setOnTouchListener(null);
                }
            });
            map = v.findViewById(R.id.map);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
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
            Marker highlightMarker = new Marker(holder.map);
            Marker positionMarker = new Marker(holder.map);
            highlightMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_highlight_32dp));
            highlightMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            holder.recyclerView.addItemDecoration(new DividerItemDecoration(holder.recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            StationAdapter adapter = new StationAdapter(stationList, context);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);
            holder.recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, holder.recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    setHighlightMarker(position, holder, highlightMarker);
                    adapter.setSelected(position);
                }

                @Override
                public void onLongItemClick(View view, int position) {
                    String loc = stationList.get(position).getLatitude() + "," + stationList.get(position).getLongitude();
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + loc + "?q=" + loc)));
                    } catch (ActivityNotFoundException ignored) {
                        Toast.makeText(context,R.string.error_no_map_app, Toast.LENGTH_LONG).show();
                    }
                }
            }));

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp.getBoolean("pref_map",true)) {
                holder.map.setVisibility(View.VISIBLE);
                holder.map.setMultiTouchControls(true);
                holder.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

                Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
                holder.map.setTileSource(TileSourceFactory.MAPNIK);
                holder.map.setTilesScaledToDpi(true);

                int nightmodeflag = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                if (nightmodeflag == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                    holder.map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
                else holder.map.getOverlayManager().getTilesOverlay().setColorFilter(null);

                SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());

                IMapController mapController = holder.map.getController();
                mapController.setZoom(12d);
                GeoPoint startPoint = new GeoPoint(database.getCityToWatch(cityID).getLatitude(), database.getCityToWatch(cityID).getLongitude());
                mapController.setCenter(startPoint);

                CopyrightOverlay copyrightOverlay = new CopyrightOverlay(context);
                copyrightOverlay.setTextColor(context.getColor(R.color.colorPrimaryDark));
                holder.map.getOverlays().add(copyrightOverlay);
                List<Station> stations = database.getStationsByCityId(cityID);

                for (Station station : stations) {
                    if (!sp.getBoolean("pref_hide_closed", false) || station.isOpen()) {  //only show open stations on map
                        GeoPoint stationPosition = new GeoPoint(station.getLatitude(), station.getLongitude());
                        String stationName = station.getBrand();
                        Marker stationMarker = new Marker(holder.map);
                        stationMarker.setPosition(stationPosition);
                        stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        if (station.isOpen())
                            stationMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_local_gas_station_green_24dp));
                        else
                            stationMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_local_gas_station_red_24dp));
                        String priceinfo = "";
                        if (station.getE5() > 0)
                            priceinfo = priceinfo + StringFormatUtils.formatPrice(context, "E5: ", station.getE5(), " €  ");
                        if (station.getE10() > 0)
                            priceinfo = priceinfo + StringFormatUtils.formatPrice(context, "E10: ", station.getE10(), " €  ");
                        if (station.getDiesel() > 0)
                            priceinfo = priceinfo + StringFormatUtils.formatPrice(context, "D: ", station.getDiesel(), " €  ");
                        stationMarker.setSnippet(priceinfo);
                        stationMarker.setTitle(stationName);

                        stationMarker.setId(station.getUuid());
                        stationMarker.setOnMarkerClickListener((marker, mapView) -> {
                            int pos = adapter.getPosUUID(marker.getId());
                            holder.recyclerView.getLayoutManager().scrollToPosition(pos);
                            setHighlightMarker(pos, holder, highlightMarker);
                            adapter.setSelected(pos);
                            return false;
                        });


                        holder.map.getOverlays().add(stationMarker);
                    }
                }
                MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) {
                        return false;
                    }

                    @Override
                    public boolean longPressHelper(GeoPoint location) {
                        if (cityID == getWidgetCityID(context) ) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setMessage(context.getString(R.string.dialog_search_here));
                            alertDialogBuilder.setPositiveButton(context.getString(R.string.dialog_OK_button), (dialog, which) -> {
                                SQLiteHelper db = SQLiteHelper.getInstance(context);
                                CityToWatch city = db.getCityToWatch(getWidgetCityID(context));
                                city.setLatitude((float) location.getLatitude());
                                city.setLongitude((float) location.getLongitude());
                                city.setCityName(String.format(Locale.getDefault(), "%.2f° / %.2f°", location.getLatitude(), location.getLongitude()));
                                db.updateCityToWatch(city);
                                db.deleteStationsByCityId(getWidgetCityID(context));
                                CityPagerAdapter.refreshSingleData(context,true,getWidgetCityID(context));
                                CityGasPricesActivity.startRefreshAnimation();
                                CityGasPricesActivity.initPagerAdapter();
                                CityGasPricesActivity.refreshTab0Header(city.getCityName());
                            });
                            alertDialogBuilder.setNegativeButton(context.getString(R.string.dialog_NO_button), (dialog, which) -> {

                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        }
                        return false;
                    }
                };

                MapEventsOverlay OverlayEventos = new MapEventsOverlay(mapEventsReceiver);
                holder.map.getOverlays().add(OverlayEventos);

                myPositionListenerGPS = location -> {
                    if (holder.map.getOverlays().contains(positionMarker))
                        holder.map.getOverlays().remove(positionMarker);
                    if (location.hasBearing() && location.hasSpeed() && location.getSpeed()>0.5){
                        Drawable originalDrawable = ContextCompat.getDrawable(context, R.drawable.ic_direction_32dp);
                        Drawable rotatedDrawable = getRotatedDrawable(originalDrawable, location.getBearing());
                        positionMarker.setIcon(rotatedDrawable);
                        positionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    } else {
                        positionMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_location_32dp));
                        positionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    }
                    GeoPoint myPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
                    positionMarker.setPosition(myPosition);
                    positionMarker.setInfoWindow(null);
                    holder.map.getOverlays().add(positionMarker);
                    holder.map.invalidate();
                };
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
                if (prefManager.getBoolean("pref_GPS", true) == TRUE && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, myPositionListenerGPS);
                }
            } else {
                holder.map.setVisibility(View.GONE);
            }

            if (!stationList.isEmpty()){
                long time = stationList.get(0).getTimestamp();
                long zoneseconds = TimeZone.getDefault().getOffset(Instant.now().toEpochMilli()) / 1000L;
                long updateTime = ((time + zoneseconds) * 1000);

                holder.recyclerViewHeader.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_stations_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
            }

        }
        //No update for error needed
    }

    @NonNull
    private Drawable getRotatedDrawable(Drawable originalDrawable, Float angle) {
        Bitmap originalBitmap = Bitmap.createBitmap(originalDrawable.getIntrinsicWidth(), originalDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(originalBitmap);
        originalDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        originalDrawable.draw(canvas);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        return new BitmapDrawable(context.getResources(), rotatedBitmap);
    }

    private void setHighlightMarker(int position, StationViewHolder holder, Marker highlightMarker) {
        if (holder.map.getOverlays().contains(highlightMarker)) holder.map.getOverlays().remove(highlightMarker);
        GeoPoint highlightPosition = new GeoPoint(stationList.get(position).getLatitude(), stationList.get(position).getLongitude());
        highlightMarker.setPosition(highlightPosition);
        highlightMarker.setInfoWindow(null);
        holder.map.getOverlays().add(highlightMarker);
        holder.map.invalidate();
    }

    @Override
    public int getItemCount() {
        return dataSetTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }

    public void removeMyPositionListenerGPS() {
        if (myPositionListenerGPS!=null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(myPositionListenerGPS);
        }
        myPositionListenerGPS=null;
    }
}