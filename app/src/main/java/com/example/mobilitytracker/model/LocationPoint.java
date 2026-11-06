package com.example.mobilitytracker.model;

/**
 * 位置点数据模型类 (LocationPoint)
 * 记录传感器采集到的单个 GPS 定位点信息，
 * 包括纬度、经度、海拔、移动速度、时间戳以及当时的交通工具识别状态。
 */
public class LocationPoint {
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private long timestamp;
    private String activityType;

    public String getFormattedActivityName() {
        if (activityType == null) return "Inconnu";
        switch (activityType) {
            case "ON_FOOT":
            case "WALKING":
            case "RUNNING":
                return "À pied";
            case "IN_VEHICLE":
                return "En véhicule";
            case "ON_BICYCLE":
                return "À vélo";
            case "STILL":
                return "Immobile";
            default:
                return "Inconnu";
        }
    }

    public LocationPoint() {
    }

    public LocationPoint(double latitude, double longitude, double altitude, float speed, long timestamp, String activityType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.timestamp = timestamp;
        this.activityType = activityType;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
}