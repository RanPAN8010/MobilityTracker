package com.example.mobilitytracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * 活动识别广播接收器 (ActivityRecognitionReceiver)
 * 接收系统的 Activity Recognition API 广播通知，
 * 提取用户当前的运动状态 (ON_FOOT, IN_VEHICLE, STILL等)，
 * 并将状态实时转发给 TrackingService。
 */
public class ActivityRecognitionReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivityReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // 检查广播中是否包含活动识别结果
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result != null) {
                DetectedActivity mostProbable = result.getMostProbableActivity();

                if (mostProbable != null) {
                    String activityType = convertTypeToString(mostProbable.getType());
                    int confidence = mostProbable.getConfidence();
                    Log.d(TAG, "Activité détectée: " + activityType + " (Confiance: " + confidence + "%)");

                    // 将新识别的状态通过 Intent 传递给后台前台服务 TrackingService
                    Intent serviceIntent = new Intent(context, TrackingService.class);
                    serviceIntent.setAction(TrackingService.ACTION_UPDATE_ACTIVITY);
                    serviceIntent.putExtra(TrackingService.EXTRA_ACTIVITY_TYPE, activityType);
                    context.startService(serviceIntent);
                }
            }
        }
    }

    // 将系统整数常量转换为统一的字符串标识
    private String convertTypeToString(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            default:
                return "UNKNOWN";
        }
    }
}