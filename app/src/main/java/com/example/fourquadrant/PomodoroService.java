package com.example.fourquadrant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.fourquadrant.database.repository.SettingsRepository;
import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.entity.TimerStateEntity;

public class PomodoroService extends Service {
    private static final String CHANNEL_ID = "PomodoroChannel";
    private static final int NOTIFICATION_ID = 1;
    
    // 广播动作
    public static final String ACTION_TIMER_UPDATE = "com.example.fourquadrant.TIMER_UPDATE";
    public static final String ACTION_TIMER_FINISHED = "com.example.fourquadrant.TIMER_FINISHED";
    
    // 广播额外数据键
    public static final String EXTRA_REMAINING_TIME = "remaining_time";
    public static final String EXTRA_IS_BREAK = "is_break";
    public static final String EXTRA_CURRENT_COUNT = "current_count";
    
    // 计时器相关
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isTimerPaused = false;
    private long remainingTime = 25 * 60 * 1000; // 默认25分钟，实际值从设置中获取
    private boolean isBreakTime = false;
    private int currentTomatoCount = 0;
    private String currentTaskName = "未指定任务"; // 当前任务名称
    
    // 数据库
    private SettingsRepository settingsRepository;
    private PomodoroRepository pomodoroRepository;
    
    // Binder
    private final IBinder binder = new PomodoroServiceBinder();
    
    public class PomodoroServiceBinder extends Binder {
        public PomodoroService getService() {
            return PomodoroService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        settingsRepository = new SettingsRepository(getApplication());
        pomodoroRepository = new PomodoroRepository(getApplication());
        createNotificationChannel();
        
        Log.d("PomodoroService", "Service onCreate called - attempting to restore timer state");
        
        // 尝试从数据库恢复计时器状态
        restoreTimerStateFromDatabase();
    }
    
