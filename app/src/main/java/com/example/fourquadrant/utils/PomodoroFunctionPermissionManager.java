package com.example.fourquadrant.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 番茄钟AI功能权限管理器
 */
public class PomodoroFunctionPermissionManager {
    
    private static final String PREF_NAME = "pomodoro_function_permissions";
    
    // 基础番茄钟控制功能
    private static final String KEY_START_POMODORO = "start_pomodoro";
    private static final String KEY_PAUSE_POMODORO = "pause_pomodoro";
    private static final String KEY_RESUME_POMODORO = "resume_pomodoro";
    private static final String KEY_STOP_POMODORO = "stop_pomodoro";
    private static final String KEY_GET_STATUS = "get_pomodoro_status";
    
    // 番茄钟休息流程控制功能
    private static final String KEY_START_BREAK = "start_break";
    private static final String KEY_SKIP_BREAK = "skip_break";
    
    // 番茄钟完成流程控制功能
    private static final String KEY_COMPLETE_POMODORO = "complete_pomodoro";
    private static final String KEY_CLOSE_POMODORO = "close_pomodoro";
    private static final String KEY_RESET_POMODORO = "reset_pomodoro";
    
    // 番茄钟设置管理功能
    private static final String KEY_SET_POMODORO_SETTINGS = "set_pomodoro_settings";
    private static final String KEY_GET_POMODORO_SETTINGS = "get_pomodoro_settings";
    private static final String KEY_RESET_POMODORO_SETTINGS = "reset_pomodoro_settings";
    
    // 番茄钟历史记录查询功能
    private static final String KEY_GET_POMODORO_HISTORY = "get_pomodoro_history";
    private static final String KEY_GET_POMODORO_STATS = "get_pomodoro_stats";
    
    // 番茄钟设置和分析功能（原有）
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
            // 基础控制功能
            isFunctionEnabled(FUNCTION_START_POMODORO),
            isFunctionEnabled(FUNCTION_PAUSE_POMODORO),
            isFunctionEnabled(FUNCTION_RESUME_POMODORO),
            isFunctionEnabled(FUNCTION_STOP_POMODORO),
            isFunctionEnabled(FUNCTION_GET_STATUS),
            
            // 休息流程控制
            isFunctionEnabled(FUNCTION_START_BREAK),
            isFunctionEnabled(FUNCTION_SKIP_BREAK),
            
            // 完成流程控制
            isFunctionEnabled(FUNCTION_COMPLETE_POMODORO),
            isFunctionEnabled(FUNCTION_CLOSE_POMODORO),
            isFunctionEnabled(FUNCTION_RESET_POMODORO),
            
            // 设置管理
            isFunctionEnabled(FUNCTION_SET_POMODORO_SETTINGS),
            isFunctionEnabled(FUNCTION_GET_POMODORO_SETTINGS),
            isFunctionEnabled(FUNCTION_RESET_POMODORO_SETTINGS),
            
            // 历史记录查询
            isFunctionEnabled(FUNCTION_GET_POMODORO_HISTORY),
            isFunctionEnabled(FUNCTION_GET_POMODORO_STATS),
            
            // 原有功能
            isFunctionEnabled(FUNCTION_POMODORO_SETTINGS),
            isFunctionEnabled(FUNCTION_POMODORO_ANALYTICS)
        };
    }
    
    /**
     * 批量设置功能启用状态
     */
    public void setAllFunctionStatus(boolean[] statuses) {
        if (statuses.length >= 18) {
            SharedPreferences.Editor editor = preferences.edit();
            
            // 基础控制功能
            editor.putBoolean(FUNCTION_START_POMODORO, statuses[0]);
            editor.putBoolean(FUNCTION_PAUSE_POMODORO, statuses[1]);
            editor.putBoolean(FUNCTION_RESUME_POMODORO, statuses[2]);
            editor.putBoolean(FUNCTION_STOP_POMODORO, statuses[3]);
            editor.putBoolean(FUNCTION_GET_STATUS, statuses[4]);
            
            // 休息流程控制
            editor.putBoolean(FUNCTION_START_BREAK, statuses[5]);
            editor.putBoolean(FUNCTION_SKIP_BREAK, statuses[6]);
            
            // 完成流程控制
            editor.putBoolean(FUNCTION_COMPLETE_POMODORO, statuses[7]);
            editor.putBoolean(FUNCTION_CLOSE_POMODORO, statuses[8]);
            editor.putBoolean(FUNCTION_RESET_POMODORO, statuses[9]);
            
            // 设置管理
            editor.putBoolean(FUNCTION_SET_POMODORO_SETTINGS, statuses[10]);
            editor.putBoolean(FUNCTION_GET_POMODORO_SETTINGS, statuses[11]);
            editor.putBoolean(FUNCTION_RESET_POMODORO_SETTINGS, statuses[12]);
            
            // 历史记录查询
            editor.putBoolean(FUNCTION_GET_POMODORO_HISTORY, statuses[13]);
            editor.putBoolean(FUNCTION_GET_POMODORO_STATS, statuses[14]);
            
            // 原有功能
            editor.putBoolean(FUNCTION_POMODORO_SETTINGS, statuses[15]);
            editor.putBoolean(FUNCTION_POMODORO_ANALYTICS, statuses[16]);
            
            editor.apply();
        }
    }
    
    // 功能ID常量
    // 基础控制功能
    public static final String FUNCTION_START_POMODORO = KEY_START_POMODORO;
    public static final String FUNCTION_PAUSE_POMODORO = KEY_PAUSE_POMODORO;
    public static final String FUNCTION_RESUME_POMODORO = KEY_RESUME_POMODORO;
    public static final String FUNCTION_STOP_POMODORO = KEY_STOP_POMODORO;
    public static final String FUNCTION_GET_STATUS = KEY_GET_STATUS;
    
    // 休息流程控制功能
    public static final String FUNCTION_START_BREAK = KEY_START_BREAK;
    public static final String FUNCTION_SKIP_BREAK = KEY_SKIP_BREAK;
    
    // 完成流程控制功能
    public static final String FUNCTION_COMPLETE_POMODORO = KEY_COMPLETE_POMODORO;
    public static final String FUNCTION_CLOSE_POMODORO = KEY_CLOSE_POMODORO;
    public static final String FUNCTION_RESET_POMODORO = KEY_RESET_POMODORO;
    
    // 设置管理功能
    public static final String FUNCTION_SET_POMODORO_SETTINGS = KEY_SET_POMODORO_SETTINGS;
    public static final String FUNCTION_GET_POMODORO_SETTINGS = KEY_GET_POMODORO_SETTINGS;
    public static final String FUNCTION_RESET_POMODORO_SETTINGS = KEY_RESET_POMODORO_SETTINGS;
    
    // 历史记录查询功能
    public static final String FUNCTION_GET_POMODORO_HISTORY = KEY_GET_POMODORO_HISTORY;
    public static final String FUNCTION_GET_POMODORO_STATS = KEY_GET_POMODORO_STATS;
    
    // 原有功能
    public static final String FUNCTION_POMODORO_SETTINGS = KEY_POMODORO_SETTINGS;
    public static final String FUNCTION_POMODORO_ANALYTICS = KEY_POMODORO_ANALYTICS;
}