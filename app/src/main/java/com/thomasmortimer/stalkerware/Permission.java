package com.thomasmortimer.stalkerware;

public enum Permission {
    ACCESS_COARSE_LOCATION("android.permission.ACCESS_COARSE_LOCATION"),
    ACCESS_FINE_LOCATION("android.permission.ACCESS_FINE_LOCATION"),
    READ_PHONE_STATE("android.permission.READ_PHONE_STATE"),
    //No longer allowed on current Android version
    //READ_EXTERNAL_STORAGE("android.permission.READ_EXTERNAL_STORAGE"),
    READ_MEDIA_AUDIO("android.permission.READ_MEDIA_AUDIO"),
    //No longer allowed on current Android version
    //READ_MEDIA_IMAGES("android.permission.READ_MEDIA_IMAGES"),
    //No longer allowed on current Android version
    //READ_MEDIA_VIDEO("android.permission.READ_MEDIA_VIDEO"),
    RECEIVE_BOOT_COMPLETED("android.permission.RECEIVE_BOOT_COMPLETED"),
    INTERNET("android.permission.INTERNET"),
    ACCESS_NETWORK_STATE("android.permission.ACCESS_NETWORK_STATE"),
    READ_MEDIA_VISUAL_USER_SELECTED("android.permission.READ_MEDIA_VISUAL_USER_SELECTED"),
    FOREGROUND_SERVICE_DATA_SYNC("android.permission.FOREGROUND_SERVICE_DATA_SYNC"),
    POST_NOTIFICATIONS("android.permission.POST_NOTIFICATIONS")
    ;

    private final String permissionAttribute;

    Permission(String permissionAttribute) {
        this.permissionAttribute = permissionAttribute;
    }

    public String getPermissionAttribute() {
        return permissionAttribute;
    }
}
