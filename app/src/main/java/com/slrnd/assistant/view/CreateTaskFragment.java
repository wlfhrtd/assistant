package com.slrnd.assistant.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentCreateTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.model.TaskRepository;
import com.slrnd.assistant.util.TaskWorker;
import com.slrnd.assistant.viewmodel.TaskCreateViewModel;
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CreateTaskFragment extends Fragment implements
        TaskCreateButtonListener,
        // DateClickListener,
        TimeClickListener,
        // DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskDetailsViewModel viewModel;
    private FragmentCreateTaskBinding binding;

    private TaskCreateViewModel taskCreateViewModel;
    private ArrayList<Task> tasks;

    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int minute = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_create_todo, container, false);

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_task, container, false);

        this.tasks = new ArrayList<Task>();

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        this.taskCreateViewModel = new ViewModelProvider(this).get(TaskCreateViewModel.class);

        this.binding.setTask(new Task("", "", 0));
        this.binding.setCreateButtonListener(this);
        // date and time pickers
        // this.binding.setListenerDate(this);
        this.binding.setListenerTime(this);

        this.year = CreateTaskFragmentArgs.fromBundle(requireArguments()).getSelectedYear();
        this.month = CreateTaskFragmentArgs.fromBundle(requireArguments()).getSelectedMonth();
        this.day = CreateTaskFragmentArgs.fromBundle(requireArguments()).getSelectedDayOfMonth();

        observeViewModel();
    }

    private void observeViewModel() {
        this.taskCreateViewModel.taskLD.observe(getViewLifecycleOwner(), list -> {

            this.updateTaskList(list);
        });
    }

    public void updateTaskList(List<Task> newTasks) {

        this.tasks.clear();
        this.tasks.addAll(newTasks);
    }

    @Override
    public void onTaskCreateButton(View v) {
        // get task obj
        Task task = this.binding.getTask();
        // getting date (check listeners)
        Calendar calendar = Calendar.getInstance();
        calendar.set(this.year, this.month, this.day, this.hour, this.minute);

        task.setString_date(
                String.valueOf(this.year)
                        + '-'
                        + this.month
                        + '-'
                        + this.day
        );
        task.setString_time(
                String.valueOf(this.hour)
                        + '-'
                        + this.minute
        );

        if (this.tasks != null) {
            Log.d("SHIT", "ALLTASKS NOT NULL");
            for (int i = 0; i < this.tasks.size(); i++) {
                if (this.tasks.get(i).getString_date().equals(task.getString_date()) && this.tasks.get(i).getString_time().equals(task.getString_time())) {
                    Log.d(task.getString_date(), "CATCH!");
                    Toast.makeText(this.getContext(), "TASK DUPLICATION", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        Calendar today = Calendar.getInstance();
        // diff for work manager delay
        long diff = (calendar.getTimeInMillis() / 1000L) - (today.getTimeInMillis() / 1000L);

        // saving task date to task obj
        task.setDate(calendar.getTimeInMillis() / 1000L);
        // loading new task obj to LD; list for no reason w/e
        List<Task> tasks = Arrays.asList(task);
        this.viewModel.addTask(tasks);

        // immediate notif
        // new NotificationHelper(this.getContext()).createNotification("Task Created", "The new task has been created");

        // WorkManager.getInstance(requireContext()).enqueue(myWorkRequest); // uniqueness issue

        // checking for existing/duplication; used task.date as uuid
        String uniqueWorkName = String.valueOf(task.getDate());

        if (!isWorkScheduled(uniqueWorkName, getContext())) {

            scheduleWork(uniqueWorkName, diff);

            Toast.makeText(this.getContext(), "Task created", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(uniqueWorkName, "CATCH");
            Toast.makeText(this.getContext(), "TASK DUPLICATION", Toast.LENGTH_LONG).show();
        }

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

    private boolean isWorkScheduled(String uniqueWorkName, Context context) {

        WorkManager workManager = WorkManager.getInstance(context);

        ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosForUniqueWork(uniqueWorkName);

        boolean running = false;
        List<WorkInfo> workInfoList = Collections.emptyList();

        try {
            workInfoList = statuses.get();
        } catch (ExecutionException e) {
            Log.d(uniqueWorkName, "ExecutionException in isWorkScheduled: " + e);
        } catch (InterruptedException e) {
            Log.d(uniqueWorkName, "InterruptedException in isWorkScheduled: " + e);
        }

        for (WorkInfo workInfo : workInfoList) {
            WorkInfo.State state = workInfo.getState();
            running = running || (state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED);
        }

        return running;
    }

    /*
    @Override
    public void onDateClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_year = calendar.get(Calendar.YEAR);
        int l_month = calendar.get(Calendar.MONTH);
        int l_day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getActivity(), this, l_year, l_month, l_day).show();
    }*/

    @Override
    public void onTimeClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int l_minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), this, l_hour, l_minute, DateFormat.is24HourFormat(getActivity())).show();
    }

    /*
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

        Calendar.getInstance().set(year, month, day);

        TextView txtDate = this.binding.getRoot().findViewById(R.id.txtDate);
        // add leading zero to month
        int human_month = month+1; // fix start of January as 0 to start with 1
        String human_month_string = String.valueOf(human_month);
        String formatted_human_month_string = String.format("%2s", human_month_string).replace(' ', '0');
        // add leading zero to day
        String day_string = String.valueOf(day);
        String formatted_day_string = String.format("%2s", day_string).replace(' ', '0');
        // dd - mm - yyyy
        // 01 - 05 - 2099
        txtDate.setText(formatted_day_string + " - " + formatted_human_month_string + " - " + year);

        this.year = year;
        this.month = month;
        this.day = day;
    }*/

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
}