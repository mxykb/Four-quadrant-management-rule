package com.example.fourquadrant;

import java.util.List;

public class TaskAnalysisData {
    
    // 高优先级任务数据
    public static class HighPriorityTask {
        private String taskName;
        private int importance;
        private int urgency;
        private boolean isCompleted;
        private String taskId;
        
        public HighPriorityTask(String taskName, int importance, int urgency, boolean isCompleted, String taskId) {
            this.taskName = taskName;
            this.importance = importance;
            this.urgency = urgency;
            this.isCompleted = isCompleted;
            this.taskId = taskId;
        }
        
        // Getters
        public String getTaskName() { return taskName; }
        public int getImportance() { return importance; }
        public int getUrgency() { return urgency; }
        public boolean isCompleted() { return isCompleted; }
        public String getTaskId() { return taskId; }
        
        // Setters
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public void setImportance(int importance) { this.importance = importance; }
        public void setUrgency(int urgency) { this.urgency = urgency; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
    }
    
    // 耗时最长任务数据
    public static class LongestDurationTask {
        private String taskName;
        private int durationDays;
        private boolean isCompleted;
        private String taskId;
        
        public LongestDurationTask(String taskName, int durationDays, boolean isCompleted, String taskId) {
            this.taskName = taskName;
            this.durationDays = durationDays;
            this.isCompleted = isCompleted;
            this.taskId = taskId;
        }
        
        // Getters
        public String getTaskName() { return taskName; }
        public int getDurationDays() { return durationDays; }
        public boolean isCompleted() { return isCompleted; }
        public String getTaskId() { return taskId; }
        
        // Setters
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
    }
    
    // 建议数据
    public static class Suggestion {
        private String text;
        private String type; // "priority", "timing", "balance"
        
        public Suggestion(String text, String type) {
            this.text = text;
            this.type = type;
        }
        
        // Getters
        public String getText() { return text; }
        public String getType() { return type; }
        
        // Setters
        public void setText(String text) { this.text = text; }
        public void setType(String type) { this.type = type; }
    }
    
    // 任务分析数据集合
    public static class TaskAnalysisDataSet {
        private List<HighPriorityTask> highPriorityTasks;
        private List<LongestDurationTask> longestDurationTasks;
        private List<Suggestion> suggestions;
        
        public TaskAnalysisDataSet(List<HighPriorityTask> highPriorityTasks,
                                  List<LongestDurationTask> longestDurationTasks,
                                  List<Suggestion> suggestions) {
            this.highPriorityTasks = highPriorityTasks;
            this.longestDurationTasks = longestDurationTasks;
            this.suggestions = suggestions;
        }
        
        // Getters
        public List<HighPriorityTask> getHighPriorityTasks() { return highPriorityTasks; }
        public List<LongestDurationTask> getLongestDurationTasks() { return longestDurationTasks; }
        public List<Suggestion> getSuggestions() { return suggestions; }
        
        // Setters
        public void setHighPriorityTasks(List<HighPriorityTask> highPriorityTasks) { 
            this.highPriorityTasks = highPriorityTasks; 
        }
        public void setLongestDurationTasks(List<LongestDurationTask> longestDurationTasks) { 
            this.longestDurationTasks = longestDurationTasks; 
        }
        public void setSuggestions(List<Suggestion> suggestions) { 
            this.suggestions = suggestions; 
        }
    }
}
