package com.example.fourquadrant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 提醒项数据模型
 */
public class ReminderItem {
    private String id;              // 唯一标识
    private String content;         // 提醒内容
    private String taskName;        // 关联任务名称（可选）
    private long reminderTime;      // 提醒时间（时间戳）
    private boolean vibrate;        // 是否震动
    private boolean sound;          // 是否播放声音
    private boolean repeat;         // 是否重复提醒
    private boolean active;         // 是否激活
    private long createdTime;       // 创建时间
    
    public ReminderItem() {
        this.id = generateId();
        this.createdTime = System.currentTimeMillis();
        this.active = true;
        this.vibrate = true;
        this.sound = true;
        this.repeat = false;
    }
    
    public ReminderItem(String content, long reminderTime) {
        this();
        this.content = content;
        this.reminderTime = reminderTime;
    }
    
    // 生成唯一ID
    private String generateId() {
        return "reminder_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public long getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public boolean isVibrate() {
        return vibrate;
    }
    
    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }
    
    public boolean isSound() {
        return sound;
    }
    
    public void setSound(boolean sound) {
        this.sound = sound;
    }
    
    public boolean isRepeat() {
        return repeat;
    }
    
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
    
    // 工具方法
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        return sdf.format(new Date(reminderTime));
    }
    
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(reminderTime));
    }
    
    public boolean isPast() {
        return reminderTime < System.currentTimeMillis();
    }
    
    public boolean isToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());
        String reminderDateStr = sdf.format(new Date(reminderTime));
        return todayStr.equals(reminderDateStr);
    }
    
    public String getContentSummary() {
        if (content == null || content.trim().isEmpty()) {
            return "无内容";
        }
        if (content.length() > 20) {
            return content.substring(0, 20) + "...";
        }
        return content;
    }
} 