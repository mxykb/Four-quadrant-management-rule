package com.fourquadrant.ai.commands;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.fourquadrant.PomodoroService;
import com.fourquadrant.ai.AiExecutable;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 番茄钟完成流程控制功能实现
 * 支持完成番茄钟和关闭重置操作
 */
public class PomodoroCompletionControl implements AiExecutable {
    private static final String TAG = "PomodoroCompletionControl";
    private Context context;
    private String defaultAction;
    
    public PomodoroCompletionControl(Context context) {
        this.context = context;
        this.defaultAction = "complete";
    }
    
    public PomodoroCompletionControl(Context context, String defaultAction) {
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
            
            Log.i(TAG, "执行番茄钟完成控制操作: " + action);
            
            // 绑定服务并执行操作
            bindServiceAndExecute(action, args);
            
        } catch (Exception e) {
            Log.e(TAG, "执行番茄钟完成控制操作失败", e);
        }
    }
    
    /**
     * 绑定服务并执行操作
     */
    private void bindServiceAndExecute(String action, Map<String, Object> args) {
        CountDownLatch serviceLatch = new CountDownLatch(1);
        final AtomicReference<PomodoroService> pomodoroServiceRef = new AtomicReference<>();
        final AtomicBoolean isServiceBoundRef = new AtomicBoolean(false);
        
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PomodoroService.PomodoroServiceBinder binder = (PomodoroService.PomodoroServiceBinder) service;
                pomodoroServiceRef.set(binder.getService());
                isServiceBoundRef.set(true);
                serviceLatch.countDown();
                Log.d(TAG, "服务连接成功");
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                pomodoroServiceRef.set(null);
                isServiceBoundRef.set(false);
                Log.d(TAG, "服务连接断开");
            }
        };
        
        try {
            // 启动并绑定服务
            Intent serviceIntent = new Intent(context, PomodoroService.class);
            context.startService(serviceIntent);
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            
            // 等待服务连接并执行操作
            if (serviceLatch.await(5, TimeUnit.SECONDS) && isServiceBoundRef.get() && pomodoroServiceRef.get() != null) {
                executeAction(action, pomodoroServiceRef.get(), args);
            } else {
                Log.e(TAG, "服务连接超时或失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "绑定服务失败", e);
        } finally {
            // 解绑服务
            if (isServiceBoundRef.get()) {
                try {
                    context.unbindService(serviceConnection);
                } catch (Exception e) {
                    Log.e(TAG, "解绑服务失败", e);
                }
            }
        }
    }
    
    /**
     * 执行具体的完成控制操作
     */
    private void executeAction(String action, PomodoroService pomodoroService, Map<String, Object> args) {
        if (pomodoroService == null) {
            Log.e(TAG, "番茄钟服务未连接");
            return;
        }
        
        switch (action.toLowerCase()) {
            case "complete":
                completePomodoro(pomodoroService);
                break;
            case "close":
                closePomodoro(pomodoroService);
                break;
            case "reset":
                resetPomodoro(pomodoroService);
                break;
            default:
                Log.w(TAG, "未知的操作类型: " + action);
                break;
        }
    }
    
    /**
     * 完成当前番茄钟
     */
    private void completePomodoro(PomodoroService pomodoroService) {
        try {
            // 检查当前状态
            if (pomodoroService.isTimerRunning() && !pomodoroService.isBreakTime()) {
                // 如果正在工作，强制完成
                Log.i(TAG, "强制完成当前番茄钟");
                // 这里可以调用服务的完成方法，如果有的话
                // pomodoroService.forceComplete();
            } else if (pomodoroService.isBreakTime()) {
                // 如果正在休息，跳过休息
                pomodoroService.skipBreakByUser();
                Log.i(TAG, "跳过休息，番茄钟完成");
            } else {
                Log.i(TAG, "番茄钟已完成或未运行");
            }
        } catch (Exception e) {
            Log.e(TAG, "完成番茄钟失败", e);
        }
    }
    
    /**
     * 关闭番茄钟
     */
    private void closePomodoro(PomodoroService pomodoroService) {
        try {
            pomodoroService.closeByUser();
            Log.i(TAG, "番茄钟已关闭并重置");
        } catch (Exception e) {
            Log.e(TAG, "关闭番茄钟失败", e);
        }
    }
    
    /**
     * 重置番茄钟
     */
    private void resetPomodoro(PomodoroService pomodoroService) {
        try {
            pomodoroService.abandonTimer();
            Log.i(TAG, "番茄钟已重置");
        } catch (Exception e) {
            Log.e(TAG, "重置番茄钟失败", e);
        }
    }
    
    @Override
    public String getDescription() {
        return "番茄钟完成流程控制，支持完成(complete)、关闭(close)和重置(reset)操作";
    }
    
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
        return action.equalsIgnoreCase("complete") || 
               action.equalsIgnoreCase("close") || 
               action.equalsIgnoreCase("reset");
    }
    
    /**
     * 获取支持的参数
     */
    public static Map<String, String> getSupportedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "操作类型：complete(完成)、close(关闭)、reset(重置)");
        return params;
    }
}
