package com.example.fourquadrant.utils;

import android.content.Context;
import android.util.Log;

/**
 * 权限系统测试类
 * 用于验证两层权限控制系统的完整性
 */
public class PermissionSystemTest {
    
    private static final String TAG = "PermissionSystemTest";
    
    /**
     * 测试模块权限管理
     */
    public static void testModulePermissions(Context context) {
        Log.d(TAG, "开始测试模块权限管理...");
        
        ModulePermissionManager moduleManager = ModulePermissionManager.getInstance(context);
        
        // 测试默认状态
        Log.d(TAG, "任务AI默认状态: " + moduleManager.isModuleEnabled("task_ai"));
        Log.d(TAG, "番茄钟AI默认状态: " + moduleManager.isModuleEnabled("pomodoro_ai"));
        Log.d(TAG, "统计AI默认状态: " + moduleManager.isModuleEnabled("statistics_ai"));
        Log.d(TAG, "设置AI默认状态: " + moduleManager.isModuleEnabled("settings_ai"));
        
        // 测试权限切换
        moduleManager.setModuleEnabled("pomodoro_ai", true);
        Log.d(TAG, "启用番茄钟AI后状态: " + moduleManager.isModuleEnabled("pomodoro_ai"));
        
        moduleManager.setModuleEnabled("task_ai", false);
        Log.d(TAG, "禁用任务AI后状态: " + moduleManager.isModuleEnabled("task_ai"));
        
        // 恢复默认状态
        moduleManager.resetAllPermissions();
        Log.d(TAG, "重置后任务AI状态: " + moduleManager.isModuleEnabled("task_ai"));
        
        Log.d(TAG, "模块权限管理测试完成");
    }
    
    /**
     * 测试任务功能权限管理
     */
    public static void testTaskFunctionPermissions(Context context) {
        Log.d(TAG, "开始测试任务功能权限管理...");
        
        TaskFunctionPermissionManager functionManager = TaskFunctionPermissionManager.getInstance(context);
        
        // 测试默认状态
        Log.d(TAG, "创建任务默认状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK));
        Log.d(TAG, "查看任务默认状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_VIEW_TASK));
        Log.d(TAG, "编辑任务默认状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_EDIT_TASK));
        Log.d(TAG, "删除任务默认状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_DELETE_TASK));
        
        // 测试权限切换
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_DELETE_TASK, false);
        Log.d(TAG, "禁用删除任务后状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_DELETE_TASK));
        
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_TASK_ANALYSIS, true);
        Log.d(TAG, "启用任务分析后状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_TASK_ANALYSIS));
        
        // 恢复默认状态
        functionManager.resetAllPermissions();
        Log.d(TAG, "重置后删除任务状态: " + functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_DELETE_TASK));
        
        Log.d(TAG, "任务功能权限管理测试完成");
    }
    
    /**
     * 测试两层权限控制逻辑
     */
    public static void testTwoLevelPermissionControl(Context context) {
        Log.d(TAG, "开始测试两层权限控制逻辑...");
        
        ModulePermissionManager moduleManager = ModulePermissionManager.getInstance(context);
        TaskFunctionPermissionManager functionManager = TaskFunctionPermissionManager.getInstance(context);
        
        // 场景1：模块启用，功能启用
        moduleManager.setModuleEnabled("task_ai", true);
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK, true);
        boolean canCreateTask1 = moduleManager.isModuleEnabled("task_ai") && 
                                functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK);
        Log.d(TAG, "场景1 - 模块启用，功能启用，可以创建任务: " + canCreateTask1);
        
        // 场景2：模块启用，功能禁用
        moduleManager.setModuleEnabled("task_ai", true);
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK, false);
        boolean canCreateTask2 = moduleManager.isModuleEnabled("task_ai") && 
                                functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK);
        Log.d(TAG, "场景2 - 模块启用，功能禁用，可以创建任务: " + canCreateTask2);
        
        // 场景3：模块禁用，功能启用
        moduleManager.setModuleEnabled("task_ai", false);
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK, true);
        boolean canCreateTask3 = moduleManager.isModuleEnabled("task_ai") && 
                                functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK);
        Log.d(TAG, "场景3 - 模块禁用，功能启用，可以创建任务: " + canCreateTask3);
        
        // 场景4：模块禁用，功能禁用
        moduleManager.setModuleEnabled("task_ai", false);
        functionManager.setFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK, false);
        boolean canCreateTask4 = moduleManager.isModuleEnabled("task_ai") && 
                                functionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK);
        Log.d(TAG, "场景4 - 模块禁用，功能禁用，可以创建任务: " + canCreateTask4);
        
        // 恢复默认状态
        moduleManager.resetAllPermissions();
        functionManager.resetAllPermissions();
        
        Log.d(TAG, "两层权限控制逻辑测试完成");
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests(Context context) {
        Log.d(TAG, "=== 开始权限系统完整性测试 ===");
        
        testModulePermissions(context);
        testTaskFunctionPermissions(context);
        testTwoLevelPermissionControl(context);
        
        Log.d(TAG, "=== 权限系统完整性测试完成 ===");
    }
}