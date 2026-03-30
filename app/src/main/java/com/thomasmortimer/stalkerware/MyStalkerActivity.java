package com.thomasmortimer.stalkerware;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class MyStalkerActivity extends AppCompatActivity {

    public static final String APP_LOG_NAME = "Thomas_Mortimer_App";
    public static final int MY_REQUEST_CODE = 31052604;

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

        addTextViewListener();
        configureButtons();
    }

    private void addTextViewListener() {
        TextView logTextView = getLogTextView();

        MonitoringServiceIDViewModel viewModel = new ViewModelProvider(this).get(MonitoringServiceIDViewModel.class);
        viewModel.getWorkIdLiveData().observe(this, workId -> {
            if (workId != null) {
                try {
                    WorkInfo workInfo = WorkManager.getInstance(this)
                            .getWorkInfoById(workId).get();
                    if (workInfo == null) {
                        Log.d(APP_LOG_NAME, "Work info null");
                    } else {
                        Log.d(APP_LOG_NAME, "WorkInfo state: " + workInfo.getState());
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data data = workInfo.getOutputData();
                            logTextView.append(String.format("%s: %s\n", MyWorker.CURRENT_TIME, data.getString(MyWorker.CURRENT_TIME)));
                            logTextView.append(String.format("%s: %s\n", MyWorker.MODEL, data.getString(MyWorker.MODEL)));
                            logTextView.append(String.format("%s: %s\n", MyWorker.ANDROID_VERSION, data.getString(MyWorker.ANDROID_VERSION)));
                            logTextView.append(String.format("%s: %s\n", MyWorker.INTERNET_AVAILABLE, data.getString(MyWorker.INTERNET_AVAILABLE)));
                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(APP_LOG_NAME, "Error getting work info for job", e);
                }
            }
        });

    }

    private void configureButtons() {
        Button stalkerButton = findViewById(R.id.startStalkingButton);
        stalkerButton.setOnClickListener(clickListener -> {
            //if (checkPermissionAccessAndRequest(new Permission[]{Permission.FOREGROUND_SERVICE_DATA_SYNC})) {
                Log.d(APP_LOG_NAME, "Starting stalking service");
                Intent serviceIntent = new Intent(MyStalkerActivity.this, MonitoringService.class);
                startService(serviceIntent);
                Log.d(APP_LOG_NAME, "Service started");
            /*} else {
                Log.d(APP_LOG_NAME, "Missing data sync permission, try again later");
                getLogTextView().append("Missing data sync permission, try again later\n");

            }*/
        });

        Button requestPermissionsButton = findViewById(R.id.requestPermissionsButton);
        requestPermissionsButton.setOnClickListener(clickListener -> {
            Log.d(APP_LOG_NAME, "Checking permissions");
            checkPermissionAccessAndRequest(Permission.values());
        });

        Button checkNextRunTimeButton = findViewById(R.id.checkWorkersInfoButton);
        checkNextRunTimeButton.setOnClickListener(clickListener-> {
            try {
                List<WorkInfo> workInfos = WorkManager.getInstance(this).getWorkInfosByTag(MonitoringService.MY_WORKER_TAG).get();
                workInfos.forEach(workInfo -> {
                    WorkInfo.State state = workInfo.getState();
                    if (state == WorkInfo.State.ENQUEUED) {
                        long nextScheduled = workInfo.getNextScheduleTimeMillis();
                        long nextScheduledInSeconds = nextScheduled * 1000;
                        TextView logTextView = getLogTextView();
                        logTextView.append("Next run of job is in " + nextScheduledInSeconds + " seconds\n");
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error getting workers", LENGTH_SHORT).show();
                Log.e(APP_LOG_NAME, "Error getting workers", e);
            }
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
}