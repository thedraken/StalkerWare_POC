package com.thomasmortimer.stalkerware.domain;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MonitorLog.class}, version = 1)
@TypeConverters({DateTimeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MonitorLogDAO monitorLogDAO();
}
