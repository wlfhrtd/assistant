package com.slrnd.assistant.view;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.WorkManager;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentDetailsTaskBinding;
import com.slrnd.assistant.model.Task;
import com.slrnd.assistant.viewmodel.TaskViewModel;

import java.time.Month;

@RequiresApi(api = Build.VERSION_CODES.O) // Month enum usage
public class DetailsTaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private FragmentDetailsTaskBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details_task, container, false);

        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        // task.id
        int id = DetailsTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.taskViewModel.fetch(id);

        observeViewModel();
    }

    private void observeViewModel() {

        this.taskViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), task -> {

            this.binding.setTask(task);

            setupActions(task);
        });
    }

    private void setupActions(Task task) {

        View doneBar = this.binding.doneBar; // shown for task.is_done==1
        View detailsFragmentTaskActions = this.binding.detailsFragmentTaskActions; // shown for task.is_done==0

        if (task.getIs_done() == 0) {
            // visible in layout by default (for is_done==1) in place of action bar; is_done==1 - no actions allowed
            doneBar.setVisibility(View.GONE);
            // invisible in layout by default (for is_done==1); is_done==0 - allow actions
            detailsFragmentTaskActions.setVisibility(View.VISIBLE);

            WorkManager workManager = WorkManager.getInstance(requireContext());

            // DELETE
            this.binding.detailsFragmentTaskActions.findViewById(R.id.imgDetailsTaskDelete).setOnClickListener(view13 -> {
                // real task DELETE from db, cancel queued associated work
                this.taskViewModel.deleteTask(task);
                // use task dateTime in seconds as unique names for works
                workManager.cancelUniqueWork(String.valueOf(task.getDatetimeInSeconds()));

                Toast.makeText(this.getContext(), R.string.taskdeleted, Toast.LENGTH_SHORT).show();
                // popBack to taskListView
                Navigation.findNavController(view13).popBackStack();
            });
            // EDIT
            this.binding.detailsFragmentTaskActions.findViewById(R.id.imgDetailsTaskEdit).setOnClickListener(view1 -> {
                // navigate to editTaskFragment, task.id required
                DetailsTaskFragmentDirections.ActionEditTaskFragment action = DetailsTaskFragmentDirections.actionEditTaskFragment(task.getId());
                Navigation.findNavController(view1).navigate(action);
            });
            // FINISH
            this.binding.detailsFragmentTaskActions.findViewById(R.id.imgDetailsTaskFinish).setOnClickListener(view12 -> {
                // set is_done==1, update entity in db
                this.taskViewModel.finishTask(task);
                // design: if task finished IRL before triggering work and work is no longer needed
                workManager.cancelUniqueWork(String.valueOf(task.getDatetimeInSeconds()));

                Toast.makeText(this.getContext(), R.string.taskfinished, Toast.LENGTH_SHORT).show();
                // popBack to taskListView
                Navigation.findNavController(view12).popBackStack();
            });
        } else {
            // is_done==1
            // visibility is already set in layout but also explicitly here just to be sure
            doneBar.setVisibility(View.VISIBLE);
            detailsFragmentTaskActions.setVisibility(View.GONE);
        }
        // txtDetailsTaskDate
        int year = task.getYear();
        int month = task.getMonth();
        int day = task.getDayOfMonth();
        // 5 MAY 2022
        this.binding.setTaskDate(String.valueOf(day) + ' ' + Month.of(month) + ' ' + year);
        // txtDetailsTaskTime
        // HH:mm
        this.binding.setTaskTime(task.getStringTime());
    }
}