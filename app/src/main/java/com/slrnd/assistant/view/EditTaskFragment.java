package com.slrnd.assistant.view;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentEditTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.util.TaskWorker;
import com.slrnd.assistant.viewmodel.TaskListViewModel;
import com.slrnd.assistant.viewmodel.TaskViewModel;

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

    // to check if task time has been changed and needs check for duplication conflict
    private int originalHour = 0;
    private int originalMinute = 0;
    private int hour = 0;
    private int minute = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_task, container, false);

        this.tasks = new ArrayList<>();

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        // task.id
        int id = EditTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.taskViewModel.fetch(id);
        // shared viewModel
        this.taskListViewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);
        // for "save changes" button
        this.binding.setSaveListener(this);
        // for "pick time" field
        this.binding.setListenerTime(this);

        observeTaskViewModel();

        observeTaskListViewModel();
    }

    private void observeTaskViewModel() {

        this.taskViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), task -> {

            this.binding.setTask(task);

            long datetimeInMillis = task.getDatetimeInMillis();

            this.binding.txtTaskTime.setText(new SimpleDateFormat("HH:mm").format(datetimeInMillis));

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
            // update task datetime before check, obj contains old value
            int year = obj.getYear();
            int month = obj.getMonth();
            int day = obj.getDayOfMonth();
            // h,m from time picker onTimeSet()
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day, this.hour, this.minute); // CALENDAR MONTH FIX -1

            long timeToCheck = calendar.getTimeInMillis();
            String stringTimeToCheck = new SimpleDateFormat("HH:mm").format(timeToCheck);
            // duplication check
            if (this.tasks != null) {

                for (int i = 0; i < this.tasks.size(); i++) {
                    // getStringTime() returns HH:mm
                    if (this.tasks.get(i).getStringTime().equals(stringTimeToCheck)) {

                        Toast.makeText(this.getContext(), R.string.duplicationtask, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            // passed duplication check
            // cancel old work
            WorkManager workManager = WorkManager.getInstance(requireContext());
            workManager.cancelUniqueWork(String.valueOf(obj.getDatetimeInSeconds())); // task still has old datetime
            // save new datetime to task
            obj.setDatetimeInMillis(timeToCheck); // now task has new datetime
            // setup new work
            Calendar now = Calendar.getInstance();
            // diff for work manager delay
            long diff = (calendar.getTimeInMillis() / 1000L) - (now.getTimeInMillis() / 1000L);
            // enqueue new work for updated task
            String uniqueWorkName = String.valueOf(obj.getDatetimeInSeconds());
            scheduleWork(uniqueWorkName, diff);
        }
        // updating task object
        this.taskViewModel.update(obj); // only title&note updated thx to 2sided @={} databinding

        Toast.makeText(v.getContext(), R.string.taskupdated, Toast.LENGTH_SHORT).show();
        // popBack to detailsTaskFragment
        Navigation.findNavController(v).popBackStack();
    }

    public void scheduleWork(String uniqueWorkName, long diff) {

        WorkManager workManager = WorkManager.getInstance(requireContext());

        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .setInputData(new Data.Builder()
                        .putString("title", R.string.taskupdated + ": " + this.binding.getTask().getTitle())
                        .putString("message", R.string.taskupdated + ": " + this.binding.getTask().getNote())
                        .build())
                .build();

        workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                myWorkRequest);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        // add leading zero to hourOfDay
        String hour_string = String.valueOf(hourOfDay);
        String formatted_hour_string = String.format("%2s", hour_string).replace(' ', '0');
        // add leading zero to day
        String minute_string = String.valueOf(minute);
        String formatted_minute_string = String.format("%2s", minute_string).replace(' ', '0');
        // HH:MM
        // 08:05
        this.binding.txtTaskTime.setText(formatted_hour_string + ':' + formatted_minute_string);

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