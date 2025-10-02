package com.dsvl0.topautoalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmForegroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, "alarm_channel")
                .setContentTitle("Будильник")
                .setContentText("Будильник активирован")
                .setSmallIcon(R.drawable.alarm_off)
                .build();

        startForeground(1, notification);

        Intent alarmIntent = new Intent(this, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(alarmIntent);

        Log.d("AlarmForegroundService", "end");
        //stopSelf();
        return START_NOT_STICKY;
    }

    public void stopAlarmService() {
        stopForeground(true);
        stopSelf();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

