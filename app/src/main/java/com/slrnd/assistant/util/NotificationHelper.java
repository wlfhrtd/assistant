package com.slrnd.assistant.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.slrnd.assistant.R;
import com.slrnd.assistant.view.MainActivity;

public class NotificationHelper {

    private Context context;

    private String CHANNEL_ID = "todo_channel_id";
    private String description = "Todo channel description";
    private int NOTIFICATION_ID = 1;

    public NotificationHelper(Context context) {

        this.context = context;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // oreo+ req
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void createNotification(String title, String message) {
        
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.todochar);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.checklist)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                        .bigPicture(icon)
                        .bigLargeIcon(null)
                )
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
                ;

        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, notification);
    }
}