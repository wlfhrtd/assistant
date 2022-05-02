package com.slrnd.assistant.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentEditTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;

public class EditTaskFragment extends Fragment implements TaskSaveChangesListener{

    private TaskDetailsViewModel viewModel;
    private FragmentEditTaskBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_task, container, false);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        int id = EditTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.viewModel.fetch(id);

        this.binding.setSaveListener(this);

        observeViewModel();
    }

    private void observeViewModel() {

        this.viewModel.todoLD.observe(getViewLifecycleOwner(), task -> binding.setTask(task));
    }

    @Override
    public void onTaskSaveChanges(View v, Task obj) {

        this.viewModel.update(obj);

        Toast.makeText(v.getContext(), "Task Updated", Toast.LENGTH_SHORT).show();
    }
}