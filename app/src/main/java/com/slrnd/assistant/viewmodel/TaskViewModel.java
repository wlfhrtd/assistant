package com.slrnd.assistant.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    private LiveData<Task> taskLD;

    public TaskViewModel(@NonNull Application application) {

        super(application);

        this.taskRepository = new TaskRepository(application);
        this.taskLD = new MutableLiveData<>();
    }

    public void fetch(int id) {

        this.taskLD = this.taskRepository.findOneById(id);
    }

    public void addTask(List<Task> list) {

        this.taskRepository.insert(list);
    }

    public void update(Task task) {

        this.taskRepository.update(task);
    }

    public void finishTask(Task task) {

        task.setIs_done(1);
        this.taskRepository.update(task);
    }

    public void deleteTask(Task task) {

        this.taskRepository.delete(task);
    }

    public LiveData<Task> getTaskLiveData() {

        return this.taskLD;
    }
}