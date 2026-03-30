package com.thomasmortimer.stalkerware;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

public class MonitoringServiceIDViewModel extends AndroidViewModel {

    public MonitoringServiceIDViewModel(@NonNull Application application) {
        super(application);
    }

    private final MutableLiveData<UUID> workIdLiveData = new MutableLiveData<>();

    public void setWorkId(UUID workId) {
        workIdLiveData.postValue(workId);
    }

    public LiveData<UUID> getWorkIdLiveData() {
        return workIdLiveData;
    }
}