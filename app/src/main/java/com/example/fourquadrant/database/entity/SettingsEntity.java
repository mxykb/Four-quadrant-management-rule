package com.example.fourquadrant.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * 设置实体类
 */
@Entity(tableName = "settings")
public class SettingsEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    private String key;
    
    @ColumnInfo(name = "value")
    private String value;
    
    @ColumnInfo(name = "type")
    private String type; // INT, STRING, BOOLEAN, FLOAT
    
    @ColumnInfo(name = "category")
    private String category; // POMODORO, REMINDER, APP, USER
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // 构造函数
    public SettingsEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public SettingsEntity(String key, String value, String type, String category) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // 便捷构造函数
    public static SettingsEntity createIntSetting(String key, int value, String category) {
        return new SettingsEntity(key, String.valueOf(value), "INT", category);
    }

    public static SettingsEntity createBooleanSetting(String key, boolean value, String category) {
        return new SettingsEntity(key, String.valueOf(value), "BOOLEAN", category);
    }

    public static SettingsEntity createStringSetting(String key, String value, String category) {
        return new SettingsEntity(key, value, "STRING", category);
    }

    // 便捷获取方法
    public int getIntValue() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getBooleanValue() {
        return Boolean.parseBoolean(value);
    }

    public String getStringValue() {
        return value != null ? value : "";
    }

    public float getFloatValue() {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public long getLongValue() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // Getters and Setters
    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
