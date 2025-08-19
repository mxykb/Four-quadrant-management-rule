package com.example.fourquadrant.model;

/**
 * 番茄钟AI功能项数据类
 */
public class PomodoroFunction {
    private String id;
    private String name;
    private String description;
    private int iconResId;
    private boolean enabled;

    public PomodoroFunction(String id, String name, String description, int iconResId, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.enabled = enabled;
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

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}