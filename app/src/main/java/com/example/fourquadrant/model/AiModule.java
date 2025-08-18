package com.example.fourquadrant.model;

/**
 * AI模块数据类
 * 用于表示智能工具主页的AI模块信息
 */
public class AiModule {
    private String id;
    private String name;
    private String description;
    private String icon;
    private boolean enabled;
    private Class<?> targetActivity;

    public AiModule(String id, String name, String description, String icon, boolean enabled, Class<?> targetActivity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.enabled = enabled;
        this.targetActivity = targetActivity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Class<?> getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(Class<?> targetActivity) {
        this.targetActivity = targetActivity;
    }
}