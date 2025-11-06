package com.example.clientsyncapp.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.clientsyncapp.data.AppDatabase;
import com.example.clientsyncapp.data.LogApp;
import com.example.clientsyncapp.network.ApiService;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SyncLogWorker extends Worker {

    private static final String TAG = "SyncLogWorker";

    public SyncLogWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        List<LogApp> logs = database.logAppDao().getAll();

        if (logs.isEmpty()) {
            return Result.success();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webhook.site/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        try {
            Response<Void> response = apiService.uploadLogs("https://webhook.site/019e0cf0-63bb-4ee3-960d-5390b65dfc46", logs).execute();
            if (response.isSuccessful()) {
                database.logAppDao().deleteAll();
                return Result.success();
            } else {
                Log.e(TAG, "Error syncing logs: " + response.code());
                return Result.retry();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error syncing logs", e);
            return Result.retry();
        }
    }
}
