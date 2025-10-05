package com.dsvl0.topautoalarm;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Brain {
    private static String getDay() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static void memorizeAlarm(Context context, String AlarmTime) {
        Prefs AlarmPrefs = new Prefs();
        AlarmPrefs.init(context, "LastAlarmData");


        AlarmPrefs.putString(
                "AlarmTime",
                AlarmTime
        );

        AlarmPrefs.putString(
                "SettedUp",
                getDay()
        );
    }

    public static void forgotAlarm(Context context) {
        Prefs AlarmPrefs = new Prefs();
        AlarmPrefs.init(context, "LastAlarmData");

        if (AlarmPrefs.isKeyExists("AlarmTime")) {
            AlarmPrefs.remove("AlarmTime");
        }

        if (AlarmPrefs.isKeyExists("SettedUp")) {
            AlarmPrefs.remove("SettedUp");
        }
    }


    public static int isActualAlarm(Context context, String incomingTime) {
        Prefs AlarmPrefs = new Prefs();
        AlarmPrefs.init(context, "LastAlarmData");

        String LastAlarmTime = AlarmPrefs.getString("AlarmTime", ""); // like 07:20
        String LastAlarmStatusNotify = AlarmPrefs.getString("SettedUp", ""); // like 2025-10-24


        // getDay() -> (String) [YYYY-MM-DD] (Current Day)
        if (LastAlarmStatusNotify.equalsIgnoreCase(getDay()) && LastAlarmTime.equalsIgnoreCase(incomingTime)) {
            return 1; // alarm is setted up today for correct time -> (True);
        } else if (!LastAlarmTime.equalsIgnoreCase(incomingTime)) {
            return 2; // alarm is setted up today but the time has changed -> (False);
        }


        return 0; // alarm is setted up NOT today -> (False);
    }
}
