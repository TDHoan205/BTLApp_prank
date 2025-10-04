package com.example.btlapp_prank.UI;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefManager {
    private static final String PREF_NAME = "my_app_pref";
    private static final String KEY_USER_EMAIL = "currentUserEmail";
    private static final String KEY_USER_ROLE = "currentUserRole";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public PrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // ---- Save & Load String ----
    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    // ---- User Session (email + role) ----
    public void saveUserSession(String email, String role) {
        if (email != null) {
            email = email.trim().toLowerCase(); // chuẩn hóa email
        }
        sharedPreferences.edit()
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public String getCurrentUserEmail() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, null);
        return (email != null) ? email.trim().toLowerCase() : null;
    }

    public String getCurrentUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "user"); // mặc định user
    }

    public boolean isUserLoggedIn() {
        return getCurrentUserEmail() != null;
    }

    public void clearUserSession() {
        sharedPreferences.edit()
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_ROLE)
                .apply();
    }

    // ---- Save & Load List<String> ----
    public void saveButtonsFor(String key, List<String> list) {
        String json = gson.toJson(list);
        sharedPreferences.edit().putString(key, json).apply();
    }

    public ArrayList<String> getButtonsFor(String key) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ---- Save & Load List<Sound> ----
    public void saveSoundsFor(String key, List<Sound> sounds) {
        String json = gson.toJson(sounds);
        sharedPreferences.edit().putString(key, json).apply();
    }

    public ArrayList<Sound> getSoundsFor(String key) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<Sound>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ---- Clear ALL ----
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}
