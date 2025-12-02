package com.sugboaid.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefHelper {
    private static final String PREF_NAME = "SugboAidPrefs";
    private SharedPreferences prefs;
    private Gson gson;

    public SharedPrefHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Generic object save/get methods
    public <T> void saveObject(String key, T obj) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, gson.toJson(obj));
        editor.apply();
    }

    public <T> T getObject(String key, Class<T> cls) {
        String json = prefs.getString(key, null);
        return json == null ? null : gson.fromJson(json, cls);
    }

    // Generic list save/get methods
    public <T> void saveList(String key, List<T> list) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, gson.toJson(list));
        editor.apply();
    }

    public <T> List<T> getList(String key, Type type) {
        String json = prefs.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, type);
    }

    // Primitive data methods
    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public void saveFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }

    // Clear methods
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public boolean contains(String key) {
        return prefs.contains(key);
    }
}