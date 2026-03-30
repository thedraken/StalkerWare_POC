package com.thomasmortimer.stalkerware.domain;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface MonitorLogDAO {

    @Insert
    void insert(MonitorLog monitorLog);

    //@Query("Select * from T_MONITOR_LOG order by dateTime DESC")
    //List<MonitorLog> getAllLogs();
}
