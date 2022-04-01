package org.woheller69.spritpreise.api.tankerkoenig;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

/**
 * This class processes the HTTP requests that are made to the OpenWeatherMap API requesting the
 * current weather for all stored cities.
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
                    Station station = extractor.extractStation(currentItem);
                    // Data were not well-formed, abort
                    if (station == null) {
                        final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                        if (NavigationActivity.isVisible)
                            Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Could retrieve all data, so proceed
                    else {
                        station.setCity_id(cityId);
                        // add it to the database
                        dbHelper.addStation(station);
                        stations.add(station);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Extract","Error, no station found");
        }
        ViewUpdater.updateStations(stations);
        possiblyUpdateWidgets(cityId, stations);
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
        int widgetCityID = Widget.getWidgetCityID(context);

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
