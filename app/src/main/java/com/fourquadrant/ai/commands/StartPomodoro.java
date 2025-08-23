package com.fourquadrant.ai.commands;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.fourquadrant.PomodoroService;
import com.example.fourquadrant.database.repository.SettingsRepository;
import com.fourquadrant.ai.AiExecutable;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 启动番茄钟功能实现
 */
public class StartPomodoro implements AiExecutable {
    private static final String TAG = "StartPomodoro";
    private Context context;
    private SettingsRepository settingsRepository;
    
    // 设置键名常量 - 与PomodoroSettingsControl保持一致
    private static final String KEY_TOMATO_DURATION = "tomato_duration";
    
    public StartPomodoro(Context context) {
        this.context = context;
        // 初始化SettingsRepository以读取当前设置
        if (context.getApplicationContext() instanceof android.app.Application) {
            this.settingsRepository = new SettingsRepository((android.app.Application) context.getApplicationContext());
        } else {
            this.settingsRepository = null;
            Log.w(TAG, "无法获取Application实例，某些功能可能不可用");
        }
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 获取持续时间参数，如果没有传入则从设置中读取
            int duration = 25; // 默认值
            if (args.containsKey("duration")) {
                Object durationObj = args.get("duration");
                if (durationObj instanceof Integer) {
                    duration = (Integer) durationObj;
                } else if (durationObj instanceof String) {
                    duration = Integer.parseInt((String) durationObj);
                }
            } else {
                // 如果没有传入duration参数，从设置中读取当前配置
                try {
                    if (settingsRepository != null) {
                        // 读取番茄钟时长设置
                        Integer currentDuration = settingsRepository.getIntSettingSync(KEY_TOMATO_DURATION);
                        if (currentDuration != null && currentDuration > 0) {
                            duration = currentDuration;
                            Log.i(TAG, "从设置中读取番茄钟时长: " + duration + " 分钟");
                        } else {
                            Log.i(TAG, "设置中未找到番茄钟时长，使用默认值: " + duration + " 分钟");
                        }
                    } else {
                        Log.w(TAG, "SettingsRepository不可用，使用默认时长: " + duration + " 分钟");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "读取设置失败，使用默认时长: " + duration + " 分钟", e);
                }
            }
            
            // 验证持续时间范围
            if (duration < 1 || duration > 120) {
                Log.w(TAG, "持续时间超出范围，使用默认值25分钟");
                duration = 25;
            }
            
            // 获取任务相关参数（必需参数）
            String taskName = (String) args.get("task_name");
            
            String taskId = null;
            if (args.containsKey("task_id")) {
                Object taskIdObj = args.get("task_id");
                if (taskIdObj instanceof String) {
                    taskId = (String) taskIdObj;
                }
            }
            
                    // 启动番茄钟服务（在主线程中进行，因为服务内部的CountDownTimer需要在主线程创建）
        Log.i(TAG, "启动番茄钟：" + duration + " 分钟，任务：" + taskName);
        
        // 启动并绑定服务
        Intent serviceIntent = new Intent(context, PomodoroService.class);
        Log.d(TAG, "创建服务Intent: " + serviceIntent);
        
        ComponentName startServiceResult = context.startService(serviceIntent);
        Log.d(TAG, "startService结果: " + startServiceResult);
        
        // 使用异步方式绑定服务并设置任务名称
        bindServiceAndStartTimerAsync(serviceIntent, duration, taskName, taskId);
        
        Log.d(TAG, "番茄钟启动流程开始");
        
        } catch (Exception e) {
            Log.e(TAG, "StartPomodoro 执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 异步绑定服务并启动番茄钟
     */
    private void bindServiceAndStartTimerAsync(Intent serviceIntent, int duration, String taskName, String taskId) {
        new Thread(() -> {
            bindServiceAndStartTimer(serviceIntent, duration, taskName, taskId);
        }).start();
    }
    
    /**
     * 绑定服务并启动番茄钟
     */
    private void bindServiceAndStartTimer(Intent serviceIntent, int duration, String taskName, String taskId) {
        Log.d(TAG, "开始绑定服务，时长: " + duration + "分钟，任务: " + taskName);
        
        CountDownLatch serviceLatch = new CountDownLatch(1);
        final AtomicReference<PomodoroService> pomodoroServiceRef = new AtomicReference<>();
        final AtomicBoolean isServiceBoundRef = new AtomicBoolean(false);
        
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "服务连接回调触发，ComponentName: " + name);
                PomodoroService.PomodoroServiceBinder binder = (PomodoroService.PomodoroServiceBinder) service;
                pomodoroServiceRef.set(binder.getService());
                isServiceBoundRef.set(true);
                serviceLatch.countDown();
                Log.d(TAG, "服务连接成功，PomodoroService: " + pomodoroServiceRef.get());
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "服务连接断开回调触发，ComponentName: " + name);
                pomodoroServiceRef.set(null);
                isServiceBoundRef.set(false);
            }
        };
        
        try {
            Log.d(TAG, "尝试绑定服务...");
            // 绑定服务
            boolean bindResult = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService结果: " + bindResult);
            
            if (!bindResult) {
                Log.e(TAG, "绑定服务失败");
                return;
            }
            
            Log.d(TAG, "等待服务连接...");
            // 等待服务连接，减少等待时间避免长时间阻塞
            if (serviceLatch.await(2, TimeUnit.SECONDS) && isServiceBoundRef.get() && pomodoroServiceRef.get() != null) {
                Log.d(TAG, "服务连接成功，开始设置任务名称和启动计时器");
                
                // 在主线程中执行服务调用（因为CountDownTimer需要在主线程创建）
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    try {
                        // 设置任务名称
                        pomodoroServiceRef.get().setCurrentTaskName(taskName);
                        Log.d(TAG, "任务名称设置完成: " + taskName);
                        
                        // 启动番茄钟
                        long durationMillis = duration * 60 * 1000L;
                        Log.d(TAG, "调用startTimer，时长: " + duration + "分钟 (" + durationMillis + "毫秒)");
                        
                        pomodoroServiceRef.get().startTimer(durationMillis, false, 0, 1);
                        
                        Log.i(TAG, "番茄钟启动成功，时长：" + duration + "分钟，任务：" + taskName);
                    } catch (Exception e) {
                        Log.e(TAG, "在主线程中启动番茄钟失败: " + e.getMessage(), e);
                    }
                });
            } else {
                Log.e(TAG, "服务连接失败或超时，isServiceBound: " + isServiceBoundRef.get() + ", service: " + pomodoroServiceRef.get());
            }
        } catch (Exception e) {
            Log.e(TAG, "启动番茄钟失败: " + e.getMessage(), e);
        } finally {
            Log.d(TAG, "开始清理服务连接...");
            // 解绑服务
            if (isServiceBoundRef.get()) {
                try {
                    context.unbindService(serviceConnection);
                    Log.d(TAG, "服务解绑成功");
                } catch (Exception e) {
                    Log.e(TAG, "解绑服务失败", e);
                }
            }
        }
    }
    
    @Override
    public String getDescription() {
        return "启动番茄钟计时器，支持指定时长和关联任务";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        // task_name 是必需参数
        if (!args.containsKey("task_name")) {
            return false;
        }
        Object taskName = args.get("task_name");
        if (!(taskName instanceof String)) {
            return false;
        }
        String name = (String) taskName;
        if (name.trim().isEmpty()) {
            return false;
        }
        
        // 验证duration参数
        if (args.containsKey("duration")) {
            Object duration = args.get("duration");
            if (duration instanceof Integer) {
                int dur = (Integer) duration;
                if (dur < 1 || dur > 120) {
                    return false;
                }
            } else if (duration instanceof String) {
                try {
                    int dur = Integer.parseInt((String) duration);
                    if (dur < 1 || dur > 120) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // 验证task_id参数
        if (args.containsKey("task_id")) {
            Object taskId = args.get("task_id");
            if (taskId != null && !(taskId instanceof String)) {
                return false;
            }
        }
        
        return true;
    }
}