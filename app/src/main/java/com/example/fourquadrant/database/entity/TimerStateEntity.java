package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * 计时器状态实体类
 */
@Entity(tableName = "timer_state")
public class TimerStateEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id = "timer_state_singleton"; // 单例ID，确保只有一条记录
    
    @ColumnInfo(name = "start_time")
    public long startTime;
    
    @ColumnInfo(name = "is_running")
    public boolean isRunning;
    
    @ColumnInfo(name = "is_paused")
    public boolean isPaused;
    
    @ColumnInfo(name = "remaining_time")
    public long remainingTime;
    
    @ColumnInfo(name = "is_break")
    public boolean isBreak;
    
    @ColumnInfo(name = "current_count")
    public int currentCount;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    // 构造函数
    public TimerStateEntity() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    public TimerStateEntity(long startTime, boolean isRunning, boolean isPaused, 
                           long remainingTime, boolean isBreak, int currentCount) {
        this.startTime = startTime;
        this.isRunning = isRunning;
        this.isPaused = isPaused;
        this.remainingTime = remainingTime;
        this.isBreak = isBreak;
        this.currentCount = currentCount;
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
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void setRunning(boolean running) {
        isRunning = running;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        isPaused = paused;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public long getRemainingTime() {
        return remainingTime;
    }
    
    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public boolean isBreak() {
        return isBreak;
    }
    
    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}