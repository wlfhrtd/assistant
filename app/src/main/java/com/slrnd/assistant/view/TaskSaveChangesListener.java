package com.slrnd.assistant.view;

import android.view.View;

import com.slrnd.assistant.model.Task;

public interface TaskSaveChangesListener {

    void onTaskSaveChanges(View v, Task obj);
}
