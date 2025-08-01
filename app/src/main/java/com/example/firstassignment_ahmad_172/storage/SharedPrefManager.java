package com.example.firstassignment_ahmad_172.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.firstassignment_ahmad_172.model.User;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "my_shared_pref";
    private static final String KEY_USER_ID = "userId";

    private static SharedPrefManager instance;
    private Context ctx;

    private SharedPrefManager(Context context) {
        ctx = context.getApplicationContext();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Save user ID
    public void saveUser(User user) {
        if (user == null) {
            Log.e("SharedPref", "Attempted to save null user");
            return;
        }

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String userId = user.getId();
        if (userId == null || userId.isEmpty()) {
            Log.e("SharedPref", "Attempted to save null/empty user ID");
            return;
        }

        Log.d("SharedPref", "Saving userId: " + userId);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();  // Consider using commit() if you need immediate confirmation
    }
    // Get user ID
    public String getUserId() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(KEY_USER_ID, null);
        Log.d("SharedPref", "Retrieved userId: " + id); // 👈 DEBUG LINE
        return id;
    }


    // Logout
    public void logout() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
