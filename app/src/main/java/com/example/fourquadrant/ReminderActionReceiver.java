package com.example.fourquadrant;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class ReminderActionReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderActionReceiver";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String reminderId = intent.getStringExtra("reminder_id");
        
        Log.d(TAG, "收到操作: " + action + ", 提醒ID: " + reminderId);
        
        // 取消当前通知
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        
        if ("ACTION_DISMISS".equals(action)) {
            handleDismiss(context, reminderId);
        } else if ("ACTION_SNOOZE".equals(action)) {
            handleSnooze(context, intent);
        }
    }
    
    private void handleDismiss(Context context, String reminderId) {
        Log.d(TAG, "用户选择知道了，结束提醒: " + reminderId);
        Toast.makeText(context, "提醒已完成", Toast.LENGTH_SHORT).show();
        // 这里可以添加更多的完成逻辑，比如记录到日志等
    }
    
    private void handleSnooze(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String content = intent.getStringExtra("reminder_content");
        boolean isVibrate = intent.getBooleanExtra("reminder_vibrate", true);
        boolean isSound = intent.getBooleanExtra("reminder_sound", true);
        
        Log.d(TAG, "用户选择稍后提醒，5分钟后再次提醒: " + reminderId);
        
        // 设置5分钟后的提醒
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, 5);
        
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent reminderIntent = new Intent(context, ReminderReceiver.class);
            reminderIntent.putExtra("reminder_id", reminderId + "_snooze");
            reminderIntent.putExtra("reminder_content", content);
            reminderIntent.putExtra("reminder_vibrate", isVibrate);
            reminderIntent.putExtra("reminder_sound", isSound);
            reminderIntent.putExtra("reminder_repeat", false); // 稍后提醒不再重复
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (reminderId + "_snooze").hashCode(),
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    pendingIntent
                );
                
                Toast.makeText(context, "将在5分钟后再次提醒", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "设置稍后提醒失败", e);
            Toast.makeText(context, "设置稍后提醒失败", Toast.LENGTH_SHORT).show();
        }
    }
}