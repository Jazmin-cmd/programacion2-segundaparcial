package com.example.clientsyncapp;

import android.app.Application;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.clientsyncapp.workers.SyncLogWorker;

import java.util.concurrent.TimeUnit;

public class ClientSyncApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleSyncLogWorker();
    }

    private void scheduleSyncLogWorker() {
        PeriodicWorkRequest syncLogWorkRequest = new PeriodicWorkRequest.Builder(SyncLogWorker.class, 5, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(syncLogWorkRequest);
    }
}
