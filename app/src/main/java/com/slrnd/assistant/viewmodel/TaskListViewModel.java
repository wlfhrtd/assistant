package com.slrnd.assistant.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;

import java.util.List;

public class TaskListViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    private LiveData<List<Task>> taskLD;

    private static volatile int DATE; // date in yyyyMMdd format of currently loaded records

    public TaskListViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
        DATE = 0;
    }

    public void fetch(int date) {

        this.taskLD = this.taskRepository.findByDate(date);
        DATE = date;
    }

    public LiveData<List<Task>> getTaskLiveData() {
        return this.taskLD;
    }

    public static int getDATE() {
        return DATE;
    }
}