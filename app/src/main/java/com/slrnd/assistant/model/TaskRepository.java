package com.slrnd.assistant.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskRepository {

    private TaskDao taskDao;

    public TaskRepository(Application application) {

        TaskDatabase db = TaskDatabase.getDatabase(application);

        this.taskDao = db.taskDao();
    }

    public void update(Task task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.update(task);
        });
    }

    public void insert(List<Task> task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.insertAll(task);
        });
    }

    public LiveData<Task> findOneById(int id) {

        return taskDao.findOneById(id);
    }

    public void delete(Task task) {
        TaskDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.delete(task);
        });
    }

    public LiveData<List<Task>> findByDate(int date) {

        return taskDao.findByDate(date);
    }
}