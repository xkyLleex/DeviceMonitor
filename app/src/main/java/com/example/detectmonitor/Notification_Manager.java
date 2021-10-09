package com.example.detectmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class Notification_Manager extends ContextWrapper {
    private NotificationManager notificationManager;
    protected boolean TimeAfter = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification_Manager(Context context) {
        super(context);
        createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel("DetectCHID", "DetectMonitor", NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    public Notification notificationChannelBuild(String message){
        if(TimeAfter)   // have timeout after
            return new NotificationCompat.Builder(getApplicationContext(),"DetectCHID")
                    .setContentTitle("DetectMonitor")   // 標題
                    .setContentText(message)            // 訊息
                    .setSmallIcon(R.drawable.icon)      // 圖示
                    .setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0))
                    .setAutoCancel(true)
                    .setTimeoutAfter(5000)
                    .build();
        else            // didn't have timeout after
            return new NotificationCompat.Builder(getApplicationContext(),"DetectCHID")
                    .setContentTitle("DetectMonitor")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0))
                    .setAutoCancel(true)
                    .build();
    }
}
