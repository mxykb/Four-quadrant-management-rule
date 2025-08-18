package com.example.fourquadrant.model;

/**
 * AI工具数据模型
 */
public class AiTool {
    private String name;           // 工具名称
    private String displayName;    // 显示名称
    private String description;    // 工具描述
    private boolean enabled;       // 是否启用
    private int iconResId;         // 图标资源ID

    public AiTool(String name, String displayName, String description, boolean enabled, int iconResId) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.enabled = enabled;
        this.iconResId = iconResId;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    @Override
    public String toString() {
        return "AiTool{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", iconResId=" + iconResId +
                '}';
    }
}