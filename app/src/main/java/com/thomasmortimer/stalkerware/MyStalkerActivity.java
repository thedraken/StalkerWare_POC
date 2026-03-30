package com.thomasmortimer.stalkerware;

import static android.widget.Toast.LENGTH_LONG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class MyStalkerActivity extends AppCompatActivity {

    public static final String APP_LOG_NAME = "Thomas_Mortimer_App";
    public static final int MY_REQUEST_CODE = 31052604;

    private final BroadcastReceiver textViewUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MonitoringService.MONITOR_UPDATE.equals(intent.getAction())) {
                TextView textView = getLogTextView();
                boolean isInternetAvailable = intent.getBooleanExtra(MonitoringService.INTERNET_AVAILABLE, false);
                String androidVersion = intent.getStringExtra(MonitoringService.ANDROID_VERSION);
                String model = intent.getStringExtra(MonitoringService.MODEL);
                String dateTimeAsString = intent.getStringExtra(MonitoringService.CURRENT_TIME);
                boolean screenOn = intent.getBooleanExtra(MonitoringService.SCREEN_ON, false);
                boolean screenOff = intent.getBooleanExtra(MonitoringService.SCREEN_OFF, false);
                boolean deviceBooted = intent.getBooleanExtra(MonitoringService.DEVICE_BOOTED, false);
                String internetType = intent.getStringExtra(MonitoringService.INTERNET_TYPE);
                String deviceManufacturer = intent.getStringExtra(MonitoringService.DEVICE_MANUFACTURER);
                int batteryLevel = intent.getIntExtra(MonitoringService.BATTERY_LEVEL, 0);

                StringBuilder stringBuilder =
                        new StringBuilder("Monitor{\n  Model:").append(model)
                                .append(",\n  Version:").append(androidVersion)
                                .append(",\n  Internet?:").append(isInternetAvailable)
                                .append(",\n  Internet type:").append(internetType)
                                .append(",\n  Device Manu:").append(deviceManufacturer)
                                .append(",\n  Battery:").append(batteryLevel)
                                .append(",\n");
                if (screenOff) {
                    stringBuilder.append("  Screen off:true,\n");
                }
                if (screenOn) {
                    stringBuilder.append("  Screen on:true,\n");
                }
                if (deviceBooted) {
                    stringBuilder.append("  Device booted:true,\n");
                }
                stringBuilder
                        .append("  DateTime:").append(dateTimeAsString).append("\n}\n");
                textView.append(stringBuilder);
            }
        }
    };



    private void configureButtons() {
        Button stalkerButton = findViewById(R.id.startStalkingButton);
        stalkerButton.setOnClickListener(clickListener -> {
            Log.d(APP_LOG_NAME, "Click button to start stalking service");
            Intent intent = new Intent(this, MonitoringService.class);
            ContextCompat.startForegroundService(this, intent);
        });

        Button requestPermissionsButton = findViewById(R.id.requestPermissionsButton);
        requestPermissionsButton.setOnClickListener(clickListener -> {
            Log.d(APP_LOG_NAME, "Checking permissions");
            checkPermissionAccessAndRequest(Permission.values());
        });
    }

    private void checkPermissionAccessAndRequest(Permission[] permissionsToProcess) {
        Set<String> permissionsToRequest = new HashSet<>();
        StringBuilder permissionsAlreadyGranted = new StringBuilder();
        for (Permission permission : permissionsToProcess) {
            if (ContextCompat.checkSelfPermission(this, permission.getPermissionAttribute()) < 0) {
                if (shouldShowRequestPermissionRationale(permission.getPermissionAttribute())) {
                    Toast.makeText(this, "We need it", LENGTH_LONG).show();
                }
                permissionsToRequest.add(permission.getPermissionAttribute());
            } else {
                if (permissionsAlreadyGranted.length() == 0) {
                    permissionsAlreadyGranted.append("The following permissions were already granted:\n");
                }
                permissionsAlreadyGranted.append(permission.getPermissionAttribute()).append("\n");
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            Log.d(APP_LOG_NAME, "Need to request " + permissionsToRequest.size() + " permissions");
            String[] permissionArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissionArray, MY_REQUEST_CODE);
        }
        if (permissionsAlreadyGranted.length() > 0) {
            TextView logTextView = getLogTextView();
            logTextView.append(permissionsAlreadyGranted);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            IntStream.range(0, permissions.length).forEach(i -> {
                TextView logTextView = getLogTextView();
                String permission = permissions[i];
                int result = grantResults[i];
                logTextView.append("Permission for " + permission + " was "
                        + (result == PackageManager.PERMISSION_GRANTED ? " granted" : "denied")
                        + "\n");
            });
        }
    }

    private TextView getLogTextView() {
        return findViewById(R.id.logsTextView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_stalker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IntentFilter filter = new IntentFilter(MonitoringService.MONITOR_UPDATE);
        ContextCompat.registerReceiver(
                this,
                textViewUpdateBroadcastReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        configureButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(textViewUpdateBroadcastReceiver);
    }
}