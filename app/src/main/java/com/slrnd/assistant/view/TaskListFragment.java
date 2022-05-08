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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.slrnd.assistant.R;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.viewmodel.TaskListViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TaskListFragment extends Fragment {

    private TaskListViewModel taskListViewModel;
    private TaskListAdapter taskListAdapter;
    // private onCheckedChangedListener listener;

    private int selectedDate = 0; // yyyyMMdd
    private int TODAY = 0;

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

        this.taskListViewModel = new ViewModelProvider(this).get(TaskListViewModel.class);

        // TODO add 'last open' functionality, change popBackStack() from CreateFrag behaviour - it resets calendarView and list to TODAY everytime
        // opening TODAY by default; not working with 'saved state' or 'last open' yet
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
        int date = Integer.parseInt(year + formatted_human_month_string + formatted_day_string);

        this.taskListViewModel.fetch(date);
        this.selectedDate = date;
        this.TODAY = date;

        TextView txtBottomSheetDay = view.findViewById(R.id.txtBottomSheetDay);
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
        // simple animation for arrow
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
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                // month: 0 for January 11 december; single digit dayOfMonth
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

                // TODO attention to TODAY when change popbacks, 'last open's etc behaviour
                if (selectedDate < TODAY) {

                    bottomSheetAdd.setVisibility(View.GONE);
                } else {

                    bottomSheetAdd.setVisibility(View.VISIBLE);
                }
            }
        });

        // CREATE
        bottomSheetAdd.setOnClickListener(l -> {

            NavDirections action = TaskListFragmentDirections.actionCreateTask(this.selectedDate);
            Navigation.findNavController(view).navigate(action);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        this.taskListViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), list -> {

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