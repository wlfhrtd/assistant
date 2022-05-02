package com.slrnd.assistant.view;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;

import java.util.Calendar;
import java.util.Date;

public class EditTaskFragment extends Fragment implements
        TaskSaveChangesListener,
        TimeClickListener,
        TimePickerDialog.OnTimeSetListener

{

    private TaskDetailsViewModel viewModel;
    private FragmentEditTaskBinding binding;

    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int minute = 0;

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
        this.binding.setListenerTime(this);

        observeViewModel();
    }

    private void observeViewModel() {

        this.viewModel.todoLD.observe(getViewLifecycleOwner(), task -> {

            binding.setTask(task);

            TextView txtTime = this.binding.getRoot().findViewById(R.id.txtTime);
            txtTime.setText(task.getString_time());

            /*
            Date date = new Date(task.getDate());
            this.year = date.getYear();
            this.month = date.getMonth();
            this.day = date.getDay();
            this.hour = date.getHours();
            this.minute = date.getMinutes();*/
        });
    }

    @Override
    public void onTaskSaveChanges(View v, Task obj) {
        // TODO work manager!!!
        // WorkManager workManager = WorkManager.getInstance(requireContext());
        // workManager.cancelUniqueWork(String.valueOf(obj.getDate()));

        // implement time setting logic as in Create, get working db update action; then mess with workmanager
        Calendar calendar = Calendar.getInstance();
        calendar.set(this.year, this.month, this.day, this.hour, this.minute);

        obj.setString_time(
                String.valueOf(this.hour)
                        + '-'
                        + this.minute
        );

        this.viewModel.update(obj); // only title&note updated thx to 2sided @={} databinding

        Toast.makeText(v.getContext(), "Task Updated", Toast.LENGTH_SHORT).show();
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