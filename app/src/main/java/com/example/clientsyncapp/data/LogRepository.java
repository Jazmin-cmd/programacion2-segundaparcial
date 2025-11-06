package com.example.clientsyncapp.data;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogRepository {

    private final LogAppDao logAppDao;
    private final ExecutorService executorService;

    public LogRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        logAppDao = db.logAppDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertLog(String errorDescription, String className) {
        executorService.execute(() -> {
            LogApp log = new LogApp();
            log.setFechaHora(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            log.setDescripcionError(errorDescription);
            log.setClaseOrigen(className);
            logAppDao.insert(log);
        });
    }
}
