package com.example.fourquadrant;

public class StatisticsData {
    private int completedTasksToday;
    private int pomodoroCountWeek;
    private float completionRateWeek;
    private float avgImportanceScore;
    
    public StatisticsData() {
        // 构造函数
    }
    
    public StatisticsData(int completedTasksToday, int pomodoroCountWeek, 
                         float completionRateWeek, float avgImportanceScore) {
        this.completedTasksToday = completedTasksToday;
        this.pomodoroCountWeek = pomodoroCountWeek;
        this.completionRateWeek = completionRateWeek;
        this.avgImportanceScore = avgImportanceScore;
    }
    
    // Getters
    public int getCompletedTasksToday() {
        return completedTasksToday;
    }
    
    public int getPomodoroCountWeek() {
        return pomodoroCountWeek;
    }
    
    public float getCompletionRateWeek() {
        return completionRateWeek;
    }
    
    public float getAvgImportanceScore() {
        return avgImportanceScore;
    }
    
    // Setters
    public void setCompletedTasksToday(int completedTasksToday) {
        this.completedTasksToday = completedTasksToday;
    }
    
    public void setPomodoroCountWeek(int pomodoroCountWeek) {
        this.pomodoroCountWeek = pomodoroCountWeek;
    }
    
    public void setCompletionRateWeek(float completionRateWeek) {
        this.completionRateWeek = completionRateWeek;
    }
    
    public void setAvgImportanceScore(float avgImportanceScore) {
        this.avgImportanceScore = avgImportanceScore;
    }
}
