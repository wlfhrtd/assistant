package com.slrnd.assistant.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;

import java.util.List;

public class TaskCreateViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    public LiveData<List<Task>> taskLD;

    public TaskCreateViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
        this.taskLD = this.taskRepository.getTaskLiveData();
    }

    public LiveData<List<Task>> getTaskLiveData() {
        return this.taskLD;
    }

}
