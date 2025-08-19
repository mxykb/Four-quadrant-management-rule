package com.fourquadrant.ai.commands;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.fourquadrant.PomodoroService;
import com.fourquadrant.ai.AiExecutable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 番茄钟控制功能实现
 * 支持暂停、恢复、停止和查询状态四个操作
 */
public class PomodoroControl implements AiExecutable {
    private static final String TAG = "PomodoroControl";
    private Context context;
    private String defaultAction;
    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;
    private CountDownLatch serviceLatch;
    
    public PomodoroControl(Context context) {
        this.context = context;
        this.defaultAction = "status";
    }
    
    public PomodoroControl(Context context, String defaultAction) {
        this.context = context;
        this.defaultAction = defaultAction;
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 从参数中获取操作类型，如果没有则使用默认操作
            String action = (String) args.get("action");
            if (action == null || action.isEmpty()) {
                action = defaultAction;
            }
            
            if (action == null || action.isEmpty()) {
                Log.e(TAG, "无法确定操作类型");
                return;
            }
            
            Log.i(TAG, "执行番茄钟控制操作: " + action);
            
            // 绑定服务并执行操作
            bindServiceAndExecute(action, args);
            
        } catch (Exception e) {
            Log.e(TAG, "执行番茄钟控制操作失败", e);
        }
    }
    

    
    /**
     * 绑定服务并执行操作
     */
    private void bindServiceAndExecute(String action, Map<String, Object> args) {
        serviceLatch = new CountDownLatch(1);
        
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PomodoroService.PomodoroServiceBinder binder = (PomodoroService.PomodoroServiceBinder) service;
                pomodoroService = binder.getService();
                isServiceBound = true;
                serviceLatch.countDown();
                Log.d(TAG, "服务连接成功");
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                pomodoroService = null;
                isServiceBound = false;
                Log.d(TAG, "服务连接断开");
            }
        };
        
        // 启动并绑定服务
        Intent serviceIntent = new Intent(context, PomodoroService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // 在新线程中等待服务连接并执行操作
        new Thread(() -> {
            try {
                // 等待服务连接，最多等待5秒
                if (serviceLatch.await(5, TimeUnit.SECONDS) && isServiceBound) {
                    executeAction(action, args);
                } else {
                    Log.e(TAG, "服务连接超时或失败");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "等待服务连接被中断", e);
            } finally {
                // 解绑服务
                if (isServiceBound) {
                    context.unbindService(serviceConnection);
                    isServiceBound = false;
                }
            }
        }).start();
    }
    
    /**
     * 执行具体的番茄钟控制操作
     */
    private void executeAction(String action, Map<String, Object> args) {
        if (pomodoroService == null) {
            Log.e(TAG, "番茄钟服务未连接");
            return;
        }
        
        switch (action) {
            case "pause":
                pausePomodoro();
                break;
            case "resume":
                resumePomodoro();
                break;
            case "stop":
                stopPomodoro();
                break;
            case "status":
                getPomodoroStatus();
                break;
            default:
                Log.w(TAG, "未知的操作类型: " + action);
                break;
        }
    }
    
    /**
     * 暂停番茄钟
     */
    private void pausePomodoro() {
        try {
            pomodoroService.pauseTimer();
            Log.i(TAG, "番茄钟已暂停");
        } catch (Exception e) {
            Log.e(TAG, "暂停番茄钟失败", e);
        }
    }
    
    /**
     * 恢复番茄钟
     */
    private void resumePomodoro() {
        try {
            pomodoroService.resumeTimer();
            Log.i(TAG, "番茄钟已恢复");
        } catch (Exception e) {
            Log.e(TAG, "恢复番茄钟失败", e);
        }
    }
    
    /**
     * 停止番茄钟
     */
    private void stopPomodoro() {
        try {
            pomodoroService.closeByUser();
            Log.i(TAG, "番茄钟已停止并重置");
        } catch (Exception e) {
            Log.e(TAG, "停止番茄钟失败", e);
        }
    }
    
    /**
     * 获取番茄钟状态
     */
    private void getPomodoroStatus() {
        try {
            // 获取番茄钟状态信息
            boolean isRunning = pomodoroService.isTimerRunning();
            boolean isPaused = pomodoroService.isTimerPaused();
            long remainingTime = pomodoroService.getRemainingTime();
            boolean isBreakTime = pomodoroService.isBreakTime();
            int currentCount = pomodoroService.getCurrentTomatoCount();
            String taskName = pomodoroService.getCurrentTaskName();
            
            // 格式化剩余时间
            String formattedTime = formatTime(remainingTime);
            
            // 构建状态信息
            StringBuilder statusInfo = new StringBuilder();
            statusInfo.append("番茄钟状态:\n");
            statusInfo.append("运行状态: ").append(isRunning ? "运行中" : "已停止").append("\n");
            if (isRunning) {
                statusInfo.append("暂停状态: ").append(isPaused ? "已暂停" : "正在计时").append("\n");
            }
            statusInfo.append("剩余时间: ").append(formattedTime).append("\n");
            statusInfo.append("当前模式: ").append(isBreakTime ? "休息时间" : "工作时间").append("\n");
            statusInfo.append("完成数量: ").append(currentCount).append("个番茄钟\n");
            statusInfo.append("当前任务: ").append(taskName);
            
            Log.i(TAG, statusInfo.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "查询番茄钟状态失败", e);
        }
    }
    
    /**
     * 格式化时间显示
     */
    private String formatTime(long timeInMillis) {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * 验证参数
     */
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        if (args == null) {
            return false;
        }
        
        String action = (String) args.get("action");
        if (action == null || action.isEmpty()) {
            return false;
        }
        
        // 验证操作类型是否有效
        return action.equals("pause") || action.equals("resume") || 
               action.equals("stop") || action.equals("status");
    }
    
    /**
     * 获取功能描述
     */
    @Override
    public String getDescription() {
        return "番茄钟控制功能，支持暂停(pause)、恢复(resume)、停止(stop)和查询状态(status)操作";
    }
    
    /**
     * 获取支持的参数
     */
    public static Map<String, String> getSupportedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "操作类型：pause(暂停)、resume(恢复)、stop(停止)、status(查询状态)");
        return params;
    }
}