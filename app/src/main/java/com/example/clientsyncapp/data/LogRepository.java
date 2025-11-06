package com.example.clientsyncapp.data;

import android.app.Application;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    // Método para insertar un log
    public void insertLog(String errorDescription, String className) {
        executorService.execute(() -> {
            String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String desc = errorDescription != null ? errorDescription : "Error desconocido";
            String clase = className != null ? className : "Clase desconocida";

            LogApp log = new LogApp(fechaHora, desc, clase);

            try {
                logAppDao.insert(log);
                Log.d("ROOM_TEST", "Log insertado: " + desc);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ROOM_TEST", "Error insertando log", e);
            }
        });
    }

    // NUEVO MÉTODO: imprimir todos los logs en Logcat
    public void printAllLogs() {
        executorService.execute(() -> {
            try {
                List<LogApp> logs = logAppDao.getAll();
                for (LogApp log : logs) {
                    Log.d("ROOM_TEST", log.getFechaHora() + " | " + log.getClaseOrigen() + " | " + log.getDescripcionError());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ROOM_TEST", "Error leyendo logs", e);
            }
        });
    }
}
