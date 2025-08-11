package com.example.fourquadrant;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.fourquadrant.database.repository.StatisticsRepository;
import com.example.fourquadrant.database.repository.TaskRepository;
import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.database.dao.TaskDao;
import com.example.fourquadrant.database.dao.PomodoroDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 统计数据管理器 - 基于数据库SQL查询的真实数据获取和计算
 */
public class StatisticsDataManager {
    
    private Context context;
    private StatisticsRepository statisticsRepository;
    private TaskRepository taskRepository;
    private PomodoroRepository pomodoroRepository;
    private String currentTimeRange = "today";
    
    public StatisticsDataManager(Context context) {
        this.context = context;
        
        // 初始化数据库仓库
        if (context instanceof Application) {
            Application app = (Application) context;
            this.statisticsRepository = new StatisticsRepository(app);
            this.taskRepository = new TaskRepository(app);
            this.pomodoroRepository = new PomodoroRepository(app);
        } else if (context.getApplicationContext() instanceof Application) {
            Application app = (Application) context.getApplicationContext();
            this.statisticsRepository = new StatisticsRepository(app);
            this.taskRepository = new TaskRepository(app);
            this.pomodoroRepository = new PomodoroRepository(app);
        }
    }
    
    /**
     * 记录番茄钟完成（保存到数据库）
     */
    public void recordPomodoroCompletion(String taskId, String taskName, int durationMinutes, boolean completed) {
        if (pomodoroRepository != null) {
            pomodoroRepository.recordPomodoroCompletion(taskId, taskName, durationMinutes);
        }
    }
    
    /**
     * 异步获取真实KPI数据（基于同步SQL查询确保准确性）
     */
    public LiveData<StatisticsData> getRealKpiDataAsync(String timeRange) {
        MutableLiveData<StatisticsData> result = new MutableLiveData<>();
        
        if (statisticsRepository == null) {
            // 如果数据库未初始化，返回默认数据
            result.setValue(new StatisticsData(0, 0, 0.0f, 0.0f));
            return result;
        }
        
        // 在后台线程执行同步查询
        new Thread(() -> {
            try {
                android.util.Log.d("StatisticsDataManager", "Getting real KPI data for timeRange: " + timeRange);
                
                // 使用同步方法获取准确数据
                StatisticsRepository.KpiData kpiData = statisticsRepository.getKpiDataSync(timeRange);
                
                StatisticsData statisticsData = new StatisticsData(
                    kpiData.completedTasks,
                    kpiData.pomodoroCount,
                    kpiData.completionRate,
                    kpiData.avgImportance
                );
                
                android.util.Log.d("StatisticsDataManager", "Retrieved KPI data: " + 
                    "completed=" + kpiData.completedTasks + 
                    ", pomodoro=" + kpiData.pomodoroCount + 
                    ", rate=" + kpiData.completionRate + 
                    ", importance=" + kpiData.avgImportance);
                
                // 在主线程更新结果
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    result.setValue(statisticsData);
                });
                
            } catch (Exception e) {
                android.util.Log.e("StatisticsDataManager", "Error getting real KPI data", e);
                // 在主线程设置默认值
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    result.setValue(new StatisticsData(0, 0, 0.0f, 0.0f));
                });
            }
        }).start();
        
        return result;
    }
    
    /**
     * 获取真实任务趋势数据（基于SQL查询）
     */
    public List<ChartData.CompletionTrend> getRealTaskTrendData(String timeRange) {
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        // 这里应该使用SQL查询获取趋势数据
        // 暂时返回基于时间范围的模拟数据，稍后会实现SQL查询
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        // 根据时间范围生成数据点
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        int dataPoints = getDataPointsForTimeRange(timeRange);
        long intervalMillis = (endDate.getTime() - startDate.getTime()) / dataPoints;
        
        for (int i = 0; i < dataPoints; i++) {
            Date periodStart = calendar.getTime();
            calendar.add(Calendar.MILLISECOND, (int)intervalMillis);
            
            String label = formatPeriodLabel(periodStart, timeRange, i);
            // TODO: 实现SQL查询获取每个时间段的完成任务数
            int completedCount = 0; // 暂时为0，等待SQL实现
            
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
        }
        
        return trendData;
    }
    
    /**
     * 获取真实四象限分布数据（基于SQL查询）
     */
    public List<ChartData.QuadrantDistribution> getRealQuadrantData(String timeRange) {
        List<ChartData.QuadrantDistribution> distributions = new ArrayList<>();
        
        if (statisticsRepository != null) {
            try {
                // 使用同步方式获取象限分布（在后台线程中调用）
                // TODO: 实现异步版本
                
                // 暂时返回默认分布
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要且紧急", 0, android.graphics.Color.parseColor("#F44336")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要不紧急", 0, android.graphics.Color.parseColor("#FF9800")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "紧急不重要", 0, android.graphics.Color.parseColor("#2196F3")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "不重要不紧急", 0, android.graphics.Color.parseColor("#9E9E9E")));
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return distributions;
    }
    
    /**
     * 获取真实番茄钟时间分布数据（基于SQL查询）
     */
    public List<ChartData.PomodoroDistribution> getRealPomodoroData(String timeRange) {
        List<ChartData.PomodoroDistribution> distributions = new ArrayList<>();
        
        // TODO: 实现基于SQL的番茄钟时间分布查询
        distributions.add(new ChartData.PomodoroDistribution(
            "上午", 0, android.graphics.Color.parseColor("#4CAF50")));
        distributions.add(new ChartData.PomodoroDistribution(
            "下午", 0, android.graphics.Color.parseColor("#FF9800")));
        distributions.add(new ChartData.PomodoroDistribution(
            "晚上", 0, android.graphics.Color.parseColor("#9C27B0")));
        
        return distributions;
    }
    
    /**
     * 获取真实高优先级任务列表（基于SQL查询）
     */
    public List<TaskAnalysisData.HighPriorityTask> getRealHighPriorityTasks(String timeRange) {
        List<TaskAnalysisData.HighPriorityTask> highPriorityTasks = new ArrayList<>();
        
        // TODO: 实现基于SQL的高优先级任务查询
        
        return highPriorityTasks;
    }
    
    /**
     * 获取真实耗时最长任务列表（基于SQL查询）
     */
    public List<TaskAnalysisData.LongestDurationTask> getRealLongestTasks(String timeRange) {
        List<TaskAnalysisData.LongestDurationTask> longestTasks = new ArrayList<>();
        
        // TODO: 实现基于SQL的最长任务查询
        
        return longestTasks;
    }
    
    /**
     * 获取智能建议（基于SQL统计数据）
     */
    public List<TaskAnalysisData.Suggestion> getRealSuggestions(String timeRange) {
        List<TaskAnalysisData.Suggestion> suggestions = new ArrayList<>();
        
        // TODO: 基于SQL统计结果生成智能建议
        suggestions.add(new TaskAnalysisData.Suggestion(
            "数据统计功能已切换到数据库模式，性能更优秀", 
            "general"
        ));
        
        return suggestions;
    }
    
    // ================= 工具方法 =================
    
    /**
     * 设置当前时间范围
     */
    public void setCurrentTimeRange(String timeRange) {
        this.currentTimeRange = timeRange;
    }
    
    /**
     * 获取当前时间范围
     */
    public String getCurrentTimeRange() {
        return currentTimeRange;
    }
    
    /**
     * 获取时间范围的毫秒值
     */
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