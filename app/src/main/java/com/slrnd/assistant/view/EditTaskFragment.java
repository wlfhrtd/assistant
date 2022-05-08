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
import com.slrnd.assistant.viewmodel.TaskViewModel;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EditTaskFragment extends Fragment implements
        TaskSaveChangesListener,
        TimeClickListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskViewModel taskViewModel;
    private FragmentEditTaskBinding binding;

    private TaskListViewModel taskListViewModel;
    private ArrayList<Task> tasks;

    // add originalHM to check if changed and do checks if yes
    private int originalHour = 0;
    private int originalMinute = 0;
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

        this.taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        int id = EditTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.taskViewModel.fetch(id);

        this.taskListViewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        int date = EditTaskFragmentArgs.fromBundle(requireArguments()).getDate();
        this.taskListViewModel.fetch(date);

        this.binding.setSaveListener(this);
        this.binding.setListenerTime(this);

        observeTaskViewModel();

        observeTaskListViewModel();
    }

    private void observeTaskViewModel() {

        this.taskViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), task -> {

            binding.setTask(task);

            TextView txtTime = this.binding.getRoot().findViewById(R.id.txtTime);

            long datetimeInMillis = task.getDatetimeInMillis();

            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            String taskStringTime = df.format(datetimeInMillis);

            txtTime.setText(taskStringTime);

            this.originalHour = Integer.parseInt(new SimpleDateFormat("HH").format(datetimeInMillis));
            this.originalMinute = Integer.parseInt(new SimpleDateFormat("mm").format(datetimeInMillis));
            this.hour = originalHour;
            this.minute = originalMinute;
        });
    }

    private void observeTaskListViewModel() {
        this.taskListViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

            this.updateTaskList(list);
        });
    }

    public void updateTaskList(List<Task> newTasks) {

        this.tasks.clear();
        this.tasks.addAll(newTasks);
    }

    @Override
    public void onTaskSaveChanges(View v, Task obj) {
        // if date unchanged we don't set new datetime and don't cancel work
        if (this.originalHour != this.hour || this.originalMinute != this.minute) {
            // update task datetime before check, obj arg contains old value
            String date = String.valueOf(obj.getDate()); // yyyyMMdd
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4, 6));
            int day = Integer.parseInt(date.substring(6));
            // y,m,d from task obj; h,m from time picker onTimeSet()
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day, this.hour, this.minute); // CALENDAR MONTH FIX -1

            // save old datetime to cancel old work; used datetimeInSeconds as unique work names
            String oldWorkName = String.valueOf(obj.getDatetimeInSeconds());

            // new datetime and duplication check
            obj.setDatetimeInMillis(calendar.getTimeInMillis());
            // duplication check
            if (this.tasks != null) {

                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                String taskStringTime = df.format(obj.getDatetimeInMillis());

                for (int i = 0; i < this.tasks.size(); i++) {

                    if (df.format(this.tasks.get(i).getDatetimeInMillis()).equals(taskStringTime)) {

                        Toast.makeText(this.getContext(), "TASK DUPLICATION", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            // cancel old work if check passed
            WorkManager workManager = WorkManager.getInstance(requireContext());
            workManager.cancelUniqueWork(oldWorkName);
            // set new work
            Calendar now = Calendar.getInstance();
            // diff for work manager delay
            long diff = (calendar.getTimeInMillis() / 1000L) - (now.getTimeInMillis() / 1000L);
            // enqueue new work for updated task
            String uniqueWorkName = String.valueOf(obj.getDatetimeInSeconds());
            scheduleWork(uniqueWorkName, diff);
        }

        this.taskViewModel.update(obj); // only title&note updated thx to 2sided @={} databinding

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