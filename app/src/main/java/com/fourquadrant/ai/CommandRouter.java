package com.fourquadrant.ai;

import android.content.Context;
import android.util.Log;

import com.fourquadrant.ai.commands.OpenStatistics;
import com.fourquadrant.ai.commands.StartPomodoro;
import com.fourquadrant.ai.commands.TaskManagement;
import com.fourquadrant.ai.commands.ToggleDarkMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 命令路由器 - 功能注册表和执行器
 * 使用注册表/工厂模式实现AI功能的动态路由
 */
public class CommandRouter {
    private static final String TAG = "CommandRouter";
    private static final Map<String, AiExecutable> toolRegistry = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * 初始化功能注册表
     * @param context 应用上下文
     */
    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        
        try {
            // 注册核心功能
            toolRegistry.put("start_pomodoro", new StartPomodoro(context));
            toolRegistry.put("open_statistics", new OpenStatistics(context));
            toolRegistry.put("toggle_dark_mode", new ToggleDarkMode(context));
            toolRegistry.put("task_management", new TaskManagement(context));
            
            // TODO: 在这里注册更多功能
            // toolRegistry.put("create_reminder", new CreateReminder(context));
            // toolRegistry.put("export_data", new ExportData(context));
            
            initialized = true;
            Log.i(TAG, "命令路由器初始化完成，注册了 " + toolRegistry.size() + " 个功能");
            
        } catch (Exception e) {
            Log.e(TAG, "命令路由器初始化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行指定功能
     * @param functionName 功能名称
     * @param args 参数映射
     * @return 执行结果
     */
    public static ExecutionResult executeCommand(String functionName, Map<String, Object> args) {
        if (!initialized) {
            Log.w(TAG, "命令路由器未初始化");
            return ExecutionResult.failure("命令路由器未初始化");
        }
        
        if (functionName == null || functionName.trim().isEmpty()) {
            Log.w(TAG, "功能名称为空");
            return ExecutionResult.failure("功能名称不能为空");
        }
        
        AiExecutable command = toolRegistry.get(functionName.trim());
        if (command == null) {
            Log.w(TAG, "未找到功能：" + functionName);
            return ExecutionResult.failure("未找到功能：" + functionName);
        }
        
        // 检查工具是否启用
        if (!isToolEnabled(functionName.trim())) {
            Log.w(TAG, "功能已禁用：" + functionName);
            return ExecutionResult.failure("功能已禁用：" + functionName);
        }
        
        try {
            // 参数验证
            Map<String, Object> safeArgs = args != null ? args : new HashMap<>();
            if (!command.validateArgs(safeArgs)) {
                Log.w(TAG, "功能 " + functionName + " 参数验证失败");
                return ExecutionResult.failure("参数验证失败");
            }
            
            // 执行功能
            Log.d(TAG, "执行功能：" + functionName + "，参数：" + safeArgs);
            command.execute(safeArgs);
            
            Log.i(TAG, "功能 " + functionName + " 执行成功");
            return ExecutionResult.success("功能执行成功");
            
        } catch (Exception e) {
            Log.e(TAG, "功能 " + functionName + " 执行失败: " + e.getMessage(), e);
            return ExecutionResult.failure("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 注册新功能
     * @param functionName 功能名称
     * @param executable 功能实现
     */
    public static void registerFunction(String functionName, AiExecutable executable) {
        if (functionName != null && executable != null) {
            toolRegistry.put(functionName, executable);
            Log.i(TAG, "注册新功能：" + functionName);
        }
    }
    
    /**
     * 注销功能
     * @param functionName 功能名称
     */
    public static void unregisterFunction(String functionName) {
        if (toolRegistry.remove(functionName) != null) {
            Log.i(TAG, "注销功能：" + functionName);
        }
    }
    
    /**
     * 获取所有已注册的功能名称
     * @return 功能名称集合
     */
    public static Set<String> getRegisteredFunctions() {
        return toolRegistry.keySet();
    }
    
    /**
     * 获取功能描述
     * @param functionName 功能名称
     * @return 功能描述
     */
    public static String getFunctionDescription(String functionName) {
        AiExecutable command = toolRegistry.get(functionName);
        return command != null ? command.getDescription() : "未知功能";
    }
    
    /**
     * 检查功能是否存在
     * @param functionName 功能名称
     * @return 是否存在
     */
    public static boolean hasFunction(String functionName) {
        return toolRegistry.containsKey(functionName);
    }
    
    /**
     * 获取所有功能的信息
     * @return 功能信息映射
     */
    public static Map<String, String> getAllFunctionInfo() {
        Map<String, String> info = new HashMap<>();
        for (Map.Entry<String, AiExecutable> entry : toolRegistry.entrySet()) {
            info.put(entry.getKey(), entry.getValue().getDescription());
        }
        return info;
    }
    
    /**
     * 获取所有已注册工具的详细信息
     * @return 工具详细信息映射
     */
    public static Map<String, AiExecutable> getAllRegisteredTools() {
        return new HashMap<>(toolRegistry);
    }
    
    // 工具启用状态管理
    private static final Map<String, Boolean> toolEnabledStatus = new HashMap<>();
    
    /**
     * 设置工具启用状态
     * @param functionName 功能名称
     * @param enabled 是否启用
     */
    public static void setToolEnabled(String functionName, boolean enabled) {
        toolEnabledStatus.put(functionName, enabled);
    }
    
    /**
     * 获取工具启用状态
     * @param functionName 功能名称
     * @return 是否启用，默认为true
     */
    public static boolean isToolEnabled(String functionName) {
        return toolEnabledStatus.getOrDefault(functionName, true);
    }
    
    /**
     * 获取所有工具的启用状态
     * @return 工具启用状态映射
     */
    public static Map<String, Boolean> getAllToolEnabledStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (String functionName : toolRegistry.keySet()) {
            status.put(functionName, isToolEnabled(functionName));
        }
        return status;
    }
    
    /**
     * 执行结果类
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String message;
        
        private ExecutionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static ExecutionResult success(String message) {
            return new ExecutionResult(true, message);
        }
        
        public static ExecutionResult failure(String message) {
            return new ExecutionResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "ExecutionResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}