package com.example.fourquadrant.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 任务AI功能权限管理器
 */
public class TaskFunctionPermissionManager {
    
    private static final String PREF_NAME = "task_function_permissions";
    private static final String KEY_CREATE_TASK = "create_task";
    private static final String KEY_VIEW_TASK = "view_task";
    private static final String KEY_EDIT_TASK = "edit_task";
    private static final String KEY_DELETE_TASK = "delete_task";
    private static final String KEY_TASK_ANALYSIS = "task_analysis";
    private static final String KEY_TASK_REMINDER = "task_reminder";
    
    private static TaskFunctionPermissionManager instance;
    private SharedPreferences preferences;
    
    private TaskFunctionPermissionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized TaskFunctionPermissionManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskFunctionPermissionManager(context.getApplicationContext());
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
    
    // 功能ID常量
    public static final String FUNCTION_CREATE_TASK = KEY_CREATE_TASK;
    public static final String FUNCTION_VIEW_TASK = KEY_VIEW_TASK;
    public static final String FUNCTION_EDIT_TASK = KEY_EDIT_TASK;
    public static final String FUNCTION_DELETE_TASK = KEY_DELETE_TASK;
    public static final String FUNCTION_TASK_ANALYSIS = KEY_TASK_ANALYSIS;
    public static final String FUNCTION_TASK_REMINDER = KEY_TASK_REMINDER;
}