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

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> implements TaskDetailsClickListener {

    public class TaskListViewHolder extends RecyclerView.ViewHolder {

        private TaskListItemLayoutBinding taskItemLayoutBinding;

        public TaskListViewHolder(TaskListItemLayoutBinding todoItemLayoutBinding) {

            super(todoItemLayoutBinding.getRoot());

            this.taskItemLayoutBinding = todoItemLayoutBinding;
        }
    }

    private ArrayList<Task> tasks;

    // private final TaskListFragment.onCheckedChangedListener mListener;

    public TaskListAdapter(ArrayList<Task> tasks) { // TaskListFragment.onCheckedChangedListener listener) {

        this.tasks = tasks;

        // mListener = listener;

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

        Task task = this.tasks.get(position);
        holder.taskItemLayoutBinding.setTask(task);
        if (task.getIs_done() == 0) {
            holder.taskItemLayoutBinding.imgIcon.setImageResource(R.drawable.ic_baseline_close_24);
        }
        if (task.getIs_done() == 1) {
            holder.taskItemLayoutBinding.imgIcon.setImageResource(R.drawable.ic_baseline_done_24);
        }
        // holder.taskItemLayoutBinding.setListener(this);
        holder.taskItemLayoutBinding.setDetailsListener(this);

    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    /*
    @Override
    public void onCheckChanged(CompoundButton cb, Boolean isChecked, Task obj) {

        if (isChecked) {

            mListener.onCheckedChanged(obj);
        }
    }*/

    @Override
    public void onDetailsClick(View v) {

        int id = Integer.parseInt(v.getTag().toString());

        TaskListFragmentDirections.ActionDetailsTaskFragment action = TaskListFragmentDirections.actionDetailsTaskFragment(id);
        Navigation.findNavController(v).navigate(action);
    }
}