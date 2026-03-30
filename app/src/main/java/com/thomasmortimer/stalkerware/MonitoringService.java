package com.thomasmortimer.stalkerware;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.joda.time.DateTime;

public class MonitoringService extends Service {

    public static final String MONITOR_UPDATE = "MONITOR_UPDATE";
    public static final String CURRENT_TIME = "Current_Time";
    public static final String MODEL = "Model";
    public static final String ANDROID_VERSION = "Android_Version";
    public static final String INTERNET_AVAILABLE = "Internet_Available";
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                collectData();
                handler.postDelayed(this, 30 * 1000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC);

        handler.post(runnable);

        return START_STICKY;
    }

    private void collectData() {
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE;
        Log.d(MyStalkerActivity.APP_LOG_NAME, "Model: " + model);
        Intent intent = new Intent(MONITOR_UPDATE);
        intent.putExtra(MODEL, model);
        intent.putExtra(ANDROID_VERSION, version);
        intent.putExtra(INTERNET_AVAILABLE, isInternetAvailable(this));
        intent.putExtra(CURRENT_TIME, new DateTime());

        sendBroadcast(intent);
    }

    private Notification createNotification() {

        String monitorChannelId = "monitor_channel";
        NotificationChannel channel = new NotificationChannel(
                monitorChannelId,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);

        return new Notification.Builder(this, monitorChannelId)
                .setContentTitle("Monitoring active")
                .setContentText("Collecting device data")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);

            return caps != null && (
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        }
        return false;
    }
}