package com.dsvl0.topautoalarm;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends JobService {
    private Context context;
    JobParameters sheduler;


    private void onRunEvent(){
        SchedHelper schedHelper = new SchedHelper();
        if (!schedHelper.IsUserRegistered(context)) {
            jobFinished(sheduler, true);
            return;
        }
        schedHelper.getFirstLessonTime(context,
                new SchedHelper.LessonCallback() { // Не работает
                    @Override
                    public void onResult(String time) {
                        Log.d("ServiceRunFinalResult S", String.valueOf(time));
                        Toast.makeText(context, "Parsed Time: "+time, Toast.LENGTH_SHORT).show();
                        jobFinished(sheduler, true);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("ServiceRunFinalResult E", String.valueOf(e));
                        jobFinished(sheduler, true);
                    }

                });
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        context = this;
        sheduler = params;
        onRunEvent();
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}
