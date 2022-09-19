package org.woheller69.spritpreise.api.tankerkoenig;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.api.IDataExtractor;
import java.util.Date;
import java.util.TimeZone;


public class TKDataExtractor implements IDataExtractor {

    @Override
    public boolean wasCityFound(String data) {
        try {
            JSONObject json = new JSONObject(data);
            return json.has("ok") && (json.getBoolean("ok"));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Station extractStation(String data, Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Station station = new Station();
            station.setTimestamp((long) ((System.currentTimeMillis())/ 1000));

            JSONObject json = new JSONObject(data);

            if (json.has("diesel") && !json.isNull("diesel")) station.setDiesel(json.getDouble("diesel"));
            if (json.has("e5") && !json.isNull("e5")) station.setE5( json.getDouble("e5"));
            if (json.has("e10") && !json.isNull("e10")) station.setE10( json.getDouble("e10"));

            if (json.has("price")) {
                if (!json.isNull("price")) {
                    switch (sharedPreferences.getString("pref_type", "all")) {
                        case "diesel":
                            station.setDiesel(json.getDouble("price"));
                            break;
                        case "e5":
                            station.setE5(json.getDouble("price"));
                            break;
                        case "e10":
                            station.setE10(json.getDouble("price"));
                            break;
                    }
                } else return null;
            }
            station.setOpen(json.getBoolean("isOpen"));
            station.setBrand(json.getString("brand"));
            if (json.getString("brand").equals("")) station.setBrand(json.getString("name"));
            station.setName(json.getString("name"));
            station.setAddress1(json.getString("street")+" "+json.getString("houseNumber"));
            station.setAddress2(formatPostCode(json.getString("postCode")) +" "+json.getString("place"));
            station.setDistance(json.getDouble("dist"));
            station.setLatitude(json.getDouble("lat"));
            station.setLongitude(json.getDouble("lng"));
            station.setUuid(json.getString("id"));

            return station;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatPostCode(String string) {
        // Adds a leading 0 to the postcode string if needed
        return string.length() == 4 ? ("0" + string) : string;
    }
}
