package com.dsvl0.topautoalarm;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainService extends BroadcastReceiver {
    private Context context;
    final int AlarmUniqueID = 2704;
    String LastTimeAlarmInstalled = "";

    @Override
    public void onReceive(Context reciviedContext, Intent intent) {
        context = reciviedContext;
        SchedHelper schedHelper = new SchedHelper();
        if (!schedHelper.IsUserRegistered(context)) {
            return;
        }

        schedHelper.getFirstLessonTime(context, new SchedHelper.LessonCallback() {
            @Override
            public void onResult(String time) {
                final String LastAlarmTime = AlarmHelper.getAlarmTime(context, AlarmUniqueID);

                if (LastAlarmTime != null && LastAlarmTime.equals(time)) {
                    Log.d("ServiceRunFinalResult", "Будильник уже установлен");
                    return;
                }

                String AlarmTime = shiftTime(context, time);
                NotificationCenter.showNotification(context,
                        "Новый будильник установлен!",
                        "Пары в " + time + ", будильник - в " + AlarmTime,
                        "Завтра пары начинаются в " + time +
                                " и с учетом указанного сдвига времени мы поставили будильник на " + AlarmTime);

                AlarmHelper.cancelAlarm(context, AlarmUniqueID);
                AlarmHelper.setAlarm(context,
                        GetFinalHours(AlarmTime),
                        GetFinalMinutes(AlarmTime),
                        AlarmUniqueID);
            }

            @Override
            public void onEmptySched() {
                AlarmHelper.cancelAlarm(context, 2704);
                NotificationCenter.showNotification(context,
                        "Будильников на завтра нет!",
                        "Кажется пар завтра нет, как и будильников!",
                        "Посмотрев пары на завтра мы увидели пустой список, поэтому будильника на завтра не будет :)");
            }

            @Override
            public void onError(Exception e) {
                Log.d("ServiceRunFinalResult E", String.valueOf(e));
            }
        });
    }

    private String shiftTime(Context context, String time) {
        Prefs TimePrefs = new Prefs();
        TimePrefs.init(context, "CorrectionSettings");
        final int HoursCorrection = TimePrefs.getInt("Hours", 0);
        final int MinutesCorrection = TimePrefs.getInt("Minutes", 0);
        final boolean BackToTime = TimePrefs.getBool("BackToTime", true);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int factor = BackToTime ? -1 : 1;
            calendar.add(Calendar.HOUR_OF_DAY, factor * HoursCorrection);
            calendar.add(Calendar.MINUTE, factor * MinutesCorrection);

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

}
