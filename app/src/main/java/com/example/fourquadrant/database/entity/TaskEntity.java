package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * 任务实体类
 */
@Entity(tableName = "tasks")
public class TaskEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "importance")
    private int importance;
    
    @ColumnInfo(name = "urgency")
    private int urgency;
    
    @ColumnInfo(name = "quadrant")
    private int quadrant;
    
    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "completed_at")
    private Long completedAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted;

    // 构造函数
    public TaskEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
    }

    @Ignore
    public TaskEntity(String id, String name, int importance, int urgency) {
        this.id = id;
        this.name = name;
        this.importance = importance;
        this.urgency = urgency;
        this.quadrant = calculateQuadrant(importance, urgency);
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isDeleted = false;
    }

    // 计算象限
    private int calculateQuadrant(int importance, int urgency) {
        if (importance >= 5 && urgency >= 5) {
            return 1; // 重要且紧急
        } else if (importance >= 5 && urgency < 5) {
            return 2; // 重要但不紧急
        } else if (importance < 5 && urgency >= 5) {
            return 3; // 不重要但紧急
        } else {
            return 4; // 不重要且不紧急
        }
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
        this.quadrant = calculateQuadrant(importance, this.urgency);
        this.updatedAt = System.currentTimeMillis();
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
        this.quadrant = calculateQuadrant(this.importance, urgency);
        this.updatedAt = System.currentTimeMillis();
    }

    public int getQuadrant() {
        return quadrant;
    }

    public void setQuadrant(int quadrant) {
        this.quadrant = quadrant;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = System.currentTimeMillis();
        } else if (!completed) {
            this.completedAt = null;
        }
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        this.updatedAt = System.currentTimeMillis();
    }
}
