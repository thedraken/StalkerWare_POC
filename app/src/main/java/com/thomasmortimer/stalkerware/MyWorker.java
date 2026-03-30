package com.thomasmortimer.stalkerware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.joda.time.DateTime;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

public class MyWorker extends Worker {

    public static final String CURRENT_TIME = "Current_Time";
    public static final String MODEL = "Model";
    public static final String ANDROID_VERSION = "Android_Version";
    public static final String INTERNET_AVAILABLE = "Internet_Available";

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public MyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        DateTime currentTime = new DateTime();
        String model = Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;
        boolean internetAvailable = isInternetAvailable(getApplicationContext());

        //Due to Android restricting workers, re-run the next one as a oneTimeWorkRequest
        scheduleNextRun();

        return getResults(currentTime, model, androidVersion, internetAvailable);
    }

    private void scheduleNextRun() {
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(MyWorker.class)
                        .setInitialDelay(30, TimeUnit.SECONDS)
                        .addTag(MonitoringService.MY_WORKER_TAG)
                        .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueue(oneTimeWorkRequest);
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

    private Result getResults(DateTime currentTime, String model, String androidVersion, boolean internetAvailable) {
        Data outputData = new Data.Builder()
                .putString(CURRENT_TIME, currentTime.toString())
                .putString(MODEL, model)
                .putString(ANDROID_VERSION, androidVersion)
                .putBoolean(INTERNET_AVAILABLE, internetAvailable)
                .build();
        return Result.success(outputData);
    }
}
