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
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.room.Room;

import com.thomasmortimer.stalkerware.domain.AppDatabase;
import com.thomasmortimer.stalkerware.domain.MonitorLog;

import org.joda.time.DateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonitoringService extends Service {

    public static final String MONITOR_UPDATE = "MONITOR_UPDATE";
    public static final String CURRENT_TIME = "Current_Time";
    public static final String MODEL = "Model";
    public static final String ANDROID_VERSION = "Android_Version";
    public static final String INTERNET_AVAILABLE = "Internet_Available";
    public static final String DEVICE_BOOTED = "Device_Booted";
    public static final String SCREEN_ON = "Screen_On";
    public static final String SCREEN_OFF = "Screen_Off";
    public static final String INTERNET_TYPE = "Internet_Type";
    public static final String BATTERY_LEVEL = "Battery_Level";
    public static final String DEVICE_MANUFACTURER = "Device_Manufacturer";

    private Handler handler;
    private Runnable runnable;

    private boolean screenTurnedOff = false;
    private boolean screenTurnedOn = false;
    private boolean deviceBooted = false;
    private AppDatabase db;

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


        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "monitorDB"
        ).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MyStalkerActivity.APP_LOG_NAME, "Starting service");
        if (intent != null && SystemEventReceiver.INTENT_RECEIVED.equals(intent.getAction())) {
            String value = intent.getStringExtra(SystemEventReceiver.EXTRA_INFO);
            if (Intent.ACTION_BOOT_COMPLETED.equals(value)) {
                deviceBooted = true;
            } else if (Intent.ACTION_SCREEN_OFF.equals(value)) {
                screenTurnedOff = true;
            } else if (Intent.ACTION_SCREEN_ON.equals(value)) {
                screenTurnedOn = true;
            }
        }
        startForeground(1, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC);

        handler.post(runnable);

        Log.d(MyStalkerActivity.APP_LOG_NAME, "Service started");
        return START_STICKY;
    }

    private void collectData() {
        DateTime currentDateTime = new DateTime();
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE;
        String deviceManufacturer = Build.MANUFACTURER;

        Intent intent = new Intent(MONITOR_UPDATE);
        intent.putExtra(MODEL, model);
        intent.putExtra(ANDROID_VERSION, version);
        intent.putExtra(DEVICE_MANUFACTURER, deviceManufacturer);

        ConnectionType connectionType = isInternetAvailable(this);
        int batteryLevel = getBatteryLevel(this);

        intent.putExtra(INTERNET_AVAILABLE, connectionType != null);
        intent.putExtra(INTERNET_TYPE, connectionType != null ? connectionType.toString() : "");
        intent.putExtra(BATTERY_LEVEL, batteryLevel);

        saveDataToDB(currentDateTime, model, version, deviceManufacturer, connectionType, batteryLevel, deviceBooted, screenTurnedOff, screenTurnedOn);

        if (deviceBooted) {
            deviceBooted = false;
            intent.putExtra(DEVICE_BOOTED, true);
        }
        if (screenTurnedOn) {
            screenTurnedOn = false;
            intent.putExtra(SCREEN_ON, true);
        }
        if (screenTurnedOff) {
            screenTurnedOff = false;
            intent.putExtra(SCREEN_OFF, true);
        }
        intent.putExtra(CURRENT_TIME, currentDateTime.toString("yyyy/MM/dd HH:mm"));

        intent.setPackage(getPackageName());



        sendBroadcast(intent);
    }

    private void saveDataToDB(DateTime currentDateTime, String model, String version,
                              String deviceManufacturer, ConnectionType connectionType,
                              int batteryLevel, boolean deviceBooted, boolean screenTurnedOff,
                              boolean screenTurnedOn) {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            Log.d(MyStalkerActivity.APP_LOG_NAME, "Saving entry to DB");
            executorService.execute(() -> {
                MonitorLog log = new MonitorLog();
                log.model = model;
                log.androidVersion = version;
                log.internetAvailable = connectionType != null;
                log.internetType = connectionType != null ? connectionType.toString() : "";
                log.deviceManufacturer = deviceManufacturer;
                log.batteryLevel = batteryLevel;
                log.deviceBooted = deviceBooted;
                log.screenOff = screenTurnedOff;
                log.screenOn = screenTurnedOn;
                log.dateTime = currentDateTime;
                db.monitorLogDAO().insert(log);
                Log.d(MyStalkerActivity.APP_LOG_NAME, "Entry saved");
            });
        }
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

    private int getBatteryLevel(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private ConnectionType isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);

            if (caps != null) {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return ConnectionType.CELLULAR;
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return ConnectionType.ETHERNET;
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return ConnectionType.WIFI;
                }
            }
        }
        return null;
    }
}