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
    final int AlarmUniqueID = 2704;

    String LastTimeAlarmInstalled = "";


    public String shiftTime(String time) {
        Prefs TimePrefs = new Prefs();
        TimePrefs.init(context, "CorrectionSettings");
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

    private int GetFinalHours(String finalTime) {
        return Integer.parseInt(finalTime.split(":")[0]);
    }
    private int GetFinalMinutes(String finalTime) {
        return Integer.parseInt(finalTime.split(":")[1]);
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
                        final String LastAlarmTime = AlarmHelper.getAlarmTime(context, AlarmUniqueID);

                        if (LastAlarmTime != null && LastAlarmTime.equals(time)
                                || LastTimeAlarmInstalled.equals(BestTime.now())
                        ) {
                            Log.d("ServiceRunFinalResult", "Будильник уже установлен");
                            jobFinished(sheduler, true);
                            return;
                        }

                        String AlarmTime = shiftTime(time);
                        NotificationCenter.showNotification(context,
                                "Новый будильник установлен!",
                                "Пары в "+time+", будильник - в "+AlarmTime,
                                "Завтра пары начинаются в "+time+" и с учетом указанного сдвига времени мы поставили будильник на "+AlarmTime);


                        AlarmHelper.cancelAlarm(context, AlarmUniqueID);
                        AlarmHelper.setAlarm(context, GetFinalHours(AlarmTime), GetFinalMinutes(AlarmTime), AlarmUniqueID);
                        LastTimeAlarmInstalled = BestTime.now();
                        jobFinished(sheduler, true);
                    }

                    @Override
                    public void onEmptySched(){
                        AlarmHelper.cancelAlarm(context, AlarmUniqueID);
                        NotificationCenter.showNotification(context,
                                "Будильников на завтра нет!",
                                "Кажется пар завтра нет, как и будильников!",
                                "Посмотрев пары на завтра мы увидели пустой список, поэтому будильника на завтра не будет :)");

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
