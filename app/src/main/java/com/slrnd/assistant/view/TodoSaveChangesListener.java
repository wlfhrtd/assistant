package com.slrnd.assistant.view;

import android.view.View;

import com.slrnd.assistant.model.Task;

public interface TodoSaveChangesListener {
    void onTodoSaveChanges(View v, Task obj);
}