    private void restoreTimerStateFromDatabase() {
        if (pomodoroRepository != null) {
            // 使用一次性查询而不是持续观察
            new Thread(() -> {
                try {
                    TimerStateEntity timerState = pomodoroRepository.getTimerStateSync();
                    if (timerState != null && timerState.isRunning()) {
                        Log.d("PomodoroService", "Found saved timer state: isRunning=" + timerState.isRunning() + ", isPaused=" + timerState.isPaused() + ", remainingTime=" + timerState.getRemainingTime());
                        
                        // 恢复状态变量
                        this.isTimerRunning = timerState.isRunning();
                        this.isTimerPaused = timerState.isPaused();
                        this.remainingTime = timerState.getRemainingTime();
                        this.isBreakTime = timerState.isBreak();
                        this.currentTomatoCount = timerState.getCurrentCount();
                        
                        // 如果有开始时间且计时器正在运行，需要重新计算剩余时间
                        if (timerState.getStartTime() > 0 && !timerState.isPaused()) {
                            long currentTime = System.currentTimeMillis();
                            long elapsedTime = currentTime - timerState.getStartTime();
                            
                            // 获取原始持续时间
                            long originalDuration;
                            if (timerState.isBreak()) {
                                originalDuration = TomatoSettingsDialog.getBreakDuration(this) * 60 * 1000;
                            } else {
                                originalDuration = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
                            }
                            
                            this.remainingTime = originalDuration - elapsedTime;
                            
                            Log.d("PomodoroService", "Recalculated remaining time: " + this.remainingTime + " (elapsed: " + elapsedTime + ")");
                            
                            // 如果时间已经用完，直接完成计时器
                            if (this.remainingTime <= 0) {
                                Log.d("PomodoroService", "Timer should have finished, triggering completion");
                                onTimerFinished();
                                return;
                            }
                        }
                        
                        // 在主线程中执行UI相关操作
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            // 如果计时器正在运行且未暂停，重新启动计时器
                            if (this.isTimerRunning && !this.isTimerPaused) {
                                Log.d("PomodoroService", "Restarting timer from restored state");
                                continueTimer();
                            }
                            
                            // 立即发送广播更新UI状态
                            Intent intent = new Intent(ACTION_TIMER_UPDATE);
                            intent.putExtra(EXTRA_REMAINING_TIME, this.remainingTime);
                            intent.putExtra(EXTRA_IS_BREAK, this.isBreakTime);
                            intent.putExtra(EXTRA_CURRENT_COUNT, this.currentTomatoCount);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                            
                            // 更新通知
                            updateNotification();
                            
                            Log.d("PomodoroService", "Timer state restored successfully and UI updated");
                        });
                    } else {
                        Log.d("PomodoroService", "No saved timer state found or timer not running");
                    }
                } catch (Exception e) {
                    Log.e("PomodoroService", "Error restoring timer state", e);
                }
            }).start();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 检查通知权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w("PomodoroService", "POST_NOTIFICATIONS权限未授予，通知可能无法显示");
            }
        }
        
        // 检查电池优化白名单
        checkBatteryOptimization();
        
        try {
            startForeground(NOTIFICATION_ID, createNotification());
            Log.d("PomodoroService", "前台服务启动成功，通知ID: " + NOTIFICATION_ID);
        } catch (Exception e) {
            Log.e("PomodoroService", "启动前台服务失败", e);
        }
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "番茄钟计时器",
                NotificationManager.IMPORTANCE_HIGH  // 提升为高重要性
            );
            channel.setDescription("显示番茄钟计时进度");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(true);  // 启用指示灯
            channel.enableVibration(false);  // 禁用震动避免干扰
            channel.setBypassDnd(true);  // 绕过勿扰模式
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    /**
     * 检查电池优化设置
     */
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.w("PomodoroService", "应用未在电池优化白名单中，可能影响后台运行");
                // 这里可以发送广播通知UI层提示用户
                Intent intent = new Intent("com.example.fourquadrant.BATTERY_OPTIMIZATION_WARNING");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                Log.d("PomodoroService", "应用已在电池优化白名单中");
            }
        }
    }
    
    private Notification createNotification() {
        try {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            String title = isBreakTime ? "🛌 休息时间" : "🍅 专注时间";
            String content = String.format("%s - 任务: %s", formatTime(remainingTime), currentTaskName);
            
            Log.d("PomodoroService", "创建通知: " + title + " - " + content);
            
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_timer) // 使用自定义图标
                .setContentIntent(pendingIntent)
                .setOngoing(true) // 持续通知，不能被滑动删除
                .setPriority(NotificationCompat.PRIORITY_LOW) // 前台服务使用低优先级避免干扰
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(false) // 不显示时间戳
                .setAutoCancel(false) // 不自动取消
                .setCategory(NotificationCompat.CATEGORY_SERVICE) // 服务类别
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // 立即显示前台服务
                .setOnlyAlertOnce(true) // 只在第一次显示时提醒
                .setSilent(true) // 静默通知，避免频繁更新时的声音
                .build();
                
            Log.d("PomodoroService", "通知创建成功");
            return notification;
        } catch (Exception e) {
            Log.e("PomodoroService", "创建通知失败", e);
            // 返回一个简单的通知作为备用
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("番茄钟")
                .setContentText("计时中...")
                .setSmallIcon(R.drawable.ic_timer)
                .setOngoing(true)
                .build();
        }
    }
    
    private void updateNotification() {
        try {
            // 只在计时器运行时显示通知
            if (isTimerRunning) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.notify(NOTIFICATION_ID, createNotification());
                    Log.d("PomodoroService", "通知已更新");
                } else {
                    Log.e("PomodoroService", "NotificationManager为null，无法更新通知");
                }
            } else {
                // 计时器未运行时清除通知
                clearNotification();
                Log.d("PomodoroService", "计时器未运行，已清除通知");
            }
        } catch (Exception e) {
            Log.e("PomodoroService", "更新通知失败", e);
        }
    }
    
    /**
     * 清除通知的专用方法
     */
    private void clearNotification() {
        try {
            // 方法1：使用stopForeground清除前台服务通知
            stopForeground(true);
            
            // 方法2：使用NotificationManager直接取消通知（双重保险）
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(NOTIFICATION_ID);
                Log.d("PomodoroService", "通过NotificationManager取消通知");
            }
        } catch (Exception e) {
            Log.e("PomodoroService", "清除通知失败", e);
        }
    }
    
    private void showCompletionNotification() {
        try {
            // 创建点击通知时的Intent
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            // 构建完成通知
            String title = "🍅 番茄钟已完成！";
            String content = String.format("任务：%s - 第%d个番茄钟完成", currentTaskName, currentTomatoCount);
            
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true) // 点击后自动取消
                    .setPriority(NotificationCompat.PRIORITY_MAX) // 使用最高优先级
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setDefaults(NotificationCompat.DEFAULT_ALL) // 启用所有默认设置（声音、振动、灯光）
                    .setOngoing(false) // 确保不是持续通知
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在锁屏上显示
                    .setLights(0xFF00FF00, 1000, 1000) // 绿色指示灯闪烁
                    .setVibrate(new long[]{0, 500, 200, 500}) // 振动模式
                    .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // 通知声音
                    .setWhen(System.currentTimeMillis()) // 设置通知时间
                    .setShowWhen(true) // 显示通知时间
                    .build();
            
            // 显示通知，使用不同的通知ID避免与前台服务通知冲突
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID + 1, notification); // 使用不同的ID
                Log.d("PomodoroService", "番茄钟完成通知已显示，通知ID: " + (NOTIFICATION_ID + 1));
            }
        } catch (Exception e) {
            Log.e("PomodoroService", "显示完成通知失败", e);
        }
    }
    
    private String formatTime(long timeInMillis) {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public void startTimer() {
        Log.d("PomodoroService", "startTimer() called");
        long duration = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
        int totalCount = TomatoSettingsDialog.getTomatoCount(this);
        Log.d("PomodoroService", "startTimer - duration: " + duration + ", totalCount: " + totalCount);
        startTimer(duration, false, 0, totalCount);
    }
    
    public void startTimer(long duration, boolean isBreak, int currentCount, int totalCount) {
        Log.d("PomodoroService", "Starting timer with duration: " + duration + ", isBreak: " + isBreak + ", currentCount: " + currentCount + ", totalCount: " + totalCount);
        
        if (!isTimerRunning || isTimerPaused) {
            isTimerRunning = true;
            isTimerPaused = false;
            isBreakTime = isBreak;
            currentTomatoCount = currentCount;
            
            if (!isTimerPaused) {
                remainingTime = duration;
            }
            
            Log.d("PomodoroService", "Timer state after start - isTimerRunning: " + isTimerRunning + ", isTimerPaused: " + isTimerPaused + ", isBreakTime: " + isBreakTime + ", remainingTime: " + remainingTime);
            
            continueTimer();
            
            // 发送状态更新广播
            Intent intent = new Intent(ACTION_TIMER_UPDATE);
            intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
            intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
            intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    
    public void pauseTimer() {
        Log.d("PomodoroService", "Pausing timer - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        if (isTimerRunning && !isTimerPaused) {
            isTimerPaused = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            
            Log.d("PomodoroService", "Timer paused - new state: isTimerPaused=" + isTimerPaused);
            
            // 发送状态更新广播
            Intent intent = new Intent(ACTION_TIMER_UPDATE);
            intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
            intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
            intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    
    public void resumeTimer() {
        Log.d("PomodoroService", "Resuming timer - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        if (isTimerRunning && isTimerPaused) {
            isTimerPaused = false;
            
            Log.d("PomodoroService", "Timer resumed - new state: isTimerPaused=" + isTimerPaused);
            
            continueTimer();
            
            // 发送状态更新广播
            Intent intent = new Intent(ACTION_TIMER_UPDATE);
            intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
            intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
            intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    
    public void abandonTimer() {
        Log.d("PomodoroService", "Abandoning timer - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
        
        Log.d("PomodoroService", "Timer abandoned - new state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        // 清除数据库中的计时器状态
        if (pomodoroRepository != null) {
            pomodoroRepository.clearTimerStateSync();
            Log.d("PomodoroService", "Cleared timer state from database");
        }
        
        // 发送状态更新广播
        Intent intent = new Intent(ACTION_TIMER_UPDATE);
        intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
        intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
        intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        updateNotification();
        clearNotification();
        stopSelf();
    }
    
    private void continueTimer() {
        // 如果已经有计时器在运行，先取消它
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            Log.d("PomodoroService", "Cancelled existing timer before creating new one");
        }
        
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                
                // 更新通知
                updateNotification();
                
                // 发送广播更新UI
                Intent intent = new Intent(ACTION_TIMER_UPDATE);
                intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
                intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
                intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
                LocalBroadcastManager.getInstance(PomodoroService.this).sendBroadcast(intent);
            }
            
            @Override
            public void onFinish() {
                onTimerFinished();
            }
        };
        countDownTimer.start();
        Log.d("PomodoroService", "Started new timer with remaining time: " + remainingTime);
    }
    
    private void onTimerFinished() {
        if (!isBreakTime) {
            // 完成一个番茄钟
            currentTomatoCount++;
            
            // 停止计时器，等待用户选择下一步操作
            isTimerRunning = false;
            isTimerPaused = false;
            remainingTime = 0;
            
            // 显示完成通知而不是清除通知
            showCompletionNotification();
            
            // 保存完成待确认状态到数据库
            if (pomodoroRepository != null) {
                int totalCount = TomatoSettingsDialog.getTomatoCount(this);
                pomodoroRepository.savePomodoroCompletionPending(currentTaskName, currentTomatoCount, totalCount);
            }
            
            Log.d("PomodoroService", "Pomodoro completed, waiting for user action. Count: " + currentTomatoCount);
        } else {
            // 休息结束，开始下一个番茄钟
            finishBreakTime();
        }
        
        // 发送完成广播
        Intent intent = new Intent(ACTION_TIMER_FINISHED);
        intent.putExtra(EXTRA_IS_BREAK, isBreakTime);
        intent.putExtra(EXTRA_CURRENT_COUNT, currentTomatoCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void startBreakTime() {
        isBreakTime = true;
        remainingTime = TomatoSettingsDialog.getBreakDuration(this) * 60 * 1000;
        isTimerRunning = true;
        isTimerPaused = false;
        
        Log.d("PomodoroService", "Starting break time: " + remainingTime + "ms");
        
        // 启动休息计时器
        continueTimer();
    }
    
    private void finishBreakTime() {
        isBreakTime = false;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
        
        // 检查是否自动开始下一个计时器
        boolean autoNext = TomatoSettingsDialog.getAutoNext(this);
        if (autoNext) {
            continueTimer();
        } else {
            isTimerRunning = false;
            isTimerPaused = false;
            // 清除通知，因为计时器已停止
            clearNotification();
        }
    }
    
    private void finishAllPomodoros() {
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
        
        updateNotification();
        clearNotification();
        stopSelf();
    }
    
    // Getter方法供Fragment使用
    public boolean isTimerRunning() {
        return this.isTimerRunning;
    }
    
    public boolean isTimerPaused() {
        return this.isTimerPaused;
    }
    
    public long getRemainingTime() {
        return remainingTime;
    }
    
    public boolean isBreakTime() {
        return isBreakTime;
    }
    
    public int getCurrentTomatoCount() {
        return currentTomatoCount;
    }
    
    public String getCurrentTaskName() {
        return currentTaskName;
    }
    
    public void setCurrentTaskName(String taskName) {
        this.currentTaskName = taskName != null ? taskName : "未指定任务";
    }
    
    // 同步状态方法，用于Fragment恢复时同步状态
    public void syncState(boolean running, boolean paused, long remaining, boolean isBreak, int count) {
        Log.d("PomodoroService", "Syncing state - running: " + running + ", paused: " + paused + ", remaining: " + remaining + ", isBreak: " + isBreak + ", count: " + count);
        
        // 先停止现有的计时器（如果有的话）
        if (countDownTimer != null) {
            Log.d("PomodoroService", "Stopping existing timer before sync");
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        this.isTimerRunning = running;
        this.isTimerPaused = paused;
        this.remainingTime = remaining;
        this.isBreakTime = isBreak;
        this.currentTomatoCount = count;
        
        Log.d("PomodoroService", "State synced - new state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        // 如果计时器正在运行且未暂停，需要重新启动计时器
        if (running && !paused) {
            Log.d("PomodoroService", "Starting timer after state sync");
            continueTimer();
        }
        
        // 清除通知，因为计时器已停止
        stopForeground(true);
        
        Log.d("PomodoroService", "State sync completed");
    }
    
    /**
     * 用户选择开始休息
     */
    public void startBreakByUser() {
        Log.d("PomodoroService", "User chose to start break");
        startBreakTime();
    }
    
    /**
     * 用户选择跳过休息，继续工作
     */
    public void skipBreakByUser() {
        Log.d("PomodoroService", "User chose to skip break");
        
        int totalTomatoCount = TomatoSettingsDialog.getTomatoCount(this);
        if (currentTomatoCount >= totalTomatoCount) {
            // 完成所有番茄钟
            finishAllPomodoros();
        } else {
            // 直接开始下一个番茄钟
            isBreakTime = false;
            remainingTime = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
            isTimerRunning = true;
            isTimerPaused = false;
            continueTimer();
        }
    }
    

    
    /**
     * 用户选择关闭，重置番茄钟
     */
    public void closeByUser() {
        Log.d("PomodoroService", "User chose to close and reset");
        
        // 重置所有状态
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
        
        // 取消计时器
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        // 清除数据库状态
        if (pomodoroRepository != null) {
            new Thread(() -> {
                try {
                    pomodoroRepository.clearTimerStateSync();
                    pomodoroRepository.clearPomodoroCompletionPending();
                    Log.d("PomodoroService", "Timer state and completion pending cleared from database");
                } catch (Exception e) {
                    Log.e("PomodoroService", "Failed to clear timer state", e);
                }
            }).start();
        }
        
        // 清除通知
        clearNotification();
        Log.d("PomodoroService", "通知已清除");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Log.d("PomodoroService", "Service onDestroy called - saving state before destruction");
        
        // 如果计时器正在运行，保存当前状态到数据库
        if (isTimerRunning) {
            Log.d("PomodoroService", "Timer is running, saving state: isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime + ", isBreakTime=" + isBreakTime + ", currentCount=" + currentTomatoCount);
            
            // 计算当前应该保存的开始时间
            long currentTime = System.currentTimeMillis();
            long startTime;
            
            if (isTimerPaused) {
                // 如果已暂停，保存0作为开始时间，依赖剩余时间恢复
                startTime = 0;
                Log.d("PomodoroService", "Timer is paused, saving startTime as 0");
            } else {
                // 如果正在运行，计算开始时间
                long originalDuration;
                if (isBreakTime) {
                    originalDuration = TomatoSettingsDialog.getBreakDuration(this) * 60 * 1000;
                } else {
                    originalDuration = TomatoSettingsDialog.getTomatoDuration(this) * 60 * 1000;
                }
                startTime = currentTime - (originalDuration - remainingTime);
                Log.d("PomodoroService", "Timer is running, calculated startTime=" + startTime);
            }
            
            // 保存状态到数据库
            if (pomodoroRepository != null) {
                pomodoroRepository.saveTimerState(startTime, isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
                Log.d("PomodoroService", "Timer state saved to database before service destruction");
            }
        }
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // 确保清除通知
        clearNotification();
        
        Log.d("PomodoroService", "Service destroyed");
    }
}