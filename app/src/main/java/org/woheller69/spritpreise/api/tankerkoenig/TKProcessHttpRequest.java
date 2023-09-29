package org.woheller69.spritpreise.api.tankerkoenig;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.NavigationActivity;
import org.woheller69.spritpreise.database.CityToWatch;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.ui.updater.ViewUpdater;
import org.woheller69.spritpreise.api.IDataExtractor;
import org.woheller69.spritpreise.api.IProcessHttpRequest;
import org.woheller69.spritpreise.widget.Widget;
import static org.woheller69.spritpreise.database.SQLiteHelper.getWidgetCityID;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class processes the HTTP requests that are made to the Tankerkönig API requesting the
 * current prices for all stored cities.
 */
public class TKProcessHttpRequest implements IProcessHttpRequest {

    /**
     * Member variables
     */
    private Context context;
    private SQLiteHelper dbHelper;

    /**
     * Constructor.
     *
     * @param context The context of the HTTP request.
     */
    public TKProcessHttpRequest(Context context) {
        this.context = context;
        this.dbHelper = SQLiteHelper.getInstance(context);
    }

    /**
     * Converts the response to JSON and updates the database. Note that for this method no
     * UI-related operations are performed.
     *
     * @param response The response of the HTTP request.
     */
    @Override
    public void processSuccessScenario(String response, int cityId) {
        IDataExtractor extractor = new TKDataExtractor();
        dbHelper.deleteStationsByCityId(cityId); //start with empty stations list
        List<Station> stations = new ArrayList<>();
        if (extractor.wasCityFound(response)) {
            try {
                JSONObject json = new JSONObject(response);
                JSONArray list = json.getJSONArray("stations");
                for (int i = 0; i < list.length(); i++) {
                    String currentItem = list.get(i).toString();
                    Log.d("Extract", currentItem);
                    Station station = extractor.extractStation(currentItem,context);
                    if (station != null) { // Could retrieve all data, so add it to the list
                        station.setCity_id(cityId);
                        stations.add(station);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            final String ERROR_MSG = context.getResources().getString(R.string.error_fetch_stations);
            if (NavigationActivity.isVisible)
                Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
        }

        CalculateStationRating(stations);
        // add all stations to the database
        dbHelper.addStations(stations);

        ViewUpdater.updateStations(stations,cityId);
        possiblyUpdateWidgets(cityId, stations);
    }

    /**
     * Manipulate list and add ratings based on comparing all stations in the list
     * @param stations the list of stations to compare to each other
     */
    private void CalculateStationRating(List<Station> stations) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sortBy = sharedPreferences.getString("pref_type","all");
        if (sortBy.equals("all")) return;

        // Prepare min, max and value to be used for rating:
        double min = Integer.MAX_VALUE;
        double max = 0.0;
        for (Station station : stations) {
            switch (sortBy.toLowerCase()) {
                case "e5":
                    station.setSortValue(station.getE5());
                    break;
                case "e10":
                    station.setSortValue(station.getE10());
                    break;
                case "diesel":
                    station.setSortValue(station.getDiesel());
                    break;
            }
            double val = station.getSortValue();
            min = Math.min(val, min);
            max = Math.max(val, max);
        }

        double dist = max - min;
        double part1 = min + dist * 0.1;
        double part2 = min + dist * 0.3;
        double part3 = min + dist * 0.6;
        for (Station station : stations) {
            double val = station.getSortValue();
            if      (val <= part1) station.setRating(0);
            else if (val <= part2) station.setRating(1);
            else if (val <= part3) station.setRating(2);
            else station.setRating(3);
        }
    }

    /**
     * Shows an error that the data could not be retrieved.
     *
     * @param error The error that occurred while executing the HTTP request.
     */
    @Override
    public void processFailScenario(final VolleyError error) {
        Log.d("Error", String.valueOf(error));
        Handler h = new Handler(this.context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                if (NavigationActivity.isVisible) Toast.makeText(context, context.getResources().getString(R.string.error_fetch_stations), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void possiblyUpdateWidgets(int cityID, List<Station> stations) {
        //search for widgets with same city ID
        int widgetCityID = getWidgetCityID(context);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, Widget.class));

        for (int widgetID : widgetIDs) {
            //check if city ID is same
            if (cityID == widgetCityID) {
                //perform update for the widget

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city = dbHelper.getCityToWatch(cityID);

                Widget.updateView(context, appWidgetManager, views, widgetID, city, stations);
                appWidgetManager.updateAppWidget(widgetID, views);
            }
        }
    }
}
