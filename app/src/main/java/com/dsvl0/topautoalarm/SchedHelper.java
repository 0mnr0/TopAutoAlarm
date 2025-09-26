package com.dsvl0.topautoalarm;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

public class SchedHelper {
    private int FailedTimes = 0;

    public interface LessonCallback {
        void onResult(String time);
        void onError(Exception e);
        void onEmptySched();
    }


    public void ReRunOperation(Context ctx, LessonCallback callback) {
        if (!(FailedTimes >= 15)) {
            new Handler().postDelayed(() -> getFirstLessonTime(ctx, callback), 2000);
        }
    }

    public boolean IsUserRegistered(Context context) {
        Prefs UserPrefs = Prefs.init(context, "authdata");
        return UserPrefs.isKeyExists("login") && UserPrefs.isKeyExists("password");
    }

    public void getFirstLessonTime(Context context, LessonCallback callback) {
        if (BestTime.isEvening()) {
            String DayForAlarm = BestTime.forAlarm();
            String url = "https://msapi.top-academy.ru/api/v2/schedule/operations/get-by-date?date_filter=" + DayForAlarm;
            Log.d("ServiceRunResult Run", "Init ATW");
            AccessTokenWorker accessTokenWorker = new AccessTokenWorker();
            accessTokenWorker.pushContext(context);
            accessTokenWorker.initAuthData();
            Log.d("ServiceRunResult Run", "Init ATW - Finish. Fetching...");
            accessTokenWorker.fetchToken(new AccessTokenWorker.TokenCallback() {
                @Override
                public void onResult(String token) {
                    Log.d("ServiceRunResult Run", "JWT Fetched. Fetching Sched...");
                    EasyFetch.run(
                            url,
                            "GET",
                            null,
                            new OnReadyCallback() {
                                @Override
                                public void onReady(Object json, boolean isJson) {
                                    Log.d("ServiceRunResult Run", "Sched Fetched");
                                    try {
                                        JSONArray Lessons = new JSONArray(json.toString());
                                        if (Lessons.length() >= 1) {
                                            String FirstLessonTime = Lessons.getJSONObject(0).getString("started_at");
                                            callback.onResult(FirstLessonTime);
                                        } else {
                                            callback.onEmptySched();
                                        }

                                    } catch (JSONException e) {
                                        Log.d("ServiceRunResult Run", "Sched Fetched but failed to parse    ");
                                        FailedTimes += 1;
                                        ReRunOperation(context, callback);
                                        callback.onError(e);
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    FailedTimes += 1;
                                    ReRunOperation(context, callback);
                                    callback.onError(e);
                                }
                            },
                            accessTokenWorker.access_token,
                            EasyFetch.JournalREF
                    );
                }

                @Override
                public void onError(Exception e) {
                    Log.d("ServiceRunResult Run", "JWT Fetch Failed. Refetching...");
                    FailedTimes += 1;
                    ReRunOperation(context, callback);
                    callback.onError(e);
                }
            });
        } else {
            Log.w("SchedHelper", "Skipping fetch because its not evening");
        }
    }


}
