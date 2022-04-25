package com.slrnd.assistant.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TaskWorker extends Worker {

    private Context context;

    public TaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        new NotificationHelper(this.context)
                .createNotification(
                        getInputData().getString("title"),
                        getInputData().getString("message")
                );

        return Result.success();
    }
}