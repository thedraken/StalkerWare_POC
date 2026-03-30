package com.thomasmortimer.stalkerware.domain;

import androidx.room.TypeConverter;

import org.joda.time.DateTime;

public class DateTimeConverters {
    @TypeConverter
    public static Long fromDateTime(DateTime dateTime) {
        return dateTime == null ? null : dateTime.getMillis();
    }

    @TypeConverter
    public static DateTime toDateTime(Long millis) {
        return millis == null ? null : new DateTime(millis);
    }
}
