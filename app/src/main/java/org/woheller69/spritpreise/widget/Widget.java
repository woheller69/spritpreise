package org.woheller69.spritpreise.widget;


import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.CityGasPricesActivity;
import org.woheller69.spritpreise.database.CityToWatch;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.services.UpdateDataService;
import org.woheller69.spritpreise.ui.Help.StringFormatUtils;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.spritpreise.database.SQLiteHelper.getWidgetCityID;

import static org.woheller69.spritpreise.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

public class Widget extends AppWidgetProvider {
    private final static int MINDISTANCE = 5000;
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;

    public void updateAppWidget(Context context, final int appWidgetId) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,false);
            Intent intent = new Intent(context, UpdateDataService.class);
            intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
            intent.putExtra("cityId", cityID);
            intent.putExtra(SKIP_UPDATE_INTERVAL, true);
            enqueueWork(context, UpdateDataService.class, 0, intent);
        }
    }

    public static void updateLocation(final Context context, int cityID, boolean manual) {
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        List<CityToWatch> cities = db.getAllCitiesToWatch();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                CityToWatch city;
                double lat = locationGPS.getLatitude();
                double lon = locationGPS.getLongitude();
                for (int i=0; i<cities.size();i++){
                    if (cities.get(i).getCityId()==cityID) {
                        city = cities.get(i);
                        city.setLatitude((float) lat);
                        city.setLongitude((float) lon);
                        city.setCityName(String.format(Locale.getDefault(),"%.2f° / %.2f°", lat, lon));
                        db.updateCityToWatch(city);

                        break;
                    }
                }
            } else {
                if (manual) Toast.makeText(context.getApplicationContext(),R.string.error_no_position,Toast.LENGTH_SHORT).show(); //show toast only if manual update by refresh button
            }

        }
    }

    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, List<Station> stations) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) views.setViewVisibility(R.id.location_on, View.VISIBLE); else views.setViewVisibility(R.id.location_on,View.GONE);
        resetView(context, views, city);

        if (stations.size()>0) {
            long time = stations.get(0).getTimestamp();
            long zoneseconds = TimeZone.getDefault().getOffset(Instant.now().toEpochMilli()) / 1000L;
            long updateTime = ((time + zoneseconds) * 1000);
            views.setTextViewText(R.id.widget_updatetime, "("+StringFormatUtils.formatTimeWithoutZone(context, updateTime)+")");

            boolean foundStation = false;
            if (prefManager.getBoolean("prefBrands", false)) {  //if preferred brands are defined
                String[] brands = prefManager.getString("prefBrandsString", "").split(","); //read comma separated list
                for (Station station : stations) {
                    if (station.isOpen()) {  //display values of closest open station
                        for (String brand : brands) {  //search if one of the preferred brands is available
                            if (station.getBrand().toLowerCase().contains(brand.toLowerCase().trim())) {   //remove leading and trailing spaces and compare
                                setView(context, views, appWidgetId, station);
                                views.setViewVisibility(R.id.widget_fav,View.VISIBLE);
                                foundStation = true;
                                break;
                            }
                        }
                    }
                    if (foundStation) break;
                }
            }

            if (!foundStation) {
                for (Station station : stations) {
                    if (station.isOpen()) {  //display values of closest open station
                        setView(context, views, appWidgetId, station);
                        break;
                    }
                }
            }
        }
        Intent intentUpdate = new Intent(context, Widget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        intentUpdate.putExtra("Manual",true);
        PendingIntent pendingUpdate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_update, pendingUpdate);

        Intent intent2 = new Intent(context, CityGasPricesActivity.class);
        intent2.putExtra("cityId", getWidgetCityID(context));
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

    }

    private static void resetView(Context context, RemoteViews views, CityToWatch city) {
        views.setTextViewText(R.id.widget_city_name, city.getCityName());
        views.setViewVisibility(R.id.widget_E5,View.GONE);
        views.setViewVisibility(R.id.widget_E10,View.GONE);
        views.setViewVisibility(R.id.widget_D,View.GONE);
        views.setViewVisibility(R.id.widget_image,View.GONE);
        views.setViewVisibility(R.id.widget_fav,View.GONE);
        views.setViewVisibility(R.id.widget_types,View.GONE);
        views.setTextViewText(R.id.widget_dist,"");
        views.setTextViewText(R.id.widget_updatetime,"");
        views.setTextViewText(R.id.widget_brand, context.getString(R.string.error_no_station_found));
    }

    private static void setView(Context context, RemoteViews views, int appWidgetId, Station station) {
        views.setViewVisibility(R.id.widget_types,View.VISIBLE);
        views.setViewVisibility(R.id.widget_image, View.VISIBLE);
        if (station.getE5()>0){
            views.setViewVisibility(R.id.widget_E5, View.VISIBLE);
            views.setTextViewText(R.id.widget_E5, StringFormatUtils.formatPrice(context, "E5: ", station.getE5(), " €"));
        }
        if (station.getE10()>0){
            views.setViewVisibility(R.id.widget_E10, View.VISIBLE);
            views.setTextViewText(R.id.widget_E10, StringFormatUtils.formatPrice(context, "E10: ", station.getE10(), " €"));
        }
        if (station.getDiesel()>0){
            views.setViewVisibility(R.id.widget_D, View.VISIBLE);
            views.setTextViewText(R.id.widget_D, StringFormatUtils.formatPrice(context, "D: ", station.getDiesel(), " €"));
        }
        views.setTextViewText(R.id.widget_dist, station.getDistance() + " km");
        views.setTextViewText(R.id.widget_brand, station.getBrand());
        String loc = station.getLatitude() + "," + station.getLongitude();
        Intent intent3 = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + loc + "?q=" + loc));
        PendingIntent pendingIntentMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentMap = PendingIntent.getActivity(context, appWidgetId, intent3, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntentMap = PendingIntent.getActivity(context, appWidgetId, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntentMap);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Log.d("GPS", "Widget onUpdate");
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !powerManager.isPowerSaveMode()) {
                if (locationListenerGPS==null) {
                    Log.d("GPS", "Listener null");
                    locationListenerGPS = new LocationListener() {
                        @Override
                        public void onLocationChanged(android.location.Location location) {
                            Log.d("GPS", "Location changed");
                            // Check if location change > MINDISTANCE, then update CityToWatch and update widgets accordingly
                            SQLiteHelper db = SQLiteHelper.getInstance(context);
                            CityToWatch city=db.getCityToWatch(getWidgetCityID(context));
                            Location oldlocation = new Location(LocationManager.PASSIVE_PROVIDER);
                            oldlocation.setLatitude(city.getLatitude());
                            oldlocation.setLongitude(city.getLongitude());
                            if (oldlocation.distanceTo(location)>MINDISTANCE){  //update coordinates of CityToWatch
                                city.setLatitude((float) location.getLatitude());
                                city.setLongitude((float) location.getLongitude());
                                city.setCityName(String.format(Locale.getDefault(),"%.2f° / %.2f°", location.getLatitude(), location.getLongitude()));
                                db.updateCityToWatch(city);
                                // There may be multiple widgets active, so update all of them
                                int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget.class)); //IDs Might have changed since last call of onUpdate
                                for (int appWidgetId : appWidgetIds) {
                                    updateAppWidget(context, appWidgetId);
                                }
                            }
                        }

                        @Deprecated
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    };
                    Log.d("GPS", "Request Updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, MINDISTANCE, locationListenerGPS);  //Update every 10 min, MINDISTANCE km
                }
            }else {
                Log.d("GPS","Remove Updates");
                if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
                locationListenerGPS=null;
            }

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);

        int widgetCityID= getWidgetCityID(context);

        List<Station> stations =dbHelper.getStationsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget.class));

        for (int widgetID : widgetIDs) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

                Widget.updateView(context, appWidgetManager, views, widgetID, city, stations);
                appWidgetManager.updateAppWidget(widgetID, views);

        }
     }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.d("GPS", "Last widget removed");
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
        locationListenerGPS=null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("Manual", false)) {
            int cityID = getWidgetCityID(context);
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,true);
        }
        super.onReceive(context,intent);
    }
}

