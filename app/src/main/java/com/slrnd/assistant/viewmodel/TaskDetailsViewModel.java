package com.slrnd.assistant.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;

import java.util.List;

public class TaskDetailsViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    public LiveData<Task> todoLD;

    public TaskDetailsViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
        this.todoLD = new MutableLiveData<>();
    }

    public void fetch(int id) {

        this.todoLD = this.taskRepository.findOneById(id);
    }

    public void addTask(List<Task> list) {

        this.taskRepository.insert(list);
    }

    public void update(Task task) {
        this.taskRepository.update(task);
    }
}