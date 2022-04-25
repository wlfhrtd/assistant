package com.slrnd.assistant.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;

import java.util.List;

public class TaskListViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    public LiveData<List<Task>> taskLD;

    public TaskListViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
        this.taskLD = taskRepository.getTaskLiveData();
    }

    public LiveData<List<Task>> getTaskLiveData() {
        return this.taskLD;
    }

    public void clearTask(Task task) {

        task.setIs_done(1);
        this.taskRepository.update(task);
        // reselect after deletion/hide
        this.taskLD = this.taskRepository.getTaskLiveData();
    }

    public void update(Task task) {
        this.taskRepository.update(task);
    }
}