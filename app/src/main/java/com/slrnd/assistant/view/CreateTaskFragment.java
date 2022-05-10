package com.slrnd.assistant.view;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.slrnd.assistant.databinding.FragmentCreateTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.util.TaskWorker;
import com.slrnd.assistant.viewmodel.TaskViewModel;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateTaskFragment extends Fragment implements
        TaskCreateButtonListener,
        TimeClickListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskViewModel taskViewModel;
    private FragmentCreateTaskBinding binding;

    private TaskListViewModel taskListViewModel;
    private ArrayList<Task> tasks;

    private int selectedDate = 0; // yyyyMMdd

    private int hour = 0;
    private int minute = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_task, container, false);

        this.tasks = new ArrayList<>();

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // shared viewModel
        this.taskListViewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
        // int date in yyyyMMdd format
        this.selectedDate = TaskListViewModel.getDATE();

        // haven't choose&check time for task yet, that's why 0 for datetime
        this.binding.setTask(new Task("", "", 0, this.selectedDate));

        // submit button
        this.binding.setCreateButtonListener(this);

        // task time field to call timePicker
        this.binding.setListenerTime(this);

        observeViewModel();
    }

    private void observeViewModel() {
        this.taskListViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

            this.updateTaskList(list);
        });
    }

    public void updateTaskList(List<Task> newTasks) {

        this.tasks.clear();
        this.tasks.addAll(newTasks);
    }

    @Override
    public void onTaskCreateButton(View v) {

        Task task = this.binding.getTask();
        // date(yyyyMMdd) from taskListViewModel (should be set to object already), hh:mm from time picker onTimeSet
        int year = task.getYear();
        int month = task.getMonth(); // STORING MONTH AS 1 FOR JANUARY 12 DECEMBER; WITH LEADING ZERO FOR SINGLE DIGIT MONTH VALUES E.G 01 02 03
        int day = task.getDayOfMonth();

        // CALENDAR USES 0 FOR JANUARY 11 DECEMBER
        // EVERY TIME BUILDING CALENDAR FROM TASK OBJECTS SHOULD FIX MONTH LIKE 0 FOR JANUARY 11 DECEMBER INSTEAD OF 1 FOR JANUARY 12 DECEMBER
        // SO JUST SUBTRACT 1 FROM MONTH
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, this.hour, this.minute); // CALENDAR MONTH FIX -1

        long timeToCheck = calendar.getTimeInMillis();
        String stringTimeToCheck = new SimpleDateFormat("HH:mm").format(timeToCheck);

        // duplication check
        if (this.tasks != null) {

            for (int i = 0; i < this.tasks.size(); i++) {
                // getStringTime() returns HH:mm
                if (this.tasks.get(i).getStringTime().equals(stringTimeToCheck)) {

                    Toast.makeText(this.getContext(), "You've already set task for this time. Change time", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        // duplication check success
        task.setDatetimeInMillis(timeToCheck);

        // setup work
        Calendar now = Calendar.getInstance();
        // diff for work manager delay
        long diff = (calendar.getTimeInMillis() / 1000L) - (now.getTimeInMillis() / 1000L);

        String uniqueWorkName = String.valueOf(task.getDatetimeInSeconds());

        scheduleWork(uniqueWorkName, diff);

        Toast.makeText(this.getContext(), "Task created", Toast.LENGTH_SHORT).show();

        // loading new task obj to LD; idk about list but it was about insertAll
        this.taskViewModel.addTask(Collections.singletonList(task));

        Navigation.findNavController(v).popBackStack();
    }

    public void scheduleWork(String uniqueWorkName, long diff) {

        WorkManager workManager = WorkManager.getInstance(requireContext());

        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .setInputData(new Data.Builder()
                        .putString("title", "Task Created: " + this.binding.getTask().getTitle())
                        .putString("message", "The new task has been created: " + this.binding.getTask().getNote())
                        .build())
                .build();

        workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                myWorkRequest);
    }

    @Override
    public void onTimeClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int l_minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), this, l_hour, l_minute, DateFormat.is24HourFormat(getActivity())).show();
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
        // HH:mm
        // 08:05
        txtTime.setText(formatted_hour_string + ':' + formatted_minute_string);

        this.hour = hourOfDay;
        this.minute = minute;
    }
}