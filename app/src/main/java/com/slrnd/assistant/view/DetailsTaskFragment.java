package com.slrnd.assistant.view;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentDetailsTaskBinding;
import com.slrnd.assistant.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.time.Month;

public class DetailsTaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private FragmentDetailsTaskBinding binding;
    private int IS_DONE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details_task, container, false);

        return this.binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O) // Month enum usage
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        int id = DetailsTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.taskViewModel.fetch(id);

        observeViewModel();

        View doneBar = view.findViewById(R.id.done_bar); // for task.is_done==1
        View detailsFragmentTaskActions = view.findViewById(R.id.details_fragment_task_actions); // for task.is_done==0

        if (this.IS_DONE == 0) {
            // visible in layout by default (for is_done==1) in place of action bar; is_done==1 - no actions allowed
            doneBar.setVisibility(View.GONE);
            // invisible in layout by default (for is_done==1); is_done==0 - allow actions
            detailsFragmentTaskActions.setVisibility(View.VISIBLE);

            WorkManager workManager = WorkManager.getInstance(requireContext());

            // DELETE
            ImageView imgDetailsTaskDelete = view.findViewById(R.id.imgDetailsTaskDelete);
            imgDetailsTaskDelete.setOnClickListener(view13 -> {

                taskViewModel.deleteTask(binding.getTask());

                workManager.cancelUniqueWork(String.valueOf(binding.getTask().getDatetimeInSeconds()));

                Toast.makeText(this.getContext(), "Task deleted", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(view13).popBackStack();
            });
            // EDIT
            ImageView imgDetailsTaskEdit = view.findViewById(R.id.imgDetailsTaskEdit);
            imgDetailsTaskEdit.setOnClickListener(view1 -> {

                DetailsTaskFragmentDirections.ActionEditTaskFragment action = DetailsTaskFragmentDirections.actionEditTaskFragment(id);
                Navigation.findNavController(view1).navigate(action);
            });
            // FINISH
            ImageView imgDetailsTaskFinish = view.findViewById(R.id.imgDetailsTaskFinish);
            imgDetailsTaskFinish.setOnClickListener(view12 -> {

                taskViewModel.finishTask(binding.getTask());
                // if finished IRL before triggering work and work is no longer needed
                workManager.cancelUniqueWork(String.valueOf(binding.getTask().getDatetimeInSeconds()));

                Toast.makeText(this.getContext(), "Task finished", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(view12).popBackStack();
            });
        } else {

            // set in layout but just to be sure
            doneBar.setVisibility(View.VISIBLE);
            detailsFragmentTaskActions.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O) // Month enum usage
    private void observeViewModel() {

        this.taskViewModel.getTaskLiveData().observe(getViewLifecycleOwner(), task -> {

            binding.setTask(task);

            IS_DONE = task.getIs_done();

            // txtDetailsTaskDate
            String date = String.valueOf(task.getDate()); // yyyyMMdd
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4, 6));
            int day = Integer.parseInt(date.substring(6));
            binding.setTaskDate(String.valueOf(day) + ' ' + Month.of(month) + ' ' + year); // 5 MAY 2022
            // txtDetailsTaskTime
            long datetime = task.getDatetimeInMillis();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            binding.setTaskTime(df.format(datetime));
        });
    }
}