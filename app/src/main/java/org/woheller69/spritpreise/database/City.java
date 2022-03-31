package org.woheller69.spritpreise.database;


/**
 * Created by yonjuni on 04.01.17.
 * data object for city
 * <p>
 * Structure taken from the old orm package from previous versions of this app.
 */

public class City {

    private int cityId;
    private String cityName;
    private String countryCode;
    private float lon;
    private float lat;

    public City() {
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setLatitude(float latitude) {
        lat = latitude;
    }

    public float getLatitude() {
        return lat;
    }

    public float getLongitude() {
        return lon;
    }

    public void setLongitude(float lon) {
        this.lon = lon;
    }
}
