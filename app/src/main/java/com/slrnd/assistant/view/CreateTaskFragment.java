package com.slrnd.assistant.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentCreateTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.util.TaskWorker;
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateTaskFragment extends Fragment implements
        TaskCreateButtonListener,
        DateClickListener,
        TimeClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskDetailsViewModel viewModel;
    private FragmentCreateTaskBinding binding;

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

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        this.binding.setTask(new Task("", "", 0));
        this.binding.setCreateButtonListener(this);
        // date and time pickers
        this.binding.setListenerDate(this);
        this.binding.setListenerTime(this);
    }

    @Override
    public void onTaskCreateButton(View v) {
        // getting date (check listeners)
        Calendar calendar = Calendar.getInstance();
        calendar.set(this.year, this.month, this.day, this.hour, this.minute);
        Calendar today = Calendar.getInstance();
        // diff for work manager delay
        long diff = (calendar.getTimeInMillis() / 1000L) - (today.getTimeInMillis() / 1000L);
        // saving task date to task obj
        this.binding.getTask().setDate(calendar.getTimeInMillis() / 1000L);
        // loading new task obj to LD; list for no reason w/e
        List<Task> tasks = Arrays.asList(this.binding.getTask());
        this.viewModel.addTask(tasks);

        Toast.makeText(this.getContext(), "Task created", Toast.LENGTH_SHORT).show();
        // immediate notif
        // new NotificationHelper(this.getContext()).createNotification("Task Created", "The new task has been created");

        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .setInputData(new Data.Builder()
                        .putString("title", "Task Created: " + this.binding.getTask().getTitle())
                        .putString("message", "The new task has been created: " + this.binding.getTask().getNote())
                        .build())
                .build();

        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest);

        Navigation.findNavController(v).popBackStack();
    }

    @Override
    public void onDateClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_year = calendar.get(Calendar.YEAR);
        int l_month = calendar.get(Calendar.MONTH);
        int l_day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getActivity(), this, l_year, l_month, l_day).show();
    }

    @Override
    public void onTimeClick(View v) {

        Calendar calendar = Calendar.getInstance();
        int l_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int l_minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), this, l_hour, l_minute, DateFormat.is24HourFormat(getActivity())).show();
    }

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
}