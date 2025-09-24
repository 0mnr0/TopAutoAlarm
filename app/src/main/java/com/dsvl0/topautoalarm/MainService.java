package com.dsvl0.topautoalarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void onRunEvent(){
        SchedHelper schedHelper = new SchedHelper();
        if (!schedHelper.IsUserRegistered(context)) {
            return;
        }
        schedHelper.getFirstLessonTime(context,
                new SchedHelper.LessonCallback() {
                    @Override
                    public void onResult(String time) {
                        Toast.makeText(context, "Parsed Time:"+time, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("ServiceError", String.valueOf(e));
                        Toast.makeText(context, "BRUH", Toast.LENGTH_SHORT).show();
                    }

                });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        new Thread(() -> {
            while (true) {
                onRunEvent();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
