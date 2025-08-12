package com.example.fourquadrant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "REMINDER_CHANNEL_V2";
    private static final String CHANNEL_NAME = "定时提醒";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "收到提醒广播");
        
        // 获取提醒信息
        String reminderId = intent.getStringExtra("reminder_id");
        String content = intent.getStringExtra("reminder_content");
        String taskName = intent.getStringExtra("reminder_task_name");
        boolean isVibrate = intent.getBooleanExtra("reminder_vibrate", true);
        boolean isSound = intent.getBooleanExtra("reminder_sound", true);
        boolean isRepeat = intent.getBooleanExtra("reminder_repeat", false);
        
        // 重要：验证提醒是否仍然存在且激活
        if (!isReminderStillValid(context, reminderId)) {
            Log.d(TAG, "提醒已被删除或未激活，跳过执行: " + reminderId);
            return; // 提醒已被删除，不执行任何操作
        }
        
        Log.d(TAG, "执行提醒: " + content);
        
        // 创建通知渠道
        createNotificationChannel(context);
        
        // 显示系统通知
        showNotification(context, reminderId, content, taskName, isVibrate, isSound, isRepeat);
        
        // 执行振动
        if (isVibrate) {
            performVibration(context);
        }
        
        // 打开应用并显示弹窗
        openAppWithDialog(context, reminderId, content, taskName, isRepeat);
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // 删除旧的通知渠道（如果存在）
            if (notificationManager.getNotificationChannel("REMINDER_CHANNEL") != null) {
                notificationManager.deleteNotificationChannel("REMINDER_CHANNEL");
            }
            
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("定时提醒通知");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                channel.enableLights(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void showNotification(Context context, String reminderId, String content, String taskName,
                                 boolean isVibrate, boolean isSound, boolean isRepeat) {
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 点击通知打开应用的Intent
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("show_reminder_dialog", true);
        openIntent.putExtra("reminder_id", reminderId);
        openIntent.putExtra("reminder_content", content);
        openIntent.putExtra("reminder_task_name", taskName);
        openIntent.putExtra("reminder_repeat", isRepeat);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent openPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            openIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // "知道了"按钮的Intent
        Intent dismissIntent = new Intent(context, ReminderActionReceiver.class);
        dismissIntent.setAction("ACTION_DISMISS");
        dismissIntent.putExtra("reminder_id", reminderId);
        
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // "稍后提醒"按钮的Intent
        Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("reminder_id", reminderId);
        snoozeIntent.putExtra("reminder_content", content);
        snoozeIntent.putExtra("reminder_vibrate", isVibrate);
        snoozeIntent.putExtra("reminder_sound", isSound);
        
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知内容
        String notificationTitle = "⏰ 定时提醒";
        String notificationText = content;
        String bigText = content;
        
        // 如果有关联任务，添加到通知内容中
        if (taskName != null && !taskName.trim().isEmpty()) {
            notificationTitle = "⏰ 任务提醒";
            notificationText = "📋 " + taskName + "\n" + content;
            bigText = "📋 关联任务: " + taskName + "\n\n💬 提醒内容: " + content;
        }
        
        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_statistics) // 使用现有的图标
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_statistics, "知道了", dismissPendingIntent)
            .addAction(R.drawable.ic_statistics, "稍后提醒", snoozePendingIntent);
        
        // 设置声音
        if (isSound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        
        // 设置振动
        if (isVibrate) {
            builder.setVibrate(new long[]{0, 1000, 500, 1000});
        }
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    private void performVibration(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                Log.d(TAG, "开始执行振动");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(
                        new long[]{0, 1000, 500, 1000}, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 1000, 500, 1000}, -1);
                }
                Log.d(TAG, "振动执行完成");
            } else {
                Log.w(TAG, "设备不支持振动或振动器不可用");
            }
        } catch (Exception e) {
            Log.e(TAG, "振动失败", e);
        }
    }
    
    private void openAppWithDialog(Context context, String reminderId, String content, String taskName, boolean isRepeat) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("show_reminder_dialog", true);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("reminder_content", content);
        intent.putExtra("reminder_task_name", taskName);
        intent.putExtra("reminder_repeat", isRepeat);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    /**
     * 验证提醒是否仍然存在且激活
     * @param context 上下文
     * @param reminderId 提醒ID
     * @return true表示提醒有效，false表示提醒已被删除或未激活
     */
    private boolean isReminderStillValid(Context context, String reminderId) {
        try {
            // 使用ReminderManager验证提醒状态
            ReminderManager reminderManager = new ReminderManager(context);
            ReminderItem reminder = reminderManager.getReminderById(reminderId);
            
            if (reminder == null) {
                Log.d(TAG, "提醒不存在: " + reminderId);
                return false; // 提醒不存在
            }
            
            if (!reminder.isActive()) {
                Log.d(TAG, "提醒未激活: " + reminderId);
                return false; // 提醒未激活
            }
            
            Log.d(TAG, "提醒有效: " + reminderId);
            return true; // 提醒存在且激活
            
        } catch (Exception e) {
            Log.e(TAG, "验证提醒状态失败: " + reminderId, e);
            return false; // 异常情况下不执行提醒
        }
    }
}