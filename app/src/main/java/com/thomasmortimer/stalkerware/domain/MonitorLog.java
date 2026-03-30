package com.thomasmortimer.stalkerware.domain;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.joda.time.DateTime;

@Entity(tableName = "T_MONITOR_LOG")
public class MonitorLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public DateTime dateTime;
    public String model;
    public String androidVersion;
    public boolean internetAvailable;
    public boolean deviceBooted;
    public boolean screenOn;
    public boolean screenOff;
    public String internetType;
    public int batteryLevel;
    public String deviceManufacturer;

    /*
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
     */
}
