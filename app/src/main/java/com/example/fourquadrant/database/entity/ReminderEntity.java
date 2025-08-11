package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;

/**
 * 提醒实体类
 */
@Entity(tableName = "reminders",
        foreignKeys = @ForeignKey(
                entity = TaskEntity.class,
                parentColumns = "id",
                childColumns = "task_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("task_id")})
public class ReminderEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    
    @ColumnInfo(name = "content")
    private String content;
    
    @ColumnInfo(name = "task_id")
    private String taskId;
    
    @ColumnInfo(name = "task_name")
    private String taskName;
    
    @ColumnInfo(name = "reminder_time")
    private long reminderTime;
    
    @ColumnInfo(name = "is_active")
    private boolean isActive;
    
    @ColumnInfo(name = "is_vibrate")
    private boolean isVibrate;
    
    @ColumnInfo(name = "is_sound")
    private boolean isSound;
    
    @ColumnInfo(name = "is_repeat")
    private boolean isRepeat;
    
    @ColumnInfo(name = "repeat_count")
    private int repeatCount;
    
    @ColumnInfo(name = "status")
    private String status; // ACTIVE, COMPLETED, SNOOZED, CANCELLED
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // 构造函数
    public ReminderEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "ACTIVE";
        this.isActive = true;
        this.repeatCount = 0;
    }

    @Ignore
    public ReminderEntity(String id, String content, long reminderTime) {
        this.id = id;
        this.content = content;
        this.reminderTime = reminderTime;
        this.isActive = true;
        this.status = "ACTIVE";
        this.repeatCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.isVibrate = vibrate;
    }

    public boolean isSound() {
        return isSound;
    }

    public void setSound(boolean sound) {
        this.isSound = sound;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        this.isRepeat = repeat;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
