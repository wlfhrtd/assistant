package com.slrnd.assistant.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.slrnd.assistant.R;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.util.ArrayList;

public class TaskListFragment extends Fragment {

    private TaskListViewModel viewModel;
    private TaskListAdapter taskListAdapter;
    private onCheckedChangedListener listener;

    public TaskListFragment() {
        super();

        this.listener = new onCheckedChangedListener() {
            @Override
            public void onCheckedChanged(Task task) {
                viewModel.clearTask(task);
            }
        };

        this.taskListAdapter= new TaskListAdapter(new ArrayList<Task>(), listener);
    }

    public interface onCheckedChangedListener {

        void onCheckedChanged(Task task);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recTodoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(this.taskListAdapter);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(l -> {
            @NonNull NavDirections action = TaskListFragmentDirections.actionCreateTask();
            Navigation.findNavController(view).navigate(action);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        this.viewModel.taskLD.observe(getViewLifecycleOwner(), list -> {
            this.taskListAdapter.updateTaskList(list);
            TextView txtEmpty = requireView().findViewById(R.id.txtEmpty);
            if (list.isEmpty()) {
                txtEmpty.setVisibility(View.VISIBLE);
            } else {
                txtEmpty.setVisibility(View.GONE);
            }
        });
    }
}