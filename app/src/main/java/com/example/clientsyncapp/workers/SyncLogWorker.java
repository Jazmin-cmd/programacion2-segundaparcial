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
    private static final String WEBHOOK_URL = "https://webhook.site/467cb2cd-50aa-403b-b54f-27a1107d89bd";

    public SyncLogWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        List<LogApp> logs = database.logAppDao().getAll();

        if (logs.isEmpty()) {
            Log.d(TAG, "No hay logs para sincronizar.");
            return Result.success();
        }

        Log.d(TAG, "Iniciando sincronización de " + logs.size() + " logs...");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webhook.site/") // Base URL temporal, webhook recibirá los datos
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        try {
            // Enviar logs al servidor
            Response<Void> response = apiService.uploadLogs(WEBHOOK_URL, logs).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Logs sincronizados correctamente, eliminando registros locales...");

                // Borrar solo si se sincronizó correctamente
                database.logAppDao().deleteAll();

                Log.d(TAG, "Registros locales eliminados.");
                return Result.success();
            } else {
                Log.e(TAG, "Error en la sincronización: " + response.code() + " - " + response.message());
                return Result.retry(); // Reintentar más tarde
            }

        } catch (IOException e) {
            Log.e(TAG, "Fallo en la conexión al sincronizar logs", e);
            return Result.retry(); // Reintentar automáticamente
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado durante la sincronización", e);
            return Result.failure();
        }
    }
}
