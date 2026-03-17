package com.example.myapplication.sharedPref;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.User;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "request_user_pref";
    private static final String KEY_ID = "key_id";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_ROLE = "key_role";
    private static final String KEY_TOKEN = "key_token";


    private final Context mCtx;

    public SharedPrefManager(Context context) {
        this.mCtx = context;
    }

    // Store user data (excluding password for security reasons)
    public void storeUser(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    // Retrieve user data
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(KEY_ID, -1);
        String email = sharedPreferences.getString(KEY_EMAIL, null); // <-- Add this line
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        String role = sharedPreferences.getString(KEY_ROLE, null);
        String token = sharedPreferences.getString(KEY_TOKEN, null);

        return new User(id, email, username, null, role, token);
    }

    // Clear shared preference
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void clear() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}