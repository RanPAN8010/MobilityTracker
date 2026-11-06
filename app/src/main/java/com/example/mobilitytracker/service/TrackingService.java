package com.example.mobilitytracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mobilitytracker.R;
import com.example.mobilitytracker.model.LocationPoint;
import com.example.mobilitytracker.model.Trip;
import com.example.mobilitytracker.repository.FirestoreManager;
import com.example.mobilitytracker.util.TripCalculator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.UUID;

/**
 * 后台前台服务类 (TrackingService)
 * 职责：持续在后台采集 GPS 经纬度，保持通知栏保活，
 * 并执行“静止超过 3 分钟自动切断并结束行程”的切分状态机逻辑。
 */
public class TrackingService extends Service {
    private static final String TAG = "TrackingService";
    public static final String CHANNEL_ID = "mobility_tracker_channel";
    public static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_UPDATE_ACTIVITY = "com.example.mobilitytracker.ACTION_UPDATE_ACTIVITY";
    public static final String EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE";

    // 定义触发“自动切断并结束行程”的静止持续时间阈值
    private static final long STILL_THRESHOLD_MS = 3 * 60 * 1000;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private String currentActivityType = "STILL";

    private Trip currentTrip = null;
    private boolean isTracking = false;

    private final Handler stillHandler = new Handler(Looper.getMainLooper());
    private final Runnable stopTripRunnable = this::finalizeCurrentTrip;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification("Service de suivi activé"));

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_UPDATE_ACTIVITY.equals(intent.getAction())) {
            String newActivity = intent.getStringExtra(EXTRA_ACTIVITY_TYPE);
            if (newActivity != null) {
                handleActivityChange(newActivity);
            }
        }
        return START_STICKY;
    }

    // 处理状态变更与 3 分钟自动切分逻辑
    private void handleActivityChange(String newActivity) {
        Log.d(TAG, "Changement d'activité: " + currentActivityType + " -> " + newActivity);
        this.currentActivityType = newActivity;

        if ("STILL".equals(newActivity)) {
            // 检测到静止：启动 3 分钟倒计时
            Log.d(TAG, "Détection d'immobilité. Démarrage du minuteur de 3 minutes.");
            stillHandler.postDelayed(stopTripRunnable, STILL_THRESHOLD_MS);
        } else {
            // 检测到移动：取消静止倒计时
            stillHandler.removeCallbacks(stopTripRunnable);

            // 如果当前不在追踪中，开启新行程
            if (!isTracking) {
                startNewTrip();
            }
        }
    }

    // 开启新行程
    private void startNewTrip() {
        isTracking = true;
        String tripId = UUID.randomUUID().toString();
        String userId = "user_default"; // 默认用户 ID
        currentTrip = new Trip(tripId, userId, System.currentTimeMillis());
        Log.d(TAG, "Nouveau trajet démarré. ID: " + tripId);
    }

    // 静止满 3 分钟，自动结单切分行程
    private void finalizeCurrentTrip() {
        if (isTracking && currentTrip != null) {
            currentTrip.setEndTime(System.currentTimeMillis());
            Log.d(TAG, "Trajet terminé automatiquement après 3 minutes d'immobilité.");

            // 1. 在本地计算总距离、时长、各交通工具占比与 Dominant Mode
            TripCalculator.calculateTripMetrics(currentTrip);

            // 2. 上传数据到 Cloud Firestore 云端数据库
            FirestoreManager.getInstance().saveTrip(
                    currentTrip,
                    () -> Log.d(TAG, "Trajet envoyé avec succès à Firestore"),
                    () -> Log.e(TAG, "Échec de l'envoi du trajet à Firestore")
            );

            // 3. 重置状态
            isTracking = false;
            currentTrip = null;
        }
    }

    // 配置并启动 GPS 连续定位
    private void initLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || !isTracking || currentTrip == null) return;

                for (android.location.Location loc : locationResult.getLocations()) {
                    LocationPoint point = new LocationPoint(
                            loc.getLatitude(),
                            loc.getLongitude(),
                            loc.getAltitude(),
                            loc.getSpeed(),
                            loc.getTime(),
                            currentActivityType
                    );
                    currentTrip.getPoints().add(point);
                    Log.d(TAG, "Point ajouté: (" + loc.getLatitude() + ", " + loc.getLongitude() + ") Mode: " + currentActivityType);
                }
            }
        };

        try {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur de permission de localisation", e);
        }
    }

    // 构建前台服务常驻通知
    private Notification buildNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Suivi de Mobilité")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service de Suivi de Mobilité",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
        stillHandler.removeCallbacks(stopTripRunnable);
    }
}