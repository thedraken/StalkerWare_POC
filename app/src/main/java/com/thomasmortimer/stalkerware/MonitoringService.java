package com.thomasmortimer.stalkerware;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {

    public static final String MY_WORKER_ID = "THOMAS_MORTIMER_WORKER";
    public static final String MY_WORKER_TAG = "THOMAS_MORTIMER_BACKGROUND_WORKER_TAG";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(MyStalkerActivity.APP_LOG_NAME, "Creating service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MyStalkerActivity.APP_LOG_NAME,"service starting");
        WorkManager workManager = WorkManager.getInstance(this);
        //Cancel any pending jobs and start again
        workManager.cancelAllWorkByTag(MY_WORKER_TAG);

        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(MyWorker.class)
                        .setInitialDelay(0, TimeUnit.SECONDS)
                        .addTag(MY_WORKER_TAG)
                        .build();

        workManager.enqueue(oneTimeWorkRequest);
        UUID oneTimeWorkRequestId = oneTimeWorkRequest.getId();

        MonitoringServiceIDViewModel viewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(MonitoringServiceIDViewModel.class);
        viewModel.setWorkId(oneTimeWorkRequestId);

        Log.d(MyStalkerActivity.APP_LOG_NAME, "Service started");
        try {
            Log.d(MyStalkerActivity.APP_LOG_NAME, "Currently "
                    + workManager.getWorkInfosByTag(MY_WORKER_TAG).get().size()
                    + " services running with my tag");
        } catch (ExecutionException | InterruptedException e) {
            Log.e(MyStalkerActivity.APP_LOG_NAME,"Error polling jobs", e);
        }
        return START_STICKY;
    }
}
