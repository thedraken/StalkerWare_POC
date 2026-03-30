package com.thomasmortimer.stalkerware;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

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

public class MonitoringService extends Service {

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
                handler.postDelayed(this, 30000); // 30 sec
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
        Intent intent = new Intent("MONITOR_UPDATE");
        intent.putExtra("model", model);
        intent.putExtra("version", version);
        sendBroadcast(intent);
    }

    private Notification createNotification() {
        String channelId = "monitor_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);

        return new Notification.Builder(this, channelId)
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
}