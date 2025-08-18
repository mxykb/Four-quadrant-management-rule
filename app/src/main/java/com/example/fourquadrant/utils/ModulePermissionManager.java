package com.example.fourquadrant.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 模块权限管理器
 * 用于管理AI模块的启用/禁用状态
 */
public class ModulePermissionManager {
    
    private static final String PREF_NAME = "module_permissions";
    private static final String KEY_TASK_AI = "task_ai_enabled";
    private static final String KEY_POMODORO_AI = "pomodoro_ai_enabled";
    private static final String KEY_STATISTICS_AI = "statistics_ai_enabled";
    private static final String KEY_SETTINGS_AI = "settings_ai_enabled";
    
    private static ModulePermissionManager instance;
    private SharedPreferences preferences;
    
    private ModulePermissionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ModulePermissionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ModulePermissionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 检查模块是否启用
     */
    public boolean isModuleEnabled(String moduleId) {
        switch (moduleId) {
            case "task_ai":
                return preferences.getBoolean(KEY_TASK_AI, true); // 默认启用
            case "pomodoro_ai":
                return preferences.getBoolean(KEY_POMODORO_AI, false); // 默认禁用
            case "statistics_ai":
                return preferences.getBoolean(KEY_STATISTICS_AI, false); // 默认禁用
            case "settings_ai":
                return preferences.getBoolean(KEY_SETTINGS_AI, false); // 默认禁用
            default:
                return false;
        }
    }
    
    /**
     * 设置模块启用状态
     */
    public void setModuleEnabled(String moduleId, boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (moduleId) {
            case "task_ai":
                editor.putBoolean(KEY_TASK_AI, enabled);
                break;
            case "pomodoro_ai":
                editor.putBoolean(KEY_POMODORO_AI, enabled);
                break;
            case "statistics_ai":
                editor.putBoolean(KEY_STATISTICS_AI, enabled);
                break;
            case "settings_ai":
                editor.putBoolean(KEY_SETTINGS_AI, enabled);
                break;
        }
        editor.apply();
    }
    
    /**
     * 重置所有模块权限
     */
    public void resetAllPermissions() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_TASK_AI, true);
        editor.putBoolean(KEY_POMODORO_AI, false);
        editor.putBoolean(KEY_STATISTICS_AI, false);
        editor.putBoolean(KEY_SETTINGS_AI, false);
        editor.apply();
    }
}