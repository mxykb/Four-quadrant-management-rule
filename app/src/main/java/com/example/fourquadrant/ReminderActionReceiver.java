package com.example.fourquadrant;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class ReminderActionReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String reminderId = intent.getStringExtra("reminder_id");
        
        if ("DISMISS_REMINDER".equals(action)) {
            // 知道了 - 关闭通知
            dismissReminder(context, reminderId);
        } else if ("SNOOZE_REMINDER".equals(action)) {
            // 稍后提醒 - 5分钟后再次提醒
            snoozeReminder(context, intent);
        }
    }
    
    private void dismissReminder(Context context, String reminderId) {
        // 取消通知
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && reminderId != null) {
            notificationManager.cancel(1001 + reminderId.hashCode());
        }
    }
    
    private void snoozeReminder(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        String content = intent.getStringExtra("reminder_content");
        
        // 取消当前通知
        dismissReminder(context, reminderId);
        
        // 5分钟后重新提醒
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, 5);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
        reminderIntent.putExtra("reminder_id", reminderId + "_snooze");
        reminderIntent.putExtra("reminder_content", content);
        reminderIntent.putExtra("reminder_vibrate", true);
        reminderIntent.putExtra("reminder_sound", true);
        reminderIntent.putExtra("reminder_repeat", false);
        
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
        }
        
        // 显示确认消息
        Toast.makeText(context, "将在5分钟后再次提醒", Toast.LENGTH_SHORT).show();
    }
} 