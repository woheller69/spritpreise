package org.woheller69.spritpreise.api.tankerkoenig;

import android.content.Context;
import android.os.Handler;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.NavigationActivity;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.ui.updater.ViewUpdater;
import org.woheller69.spritpreise.api.IDataExtractor;
import org.woheller69.spritpreise.api.IProcessHttpRequest;

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
        Log.d("Extract",response);
        if (extractor.wasCityFound(response)) {
            try {
                JSONObject json = new JSONObject(response);
                JSONArray list = json.getJSONArray("stations");

                List<Station> stations = new ArrayList<>();

                dbHelper.deleteStationsByCityId(cityId); //start with empty stations list

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

                ViewUpdater.updateStations(stations);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else Log.d("Extract","Error, city not found");
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

}
