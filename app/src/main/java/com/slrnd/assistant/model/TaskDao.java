package com.slrnd.assistant.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Task> tasks);

    @Query("SELECT * FROM task")
    List<Task> selectAllTasks();

    @Query("SELECT * FROM task WHERE is_done = 0 ORDER BY datetime ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM task WHERE id = :id")
    LiveData<Task> findOneById(int id);

    @Query("SELECT * FROM task WHERE date = :date ORDER BY is_done ASC, datetime ASC")
    LiveData<List<Task>> findByDate(int date);

    @Delete
    void delete(Task task);

    @Update
    void update(Task task);
}