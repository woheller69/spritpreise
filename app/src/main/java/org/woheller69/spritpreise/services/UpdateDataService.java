package org.woheller69.spritpreise.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import androidx.core.app.JobIntentService;
import android.widget.Toast;

import org.woheller69.spritpreise.BuildConfig;
import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.NavigationActivity;
import org.woheller69.spritpreise.database.CityToWatch;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.database.SQLiteHelper;
import org.woheller69.spritpreise.api.IHttpRequestForStations;
import org.woheller69.spritpreise.api.tankerkoenig.TKHttpRequestForStations;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class provides the functionality to fetch price data for a given city as a background
 * task.
 */

public class UpdateDataService extends JobIntentService {

    public static final String UPDATE_SINGLE_ACTION = "org.woheller69.spritpreise.services.UpdateDataService.UPDATE_SINGLE_ACTION";
    public static final String SKIP_UPDATE_INTERVAL = "skipUpdateInterval";
    private static final long MIN_UPDATE_INTERVAL=20;

    private SQLiteHelper dbHelper;
    private SharedPreferences prefManager;

    /**
     * Constructor.
     */
    public UpdateDataService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = SQLiteHelper.getInstance(getApplicationContext());
        prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (!isOnline(2000)) {
            Handler h = new Handler(getApplicationContext().getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (NavigationActivity.isVisible) Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        if (intent != null) {
            if (UPDATE_SINGLE_ACTION.equals(intent.getAction())) handleUpdateSingle(intent);
        }
    }



    private void handleUpdateSingle(Intent intent) {
        int cityId = intent.getIntExtra("cityId",-1);
        CityToWatch city = dbHelper.getCityToWatch(cityId);
        handleUpdateStationsAction(intent, cityId, city.getLatitude(), city.getLongitude());
    }

    private void handleUpdateStationsAction(Intent intent, int cityId, float lat, float lon) {
        boolean skipUpdateInterval = intent.getBooleanExtra(SKIP_UPDATE_INTERVAL, false);

        long timestamp = 0;
        long systemTime = System.currentTimeMillis() / 1000;
        long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "15")) * 60);

        List<Station> stations = dbHelper.getStationsByCityId(cityId);
        if (stations.size() > 0) {             // check timestamp of stations
            timestamp = stations.get(0).getTimestamp();
        }

        if (skipUpdateInterval) {
            // check timestamp of the current stations
                if ((timestamp+MIN_UPDATE_INTERVAL-systemTime)>0) skipUpdateInterval=false;  //even if skipUpdateInterval is true, never update if less than MIN_UPDATE_INTERVAL s
        }

        // Update if update forced or if a certain time has passed
        if (skipUpdateInterval || timestamp + updateInterval - systemTime <= 0) {


                IHttpRequestForStations stationsRequest = new TKHttpRequestForStations(getApplicationContext());
                stationsRequest.perform(lat, lon, cityId);

        }
    }

    private boolean isOnline(int timeOut) { //https://stackoverflow.com/questions/9570237/android-check-internet-connection
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    URL url = new URL(BuildConfig.BASE_URL);
                    return InetAddress.getByName(url.getHost());
                } catch ( IOException e) {
                    return null;
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
        }
        return inetAddress!=null && !inetAddress.toString().isEmpty();
    }
}
