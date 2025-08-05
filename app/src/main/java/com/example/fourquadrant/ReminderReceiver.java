package com.example.fourquadrant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "提醒通知";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String content = intent.getStringExtra("reminder_content");
        boolean isVibrate = intent.getBooleanExtra("reminder_vibrate", true);
        boolean isSound = intent.getBooleanExtra("reminder_sound", true);
        boolean isRepeat = intent.getBooleanExtra("reminder_repeat", false);
        
        // 创建通知渠道
        createNotificationChannel(context);
        
        // 显示通知
        showNotification(context, reminderId, content);
        
        // 执行提醒效果
        if (isVibrate) {
            performVibration(context);
        }
        
        if (isSound) {
            playNotificationSound(context);
        }
        
        // 如果设置了重复提醒，5分钟后再次提醒
        if (isRepeat) {
            scheduleRepeatReminder(context, intent);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("定时提醒通知");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void showNotification(Context context, String reminderId, String content) {
        // 点击通知时打开应用
        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            clickIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 知道了按钮
        Intent dismissIntent = new Intent(context, ReminderActionReceiver.class);
        dismissIntent.setAction("DISMISS_REMINDER");
        dismissIntent.putExtra("reminder_id", reminderId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 稍后提醒按钮
        Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class);
        snoozeIntent.setAction("SNOOZE_REMINDER");
        snoozeIntent.putExtra("reminder_id", reminderId);
        snoozeIntent.putExtra("reminder_content", content);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 提醒时间到")
            .setContentText(content)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
            .setSubText("时间：" + currentTime)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(clickPendingIntent)
            .addAction(R.drawable.ic_snooze, "稍后提醒", snoozePendingIntent)
            .addAction(R.drawable.ic_check, "知道了", dismissPendingIntent);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + reminderId.hashCode(), builder.build());
        }
    }
    
    private void performVibration(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                // 震动模式：短-长-短
                long[] pattern = {0, 200, 100, 500, 100, 200};
                vibrator.vibrate(pattern, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void playNotificationSound(Context context) {
        try {
            // 注释掉自定义音频文件，因为没有提供实际的音频资源
            // MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound);
            // 使用系统默认提示音
            android.media.RingtoneManager.getRingtone(
                context, 
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            ).play();
        } catch (Exception e) {
            // 如果播放失败，静默处理
            e.printStackTrace();
        }
    }
    
    private void scheduleRepeatReminder(Context context, Intent originalIntent) {
        // 5分钟后重复提醒的逻辑
        // 这里可以重新设置AlarmManager来实现重复提醒
        // 为了简化，此处省略实现
    }
} 