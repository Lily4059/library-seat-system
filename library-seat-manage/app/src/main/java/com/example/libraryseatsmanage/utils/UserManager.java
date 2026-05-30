package com.example.libraryseatsmanage.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.libraryseatsmanage.model.UserInfo;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_STUDENT_NO = "student_no";
    private static final String KEY_CREDIT = "credit";
    private static final String KEY_ROLE = "role";

    private static UserManager instance;
    private final SharedPreferences preferences;
    private UserInfo currentUser;

    private UserManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadUserFromPrefs();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    public void saveUser(String token, UserInfo userInfo) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userInfo.getId());
        editor.putString(KEY_USER_NAME, userInfo.getName());
        editor.putString(KEY_STUDENT_NO, userInfo.getStudentNo());
        editor.putInt(KEY_CREDIT, userInfo.getCredit());
        editor.putString(KEY_ROLE, userInfo.getRole());
        editor.apply();

        currentUser = userInfo;
    }

    public void updateUser(UserInfo userInfo) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_NAME, userInfo.getName());
        editor.putString(KEY_STUDENT_NO, userInfo.getStudentNo());
        editor.putInt(KEY_CREDIT, userInfo.getCredit());
        editor.apply();

        currentUser = userInfo;
    }

    private void loadUserFromPrefs() {
        int userId = preferences.getInt(KEY_USER_ID, -1);
        if (userId != -1) {
            currentUser = new UserInfo();
            currentUser.setId(userId);
            currentUser.setName(preferences.getString(KEY_USER_NAME, ""));
            currentUser.setStudentNo(preferences.getString(KEY_STUDENT_NO, ""));
            currentUser.setCredit(preferences.getInt(KEY_CREDIT, 100));
            currentUser.setRole(preferences.getString(KEY_ROLE, "user"));
        }
    }

    public UserInfo getCurrentUser() {
        return currentUser;
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return getToken() != null && getUserId() != -1;
    }

    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        currentUser = null;
    }
}
