package com.slrnd.assistant.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.TaskListItemLayoutBinding;
import com.slrnd.assistant.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> implements TaskCheckedChangeListener, TaskEditClickListener {

    public class TaskListViewHolder extends RecyclerView.ViewHolder {

        private TaskListItemLayoutBinding taskItemLayoutBinding;

        public TaskListViewHolder(TaskListItemLayoutBinding todoItemLayoutBinding) {

            super(todoItemLayoutBinding.getRoot());

            this.taskItemLayoutBinding = todoItemLayoutBinding;
        }
    }

    private ArrayList<Task> tasks;

    private final TaskListFragment.onCheckedChangedListener mListener;

    public TaskListAdapter(ArrayList<Task> tasks, TaskListFragment.onCheckedChangedListener listener) {

        this.tasks = tasks;

        mListener = listener;

    }

    public void updateTaskList(List<Task> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TaskListItemLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.task_list_item_layout, parent, false);

        return new TaskListViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {

        holder.taskItemLayoutBinding.setTask(this.tasks.get(position));
        holder.taskItemLayoutBinding.setListener(this);
        holder.taskItemLayoutBinding.setEditListener(this);
    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    @Override
    public void onCheckChanged(CompoundButton cb, Boolean isChecked, Task obj) {

        if (isChecked) {

            mListener.onCheckedChanged(obj);
        }
    }

    @Override
    public void onEditClick(View v) {

        int id = Integer.parseInt(v.getTag().toString());

        TaskListFragmentDirections.ActionEditTaskFragment action = TaskListFragmentDirections.actionEditTaskFragment(id);
        Navigation.findNavController(v).navigate(action);
    }
}