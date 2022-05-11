package com.slrnd.assistant.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.slrnd.assistant.R;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TaskListFragment extends Fragment {

    private TaskListViewModel taskListViewModel;
    private TaskListAdapter taskListAdapter;

    private int selectedDate = 0; // yyyyMMdd
    private int TODAY = 0;

    public TaskListFragment() {

        super();

        this.taskListAdapter= new TaskListAdapter(new ArrayList<>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        // shared viewModel
        this.taskListViewModel = new ViewModelProvider(requireActivity()).get(TaskListViewModel.class);

        // get TODAY date, apply yyyyMMdd format
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // returns a single digit, 0 for January 11 december
        int day = calendar.get(Calendar.DAY_OF_MONTH); // single digit, no leading zero
        // DATE INTEGER FORMAT
        // add leading zero to month; required pattern yyyyMMdd
        int human_month = month+1; // fix start of January as 0 to start with 1
        String human_month_string = String.valueOf(human_month);
        String formatted_human_month_string = String.format("%2s", human_month_string).replace(' ', '0');
        // add leading zero to day
        String day_string = String.valueOf(day);
        String formatted_day_string = String.format("%2s", day_string).replace(' ', '0');
        // yyyyMMdd
        // 20990501
        int now = Integer.parseInt(year + formatted_human_month_string + formatted_day_string);
        this.TODAY = now;

        // for date in bottom sheet fragment (calendarView)
        TextView txtBottomSheetDay = view.findViewById(R.id.txtBottomSheetDay);

        // fresh start, show TODAY; DATE==0 means data not loaded in viewModel yet
        if (TaskListViewModel.getDATE() == 0) {
            // fresh start, load data for TODAY
            this.taskListViewModel.fetch(now);
            this.selectedDate = now;

            Date datetime = calendar.getTime();
            // Monday 20-april-2000
            txtBottomSheetDay.setText(
                    String.valueOf(android.text.format.DateFormat.format("EEEE", datetime))
                            + ' '
                            + day
                            + '-'
                            + android.text.format.DateFormat.format("MMMM", datetime)
                            + '-'
                            + year
            );
        } else {
            // listViewModel.DATE != 0 and returns date as int yyyyMMdd => data already loaded into viewModel
            this.selectedDate = TaskListViewModel.getDATE();

            String date = String.valueOf(this.selectedDate); // yyyyMMdd
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(4, 6)); // STORING MONTH AS 1 FOR JANUARY 12 DECEMBER
            day = Integer.parseInt(date.substring(6));
            // only date, setting time in CreateTaskFragment; old calendar instance used to get TODAY
            calendar.set(year, month - 1, day, 0, 0); // MONTH -1 FIX!!!

            Date datetime = calendar.getTime();
            // Monday 20-april-2000
            txtBottomSheetDay.setText(
                    String.valueOf(android.text.format.DateFormat.format("EEEE", datetime))
                            + ' '
                            + day
                            + '-'
                            + android.text.format.DateFormat.format("MMMM", datetime)
                            + '-'
                            + year
            );
        }

        RecyclerView recyclerView = view.findViewById(R.id.recTaskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(this.taskListAdapter);

        // bottom sheet fragment for calendarView
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
        // simple animation for arrow
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                headerArrowImage.setRotation(slideOffset * -180);
            }
        });
        // "add new task" button in bottom sheet
        ImageView bottomSheetAdd = view.findViewById(R.id.bottomSheetAdd);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                // month: 0 for January 11 december; no leading zeros single digit format for month and dayOfMonth eg 1 2 3
                // DATE INTEGER FORMAT
                // add leading zero to month; required pattern yyyyMMdd
                int human_month = month+1; // fix start of January as 0 to start with 1
                String human_month_string = String.valueOf(human_month);
                String formatted_human_month_string = String.format("%2s", human_month_string).replace(' ', '0');
                // add leading zero to day
                String day_string = String.valueOf(dayOfMonth);
                String formatted_day_string = String.format("%2s", day_string).replace(' ', '0');
                // yyyyMMdd
                // 20990501
                int date = Integer.parseInt(year + formatted_human_month_string + formatted_day_string);

                taskListViewModel.fetch(date);
                selectedDate = date;

                observeViewModel();

                // Monday 20-april-2000
                Calendar shown = Calendar.getInstance();
                shown.set(year, month, dayOfMonth, 0, 0);
                Date dateToShow = shown.getTime();
                txtBottomSheetDay.setText(
                        String.valueOf(android.text.format.DateFormat.format("EEEE", dateToShow))
                                + ' '
                                + dayOfMonth
                                + '-'
                                + android.text.format.DateFormat.format("MMMM", dateToShow)
                                + '-'
                                + year
                );
                // hide "add new task" button if date is not actual
                if (selectedDate < TODAY) {

                    bottomSheetAdd.setVisibility(View.GONE);
                } else {

                    bottomSheetAdd.setVisibility(View.VISIBLE);
                }
            }
        });

        // CREATE
        bottomSheetAdd.setOnClickListener(l -> {

            NavDirections action = TaskListFragmentDirections.actionCreateTask();
            Navigation.findNavController(view).navigate(action);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        this.taskListViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

            this.taskListAdapter.updateTaskList(list);
            // empty taskList text
            TextView txtEmpty = requireView().findViewById(R.id.txtEmpty);
            if (list.isEmpty()) {

                txtEmpty.setVisibility(View.VISIBLE);
            } else {
                txtEmpty.setVisibility(View.GONE);
            }
        });
    }
}