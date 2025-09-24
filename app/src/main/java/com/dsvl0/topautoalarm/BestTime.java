package com.dsvl0.topautoalarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BestTime {
    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }


    public static boolean isEvening() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.HOUR_OF_DAY) >= 20;
    }

    public static String forAlarm() {
        final int plusDays = isEvening() ? 1 : 0;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, plusDays);
        Date tomorrow = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return sdf.format(tomorrow);
    }
}
