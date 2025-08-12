package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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
    
    @ColumnInfo(name = "is_completed_pending")
    public boolean isCompletedPending; // 番茄钟已完成但用户未确认
    
    @ColumnInfo(name = "completed_task_name")
    public String completedTaskName; // 完成时的任务名称
    
    @ColumnInfo(name = "total_count")
    public int totalCount; // 总番茄钟数量
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    // 构造函数
    public TimerStateEntity() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    @Ignore
    public TimerStateEntity(long startTime, boolean isRunning, boolean isPaused, 
                           long remainingTime, boolean isBreak, int currentCount) {
        this.startTime = startTime;
        this.isRunning = isRunning;
        this.isPaused = isPaused;
        this.remainingTime = remainingTime;
        this.isBreak = isBreak;
        this.currentCount = currentCount;
        this.isCompletedPending = false;
        this.completedTaskName = null;
        this.totalCount = 0;
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
    
    public boolean isCompletedPending() {
        return isCompletedPending;
    }
    
    public void setCompletedPending(boolean completedPending) {
        isCompletedPending = completedPending;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getCompletedTaskName() {
        return completedTaskName;
    }
    
    public void setCompletedTaskName(String completedTaskName) {
        this.completedTaskName = completedTaskName;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.updatedAt = System.currentTimeMillis();
    }
}