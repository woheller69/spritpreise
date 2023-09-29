package org.woheller69.spritpreise.database;

/**
 * This class is the database model for the stations.
 */
public class Station {

    private int id;
    private int city_id;
    private long timestamp;
    private double diesel;
    private double e5;
    private double e10;
    private boolean isOpen;
    private String brand;
    private String name;
    private String address1;
    private String address2;
    private double distance;
    private double latitude;
    private double longitude;
    private String uuid;
    private int rating;
    private double sortValue;


    public Station() {
    }

    public Station(int id, int city_id, long timestamp, double diesel, double e5, double e10, boolean isOpen, String brand, String name, String address1, String address2, double distance, double latitude, double longitude, String uuid, int rating) {
        this.id = id;
        this.city_id = city_id;
        this.timestamp = timestamp;
        this.diesel = diesel;
        this.e5 = e5;
        this.e10 = e10;
        this.isOpen = isOpen;
        this.brand = brand;
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uuid = uuid;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }

    public double getE5() {
        return e5;
    }

    public void setE5(double e5) {
        this.e5 = e5;
    }

    public double getE10() {
        return e10;
    }

    public void setE10(double e10) {
        this.e10 = e10;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public double getSortValue() {
        return sortValue;
    }

    public void setSortValue(double sortValue) {
        this.sortValue = sortValue;
    }
}
