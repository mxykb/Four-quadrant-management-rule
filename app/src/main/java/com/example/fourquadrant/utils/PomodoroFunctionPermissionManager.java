package com.example.fourquadrant.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 番茄钟AI功能权限管理器
 */
public class PomodoroFunctionPermissionManager {
    
    private static final String PREF_NAME = "pomodoro_function_permissions";
    private static final String KEY_PAUSE_POMODORO = "pause_pomodoro";
    private static final String KEY_RESUME_POMODORO = "resume_pomodoro";
    private static final String KEY_STOP_POMODORO = "stop_pomodoro";
    private static final String KEY_GET_STATUS = "get_pomodoro_status";
    private static final String KEY_START_POMODORO = "start_pomodoro";
    private static final String KEY_POMODORO_SETTINGS = "pomodoro_settings";
    private static final String KEY_POMODORO_ANALYTICS = "pomodoro_analytics";
    
    private static PomodoroFunctionPermissionManager instance;
    private SharedPreferences preferences;
    
    private PomodoroFunctionPermissionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized PomodoroFunctionPermissionManager getInstance(Context context) {
        if (instance == null) {
            instance = new PomodoroFunctionPermissionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 检查功能是否启用
     */
    public boolean isFunctionEnabled(String functionId) {
        return preferences.getBoolean(functionId, true); // 默认启用
    }
    
    /**
     * 设置功能启用状态
     */
    public void setFunctionEnabled(String functionId, boolean enabled) {
        preferences.edit().putBoolean(functionId, enabled).apply();
    }
    
    /**
     * 重置所有功能权限为默认状态
     */
    public void resetAllPermissions() {
        preferences.edit().clear().apply();
    }
    
    /**
     * 获取所有功能的启用状态
     */
    public boolean[] getAllFunctionStatus() {
        return new boolean[] {
            isFunctionEnabled(FUNCTION_START_POMODORO),
            isFunctionEnabled(FUNCTION_PAUSE_POMODORO),
            isFunctionEnabled(FUNCTION_RESUME_POMODORO),
            isFunctionEnabled(FUNCTION_STOP_POMODORO),
            isFunctionEnabled(FUNCTION_GET_STATUS),
            isFunctionEnabled(FUNCTION_POMODORO_SETTINGS),
            isFunctionEnabled(FUNCTION_POMODORO_ANALYTICS)
        };
    }
    
    /**
     * 批量设置功能启用状态
     */
    public void setAllFunctionStatus(boolean[] statuses) {
        if (statuses.length >= 7) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FUNCTION_START_POMODORO, statuses[0]);
            editor.putBoolean(FUNCTION_PAUSE_POMODORO, statuses[1]);
            editor.putBoolean(FUNCTION_RESUME_POMODORO, statuses[2]);
            editor.putBoolean(FUNCTION_STOP_POMODORO, statuses[3]);
            editor.putBoolean(FUNCTION_GET_STATUS, statuses[4]);
            editor.putBoolean(FUNCTION_POMODORO_SETTINGS, statuses[5]);
            editor.putBoolean(FUNCTION_POMODORO_ANALYTICS, statuses[6]);
            editor.apply();
        }
    }
    
    // 功能ID常量
    public static final String FUNCTION_START_POMODORO = KEY_START_POMODORO;
    public static final String FUNCTION_PAUSE_POMODORO = KEY_PAUSE_POMODORO;
    public static final String FUNCTION_RESUME_POMODORO = KEY_RESUME_POMODORO;
    public static final String FUNCTION_STOP_POMODORO = KEY_STOP_POMODORO;
    public static final String FUNCTION_GET_STATUS = KEY_GET_STATUS;
    public static final String FUNCTION_POMODORO_SETTINGS = KEY_POMODORO_SETTINGS;
    public static final String FUNCTION_POMODORO_ANALYTICS = KEY_POMODORO_ANALYTICS;
}