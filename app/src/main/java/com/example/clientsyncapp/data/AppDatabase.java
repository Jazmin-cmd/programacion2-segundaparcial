package com.example.clientsyncapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LogApp.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LogAppDao logAppDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "logs_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
