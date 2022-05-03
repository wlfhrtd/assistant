package com.slrnd.assistant.view;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.slrnd.assistant.R;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TaskListFragment extends Fragment {

    private TaskListViewModel viewModel;
    private TaskListAdapter taskListAdapter;
    // private onCheckedChangedListener listener;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDayOfMonth;

    public TaskListFragment() {

        super();

        /*
        this.listener = new onCheckedChangedListener() {
            @Override
            public void onCheckedChanged(Task task) {
                viewModel.clearTask(task);
            }
        };*/

        this.taskListAdapter= new TaskListAdapter(new ArrayList<Task>()); // , listener);
    }

    /*
    public interface onCheckedChangedListener {

        void onCheckedChanged(Task task);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);
        /*
    task.setString_date(
                String.valueOf(this.year)
                        + '-'
                        + this.month
                        + '-'
                        + this.day
        );
     */
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String stringDate = String.valueOf(year) + '-' + month + '-' + day;
        this.viewModel.fetch(stringDate);

        Date TODAY = new Date(year - 1900, month, day);
        this.selectedYear = year;  // TODO update date setting with default fragment behavior (gonna change popBackStack() behavior with returning to chosen date)
        this.selectedMonth = month;
        this.selectedDayOfMonth = day;

        TextView txtBottomSheetDay = view.findViewById(R.id.txtBottomSheetDay);
        txtBottomSheetDay.setText(
                String.valueOf(android.text.format.DateFormat.format("EEEE", calendar.getTime()))
                + ' '
                + day
                + '-'
                + android.text.format.DateFormat.format("MMMM", calendar.getTime())
                + '-'
                + year
        );

        RecyclerView recyclerView = view.findViewById(R.id.recTodoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(this.taskListAdapter);

        // bottom sheet dialog for calendar view
        LinearLayout bottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        ImageView headerArrowImage = view.findViewById(R.id.bottomSheetArrow);
        headerArrowImage.setOnClickListener(view1 -> {

            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                headerArrowImage.setRotation(slideOffset * -180);
            }
        });

        ImageView bottomSheetAdd = view.findViewById(R.id.bottomSheetAdd);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                String stringDate = String.valueOf(year) + '-' + month + '-' + dayOfMonth;
                viewModel.fetch(stringDate);
                observeViewModel();

                Date selectedDate = new Date(year - 1900, month, dayOfMonth);
                // args for actionToCreateTask
                selectedYear = year;
                selectedMonth = month;
                selectedDayOfMonth = dayOfMonth;

                txtBottomSheetDay.setText(
                        String.valueOf(android.text.format.DateFormat.format("EEEE", selectedDate))
                                + ' '
                                + dayOfMonth
                                + '-'
                                + android.text.format.DateFormat.format("MMMM", selectedDate)
                                + '-'
                                + year
                );

                if (selectedDate.compareTo(TODAY) < 0) {

                    bottomSheetAdd.setVisibility(View.GONE);
                } else {

                    bottomSheetAdd.setVisibility(View.VISIBLE);
                }
            }
        });

        bottomSheetAdd.setOnClickListener(l -> {

            NavDirections action = TaskListFragmentDirections.actionCreateTask(this.selectedYear, this.selectedMonth, this.selectedDayOfMonth);
            Navigation.findNavController(view).navigate(action);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        this.viewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

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