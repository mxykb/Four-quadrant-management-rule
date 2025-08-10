package com.example.fourquadrant;

/**
 * 番茄钟完成记录数据模型
 */
public class PomodoroRecord {
    private String id;
    private String taskId;        // 关联的任务ID
    private String taskName;      // 任务名称
    private long startTime;       // 开始时间（毫秒）
    private long endTime;         // 结束时间（毫秒）
    private int durationMinutes;  // 实际时长（分钟）
    private boolean completed;    // 是否完成（未被中断）
    private String timeCategory;  // 时间段："上午"、"下午"、"晚上"
    
    public PomodoroRecord() {
        // 默认构造函数，用于Gson序列化
    }
    
    public PomodoroRecord(String taskId, String taskName, long startTime, int durationMinutes, boolean completed) {
        this.id = generateUniqueId();
        this.taskId = taskId;
        this.taskName = taskName;
        this.startTime = startTime;
        this.endTime = startTime + (durationMinutes * 60 * 1000L);
        this.durationMinutes = durationMinutes;
        this.completed = completed;
        this.timeCategory = getTimeCategoryFromTimestamp(startTime);
    }
    
    /**
     * 根据时间戳判断时间段
     */
    private String getTimeCategoryFromTimestamp(long timestamp) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        
        if (hour >= 6 && hour < 12) {
            return "上午";
        } else if (hour >= 12 && hour < 18) {
            return "下午";
        } else {
            return "晚上";
        }
    }
    
    private String generateUniqueId() {
        return "pomodoro_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
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
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public String getTimeCategory() {
        return timeCategory;
    }
    
    public void setTimeCategory(String timeCategory) {
        this.timeCategory = timeCategory;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PomodoroRecord record = (PomodoroRecord) obj;
        return id != null && id.equals(record.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
