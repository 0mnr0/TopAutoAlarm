package com.dsvl0.topautoalarm;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlarmScreen extends AppCompatActivity {
    boolean isUserTriedToCloseApp = false;
    BasicAlarmSound alarmSounds = new BasicAlarmSound();
    public void wakingUp(View view) {
        exitApp();
    }

    public void schedTheAlarm(View view) {
        Map<String, Double> examples = new HashMap<>();
        examples.put("3 + 3 * 3", 12.0);
        examples.put("8 * 8 * 8", 512.0);
        examples.put("15 - 3 * 2", 9.0);
        examples.put("9 * 9 - 10", 71.0);
        examples.put("100 / 4 + 25", 50.0);
        examples.put("7 * 6 + 12", 54.0);
        examples.put("2 * (5 + 9)", 28.0);
        examples.put("81 / 9 + 8", 17.0);
        examples.put("12 * 12 - 20", 124.0);
        examples.put("50 + 25 * 2", 100.0);

        Random rand = new Random();
        List<String> keys = new ArrayList<>(examples.keySet());
        String randomQuestion = keys.get(rand.nextInt(keys.size()));
        Double randomAnswer = examples.get(randomQuestion);

        EditText input = new EditText(this);
        input.setHint("Введите ответ");
        FrameLayout container = new FrameLayout(this);
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        container.setPadding(padding, padding, padding, padding);
        container.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle(randomQuestion)
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (randomAnswer == null || Double.parseDouble(text) == randomAnswer) {
                        Toast.makeText(this, "Это последние 10 минут...", Toast.LENGTH_SHORT).show();
                        AlarmHelper.setAlarmInMinutes(this, 10);
                    } else {
                        Toast.makeText(this, "ТОЛЬКО 2 МИНУТЫ", Toast.LENGTH_SHORT).show();
                        AlarmHelper.setAlarmInMinutes(this, 1);
                    }
                    exitApp();
                })
                .setNeutralButton("Отмена", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        alarmSounds.playAlarm(this);


        // if >= 8.1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }
    }

    public void exitApp() {
        alarmSounds.stopAlarm();
        Intent serviceIntent = new Intent(this, AlarmForegroundService.class);
        stopService(serviceIntent);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        exitApp();
        isUserTriedToCloseApp = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        exitApp();
        isUserTriedToCloseApp = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitApp();
        isUserTriedToCloseApp = true;
    }

}