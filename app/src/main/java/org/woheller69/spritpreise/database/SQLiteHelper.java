package org.woheller69.spritpreise.database;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static SQLiteHelper instance = null;

    private static final String DATABASE_NAME = "Spritpreise.db";

    //Names of tables in the database
    private static final String TABLE_CITIES_TO_WATCH = "CITIES_TO_WATCH";
    private static final String TABLE_STATIONS = "STATIONS";

    //Names of columns in TABLE_CITIES_TO_WATCH
    private static final String CITIES_TO_WATCH_ID = "cities_to_watch_id";
    private static final String CITIES_TO_WATCH_CITY_ID = "city_id";
    private static final String CITIES_TO_WATCH_COLUMN_RANK = "rank";
    private static final String CITIES_TO_WATCH_NAME = "city_name";
    private static final String CITIES_TO_WATCH_LONGITUDE = "longitude";
    private static final String CITIES_TO_WATCH_LATITUDE = "latitude";

    //Names of columns in TABLE_STATIONS
    private static final String STATION_ID = "station_id";
    private static final String STATION_CITY_ID = "city_id";
    private static final String STATION_TIMESTAMP = "timestamp";
    private static final String STATION_DIESEL = "diesel";
    private static final String STATION_E5 = "e5";
    private static final String STATION_E10 = "e10";
    private static final String STATION_ISOPEN = "is_open";
    private static final String STATION_BRAND = "brand";
    private static final String STATION_NAME = "name";
    private static final String STATION_ADDRESS1 = "address1";
    private static final String STATION_ADDRESS2 = "address2";
    private static final String STATION_DISTANCE = "distance";
    private static final String STATION_LATITUDE = "latitude";
    private static final String STATION_LONGITUDE = "longitude";
    private static final String STATION_UUID = "uuid";

    /**
     * Create Table statements for all tables
     */
    private static final String CREATE_TABLE_STATIONS = "CREATE TABLE " + TABLE_STATIONS +
            "(" +
            STATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            STATION_CITY_ID + " INTEGER," +
            STATION_TIMESTAMP + " LONG NOT NULL," +
            STATION_DIESEL + " REAL," +
            STATION_E5 + " REAL," +
            STATION_E10 + " REAL," +
            STATION_ISOPEN + " BIT," +
            STATION_BRAND + " VARCHAR(200) NOT NULL," +
            STATION_NAME + " VARCHAR(200) NOT NULL," +
            STATION_ADDRESS1 + " VARCHAR(200) NOT NULL," +
            STATION_ADDRESS2 + " VARCHAR(200) NOT NULL," +
            STATION_DISTANCE + " REAL," +
            STATION_LATITUDE + " REAL," +
            STATION_LONGITUDE + " REAL," +
            STATION_UUID + " VARCHAR(200) NOT NULL ); ";

    private static final String CREATE_TABLE_CITIES_TO_WATCH = "CREATE TABLE " + TABLE_CITIES_TO_WATCH +
            "(" +
            CITIES_TO_WATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CITIES_TO_WATCH_CITY_ID + " INTEGER," +
            CITIES_TO_WATCH_COLUMN_RANK + " INTEGER," +
            CITIES_TO_WATCH_NAME + " VARCHAR(100) NOT NULL," +
            CITIES_TO_WATCH_LONGITUDE + " REAL NOT NULL," +
            CITIES_TO_WATCH_LATITUDE + " REAL NOT NULL ); ";

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CITIES_TO_WATCH);
        db.execSQL(CREATE_TABLE_STATIONS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Methods for TABLE_CITIES_TO_WATCH
     */
    public synchronized long addCityToWatch(CityToWatch city) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, city.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, city.getRank());
        values.put(CITIES_TO_WATCH_NAME,city.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,city.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,city.getLongitude());

        long id=database.insert(TABLE_CITIES_TO_WATCH, null, values);

        //use id also instead of city id as unique identifier
        values.put(CITIES_TO_WATCH_CITY_ID,id);
        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(id)});

        database.close();
        return id;
    }

    public synchronized CityToWatch getCityToWatch(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        String[] arguments = {String.valueOf(id)};

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH +
                        " WHERE " + CITIES_TO_WATCH_CITY_ID + " = ?", arguments);

        CityToWatch cityToWatch = new CityToWatch();

        if (cursor != null && cursor.moveToFirst()) {
            cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
            cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
            cityToWatch.setCityName(cursor.getString(2));
            cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
            cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
            cityToWatch.setRank(Integer.parseInt(cursor.getString(5)));

            cursor.close();
        }

        return cityToWatch;

    }


    public synchronized List<CityToWatch> getAllCitiesToWatch() {
        List<CityToWatch> cityToWatchList = new ArrayList<>();

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH
                , new String[]{});

        CityToWatch cityToWatch;

        if (cursor.moveToFirst()) {
            do {
                cityToWatch = new CityToWatch();
                cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
                cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
                cityToWatch.setCityName(cursor.getString(2));
                cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
                cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
                cityToWatch.setRank(Integer.parseInt(cursor.getString(5)));

                cityToWatchList.add(cityToWatch);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return cityToWatchList;
    }

    public synchronized void updateCityToWatch(CityToWatch cityToWatch) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, cityToWatch.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, cityToWatch.getRank());
        values.put(CITIES_TO_WATCH_NAME,cityToWatch.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,cityToWatch.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,cityToWatch.getLongitude());

        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(cityToWatch.getId())});
    }

    public void deleteCityToWatch(CityToWatch cityToWatch) {

        //First delete all weather data for city which is deleted
        deleteStationsByCityId(cityToWatch.getCityId());

        //Now remove city from CITIES_TO_WATCH
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_CITIES_TO_WATCH, CITIES_TO_WATCH_ID + " = ?",
                new String[]{Integer.toString(cityToWatch.getId())});
        database.close();
    }

    public int getWatchedCitiesCount() {
        SQLiteDatabase database = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(database, TABLE_CITIES_TO_WATCH);
        database.close();
        return (int) count;
    }

    public int getMaxRank() {
        List<CityToWatch> cities = getAllCitiesToWatch();
        int maxRank = 0;
        for (CityToWatch ctw : cities) {
            if (ctw.getRank() > maxRank) maxRank = ctw.getRank();
        }
        return maxRank;
    }


    /**
     * Methods for TABLE_FORECAST
     */
    public synchronized void addStation(Station station) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(STATION_CITY_ID, station.getCity_id());
        values.put(STATION_TIMESTAMP, station.getTimestamp());
        values.put(STATION_DIESEL, station.getDiesel());
        values.put(STATION_E5, station.getE5());
        values.put(STATION_E10, station.getE10());
        values.put(STATION_ISOPEN, station.isOpen());
        values.put(STATION_BRAND, station.getBrand());
        values.put(STATION_NAME, station.getName());
        values.put(STATION_ADDRESS1, station.getAddress1());
        values.put(STATION_ADDRESS2, station.getAddress2());
        values.put(STATION_DISTANCE, station.getDistance());
        values.put(STATION_LATITUDE, station.getLatitude());
        values.put(STATION_LONGITUDE, station.getLongitude());
        values.put(STATION_UUID, station.getUuid());
        database.insert(TABLE_STATIONS, null, values);
        database.close();
    }

    public synchronized void deleteStationsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_STATIONS, STATION_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }

    public synchronized List<Station> getStationsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_STATIONS,
                new String[]{STATION_ID,
                        STATION_CITY_ID,
                        STATION_TIMESTAMP,
                        STATION_DIESEL,
                        STATION_E5,
                        STATION_E10,
                        STATION_ISOPEN,
                        STATION_BRAND,
                        STATION_NAME,
                        STATION_ADDRESS1,
                        STATION_ADDRESS2,
                        STATION_DISTANCE,
                        STATION_LATITUDE,
                        STATION_LONGITUDE,
                        STATION_UUID}
                , STATION_CITY_ID + "=?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        List<Station> list = new ArrayList<>();
        Station station;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                station = new Station();
                station.setId(Integer.parseInt(cursor.getString(0)));
                station.setCity_id(Integer.parseInt(cursor.getString(1)));
                station.setTimestamp(Long.parseLong(cursor.getString(2)));
                station.setDiesel(Double.parseDouble(cursor.getString(3)));
                station.setE5(Double.parseDouble(cursor.getString(4)));
                station.setE10(Double.parseDouble(cursor.getString(5)));
                station.setOpen(Boolean.parseBoolean(cursor.getString(6)));
                station.setBrand(cursor.getString(7));
                station.setName(cursor.getString(8));
                station.setAddress1(cursor.getString(9));
                station.setAddress2(cursor.getString(10));
                station.setDistance(Double.parseDouble(cursor.getString(11)));
                station.setLatitude(Double.parseDouble(cursor.getString(12)));
                station.setLongitude(Double.parseDouble(cursor.getString(13)));
                station.setUuid(cursor.getString(14));
                list.add(station);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return list;
    }

}
