package com.dsvl0.topautoalarm;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.color.DynamicColors;


public class MainActivity extends AppCompatActivity {
    ConstraintLayout LoadPanel;

    Button LogIn;
    EditText LoginData, PasswordData;


    private void RunAlarmSetup() {
        Intent intent = new Intent(this, AlarmSetup.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LogIn = findViewById(R.id.LogIn);
        LoginData = findViewById(R.id.LoginData);
        LoadPanel = findViewById(R.id.LoadPanel);
        PasswordData = findViewById(R.id.PasswordData);



        Prefs AuthData = Prefs.init(this, "authdata");
        String AuthScheme = AuthData.getString("LoginCheme", null);
        String AuthLogin = AuthData.getString("login", null);
        String AuthPassword = AuthData.getString("password", null);

        if (AuthScheme != null && AuthLogin != null && AuthPassword != null) { RunAlarmSetup(); }
        if (AuthData.isKeyExists("login")) { LoginData.setText(AuthData.getString("login", "")); }
        if (AuthData.isKeyExists("password")) { PasswordData.setText(AuthData.getString("password", "")); }

        LogIn.setOnClickListener(v -> {
            LoadPanel.setVisibility(View.VISIBLE);
            String Login = LoginData.getText().toString();
            String Password = PasswordData.getText().toString();
            AuthData.putString("login", Login);
            AuthData.putString("password", Password);
            AuthData.putString("LoginCheme", "Journal");

            AccessTokenWorker TokenWorker = new AccessTokenWorker();
            TokenWorker.pushContext(this);
            TokenWorker.setAuthData(Login, Password);
            TokenWorker.isAuthDataCorrect(isCorrect -> {
                LoadPanel.setVisibility(View.GONE);
                if (isCorrect) { RunAlarmSetup(); } else {
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            });
        });

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, MainService.class);
        JobInfo jobInfo = new JobInfo.Builder(8080, componentName)
                .setPeriodic(30 * 60 * 1000)
                .setPersisted(true)
                .build();

        int result = jobScheduler.schedule(jobInfo);
        NotificationCenter.AskForPermissionIfNotPermitted(this, this);

        if (!(result == JobScheduler.RESULT_SUCCESS)) {
            Toast.makeText(this, "Ошибка планирования!", Toast.LENGTH_SHORT).show();
        }


    }
}