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
 * 番茄钟休息流程控制功能实现
 * 支持开始休息和跳过休息操作
 */
public class PomodoroBreakControl implements AiExecutable {
    private static final String TAG = "PomodoroBreakControl";
    private Context context;
    private String defaultAction;
    
    public PomodoroBreakControl(Context context) {
        this.context = context;
        this.defaultAction = "start";
    }
    
    public PomodoroBreakControl(Context context, String defaultAction) {
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
            
            Log.i(TAG, "执行番茄钟休息控制操作: " + action);
            
            // 绑定服务并执行操作
            bindServiceAndExecute(action, args);
            
        } catch (Exception e) {
            Log.e(TAG, "执行番茄钟休息控制操作失败", e);
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
     * 执行具体的休息控制操作
     */
    private void executeAction(String action, PomodoroService pomodoroService, Map<String, Object> args) {
        if (pomodoroService == null) {
            Log.e(TAG, "番茄钟服务未连接");
            return;
        }
        
        switch (action.toLowerCase()) {
            case "start":
                startBreak(pomodoroService);
                break;
            case "skip":
                skipBreak(pomodoroService);
                break;
            default:
                Log.w(TAG, "未知的操作类型: " + action);
                break;
        }
    }
    
    /**
     * 开始休息时间
     */
    private void startBreak(PomodoroService pomodoroService) {
        try {
            pomodoroService.startBreakByUser();
            Log.i(TAG, "休息时间已开始");
        } catch (Exception e) {
            Log.e(TAG, "开始休息失败", e);
        }
    }
    
    /**
     * 跳过休息时间
     */
    private void skipBreak(PomodoroService pomodoroService) {
        try {
            pomodoroService.skipBreakByUser();
            Log.i(TAG, "休息时间已跳过，继续下一个番茄钟");
        } catch (Exception e) {
            Log.e(TAG, "跳过休息失败", e);
        }
    }
    
    @Override
    public String getDescription() {
        return "番茄钟休息流程控制，支持开始休息(start)和跳过休息(skip)操作";
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
        return action.equalsIgnoreCase("start") || action.equalsIgnoreCase("skip");
    }
    
    /**
     * 获取支持的参数
     */
    public static Map<String, String> getSupportedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "操作类型：start(开始休息)、skip(跳过休息)");
        return params;
    }
}
