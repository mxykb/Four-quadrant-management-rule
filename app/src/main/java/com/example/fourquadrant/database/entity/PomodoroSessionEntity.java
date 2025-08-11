package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;

/**
 * 番茄钟会话实体类
 */
@Entity(tableName = "pomodoro_sessions",
        foreignKeys = @ForeignKey(
                entity = TaskEntity.class,
                parentColumns = "id",
                childColumns = "task_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("task_id")})
public class PomodoroSessionEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    
    @ColumnInfo(name = "task_id")
    private String taskId;
    
    @ColumnInfo(name = "task_name")
    private String taskName;
    
    @ColumnInfo(name = "start_time")
    private long startTime;
    
    @ColumnInfo(name = "end_time")
    private Long endTime;
    
    @ColumnInfo(name = "duration_minutes")
    private int durationMinutes;
    
    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;
    
    @ColumnInfo(name = "is_break_session")
    private boolean isBreakSession;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // 构造函数
    public PomodoroSessionEntity() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public PomodoroSessionEntity(String id, String taskId, String taskName, 
                                 int durationMinutes, boolean isBreakSession) {
        this.id = id;
        this.taskId = taskId;
        this.taskName = taskName;
        this.durationMinutes = durationMinutes;
        this.isBreakSession = isBreakSession;
        this.startTime = System.currentTimeMillis();
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed && this.endTime == null) {
            this.endTime = System.currentTimeMillis();
        }
    }

    public boolean isBreakSession() {
        return isBreakSession;
    }

    public void setBreakSession(boolean breakSession) {
        this.isBreakSession = breakSession;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
