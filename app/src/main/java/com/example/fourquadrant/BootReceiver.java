package com.example.fourquadrant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.d(TAG, "重新设置所有提醒闹钟");
            rescheduleAllReminders(context);
        }
    }
    
    private void rescheduleAllReminders(Context context) {
        try {
            ReminderManager reminderManager = new ReminderManager(context);
            List<ReminderItem> reminders = reminderManager.getAllReminders();
            
            for (ReminderItem reminder : reminders) {
                // 只重新设置未来的提醒
                if (reminder.getReminderTime() > System.currentTimeMillis()) {
                    reminderManager.scheduleAlarm(reminder);
                    Log.d(TAG, "重新设置提醒: " + reminder.getContent());
                }
            }
            
            Log.d(TAG, "成功重新设置 " + reminders.size() + " 个提醒");
        } catch (Exception e) {
            Log.e(TAG, "重新设置提醒失败", e);
        }
    }
}