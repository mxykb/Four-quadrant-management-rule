package com.example.fourquadrant;

import java.util.List;

public class ChartData {
    
    // 任务完成趋势数据
    public static class CompletionTrend {
        private String date;
        private int completedTasks;
        
        public CompletionTrend(String date, int completedTasks) {
            this.date = date;
            this.completedTasks = completedTasks;
        }
        
        public String getDate() { return date; }
        public int getCompletedTasks() { return completedTasks; }
        
        public void setDate(String date) { this.date = date; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    }
    
    // 四象限分布数据
    public static class QuadrantDistribution {
        private String quadrantName;
        private int taskCount;
        private int color;
        
        public QuadrantDistribution(String quadrantName, int taskCount, int color) {
            this.quadrantName = quadrantName;
            this.taskCount = taskCount;
            this.color = color;
        }
        
        public String getQuadrantName() { return quadrantName; }
        public int getTaskCount() { return taskCount; }
        public int getColor() { return color; }
        
        public void setQuadrantName(String quadrantName) { this.quadrantName = quadrantName; }
        public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
        public void setColor(int color) { this.color = color; }
    }
    
    // 番茄钟时间分布数据
    public static class PomodoroDistribution {
        private String timePeriod;
        private int pomodoroCount;
        private int color;
        
        public PomodoroDistribution(String timePeriod, int pomodoroCount, int color) {
            this.timePeriod = timePeriod;
            this.pomodoroCount = pomodoroCount;
            this.color = color;
        }
        
        public String getTimePeriod() { return timePeriod; }
        public int getPomodoroCount() { return pomodoroCount; }
        public int getColor() { return color; }
        
        public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }
        public void setPomodoroCount(int pomodoroCount) { this.pomodoroCount = pomodoroCount; }
        public void setColor(int color) { this.color = color; }
    }
    
    // 完整的图表数据集合
    public static class ChartDataSet {
        private List<CompletionTrend> completionTrends;
        private List<QuadrantDistribution> quadrantDistributions;
        private List<PomodoroDistribution> pomodoroDistributions;
        
        public ChartDataSet(List<CompletionTrend> completionTrends,
                           List<QuadrantDistribution> quadrantDistributions,
                           List<PomodoroDistribution> pomodoroDistributions) {
            this.completionTrends = completionTrends;
            this.quadrantDistributions = quadrantDistributions;
            this.pomodoroDistributions = pomodoroDistributions;
        }
        
        public List<CompletionTrend> getCompletionTrends() { return completionTrends; }
        public List<QuadrantDistribution> getQuadrantDistributions() { return quadrantDistributions; }
        public List<PomodoroDistribution> getPomodoroDistributions() { return pomodoroDistributions; }
        
        public void setCompletionTrends(List<CompletionTrend> completionTrends) { 
            this.completionTrends = completionTrends; 
        }
        public void setQuadrantDistributions(List<QuadrantDistribution> quadrantDistributions) { 
            this.quadrantDistributions = quadrantDistributions; 
        }
        public void setPomodoroDistributions(List<PomodoroDistribution> pomodoroDistributions) { 
            this.pomodoroDistributions = pomodoroDistributions; 
        }
    }
}
