package com.dsvl0.topautoalarm;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;

public class Prefs {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    private Prefs() {}

    public static Prefs init(Context context, String fileName) {
        prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = prefs.edit();
        return new Prefs();
    }

    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public boolean getBool(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public Object get(String key, Object defValue) {
        Map<String, ?> all = prefs.getAll();
        return all.containsKey(key) ? all.get(key) : defValue;
    }

    // --- SAVE ---
    public Prefs putString(String key, String value) {
        editor.putString(key, value).apply();
        return this;
    }

    public Prefs putBool(String key, boolean value) {
        editor.putBoolean(key, value).apply();
        return this;
    }

    public Prefs putInt(String key, int value) {
        editor.putInt(key, value).apply();
        return this;
    }

    public Prefs putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
        return this;
    }

    public Prefs putLong(String key, long value) {
        editor.putLong(key, value).apply();
        return this;
    }

    // --- Утилиты ---
    public boolean isKeyExists(String key) {
        return prefs.contains(key);
    }

    public void remove(String key) {
        editor.remove(key).apply();
    }

    public void clear() {
        editor.clear().apply();
    }
}
