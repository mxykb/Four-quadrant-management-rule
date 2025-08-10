package com.example.fourquadrant;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 统计数据管理器 - 负责真实数据的获取和计算
 */
public class StatisticsDataManager {
    
    private static final String PREF_POMODORO_RECORDS = "PomodoroRecords";
    private static final String KEY_POMODORO_RECORDS = "pomodoro_records";
    private static final String PREF_TASK_RECORDS = "TaskRecords";
    
    private Context context;
    private Gson gson;
    
    public StatisticsDataManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }
    
    /**
     * 记录番茄钟完成
     */
    public void recordPomodoroCompletion(String taskId, String taskName, int durationMinutes, boolean completed) {
        List<PomodoroRecord> records = loadPomodoroRecords();
        PomodoroRecord newRecord = new PomodoroRecord(
            taskId, taskName, System.currentTimeMillis(), durationMinutes, completed
        );
        records.add(newRecord);
        savePomodoroRecords(records);
    }
    
    /**
     * 获取真实KPI数据
     */
    public StatisticsData getRealKpiData(String timeRange) {
        List<TaskListFragment.TaskItem> allTasks = loadAllTasks();
        List<PomodoroRecord> pomodoroRecords = loadPomodoroRecords();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 计算完成任务数
        int completedTasks = calculateCompletedTasks(allTasks, startDate, endDate);
        
        // 计算番茄钟数
        int pomodoroCount = calculatePomodoroCount(pomodoroRecords, startDate, endDate);
        
        // 计算完成率
        float completionRate = calculateCompletionRate(allTasks, startDate, endDate);
        
        // 计算平均重要性
        float avgImportance = calculateAverageImportance(allTasks, startDate, endDate);
        
        return new StatisticsData(completedTasks, pomodoroCount, completionRate, avgImportance);
    }
    
    /**
     * 获取真实任务趋势数据
     */
    public List<ChartData.CompletionTrend> getRealTaskTrendData(String timeRange) {
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        List<TaskListFragment.TaskItem> allTasks = loadAllTasks();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 根据时间范围生成趋势数据
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        int dataPoints = getDataPointsForTimeRange(timeRange);
        long intervalMillis = (endDate.getTime() - startDate.getTime()) / dataPoints;
        
        for (int i = 0; i < dataPoints; i++) {
            Date periodStart = calendar.getTime();
            calendar.add(Calendar.MILLISECOND, (int)intervalMillis);
            Date periodEnd = calendar.getTime();
            
            int completedCount = calculateCompletedTasks(allTasks, periodStart, periodEnd);
            String label = formatPeriodLabel(periodStart, timeRange, i);
            
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
        }
        
        return trendData;
    }
    
    /**
     * 获取真实四象限分布数据
     */
    public List<ChartData.QuadrantDistribution> getRealQuadrantData(String timeRange) {
        List<ChartData.QuadrantDistribution> distributions = new ArrayList<>();
        List<TaskListFragment.TaskItem> allTasks = loadAllTasks();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 过滤时间范围内的任务
        List<TaskListFragment.TaskItem> filteredTasks = filterTasksByTimeRange(allTasks, startDate, endDate);
        
        // 按四象限分类统计
        int urgentImportant = 0;    // 重要且紧急
        int importantNotUrgent = 0; // 重要不紧急
        int urgentNotImportant = 0; // 紧急不重要
        int notUrgentNotImportant = 0; // 不重要不紧急
        
        for (TaskListFragment.TaskItem task : filteredTasks) {
            if (task.getImportance() >= 7 && task.getUrgency() >= 7) {
                urgentImportant++;
            } else if (task.getImportance() >= 7 && task.getUrgency() < 7) {
                importantNotUrgent++;
            } else if (task.getImportance() < 7 && task.getUrgency() >= 7) {
                urgentNotImportant++;
            } else {
                notUrgentNotImportant++;
            }
        }
        
        distributions.add(new ChartData.QuadrantDistribution(
            "重要且紧急", urgentImportant, android.graphics.Color.parseColor("#F44336")));
        distributions.add(new ChartData.QuadrantDistribution(
            "重要不紧急", importantNotUrgent, android.graphics.Color.parseColor("#FF9800")));
        distributions.add(new ChartData.QuadrantDistribution(
            "紧急不重要", urgentNotImportant, android.graphics.Color.parseColor("#2196F3")));
        distributions.add(new ChartData.QuadrantDistribution(
            "不重要不紧急", notUrgentNotImportant, android.graphics.Color.parseColor("#9E9E9E")));
        
        return distributions;
    }
    
    /**
     * 获取真实番茄钟时间分布数据
     */
    public List<ChartData.PomodoroDistribution> getRealPomodoroData(String timeRange) {
        List<ChartData.PomodoroDistribution> distributions = new ArrayList<>();
        List<PomodoroRecord> records = loadPomodoroRecords();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 过滤时间范围内的番茄钟记录
        List<PomodoroRecord> filteredRecords = filterPomodoroByTimeRange(records, startDate, endDate);
        
        // 按时间段统计
        int morningCount = 0;
        int afternoonCount = 0;
        int eveningCount = 0;
        
        for (PomodoroRecord record : filteredRecords) {
            if (record.isCompleted()) { // 只统计完成的番茄钟
                switch (record.getTimeCategory()) {
                    case "上午":
                        morningCount++;
                        break;
                    case "下午":
                        afternoonCount++;
                        break;
                    case "晚上":
                        eveningCount++;
                        break;
                }
            }
        }
        
        distributions.add(new ChartData.PomodoroDistribution(
            "上午", morningCount, android.graphics.Color.parseColor("#4CAF50")));
        distributions.add(new ChartData.PomodoroDistribution(
            "下午", afternoonCount, android.graphics.Color.parseColor("#FF9800")));
        distributions.add(new ChartData.PomodoroDistribution(
            "晚上", eveningCount, android.graphics.Color.parseColor("#9C27B0")));
        
        return distributions;
    }
    
    /**
     * 获取真实高优先级任务列表
     */
    public List<TaskAnalysisData.HighPriorityTask> getRealHighPriorityTasks(String timeRange) {
        List<TaskAnalysisData.HighPriorityTask> highPriorityTasks = new ArrayList<>();
        List<TaskListFragment.TaskItem> allTasks = loadAllTasks();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 过滤并排序高优先级任务
        List<TaskListFragment.TaskItem> filteredTasks = filterTasksByTimeRange(allTasks, startDate, endDate);
        
        for (TaskListFragment.TaskItem task : filteredTasks) {
            // 高优先级定义：重要性 >= 7 或 (重要性 >= 6 且紧急性 >= 7)
            if (task.getImportance() >= 7 || (task.getImportance() >= 6 && task.getUrgency() >= 7)) {
                highPriorityTasks.add(new TaskAnalysisData.HighPriorityTask(
                    task.getName(),
                    task.getImportance(),
                    task.getUrgency(),
                    task.isCompleted(),
                    task.getId()
                ));
            }
        }
        
        // 按优先级排序（重要性 + 紧急性）
        highPriorityTasks.sort((a, b) -> {
            int priorityA = a.getImportance() + a.getUrgency();
            int priorityB = b.getImportance() + b.getUrgency();
            return Integer.compare(priorityB, priorityA); // 降序
        });
        
        // 最多返回前10个
        return highPriorityTasks.subList(0, Math.min(10, highPriorityTasks.size()));
    }
    
    /**
     * 获取真实耗时最长任务列表
     */
    public List<TaskAnalysisData.LongestDurationTask> getRealLongestTasks(String timeRange) {
        List<TaskAnalysisData.LongestDurationTask> longestTasks = new ArrayList<>();
        List<PomodoroRecord> records = loadPomodoroRecords();
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 过滤时间范围内的番茄钟记录
        List<PomodoroRecord> filteredRecords = filterPomodoroByTimeRange(records, startDate, endDate);
        
        // 按任务ID汇总时长
        java.util.Map<String, Integer> taskDurations = new java.util.HashMap<>();
        java.util.Map<String, String> taskNames = new java.util.HashMap<>();
        java.util.Map<String, Boolean> taskCompletionStatus = new java.util.HashMap<>();
        
        for (PomodoroRecord record : filteredRecords) {
            if (record.isCompleted()) { // 只统计完成的番茄钟
                String taskId = record.getTaskId();
                String taskName = record.getTaskName();
                
                taskDurations.put(taskId, taskDurations.getOrDefault(taskId, 0) + record.getDurationMinutes());
                taskNames.put(taskId, taskName);
                
                // 检查任务是否已完成
                if (!taskCompletionStatus.containsKey(taskId)) {
                    TaskListFragment.TaskItem task = findTaskById(taskId);
                    taskCompletionStatus.put(taskId, task != null && task.isCompleted());
                }
            }
        }
        
        // 转换为列表并排序
        for (java.util.Map.Entry<String, Integer> entry : taskDurations.entrySet()) {
            String taskId = entry.getKey();
            int durationMinutes = entry.getValue();
            String taskName = taskNames.get(taskId);
            boolean completed = taskCompletionStatus.getOrDefault(taskId, false);
            
            // 将分钟转换为天数（用于显示，保留原始分钟数作为比较基准）
            int durationDays = Math.max(1, durationMinutes / (25 * 2)); // 假设每天2个番茄钟周期
            
            longestTasks.add(new TaskAnalysisData.LongestDurationTask(
                taskName,
                durationDays,
                completed,
                taskId
            ));
        }
        
        // 按时长排序（降序） - 这里我们需要按实际分钟数排序，但存储的是天数
        // 重新获取原始分钟数进行排序
        longestTasks.sort((a, b) -> {
            // 根据taskId重新获取分钟数进行比较
            String taskIdA = a.getTaskId();
            String taskIdB = b.getTaskId();
            int minutesA = taskDurations.getOrDefault(taskIdA, 0);
            int minutesB = taskDurations.getOrDefault(taskIdB, 0);
            return Integer.compare(minutesB, minutesA);
        });
        
        // 最多返回前10个
        return longestTasks.subList(0, Math.min(10, longestTasks.size()));
    }
    
    /**
     * 获取智能建议
     */
    public List<TaskAnalysisData.Suggestion> getRealSuggestions(String timeRange) {
        List<TaskAnalysisData.Suggestion> suggestions = new ArrayList<>();
        
        StatisticsData kpiData = getRealKpiData(timeRange);
        List<ChartData.QuadrantDistribution> quadrantData = getRealQuadrantData(timeRange);
        List<ChartData.PomodoroDistribution> pomodoroData = getRealPomodoroData(timeRange);
        
        // 基于真实数据生成建议
        
        // 1. 完成率建议
        if (kpiData.getCompletionRateWeek() < 60) {
            suggestions.add(new TaskAnalysisData.Suggestion(
                "任务完成率偏低(" + String.format("%.1f", kpiData.getCompletionRateWeek()) + "%)，建议合理安排任务量和优先级", 
                "completion"
            ));
        } else if (kpiData.getCompletionRateWeek() >= 90) {
            suggestions.add(new TaskAnalysisData.Suggestion(
                "任务完成率很高(" + String.format("%.1f", kpiData.getCompletionRateWeek()) + "%)，继续保持良好的执行力", 
                "praise"
            ));
        }
        
        // 2. 四象限分布建议
        int totalTasks = quadrantData.stream().mapToInt(ChartData.QuadrantDistribution::getTaskCount).sum();
        if (totalTasks > 0) {
            int importantNotUrgent = quadrantData.size() > 1 ? quadrantData.get(1).getTaskCount() : 0;
            float secondQuadrantRatio = (float) importantNotUrgent / totalTasks;
            
            if (secondQuadrantRatio < 0.3) {
                suggestions.add(new TaskAnalysisData.Suggestion(
                    "第二象限任务比例偏低，建议提前安排重要但不紧急的任务", 
                    "priority"
                ));
            }
        }
        
        // 3. 番茄钟时间分布建议
        int totalPomodoros = pomodoroData.stream().mapToInt(ChartData.PomodoroDistribution::getPomodoroCount).sum();
        if (totalPomodoros > 0) {
            int eveningCount = pomodoroData.size() > 2 ? pomodoroData.get(2).getPomodoroCount() : 0;
            float eveningRatio = (float) eveningCount / totalPomodoros;
            
            if (eveningRatio > 0.5) {
                suggestions.add(new TaskAnalysisData.Suggestion(
                    "番茄钟主要集中在晚上，可尝试调整作息提高白天效率", 
                    "timing"
                ));
            }
        }
        
        // 4. 番茄钟数量建议
        if (kpiData.getPomodoroCountWeek() < 5 && "week".equals(timeRange)) {
            suggestions.add(new TaskAnalysisData.Suggestion(
                "本周番茄钟数量较少，建议使用番茄钟技术提高专注度", 
                "efficiency"
            ));
        }
        
        // 如果没有建议，添加一个默认建议
        if (suggestions.isEmpty()) {
            suggestions.add(new TaskAnalysisData.Suggestion(
                "继续保持良好的任务管理习惯，定期回顾和调整工作计划", 
                "general"
            ));
        }
        
        return suggestions;
    }
    
    // ================= 私有辅助方法 =================
    
    private List<TaskListFragment.TaskItem> loadAllTasks() {
        SharedPreferences prefs = context.getSharedPreferences("TaskListPrefs", Context.MODE_PRIVATE);
        String tasksJson = prefs.getString("saved_tasks", "[]");
        Type type = new TypeToken<List<TaskListFragment.TaskItem>>(){}.getType();
        List<TaskListFragment.TaskItem> tasks = gson.fromJson(tasksJson, type);
        return tasks != null ? tasks : new ArrayList<>();
    }
    
    private List<PomodoroRecord> loadPomodoroRecords() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_POMODORO_RECORDS, Context.MODE_PRIVATE);
        String recordsJson = prefs.getString(KEY_POMODORO_RECORDS, "[]");
        Type type = new TypeToken<List<PomodoroRecord>>(){}.getType();
        List<PomodoroRecord> records = gson.fromJson(recordsJson, type);
        return records != null ? records : new ArrayList<>();
    }
    
    private void savePomodoroRecords(List<PomodoroRecord> records) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_POMODORO_RECORDS, Context.MODE_PRIVATE);
        String recordsJson = gson.toJson(records);
        prefs.edit().putString(KEY_POMODORO_RECORDS, recordsJson).apply();
    }
    
    private Date[] getDateRange(String timeRange) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(); // 当前时间
        Date startDate;
        
        switch (timeRange) {
            case "today":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
                
            case "week":
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = calendar.getTime();
                break;
                
            case "month":
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
                
            default:
                // custom时间范围处理
                if (timeRange.startsWith("custom_")) {
                    String[] parts = timeRange.split("_");
                    if (parts.length >= 3) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            startDate = sdf.parse(parts[1]);
                            endDate = sdf.parse(parts[2]);
                            
                            // 确保endDate是当天的23:59:59
                            calendar.setTime(endDate);
                            calendar.set(Calendar.HOUR_OF_DAY, 23);
                            calendar.set(Calendar.MINUTE, 59);
                            calendar.set(Calendar.SECOND, 59);
                            endDate = calendar.getTime();
                        } catch (Exception e) {
                            // 解析失败，默认为最近一周
                            calendar.add(Calendar.DAY_OF_YEAR, -7);
                            startDate = calendar.getTime();
                            endDate = new Date();
                        }
                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, -7);
                        startDate = calendar.getTime();
                    }
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    startDate = calendar.getTime();
                }
                break;
        }
        
        return new Date[]{startDate, endDate};
    }
    
    private int calculateCompletedTasks(List<TaskListFragment.TaskItem> tasks, Date startDate, Date endDate) {
        int count = 0;
        for (TaskListFragment.TaskItem task : tasks) {
            if (task.isCompleted() && 
                task.getCompletedTime() >= startDate.getTime() && 
                task.getCompletedTime() <= endDate.getTime()) {
                count++;
            }
        }
        return count;
    }
    
    private int calculatePomodoroCount(List<PomodoroRecord> records, Date startDate, Date endDate) {
        int count = 0;
        for (PomodoroRecord record : records) {
            if (record.isCompleted() && 
                record.getStartTime() >= startDate.getTime() && 
                record.getStartTime() <= endDate.getTime()) {
                count++;
            }
        }
        return count;
    }
    
    private float calculateCompletionRate(List<TaskListFragment.TaskItem> tasks, Date startDate, Date endDate) {
        int totalTasks = 0;
        int completedTasks = 0;
        
        for (TaskListFragment.TaskItem task : tasks) {
            // 任务在时间范围内创建或完成
            boolean inRange = (task.getCompletedTime() >= startDate.getTime() && task.getCompletedTime() <= endDate.getTime()) ||
                             (!task.isCompleted()); // 未完成的任务也计入（假设它们在当前期间创建）
            
            if (inRange) {
                totalTasks++;
                if (task.isCompleted() && 
                    task.getCompletedTime() >= startDate.getTime() && 
                    task.getCompletedTime() <= endDate.getTime()) {
                    completedTasks++;
                }
            }
        }
        
        return totalTasks > 0 ? (float) completedTasks / totalTasks * 100 : 0;
    }
    
    private float calculateAverageImportance(List<TaskListFragment.TaskItem> tasks, Date startDate, Date endDate) {
        List<TaskListFragment.TaskItem> filteredTasks = filterTasksByTimeRange(tasks, startDate, endDate);
        
        if (filteredTasks.isEmpty()) {
            return 0;
        }
        
        int totalImportance = 0;
        for (TaskListFragment.TaskItem task : filteredTasks) {
            totalImportance += task.getImportance();
        }
        
        return (float) totalImportance / filteredTasks.size();
    }
    
    private List<TaskListFragment.TaskItem> filterTasksByTimeRange(List<TaskListFragment.TaskItem> tasks, Date startDate, Date endDate) {
        List<TaskListFragment.TaskItem> filtered = new ArrayList<>();
        for (TaskListFragment.TaskItem task : tasks) {
            // 包含在时间范围内完成的任务，以及当前未完成的任务
            if ((task.isCompleted() && 
                 task.getCompletedTime() >= startDate.getTime() && 
                 task.getCompletedTime() <= endDate.getTime()) ||
                !task.isCompleted()) {
                filtered.add(task);
            }
        }
        return filtered;
    }
    
    private List<PomodoroRecord> filterPomodoroByTimeRange(List<PomodoroRecord> records, Date startDate, Date endDate) {
        List<PomodoroRecord> filtered = new ArrayList<>();
        for (PomodoroRecord record : records) {
            if (record.getStartTime() >= startDate.getTime() && 
                record.getStartTime() <= endDate.getTime()) {
                filtered.add(record);
            }
        }
        return filtered;
    }
    
    private TaskListFragment.TaskItem findTaskById(String taskId) {
        List<TaskListFragment.TaskItem> allTasks = loadAllTasks();
        for (TaskListFragment.TaskItem task : allTasks) {
            if (task.getId() != null && task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }
    
    private int getDataPointsForTimeRange(String timeRange) {
        switch (timeRange) {
            case "today":
                return 24; // 每小时一个数据点
            case "week":
                return 7;  // 每天一个数据点
            case "month":
                return 30; // 每天一个数据点
            default:
                return 10; // 自定义范围默认10个数据点
        }
    }
    
    private String formatPeriodLabel(Date date, String timeRange, int index) {
        java.text.SimpleDateFormat sdf;
        switch (timeRange) {
            case "today":
                sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                break;
            case "week":
            case "month":
                sdf = new java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault());
                break;
            default:
                sdf = new java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault());
                break;
        }
        return sdf.format(date);
    }
}
