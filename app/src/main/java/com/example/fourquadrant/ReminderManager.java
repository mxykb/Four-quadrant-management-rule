package com.example.fourquadrant;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 提醒数据管理类
 */
public class ReminderManager {
    private static final String PREF_NAME = "reminder_prefs";
    private static final String KEY_REMINDERS = "reminders";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private List<ReminderItem> reminders;
    private List<ReminderManagerListener> listeners;
    
    public interface ReminderManagerListener {
        void onRemindersChanged();
        void onReminderAdded(ReminderItem reminder);
        void onReminderUpdated(ReminderItem reminder);
        void onReminderDeleted(ReminderItem reminder);
    }
    
    public ReminderManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.reminders = new ArrayList<>();
        this.listeners = new ArrayList<>();
        loadReminders();
    }
    
    // 加载保存的提醒
    private void loadReminders() {
        String json = prefs.getString(KEY_REMINDERS, "[]");
        Type type = new TypeToken<List<ReminderItem>>(){}.getType();
        List<ReminderItem> savedReminders = gson.fromJson(json, type);
        
        if (savedReminders != null) {
            reminders.clear();
            reminders.addAll(savedReminders);
            // 按时间排序
            Collections.sort(reminders, (r1, r2) -> 
                Long.compare(r1.getReminderTime(), r2.getReminderTime()));
        }
    }
    
    // 保存提醒到本地
    private void saveReminders() {
        String json = gson.toJson(reminders);
        prefs.edit().putString(KEY_REMINDERS, json).apply();
    }
    
    // 添加提醒
    public void addReminder(ReminderItem reminder) {
        reminders.add(reminder);
        Collections.sort(reminders, (r1, r2) -> 
            Long.compare(r1.getReminderTime(), r2.getReminderTime()));
        saveReminders();
        notifyReminderAdded(reminder);
        notifyRemindersChanged();
    }
    
    // 更新提醒
    public void updateReminder(ReminderItem reminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.set(i, reminder);
                break;
            }
        }
        Collections.sort(reminders, (r1, r2) -> 
            Long.compare(r1.getReminderTime(), r2.getReminderTime()));
        saveReminders();
        notifyReminderUpdated(reminder);
        notifyRemindersChanged();
    }
    
    // 删除提醒
    public void deleteReminder(String reminderId) {
        ReminderItem deletedReminder = null;
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminderId)) {
                deletedReminder = reminders.remove(i);
                break;
            }
        }
        if (deletedReminder != null) {
            // 重要：删除提醒时必须取消对应的系统闹钟
            cancelAlarm(deletedReminder);
            
            saveReminders();
            notifyReminderDeleted(deletedReminder);
            notifyRemindersChanged();
            
            System.out.println("已删除提醒并取消闹钟: " + deletedReminder.getContent());
        }
    }
    
    // 根据ID获取提醒
    public ReminderItem getReminderById(String reminderId) {
        for (ReminderItem reminder : reminders) {
            if (reminder.getId().equals(reminderId)) {
                return reminder;
            }
        }
        return null;
    }
    
    // 获取所有提醒
    public List<ReminderItem> getAllReminders() {
        return new ArrayList<>(reminders);
    }
    
    // 获取活跃的提醒
    public List<ReminderItem> getActiveReminders() {
        List<ReminderItem> activeReminders = new ArrayList<>();
        for (ReminderItem reminder : reminders) {
            if (reminder.isActive()) {
                activeReminders.add(reminder);
            }
        }
        return activeReminders;
    }
    
    // 获取今天的提醒
    public List<ReminderItem> getTodayReminders() {
        List<ReminderItem> todayReminders = new ArrayList<>();
        for (ReminderItem reminder : reminders) {
            if (reminder.isToday() && reminder.isActive()) {
                todayReminders.add(reminder);
            }
        }
        return todayReminders;
    }
    
    // 获取指定日期的提醒
    public List<ReminderItem> getRemindersForDate(Date date) {
        List<ReminderItem> dateReminders = new ArrayList<>();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(date);
        
        for (ReminderItem reminder : reminders) {
            Calendar reminderCal = Calendar.getInstance();
            reminderCal.setTimeInMillis(reminder.getReminderTime());
            
            if (targetCal.get(Calendar.YEAR) == reminderCal.get(Calendar.YEAR) &&
                targetCal.get(Calendar.DAY_OF_YEAR) == reminderCal.get(Calendar.DAY_OF_YEAR)) {
                dateReminders.add(reminder);
            }
        }
        return dateReminders;
    }
    
    // 获取过期的提醒
    public List<ReminderItem> getPastReminders() {
        List<ReminderItem> pastReminders = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (ReminderItem reminder : reminders) {
            if (reminder.getReminderTime() < currentTime && reminder.isActive()) {
                pastReminders.add(reminder);
            }
        }
        return pastReminders;
    }
    
    // 清理过期的提醒
    public void cleanupPastReminders() {
        List<ReminderItem> toRemove = new ArrayList<>();
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 一周前
        
        for (ReminderItem reminder : reminders) {
            if (reminder.getReminderTime() < weekAgo && !reminder.isActive()) {
                toRemove.add(reminder);
            }
        }
        
        if (!toRemove.isEmpty()) {
            reminders.removeAll(toRemove);
            saveReminders();
            notifyRemindersChanged();
        }
    }
    
    // 监听器管理
    public void addListener(ReminderManagerListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ReminderManagerListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyRemindersChanged() {
        for (ReminderManagerListener listener : listeners) {
            listener.onRemindersChanged();
        }
    }
    
    private void notifyReminderAdded(ReminderItem reminder) {
        for (ReminderManagerListener listener : listeners) {
            listener.onReminderAdded(reminder);
        }
    }
    
    private void notifyReminderUpdated(ReminderItem reminder) {
        for (ReminderManagerListener listener : listeners) {
            listener.onReminderUpdated(reminder);
        }
    }
    
    private void notifyReminderDeleted(ReminderItem reminder) {
        for (ReminderManagerListener listener : listeners) {
            listener.onReminderDeleted(reminder);
        }
    }
    
    // 闹钟管理方法
    /**
     * 设置提醒的系统闹钟
     * @param reminder 提醒项
     */
    public void scheduleAlarm(ReminderItem reminder) {
        if (!reminder.isActive()) {
            return; // 非激活状态不设置闹钟
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_content", reminder.getContent());
        intent.putExtra("vibrate", reminder.isVibrate());
        intent.putExtra("sound", reminder.isSound());
        intent.putExtra("repeat", reminder.isRepeat());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTime(),
                    pendingIntent
                );
            } catch (SecurityException e) {
                // 静默处理，避免在非UI线程中显示Toast
            }
        }
    }
    
    /**
     * 取消提醒的系统闹钟
     * @param reminder 提醒项
     */
    public void cancelAlarm(ReminderItem reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
    
    /**
     * 切换提醒的激活状态，并相应地设置或取消闹钟
     * @param reminder 提醒项
     */
    public void toggleReminderActive(ReminderItem reminder) {
        boolean newActiveState = !reminder.isActive();
        reminder.setActive(newActiveState);
        
        if (newActiveState) {
            // 激活提醒 - 设置闹钟
            scheduleAlarm(reminder);
        } else {
            // 暂停提醒 - 取消闹钟
            cancelAlarm(reminder);
        }
        
        // 更新数据库
        updateReminder(reminder);
    }
} 