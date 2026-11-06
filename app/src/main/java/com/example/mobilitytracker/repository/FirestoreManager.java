package com.example.mobilitytracker.repository;

import android.util.Log;
import com.example.mobilitytracker.model.Trip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * 云端数据库管理类 (FirestoreManager)
 * 负责与 Google Cloud Firestore 云端数据库进行通信。
 * 采用单例模式设计，提供上传行程数据、按用户 ID 查询历史行程（普通用户模式），
 * 以及检索全平台所有用户出行数据（管理员模式）等异步 API 接口。
 */
public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_TRIPS = "trips";
    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    // 异步回调接口：用于在查询完成后将数据传回主线程
    public interface OnTripsLoadedListener {
        void onLoaded(List<Trip> trips);
        void onError(Exception e);
    }

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    // 单例模式获取实例
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // 上传单条行程数据到 Cloud Firestore
    public void saveTrip(Trip trip, Runnable onSuccess, Runnable onFailure) {
        if (trip == null || trip.getTripId() == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        db.collection(COLLECTION_TRIPS)
                .document(trip.getTripId())
                .set(trip)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trajet enregistré avec succès dans Firestore.");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'enregistrement du trajet dans Firestore", e);
                    if (onFailure != null) onFailure.run();
                });
    }

    // 普通用户模式：查询指定 userId 的历史行程
    public void getUserTrips(String userId, OnTripsLoadedListener listener) {
        db.collection(COLLECTION_TRIPS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> trips = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Trip trip = doc.toObject(Trip.class);
                        trips.add(trip);
                    }
                    if (listener != null) listener.onLoaded(trips);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des trajets de l'utilisateur", e);
                    if (listener != null) listener.onError(e);
                });
    }

    // 管理员模式：查询全平台所有用户的行程
    public void getAllUsersTrips(OnTripsLoadedListener listener) {
        db.collection(COLLECTION_TRIPS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> trips = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Trip trip = doc.toObject(Trip.class);
                        trips.add(trip);
                    }
                    if (listener != null) listener.onLoaded(trips);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération de tous les trajets", e);
                    if (listener != null) listener.onError(e);
                });
    }
}