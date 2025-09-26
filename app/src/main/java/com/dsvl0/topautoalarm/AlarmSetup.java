package com.dsvl0.topautoalarm;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.eap.EapSessionConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.CorrectionInfo;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmSetup extends AppCompatActivity {
    ConstraintLayout loadingIndicator;
    AccessTokenWorker AccessTokenWorker;
    TextView HoursCorrection, MinutesCorrection, CorrectionType;
    int CorrectionHours, CorrectionMinutes = 0;
    boolean CorrectionBackToTime = true;

    Prefs CorrectionSettings;


    int tokenTryCount = 0;
    public void initToken() {
        AccessTokenWorker.fetchToken(new AccessTokenWorker.TokenCallback() {
            @Override
            public void onResult(String token) {
                GetShed();
            }

            @Override
            public void onError(Exception e) {
                tokenTryCount++;
                if (tokenTryCount < 15) {
                    new Handler().postDelayed(() -> initToken(), 2000);
                } else {
                    Toast.makeText(AlarmSetup.this, "Сервер TOP не отдал токен за 15 попыток, выход", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }


    private void GetShed() {
        final String BestTime4Fetch = BestTime.forAlarm();
        final String url = "https://msapi.top-academy.ru/api/v2/schedule/operations/get-by-date?date_filter="+BestTime4Fetch;
        EasyFetch.run(
                url,
                "GET",
                null,
                new OnReadyCallback() {
                    @Override
                    public void onReady(Object json, boolean isJson) {
                        DisplayShed(json.toString(), BestTime4Fetch);
                        loadingIndicator.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AlarmSetup.this, "Не удалось получить расписание", Toast.LENGTH_SHORT).show();
                        loadingIndicator.setVisibility(View.GONE);
                    }
                },
                AccessTokenWorker.access_token,
                EasyFetch.JournalREF
        );
    }

    int DisplayingTries = 0;
    private void DisplayShed(String ArrayOfJsons, String Date) {
        try {
            JSONArray ParsedJson = new JSONArray(ArrayOfJsons);
            CardSchedDisplaying cardSched = new CardSchedDisplaying(this, findViewById(R.id.SchedDisplayer));
            if (ParsedJson.length() >= 1) {
                JSONObject iterationObject = ParsedJson.getJSONObject(0);
                Log.d("ParsedJson:", String.valueOf(iterationObject));
                cardSched.add(
                        iterationObject.getString("started_at") + " - " + iterationObject.getString("finished_at"),
                        iterationObject.getString("subject_name"));
            } else {
                cardSched.add("",
                        "Пар на "+Date+" не найдено :)");
            }


        } catch (JSONException e) {
            DisplayingTries += 1;
            Log.d("JSONException", e.toString());
            if (DisplayingTries >= 3) {
                Toast.makeText(this, "Не удалось получить расписание", Toast.LENGTH_SHORT).show();
            }  else {GetShed();}
        }
    }

    private void UpdateTimeCorrection() {
        String CRCTN_HOURS = String.valueOf(CorrectionHours);
        if (CRCTN_HOURS.length() == 1) { CRCTN_HOURS = "0"+CRCTN_HOURS; }

        String CRCTN_MINUTES = String.valueOf(CorrectionMinutes);
        if (CRCTN_MINUTES.length() == 1) { CRCTN_MINUTES = "0"+CRCTN_MINUTES; }

        HoursCorrection.setText(CRCTN_HOURS);
        MinutesCorrection.setText(CRCTN_MINUTES);
        CorrectionType.setText(CorrectionBackToTime ? "-" : "+");


    }

    public void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(CorrectionHours)
                .setMinute(CorrectionMinutes)
                .setTitleText("Укажите сдвиг по времени:")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            CorrectionHours = picker.getHour();
            CorrectionMinutes = picker.getMinute();

            CorrectionSettings.init(this, "CorrectionSettings");
            CorrectionSettings.putInt("Hours", CorrectionHours);
            CorrectionSettings.putInt("Minutes", CorrectionMinutes);
            UpdateTimeCorrection();
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Prefs CorrectionSettings = new Prefs();
        CorrectionSettings.init(this, "CorrectionSettings");
        CorrectionHours = CorrectionSettings.getInt("Hours", 0);
        CorrectionMinutes = CorrectionSettings.getInt("Minutes", 0);
        CorrectionBackToTime = CorrectionSettings.getBool("BackToTime", true);
        Log.d("SavedSettings:", CorrectionHours+"|"+CorrectionMinutes+"|"+CorrectionBackToTime);

        AccessTokenWorker = new AccessTokenWorker();
        AccessTokenWorker.pushContext(this);
        AccessTokenWorker.initAuthData();

        loadingIndicator = findViewById(R.id.LoadIndicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        initToken();


        HoursCorrection = findViewById(R.id.HoursCorrection);
        MinutesCorrection = findViewById(R.id.MinutesCorrection);
        CorrectionType = findViewById(R.id.CorrectionType);

        HoursCorrection.setOnClickListener(v -> showTimePicker());
        MinutesCorrection.setOnClickListener(v -> showTimePicker());

        CorrectionType.setOnClickListener(v -> {
            CorrectionBackToTime = !CorrectionBackToTime;
            CorrectionSettings.putBool("BackToTime", CorrectionBackToTime);
            UpdateTimeCorrection();
        });
        UpdateTimeCorrection();
        canScheduleExactAlarms(this);
    }

    public boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }


}