package com.fourquadrant.ai;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * AI命令系统使用示例
 * 展示如何使用命令路由器执行各种AI功能
 */
public class AiCommandDemo {
    private static final String TAG = "AiCommandDemo";
    
    /**
     * 演示AI命令系统的使用
     * @param context 应用上下文
     */
    public static void demonstrateUsage(Context context) {
        // 1. 初始化命令路由器
        CommandRouter.initialize(context);
        
        Log.i(TAG, "=== AI命令系统演示开始 ===");
        
        // 2. 显示所有可用功能
        showAvailableFunctions();
        
        // 3. 演示启动番茄钟
        demonstrateStartPomodoro();
        
        // 4. 演示打开统计页面
        demonstrateOpenStatistics();
        
        // 5. 演示切换深色模式
        demonstrateToggleDarkMode();
        
        // 6. 演示错误处理
        demonstrateErrorHandling();
        
        Log.i(TAG, "=== AI命令系统演示结束 ===");
    }
    
    /**
     * 显示所有可用功能
     */
    private static void showAvailableFunctions() {
        Log.i(TAG, "--- 可用功能列表 ---");
        Map<String, String> functions = CommandRouter.getAllFunctionInfo();
        for (Map.Entry<String, String> entry : functions.entrySet()) {
            Log.i(TAG, entry.getKey() + ": " + entry.getValue());
        }
    }
    
    /**
     * 演示启动番茄钟功能
     */
    private static void demonstrateStartPomodoro() {
        Log.i(TAG, "--- 演示启动番茄钟 ---");
        
        // 使用默认时长（必须指定任务名称）
        Map<String, Object> args1 = new HashMap<>();
        args1.put("task_name", "专注工作");
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("start_pomodoro", args1);
        Log.i(TAG, "默认时长结果: " + result1);
        
        // 指定时长
        Map<String, Object> args2 = new HashMap<>();
        args2.put("task_name", "重要任务");
        args2.put("duration", 30);
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("start_pomodoro", args2);
        Log.i(TAG, "指定时长结果: " + result2);
        
        // 关联任务的番茄钟
        Map<String, Object> args3 = new HashMap<>();
        args3.put("duration", 25);
        args3.put("task_name", "完成项目报告");
        args3.put("task_id", "task_001");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("start_pomodoro", args3);
        Log.i(TAG, "关联任务结果: " + result3);
        
        // 只有任务名称，没有ID
        Map<String, Object> args4 = new HashMap<>();
        args4.put("task_name", "学习新技术");
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("start_pomodoro", args4);
        Log.i(TAG, "仅任务名称结果: " + result4);
        
        // 缺少必需的task_name参数
        Map<String, Object> args5 = new HashMap<>();
        args5.put("duration", 25);
        CommandRouter.ExecutionResult result5 = CommandRouter.executeCommand("start_pomodoro", args5);
        Log.i(TAG, "缺少task_name结果: " + result5);
        
        // 无效时长
        Map<String, Object> args6 = new HashMap<>();
        args6.put("task_name", "测试任务");
        args6.put("duration", 150); // 超出范围
        CommandRouter.ExecutionResult result6 = CommandRouter.executeCommand("start_pomodoro", args6);
        Log.i(TAG, "无效时长结果: " + result6);
        
        // 空任务名称
        Map<String, Object> args7 = new HashMap<>();
        args7.put("task_name", ""); // 空字符串
        CommandRouter.ExecutionResult result7 = CommandRouter.executeCommand("start_pomodoro", args7);
        Log.i(TAG, "空任务名称结果: " + result7);
    }
    
    /**
     * 演示打开统计页面功能
     */
    private static void demonstrateOpenStatistics() {
        Log.i(TAG, "--- 演示打开统计页面 ---");
        
        // 默认统计
        Map<String, Object> args1 = new HashMap<>();
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("open_statistics", args1);
        Log.i(TAG, "默认统计结果: " + result1);
        
        // 指定统计类型
        Map<String, Object> args2 = new HashMap<>();
        args2.put("type", "weekly");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("open_statistics", args2);
        Log.i(TAG, "周统计结果: " + result2);
        
        // 无效统计类型
        Map<String, Object> args3 = new HashMap<>();
        args3.put("type", "invalid_type");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("open_statistics", args3);
        Log.i(TAG, "无效类型结果: " + result3);
    }
    
    /**
     * 演示切换深色模式功能
     */
    private static void demonstrateToggleDarkMode() {
        Log.i(TAG, "--- 演示切换深色模式 ---");
        
        // 切换模式
        Map<String, Object> args1 = new HashMap<>();
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("toggle_dark_mode", args1);
        Log.i(TAG, "切换模式结果: " + result1);
        
        // 启用深色模式
        Map<String, Object> args2 = new HashMap<>();
        args2.put("enable", true);
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("toggle_dark_mode", args2);
        Log.i(TAG, "启用深色模式结果: " + result2);
        
        // 禁用深色模式
        Map<String, Object> args3 = new HashMap<>();
        args3.put("enable", false);
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("toggle_dark_mode", args3);
        Log.i(TAG, "禁用深色模式结果: " + result3);
    }
    
    /**
     * 演示错误处理
     */
    private static void demonstrateErrorHandling() {
        Log.i(TAG, "--- 演示错误处理 ---");
        
        // 不存在的功能
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("non_existent_function", new HashMap<>());
        Log.i(TAG, "不存在功能结果: " + result1);
        
        // 空功能名
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("", new HashMap<>());
        Log.i(TAG, "空功能名结果: " + result2);
        
        // null功能名
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand(null, new HashMap<>());
        Log.i(TAG, "null功能名结果: " + result3);
    }
    
    /**
     * 模拟AI调用场景
     * @param context 应用上下文
     * @param aiCommand AI命令字符串
     */
    public static void simulateAiCall(Context context, String aiCommand) {
        // 确保路由器已初始化
        CommandRouter.initialize(context);
        
        Log.i(TAG, "AI调用: " + aiCommand);
        
        // 简单的命令解析示例
        CommandRouter.ExecutionResult result;
        Map<String, Object> args = new HashMap<>();
        
        if (aiCommand.contains("开始番茄钟") || aiCommand.contains("start pomodoro")) {
            // 解析时长参数
            if (aiCommand.contains("30分钟") || aiCommand.contains("30 minutes")) {
                args.put("duration", 30);
            } else if (aiCommand.contains("45分钟") || aiCommand.contains("45 minutes")) {
                args.put("duration", 45);
            }
            result = CommandRouter.executeCommand("start_pomodoro", args);
        } else if (aiCommand.contains("打开统计") || aiCommand.contains("open statistics")) {
            // 解析统计类型
            if (aiCommand.contains("周") || aiCommand.contains("weekly")) {
                args.put("type", "weekly");
            } else if (aiCommand.contains("月") || aiCommand.contains("monthly")) {
                args.put("type", "monthly");
            }
            result = CommandRouter.executeCommand("open_statistics", args);
        } else if (aiCommand.contains("深色模式") || aiCommand.contains("dark mode")) {
            if (aiCommand.contains("开启") || aiCommand.contains("enable")) {
                args.put("enable", true);
            } else if (aiCommand.contains("关闭") || aiCommand.contains("disable")) {
                args.put("enable", false);
            }
            result = CommandRouter.executeCommand("toggle_dark_mode", args);
        } else {
            result = CommandRouter.ExecutionResult.failure("无法识别的AI命令: " + aiCommand);
        }
        
        Log.i(TAG, "AI调用结果: " + result);
    }
}