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

    public TaskListViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
    }

    public void fetch(String stringDate) {

        this.taskLD = this.taskRepository.findByDate(stringDate);
    }

    public LiveData<List<Task>> getTaskLiveData() {
        return this.taskLD;
    }

    public void clearTask(Task task) {

        task.setIs_done(1);
        this.taskRepository.update(task);
    }
}