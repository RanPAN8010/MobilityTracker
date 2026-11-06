package com.example.mobilitytracker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单次行程数据模型类 (Trip)
 * 存储一次完整行程的元数据与计算结果，
 * 包括行程 ID、用户 ID、起止时间、总里程、总时长、
 * 各交通方式耗时比例、主要交通工具及点位列表。
 */
public class Trip {
    private String tripId;
    private String userId;
    private long startTime;
    private long endTime;
    private double totalDistance;
    private long totalDuration;
    private Map<String, Long> modeDurations = new HashMap<>();
    private Map<String, Double> modePercentages = new HashMap<>();
    private String dominantMode;
    private List<LocationPoint> points = new ArrayList<>();

    // 格式化输出距离（m <-> km）
    public String getFormattedDistance() {
        if (totalDistance >= 1000) {
            return String.format(java.util.Locale.FRANCE, "%.2f km", totalDistance / 1000.0);
        } else {
            return String.format(java.util.Locale.FRANCE, "%.0f m", totalDistance);
        }
    }

    // 格式化输出总时长（h / min / s）
    public String getFormattedDuration() {
        long hours = totalDuration / 3600;
        long minutes = (totalDuration % 3600) / 60;
        long seconds = totalDuration % 60;

        if (hours > 0) {
            return String.format(java.util.Locale.FRANCE, "%d h %d min", hours, minutes);
        } else if (minutes > 0) {
            return String.format(java.util.Locale.FRANCE, "%d min %d s", minutes, seconds);
        } else {
            return String.format(java.util.Locale.FRANCE, "%d s", seconds);
        }
    }

    // 定义主导交通方式
    public String getDominantModeFrench() {
        if (dominantMode == null) return "Inconnu";
        switch (dominantMode) {
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

    public Trip() {
    }

    public Trip(String tripId, String userId, long startTime) {
        this.tripId = tripId;
        this.userId = userId;
        this.startTime = startTime;
    }

    public Trip(String tripId, String userId, long startTime,
                long endTime, double totalDistance, long totalDuration,
                Map<String, Long> modeDurations,
                Map<String, Double> modePercentages, String dominantMode,
                List<LocationPoint> points) {
        this.tripId = tripId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.modeDurations = modeDurations;
        this.modePercentages = modePercentages;
        this.dominantMode = dominantMode;
        this.points = points;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Map<String, Long> getModeDurations() {
        return modeDurations;
    }

    public void setModeDurations(Map<String, Long> modeDurations) {
        this.modeDurations = modeDurations;
    }

    public Map<String, Double> getModePercentages() {
        return modePercentages;
    }

    public void setModePercentages(Map<String, Double> modePercentages) {
        this.modePercentages = modePercentages;
    }

    public String getDominantMode() {
        return dominantMode;
    }

    public void setDominantMode(String dominantMode) {
        this.dominantMode = dominantMode;
    }

    public List<LocationPoint> getPoints() {
        return points;
    }

    public void setPoints(List<LocationPoint> points) {
        this.points = points;
    }
}