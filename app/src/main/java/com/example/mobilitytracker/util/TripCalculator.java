package com.example.mobilitytracker.util;

import android.location.Location;

import com.example.mobilitytracker.model.LocationPoint;
import com.example.mobilitytracker.model.Trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行程指标计算工具类 (TripCalculator)
 * 根据行程采集到的 GPS 原始点位列表，在手机本地计算总距离、总时长、
 * 各交通工具的耗时占比，以及选出主导交通方式 (Dominant Mode)。
 */
public class TripCalculator {

    // 传入包含点位的 Trip 对象进行计算
    public static void calculateTripMetrics(Trip trip) {
        if (trip == null || trip.getPoints() == null || trip.getPoints().size() < 2) {
            return;
        }

        List<LocationPoint> points = trip.getPoints();

        double totalDistance = 0.0;
        Map<String, Long> modeDurations = new HashMap<>();

        //遍历相邻点位，累加球面距离并统计各状态耗时
        for (int i = 0; i < points.size() - 1; i++) {
            LocationPoint p1 = points.get(i);
            LocationPoint p2 = points.get(i + 1);

            // 使用 Android 原生 API（大圆航线算法）计算两点间精确距离（单位：米）
            float[] results = new float[1];
            Location.distanceBetween(
                    p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude(),
                    results
            );
            totalDistance += results[0];

            // 计算相邻两点间的时间间隔（单位：秒）
            long durationSeconds = (p2.getTimestamp() - p1.getTimestamp()) / 1000;
            if (durationSeconds < 0) durationSeconds = 0;

            // 累计当前点的交通工具耗时
            String mode = p1.getActivityType();
            if (mode == null) mode = "UNKNOWN";

            long currentDuration = modeDurations.getOrDefault(mode, 0L);
            modeDurations.put(mode, currentDuration + durationSeconds);
        }

        // 计算行程总时长（以首尾点位时间差为准）
        long startTime = points.get(0).getTimestamp();
        long endTime = points.get(points.size() - 1).getTimestamp();
        long totalDurationSeconds = (endTime - startTime) / 1000;
        if (totalDurationSeconds <= 0) totalDurationSeconds = 1; // 避免除以 0 导致异常

        // 计算各交通工具耗时百分比 (0.0% - 100.0%)
        Map<String, Double> modePercentages = new HashMap<>();
        String dominantMode = "UNKNOWN";
        long maxDuration = -1;

        for (Map.Entry<String, Long> entry : modeDurations.entrySet()) {
            String mode = entry.getKey();
            long duration = entry.getValue();

            double percentage = ((double) duration / totalDurationSeconds) * 100.0;
            modePercentages.put(mode, Math.min(100.0, percentage));

            // 找出耗时最长的交通工具作为 Dominant Mode
            if (duration > maxDuration) {
                maxDuration = duration;
                dominantMode = mode;
            }
        }

        // 将计算出的指标写回 Trip 对象
        trip.setStartTime(startTime);
        trip.setEndTime(endTime);
        trip.setTotalDistance(totalDistance);
        trip.setTotalDuration(totalDurationSeconds);
        trip.setModeDurations(modeDurations);
        trip.setModePercentages(modePercentages);
        trip.setDominantMode(dominantMode);
    }
}