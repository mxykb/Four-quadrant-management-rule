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
        
        // 4. 演示番茄钟控制功能
        demonstratePomodoroControl();
        
        // 5. 演示番茄钟休息流程控制
        demonstratePomodoroBreakControl();
        
        // 6. 演示番茄钟完成流程控制
        demonstratePomodoroCompletionControl();
        
        // 7. 演示番茄钟设置管理
        demonstratePomodoroSettingsControl();
        
        // 8. 演示番茄钟历史记录查询
        demonstratePomodoroHistoryControl();
        
        // 9. 演示打开统计页面
        demonstrateOpenStatistics();
        
        // 10. 演示切换深色模式
        demonstrateToggleDarkMode();
        
        // 11. 演示错误处理
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
    }
    
    /**
     * 演示番茄钟控制功能
     */
    private static void demonstratePomodoroControl() {
        Log.i(TAG, "--- 演示番茄钟控制 ---");
        
        // 暂停番茄钟
        Map<String, Object> args1 = new HashMap<>();
        args1.put("action", "pause");
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("pause_pomodoro", args1);
        Log.i(TAG, "暂停番茄钟结果: " + result1);
        
        // 恢复番茄钟
        Map<String, Object> args2 = new HashMap<>();
        args2.put("action", "resume");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("resume_pomodoro", args2);
        Log.i(TAG, "恢复番茄钟结果: " + result2);
        
        // 停止番茄钟
        Map<String, Object> args3 = new HashMap<>();
        args3.put("action", "stop");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("stop_pomodoro", args3);
        Log.i(TAG, "停止番茄钟结果: " + result3);
        
        // 查询状态
        Map<String, Object> args4 = new HashMap<>();
        args4.put("action", "status");
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("get_pomodoro_status", args4);
        Log.i(TAG, "查询状态结果: " + result4);
    }
    
    /**
     * 演示番茄钟休息流程控制
     */
    private static void demonstratePomodoroBreakControl() {
        Log.i(TAG, "--- 演示番茄钟休息流程控制 ---");
        
        // 开始休息
        Map<String, Object> args1 = new HashMap<>();
        args1.put("action", "start");
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("start_break", args1);
        Log.i(TAG, "开始休息结果: " + result1);
        
        // 跳过休息
        Map<String, Object> args2 = new HashMap<>();
        args2.put("action", "skip");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("skip_break", args2);
        Log.i(TAG, "跳过休息结果: " + result2);
    }
    
    /**
     * 演示番茄钟完成流程控制
     */
    private static void demonstratePomodoroCompletionControl() {
        Log.i(TAG, "--- 演示番茄钟完成流程控制 ---");
        
        // 完成番茄钟
        Map<String, Object> args1 = new HashMap<>();
        args1.put("action", "complete");
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("complete_pomodoro", args1);
        Log.i(TAG, "完成番茄钟结果: " + result1);
        
        // 关闭番茄钟
        Map<String, Object> args2 = new HashMap<>();
        args2.put("action", "close");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("close_pomodoro", args2);
        Log.i(TAG, "关闭番茄钟结果: " + result2);
        
        // 重置番茄钟
        Map<String, Object> args3 = new HashMap<>();
        args3.put("action", "reset");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("reset_pomodoro", args3);
        Log.i(TAG, "重置番茄钟结果: " + result3);
    }
    
    /**
     * 演示番茄钟设置管理
     */
    private static void demonstratePomodoroSettingsControl() {
        Log.i(TAG, "--- 演示番茄钟设置管理 ---");
        
        // 设置番茄钟配置
        Map<String, Object> args1 = new HashMap<>();
        args1.put("action", "set");
        args1.put("tomato_count", 6);
        args1.put("tomato_duration", 30);
        args1.put("break_duration", 8);
        args1.put("auto_next", true);
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("set_pomodoro_settings", args1);
        Log.i(TAG, "设置配置结果: " + result1);
        
        // 查询当前设置
        Map<String, Object> args2 = new HashMap<>();
        args2.put("action", "get");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("get_pomodoro_settings", args2);
        Log.i(TAG, "查询设置结果: " + result2);
        
        // 重置为默认设置
        Map<String, Object> args3 = new HashMap<>();
        args3.put("action", "reset");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("reset_pomodoro_settings", args3);
        Log.i(TAG, "重置设置结果: " + result3);
        
        // 部分设置（只修改工作时长）
        Map<String, Object> args4 = new HashMap<>();
        args4.put("action", "set");
        args4.put("tomato_duration", 45);
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("set_pomodoro_settings", args4);
        Log.i(TAG, "部分设置结果: " + result4);
    }
    
    /**
     * 演示番茄钟历史记录查询
     */
    private static void demonstratePomodoroHistoryControl() {
        Log.i(TAG, "--- 演示番茄钟历史记录查询 ---");
        
        // 查询最近记录
        Map<String, Object> args1 = new HashMap<>();
        args1.put("action", "recent");
        args1.put("limit", 5);
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("get_pomodoro_history", args1);
        Log.i(TAG, "查询最近记录结果: " + result1);
        
        // 查询今天记录
        Map<String, Object> args2 = new HashMap<>();
        args2.put("action", "today");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("get_pomodoro_history", args2);
        Log.i(TAG, "查询今天记录结果: " + result2);
        
        // 查询本周记录
        Map<String, Object> args3 = new HashMap<>();
        args3.put("action", "week");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("get_pomodoro_history", args3);
        Log.i(TAG, "查询本周记录结果: " + result3);
        
        // 查询本月记录
        Map<String, Object> args4 = new HashMap<>();
        args4.put("action", "month");
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("get_pomodoro_history", args4);
        Log.i(TAG, "查询本月记录结果: " + result4);
        
        // 查询统计数据
        Map<String, Object> args5 = new HashMap<>();
        args5.put("action", "stats");
        CommandRouter.ExecutionResult result5 = CommandRouter.executeCommand("get_pomodoro_stats", args5);
        Log.i(TAG, "查询统计数据结果: " + result5);
        
        // 查询特定任务记录
        Map<String, Object> args6 = new HashMap<>();
        args6.put("action", "task");
        args6.put("task_name", "专注工作");
        CommandRouter.ExecutionResult result6 = CommandRouter.executeCommand("get_pomodoro_history", args6);
        Log.i(TAG, "查询任务记录结果: " + result6);
    }
    
    /**
     * 演示打开统计页面功能
     */
    private static void demonstrateOpenStatistics() {
        Log.i(TAG, "--- 演示打开统计页面 ---");
        
        // 打开通用统计页面
        Map<String, Object> args1 = new HashMap<>();
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("open_statistics", args1);
        Log.i(TAG, "通用统计页面结果: " + result1);
        
        // 打开每日统计页面
        Map<String, Object> args2 = new HashMap<>();
        args2.put("type", "daily");
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("open_statistics", args2);
        Log.i(TAG, "每日统计页面结果: " + result2);
        
        // 打开每周统计页面
        Map<String, Object> args3 = new HashMap<>();
        args3.put("type", "weekly");
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("open_statistics", args3);
        Log.i(TAG, "每周统计页面结果: " + result3);
        
        // 打开每月统计页面
        Map<String, Object> args4 = new HashMap<>();
        args4.put("type", "monthly");
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("open_statistics", args4);
        Log.i(TAG, "每月统计页面结果: " + result4);
        
        // 无效的统计类型
        Map<String, Object> args5 = new HashMap<>();
        args5.put("type", "invalid");
        CommandRouter.ExecutionResult result5 = CommandRouter.executeCommand("open_statistics", args5);
        Log.i(TAG, "无效统计类型结果: " + result5);
    }
    
    /**
     * 演示切换深色模式功能
     */
    private static void demonstrateToggleDarkMode() {
        Log.i(TAG, "--- 演示切换深色模式 ---");
        
        // 切换深色模式（自动切换）
        Map<String, Object> args1 = new HashMap<>();
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("toggle_dark_mode", args1);
        Log.i(TAG, "自动切换深色模式结果: " + result1);
        
        // 开启深色模式
        Map<String, Object> args2 = new HashMap<>();
        args2.put("enable", true);
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("toggle_dark_mode", args2);
        Log.i(TAG, "开启深色模式结果: " + result2);
        
        // 关闭深色模式
        Map<String, Object> args3 = new HashMap<>();
        args3.put("enable", false);
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("toggle_dark_mode", args3);
        Log.i(TAG, "关闭深色模式结果: " + result3);
        
        // 使用字符串参数
        Map<String, Object> args4 = new HashMap<>();
        args4.put("enable", "true");
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("toggle_dark_mode", args4);
        Log.i(TAG, "字符串参数开启深色模式结果: " + result4);
        
        // 使用数字参数
        Map<String, Object> args5 = new HashMap<>();
        args5.put("enable", "1");
        CommandRouter.ExecutionResult result5 = CommandRouter.executeCommand("toggle_dark_mode", args5);
        Log.i(TAG, "数字参数开启深色模式结果: " + result5);
    }
    
    /**
     * 演示错误处理
     */
    private static void demonstrateErrorHandling() {
        Log.i(TAG, "--- 演示错误处理 ---");
        
        // 执行不存在的功能
        Map<String, Object> args1 = new HashMap<>();
        CommandRouter.ExecutionResult result1 = CommandRouter.executeCommand("nonexistent_function", args1);
        Log.i(TAG, "不存在功能结果: " + result1);
        
        // 使用空参数
        Map<String, Object> args2 = new HashMap<>();
        CommandRouter.ExecutionResult result2 = CommandRouter.executeCommand("start_pomodoro", args2);
        Log.i(TAG, "空参数结果: " + result2);
        
        // 使用无效参数
        Map<String, Object> args3 = new HashMap<>();
        args3.put("task_name", ""); // 空字符串
        CommandRouter.ExecutionResult result3 = CommandRouter.executeCommand("start_pomodoro", args3);
        Log.i(TAG, "无效参数结果: " + result3);
        
        // 使用错误类型参数
        Map<String, Object> args4 = new HashMap<>();
        args4.put("task_name", 123); // 应该是字符串
        CommandRouter.ExecutionResult result4 = CommandRouter.executeCommand("start_pomodoro", args4);
        Log.i(TAG, "错误类型参数结果: " + result4);
    }
}