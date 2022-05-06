package com.slrnd.assistant.view;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentEditTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.util.TaskWorker;
import com.slrnd.assistant.viewmodel.TaskCreateViewModel;
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EditTaskFragment extends Fragment implements
        TaskSaveChangesListener,
        TimeClickListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskDetailsViewModel viewModel;
    private FragmentEditTaskBinding binding;

    private TaskCreateViewModel taskCreateViewModel;
    private ArrayList<Task> tasks;

    // add originalHM to check if changed and do checks if yes
    private int hour = 0;
    private int minute = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_task, container, false);

        this.tasks = new ArrayList<Task>();

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        int id = EditTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.viewModel.fetch(id);

        this.taskCreateViewModel = new ViewModelProvider(this).get(TaskCreateViewModel.class);
        // yyyy-mm-dd
        String stringDate = EditTaskFragmentArgs.fromBundle(requireArguments()).getStringDate();
        this.taskCreateViewModel.fetch(stringDate);

        this.binding.setSaveListener(this);
        this.binding.setListenerTime(this);

        observeViewModel();

        observeTaskCreateViewModel();
    }

    private void observeViewModel() {

        this.viewModel.getTaskLiveData().observe(getViewLifecycleOwner(), task -> {

            binding.setTask(task);

            TextView txtTime = this.binding.getRoot().findViewById(R.id.txtTime);
            txtTime.setText(task.getString_time());
        });
    }

    private void observeTaskCreateViewModel() {
        this.taskCreateViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

            this.updateTaskList(list);
        });
    }

    public void updateTaskList(List<Task> newTasks) {

        this.tasks.clear();
        this.tasks.addAll(newTasks);
    }

    @Override
    public void onTaskSaveChanges(View v, Task obj) {

        Date date = new Date(obj.getDate());
        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        // y,m,d from task obj; h,m from time picker onTimeSet()
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, this.hour, this.minute);

        obj.setString_time(
                String.valueOf(this.hour)
                        + '-'
                        + this.minute
        );

        // duplication check
        if (this.tasks != null) {
            Log.d("OK", "ALLTASKS NOT NULL");
            for (int i = 0; i < this.tasks.size(); i++) {
                if (this.tasks.get(i).getString_time().equals(obj.getString_time())) {
                    Log.d(obj.getString_date(), "CATCH!");
                    Toast.makeText(this.getContext(), "TASK DUPLICATION", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        // cancel old work
        WorkManager workManager = WorkManager.getInstance(requireContext());
        workManager.cancelUniqueWork(String.valueOf(obj.getDate()));

        // saving new date
        obj.setDate(calendar.getTimeInMillis() / 1000L);

        Calendar today = Calendar.getInstance();
        // diff for work manager delay
        long diff = (calendar.getTimeInMillis() / 1000L) - (today.getTimeInMillis() / 1000L);
        // enqueue new work for updated task
        String uniqueWorkName = String.valueOf(obj.getDate());
        scheduleWork(uniqueWorkName, diff);

        this.viewModel.update(obj); // only title&note updated thx to 2sided @={} databinding

        Toast.makeText(v.getContext(), "Task Updated", Toast.LENGTH_SHORT).show();

        Navigation.findNavController(v).popBackStack();
    }

    public void scheduleWork(String uniqueWorkName, long diff) {

        WorkManager workManager = WorkManager.getInstance(requireContext());

        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .setInputData(new Data.Builder()
                        .putString("title", "Edited task: " + this.binding.getTask().getTitle())
                        .putString("message", "The task has been updated: " + this.binding.getTask().getNote())
                        .build())
                .build();

        workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                myWorkRequest);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

        TextView txtTime = this.binding.getRoot().findViewById(R.id.txtTime);
        // add leading zero to hourOfDay
        String hour_string = String.valueOf(hourOfDay);
        String formatted_hour_string = String.format("%2s", hour_string).replace(' ', '0');
        // add leading zero to day
        String minute_string = String.valueOf(minute);
        String formatted_minute_string = String.format("%2s", minute_string).replace(' ', '0');
        // HH:MM
        // 08:05
        txtTime.setText(formatted_hour_string + ':' + formatted_minute_string);

        this.hour = hourOfDay;
        this.minute = minute;
    }

    @Override
    public void onTimeClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int l_minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), this, l_hour, l_minute, DateFormat.is24HourFormat(getActivity())).show();
    }
}