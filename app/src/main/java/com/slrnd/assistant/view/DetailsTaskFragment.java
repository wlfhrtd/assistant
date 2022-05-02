package com.slrnd.assistant.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.slrnd.assistant.R;
import com.slrnd.assistant.databinding.FragmentDetailsTaskBinding;
import com.slrnd.assistant.viewmodel.TaskDetailsViewModel;

import java.util.List;

public class DetailsTaskFragment extends Fragment {

    private TaskDetailsViewModel viewModel;
    private FragmentDetailsTaskBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details_task, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        int id = DetailsTaskFragmentArgs.fromBundle(requireArguments()).getId();
        this.viewModel.fetch(id);

        observeViewModel();

        int isDone = DetailsTaskFragmentArgs.fromBundle(requireArguments()).getIsDone();
        if (isDone == 0) {

            View doneBar = view.findViewById(R.id.done_bar);
            doneBar.setVisibility(View.GONE);

            View detailsFragmentTaskActions = view.findViewById(R.id.details_fragment_task_actions);
            detailsFragmentTaskActions.setVisibility(View.VISIBLE);

            WorkManager workManager = WorkManager.getInstance(requireContext());
            // DELETE
            ImageView imgDetailsTaskDelete = view.findViewById(R.id.imgDetailsTaskDelete);
            imgDetailsTaskDelete.setOnClickListener(view13 -> {

                viewModel.deleteTask(binding.getTask());

                workManager.cancelUniqueWork(String.valueOf(binding.getTask().getDate()));

                Toast.makeText(this.getContext(), "Task deleted", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(view13).popBackStack();
            });
            // EDIT
            ImageView imgDetailsTaskEdit = view.findViewById(R.id.imgDetailsTaskEdit);
            imgDetailsTaskEdit.setOnClickListener(view1 -> {
                // TODO mess with workManager cancel+enqueue_new or possible update_current
                DetailsTaskFragmentDirections.ActionEditTaskFragment action = DetailsTaskFragmentDirections.actionEditTaskFragment(id);
                Navigation.findNavController(view1).navigate(action);
            });
            // FINISH
            ImageView imgDetailsTaskFinish = view.findViewById(R.id.imgDetailsTaskFinish);
            imgDetailsTaskFinish.setOnClickListener(view12 -> {

                viewModel.finishTask(binding.getTask());
                // if finished IRL before triggering work and work is no longer needed
                workManager.cancelUniqueWork(String.valueOf(binding.getTask().getDate()));

                Toast.makeText(this.getContext(), "Task finished", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(view12).popBackStack();
            });
        }
    }

    private void observeViewModel() {

        this.viewModel.todoLD.observe(getViewLifecycleOwner(), task -> binding.setTask(task));
    }
}