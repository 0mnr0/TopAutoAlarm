package com.dsvl0.topautoalarm;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class AccessTokenWorker {
    Map<String, String> UserAuthData = new HashMap<>();
    Context context;
    public String access_token = null;

    public interface TokenCallback {
        void onResult(String token);
        void onError(Exception e);
    }
    public interface AuthCorrectionCallback {
        void onResult(boolean isCorrect);
    }


    public void pushContext(Context OutsideContext) { context = OutsideContext; }

    public void setAuthData(String Login, String Password) {
        UserAuthData = new HashMap<>();
        UserAuthData.put("application_key", "6a56a5df2667e65aab73ce76d1dd737f7d1faef9c52e8b8c55ac75f565d8e8a6");
        UserAuthData.put("username", Login);
        UserAuthData.put("password", Password);
    }

    public void initAuthData() {
        Prefs AuthData = Prefs.init(context, "authdata");
        UserAuthData = new HashMap<>();
        UserAuthData.put("application_key", "6a56a5df2667e65aab73ce76d1dd737f7d1faef9c52e8b8c55ac75f565d8e8a6");
        UserAuthData.put("username", AuthData.getString("login", null));
        UserAuthData.put("password", AuthData.getString("password", null));
    }


    public void fetchToken(TokenCallback callback) {
        EasyFetch.run(
                "https://msapi.top-academy.ru/api/v2/auth/login",
                "POST",
                UserAuthData,
                new OnReadyCallback() {
                    @Override
                    public void onReady(Object value, boolean isJson) {
                        try {
                            if (isJson) {
                                JSONObject json = (JSONObject) value;
                                access_token = json.getString("access_token");
                                callback.onResult(access_token);
                            } else {
                                callback.onError(new Exception("Not JSON response"));
                            }
                        } catch (JSONException e) {
                            Log.w("AccessTokenWorkerValue", value.toString());
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                },
                null,
                EasyFetch.JournalREF
        );
    }

    public void isAuthDataCorrect(AuthCorrectionCallback callback) {
        EasyFetch.run(
                "https://msapi.top-academy.ru/api/v2/auth/login",
                "POST",
                UserAuthData,
                new OnReadyCallback() {
                    @Override
                    public void onReady(Object value, boolean isJson) {
                        try {
                            if (isJson) {
                                JSONObject json = (JSONObject) value;
                                json.getString("access_token");
                                callback.onResult(true);
                            } else {
                                callback.onResult(false);
                            }
                        } catch (JSONException e) {
                            callback.onResult(false);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onResult(false);
                    }
                },
                null,
                EasyFetch.JournalREF
        );
    }
}
