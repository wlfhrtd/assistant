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

    @Query("SELECT * FROM task WHERE is_done = 0")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM task WHERE id = :id")
    LiveData<Task> selectTask(Integer id);

    @Delete
    void deleteTask(Task task);

    @Update
    void update(Task task);

}