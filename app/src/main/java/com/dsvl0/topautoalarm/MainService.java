package com.dsvl0.topautoalarm;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainService extends JobService {
    private Context context;
    JobParameters sheduler;


    public String shiftTime(String time) {
        Prefs TimePrefs = Prefs.init(context, "time");
        final int HoursCorrection = TimePrefs.getInt("Hours", 0);
        final int MinutesCorrection = TimePrefs.getInt("Minutes", 0);
        final boolean BackToTime = TimePrefs.getBool("BackToTime", true);

        // Разбираем строку времени "HH:mm"
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(time);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int factor = BackToTime ? -1 : 1;
            calendar.add(Calendar.HOUR_OF_DAY, factor * HoursCorrection);
            calendar.add(Calendar.MINUTE, factor * MinutesCorrection);

            // Возвращаем в формате "HH:mm"
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }


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
                        String AlarmTime = shiftTime(time);
                        NotificationCenter.showNotification(context,
                                "Новый будильник установлен!",
                                "Пары в "+time+", будильник - в "+AlarmTime,
                                "Завтра пары начинаются в "+time+" и с учетом указанного сдвига времени мы поставили будильник на "+AlarmTime);

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
