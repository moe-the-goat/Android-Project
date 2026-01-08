package com.example.andriodproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPrefManager - Singleton class for managing SharedPreferences
 * Handles user session, remember me functionality, and app settings
 */
public class SharedPrefManager {

    private static final String PREF_NAME = "FinanceManagerPrefs";
    
    // Keys for user session
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    
    // Keys for app settings
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_DEFAULT_PERIOD = "defaultPeriod"; // daily, weekly, monthly
    
    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private SharedPrefManager(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Get singleton instance of SharedPrefManager
     */
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // ===================== USER SESSION METHODS =====================

    /**
     * Save user login session
     * @param email User's email
     * @param rememberMe Whether to remember the user
     */
    public void saveUserSession(String email, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user's email
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Check if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Clear user session (logout)
     */
    public void logout() {
        boolean rememberMe = isRememberMeEnabled();
        String email = getUserEmail();
        
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        
        // If remember me is not enabled, clear the email too
        if (!rememberMe) {
            editor.putString(KEY_USER_EMAIL, null);
        }
        
        editor.apply();
    }

    /**
     * Get saved email for remember me feature
     */
    public String getSavedEmail() {
        if (isRememberMeEnabled()) {
            return sharedPreferences.getString(KEY_USER_EMAIL, null);
        }
        return null;
    }

    /**
     * Clear all session data
     */
    public void clearSession() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_REMEMBER_ME);
        editor.apply();
    }

    // ===================== APP SETTINGS METHODS =====================

    /**
     * Set dark mode preference
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setDarkMode(boolean isDarkMode) {
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
    }

    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Set default time period for financial summaries
     * @param period "daily", "weekly", or "monthly"
     */
    public void setDefaultPeriod(String period) {
        editor.putString(KEY_DEFAULT_PERIOD, period);
        editor.apply();
    }

    /**
     * Get default time period for financial summaries
     * @return "daily", "weekly", or "monthly" (default: "monthly")
     */
    public String getDefaultPeriod() {
        return sharedPreferences.getString(KEY_DEFAULT_PERIOD, "monthly");
    }

    // ===================== UTILITY METHODS =====================

    /**
     * Clear all preferences
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    /**
     * Save a custom string preference
     */
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get a custom string preference
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Save a custom boolean preference
     */
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get a custom boolean preference
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save a custom int preference
     */
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Get a custom int preference
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
}
