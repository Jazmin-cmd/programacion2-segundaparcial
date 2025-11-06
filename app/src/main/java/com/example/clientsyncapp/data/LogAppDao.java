package com.example.clientsyncapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogAppDao {

    @Insert
    void insert(LogApp log);

    @Query("SELECT * FROM logs_app")
    List<LogApp> getAll();

    @Query("DELETE FROM logs_app")
    void deleteAll();
}
