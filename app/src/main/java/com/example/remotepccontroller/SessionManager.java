package com.example.remotepccontroller;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SSH_SESSION";
    private static final String KEY_IP = "ip";
    private static final String KEY_PORT = "port";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String ip, int port, String username, String password) {
        editor.putString(KEY_IP, ip);
        editor.putInt(KEY_PORT, port);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public String getIp() {
        return prefs.getString(KEY_IP, null);
    }

    public int getPort() {
        return prefs.getInt(KEY_PORT, 22);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    public boolean hasSession() {
        return getIp() != null && getUsername() != null && getPassword() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
