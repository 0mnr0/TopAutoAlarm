package com.dsvl0.topautoalarm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface OnReadyCallback {
    void onReady(Object json, boolean isJson);
    void onError(Exception e);
}

public class EasyFetch {
    public static String JournalREF = "https://journal.top-academy.ru";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void run(String url, String method, Map<String, String> body, OnReadyCallback callback, String AccessToken, String OriginAndReferer) {
        if (body == null) {
            body = new HashMap<>();
        }
        Request.Builder builder = new Request.Builder().url(url);

        JSONObject jsonBody = new JSONObject(body);
        RequestBody requestBody = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        if ("POST".equalsIgnoreCase(method)) {
            builder.post(requestBody);
        } else if ("PUT".equalsIgnoreCase(method)) {
            builder.put(requestBody);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.delete(requestBody);
        } else {
            builder.get();
        }

        builder.header("Authorization", "Bearer "+AccessToken);
        builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36");
        if (OriginAndReferer != null &&!OriginAndReferer.isEmpty()) {
            builder.header("Origin", OriginAndReferer);
            builder.header("Referer", OriginAndReferer);
        }

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String res = response.body().string();
                    Log.d("EasyFetchResult", "URL: "+url+"\nAccessToken: "+AccessToken+"\nResponse: "+res);
                    boolean isJson = res.charAt(0) == '{';

                    JSONObject json = isJson ? new JSONObject(res) : null;
                    mainHandler.post(() -> callback.onReady(isJson ? json : res, isJson));

                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
}
