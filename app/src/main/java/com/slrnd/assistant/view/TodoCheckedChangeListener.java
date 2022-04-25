package com.slrnd.assistant.view;

import android.widget.CompoundButton;

import com.slrnd.assistant.model.Task;

public interface TodoCheckedChangeListener {
    void onCheckChanged(CompoundButton cb, Boolean isChecked, Task obj);
}
