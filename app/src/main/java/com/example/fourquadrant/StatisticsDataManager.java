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
        android.util.Log.d("StatisticsDataManager", "开始获取任务趋势数据，时间范围: " + timeRange);
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        if (taskRepository == null) {
            android.util.Log.e("StatisticsDataManager", "taskRepository为null，返回空数据");
            return trendData;
        }
        
        Date[] dateRange = getDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        android.util.Log.d("StatisticsDataManager", "时间范围: " + startDate + " 到 " + endDate);
        
        // 根据时间范围确定统计方式
        if (timeRange.equals("today")) {
            android.util.Log.d("StatisticsDataManager", "使用今日按小时统计");
            // 今日：按24小时统计
            trendData = getTodayHourlyTrend(startDate, endDate);
        } else if (timeRange.equals("week")) {
            android.util.Log.d("StatisticsDataManager", "使用本周按天统计");
            // 本周：按7天统计
            trendData = getWeeklyDailyTrend(startDate, endDate);
        } else if (timeRange.equals("month")) {
            android.util.Log.d("StatisticsDataManager", "使用本月按天统计");
            // 本月：按30天统计
            trendData = getMonthlyDailyTrend(startDate, endDate);
        } else if (timeRange.startsWith("custom_")) {
            // 自定义时间：根据天数判断统计方式
            long daysDiff = (endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
            android.util.Log.d("StatisticsDataManager", "自定义时间范围，天数差: " + daysDiff);
            if (daysDiff <= 1) {
                android.util.Log.d("StatisticsDataManager", "使用自定义按小时统计");
                // 一天内：按小时统计
                trendData = getTodayHourlyTrend(startDate, endDate);
            } else {
                android.util.Log.d("StatisticsDataManager", "使用自定义按天统计");
                // 超过一天：按天统计
                trendData = getCustomDailyTrend(startDate, endDate, (int)daysDiff);
            }
        }
        
        android.util.Log.d("StatisticsDataManager", "获取到趋势数据点数量: " + trendData.size());
        for (int i = 0; i < trendData.size(); i++) {
            ChartData.CompletionTrend trend = trendData.get(i);
//            android.util.Log.d("StatisticsDataManager", "数据点[" + i + "]: " + trend. + " = " + trend.count);
        }
        
        return trendData;
    }
    
    /**
     * 获取真实四象限分布数据（基于SQL查询）
     */
    public List<ChartData.QuadrantDistribution> getRealQuadrantData(String timeRange) {
        android.util.Log.d("StatisticsDataManager", "开始获取四象限分布数据，时间范围: " + timeRange);
        List<ChartData.QuadrantDistribution> distributions = new ArrayList<>();
        
        if (statisticsRepository != null) {
            try {
                List<TaskDao.QuadrantCount> quadrantCounts;
                
                // 根据时间范围选择不同的查询方式
                if (timeRange.equals("active")) {
                    // 查询活跃任务的象限分布
                    android.util.Log.d("StatisticsDataManager", "查询活跃任务象限分布");
                    quadrantCounts = statisticsRepository.getActiveQuadrantDistributionSync();
                } else {
                    // 查询指定时间范围内已完成任务的象限分布
                    android.util.Log.d("StatisticsDataManager", "查询已完成任务象限分布");
                    Date[] dateRange = getDateRange(timeRange);
                    long startTime = dateRange[0].getTime();
                    long endTime = dateRange[1].getTime();
                    android.util.Log.d("StatisticsDataManager", "时间范围: " + startTime + " 到 " + endTime);
                    quadrantCounts = statisticsRepository.getCompletedQuadrantDistributionSync(startTime, endTime);
                }
                
                android.util.Log.d("StatisticsDataManager", "SQL查询返回象限数据条数: " + (quadrantCounts != null ? quadrantCounts.size() : "null"));
                
                // 初始化四个象限的计数
                Map<Integer, Integer> quadrantMap = new HashMap<>();
                quadrantMap.put(1, 0); // 重要且紧急
                quadrantMap.put(2, 0); // 重要不紧急
                quadrantMap.put(3, 0); // 紧急不重要
                quadrantMap.put(4, 0); // 不重要不紧急
                
                // 填充实际数据
                if (quadrantCounts != null) {
                    for (TaskDao.QuadrantCount count : quadrantCounts) {
                        android.util.Log.d("StatisticsDataManager", "象限 " + count.quadrant + " 有 " + count.count + " 个任务");
                        quadrantMap.put(count.quadrant, count.count);
                    }
                }
                
                // 创建分布数据
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要且紧急", quadrantMap.get(1), android.graphics.Color.parseColor("#F44336")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要不紧急", quadrantMap.get(2), android.graphics.Color.parseColor("#FF9800")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "紧急不重要", quadrantMap.get(3), android.graphics.Color.parseColor("#2196F3")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "不重要不紧急", quadrantMap.get(4), android.graphics.Color.parseColor("#9E9E9E")));
                
                android.util.Log.d("StatisticsDataManager", "四象限分布数据创建完成，共 " + distributions.size() + " 个象限");
                
            } catch (Exception e) {
                android.util.Log.e("StatisticsDataManager", "获取四象限分布数据时出错", e);
                e.printStackTrace();
                
                // 出错时返回空数据
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要且紧急", 0, android.graphics.Color.parseColor("#F44336")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "重要不紧急", 0, android.graphics.Color.parseColor("#FF9800")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "紧急不重要", 0, android.graphics.Color.parseColor("#2196F3")));
                distributions.add(new ChartData.QuadrantDistribution(
                    "不重要不紧急", 0, android.graphics.Color.parseColor("#9E9E9E")));
            }
        } else {
            android.util.Log.w("StatisticsDataManager", "statisticsRepository为null，返回空数据");
            // 数据库未初始化时返回空数据
            distributions.add(new ChartData.QuadrantDistribution(
                "重要且紧急", 0, android.graphics.Color.parseColor("#F44336")));
            distributions.add(new ChartData.QuadrantDistribution(
                "重要不紧急", 0, android.graphics.Color.parseColor("#FF9800")));
            distributions.add(new ChartData.QuadrantDistribution(
                "紧急不重要", 0, android.graphics.Color.parseColor("#2196F3")));
            distributions.add(new ChartData.QuadrantDistribution(
                "不重要不紧急", 0, android.graphics.Color.parseColor("#9E9E9E")));
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
        Date endDate;
        Date startDate;
        
        switch (timeRange) {
            case "today":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                // 设置结束时间为当天的23:59:59
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;
                
            case "week":
                // 获取当前周的周一到周日
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                // 设置结束时间为周日的23:59:59
                calendar.add(Calendar.DAY_OF_YEAR, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;
                
            case "month":
                // 获取当前月的第一天到最后一天
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                
                // 设置结束时间为当月最后一天的23:59:59
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
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
                    endDate = new Date();
                }
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    startDate = calendar.getTime();
                    endDate = new Date();
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
    
    /**
     * 获取今日按小时统计的任务完成趋势
     */
    private List<ChartData.CompletionTrend> getTodayHourlyTrend(Date startDate, Date endDate) {
        android.util.Log.d("StatisticsDataManager", "开始获取今日按小时趋势数据");
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        // 使用SQL查询按小时统计的数据
        android.util.Log.d("StatisticsDataManager", "查询时间范围: " + startDate.getTime() + " 到 " + endDate.getTime());
        List<TaskDao.HourlyCompletionStats> hourlyStats = taskRepository.getHourlyCompletionStatsSync(
            startDate.getTime(), endDate.getTime());
        android.util.Log.d("StatisticsDataManager", "SQL查询返回小时统计数据条数: " + hourlyStats.size());
        
        // 创建一个Map来存储每小时的完成数量
        Map<String, Integer> hourlyMap = new HashMap<>();
        for (TaskDao.HourlyCompletionStats stat : hourlyStats) {
            android.util.Log.d("StatisticsDataManager", "小时统计: " + stat.hour + " 时完成 " + stat.completed_count + " 个任务");
            hourlyMap.put(stat.hour, stat.completed_count);
        }
        
        // 按24小时生成完整数据（包括没有任务完成的小时）
        android.util.Log.d("StatisticsDataManager", "开始生成24小时完整数据");
        for (int hour = 0; hour < 24; hour++) {
            String hourKey = String.format("%02d", hour);
            String label = String.format("%02d:00", hour);
            int completedCount = hourlyMap.getOrDefault(hourKey, 0);
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
            android.util.Log.d("StatisticsDataManager", "小时数据点: " + label + " = " + completedCount);
        }
        
        android.util.Log.d("StatisticsDataManager", "今日按小时趋势数据生成完成，共 " + trendData.size() + " 个数据点");
        return trendData;
    }
    
    /**
     * 获取本周按天统计的任务完成趋势
     */
    private List<ChartData.CompletionTrend> getWeeklyDailyTrend(Date startDate, Date endDate) {
        android.util.Log.d("StatisticsDataManager", "开始获取本周按天趋势数据");
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        // 使用SQL查询按天统计的数据
        android.util.Log.d("StatisticsDataManager", "查询时间范围: " + startDate.getTime() + " 到 " + endDate.getTime());
        List<TaskDao.DailyCompletionStats> dailyStats = taskRepository.getDailyCompletionStatsSync(
            startDate.getTime(), endDate.getTime());
        android.util.Log.d("StatisticsDataManager", "SQL查询返回每日统计数据条数: " + dailyStats.size());
        
        // 创建一个Map来存储每天的完成数量
        Map<String, Integer> dailyMap = new HashMap<>();
        for (TaskDao.DailyCompletionStats stat : dailyStats) {
            android.util.Log.d("StatisticsDataManager", "每日统计: " + stat.date + " 完成 " + stat.completed_count + " 个任务");
            dailyMap.put(stat.date, stat.completed_count);
        }
        
        // 按7天生成完整数据（包括没有任务完成的天）
        android.util.Log.d("StatisticsDataManager", "开始生成7天完整数据");
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat keyFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        
        for (int day = 0; day < 7; day++) {
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, day);
            Date dayStart = calendar.getTime();
            
            String dateKey = keyFormat.format(dayStart);
            String label = sdf.format(dayStart);
            int completedCount = dailyMap.getOrDefault(dateKey, 0);
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
            android.util.Log.d("StatisticsDataManager", "每日数据点: " + label + " (" + dateKey + ") = " + completedCount);
        }
        
        android.util.Log.d("StatisticsDataManager", "本周按天趋势数据生成完成，共 " + trendData.size() + " 个数据点");
        return trendData;
    }
    
    /**
     * 获取本月按天统计的任务完成趋势
     */
    private List<ChartData.CompletionTrend> getMonthlyDailyTrend(Date startDate, Date endDate) {
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        // 使用SQL查询按天统计的数据
        List<TaskDao.DailyCompletionStats> dailyStats = taskRepository.getDailyCompletionStatsSync(
            startDate.getTime(), endDate.getTime());
        
        // 创建一个Map来存储每天的完成数量
        Map<String, Integer> dailyMap = new HashMap<>();
        for (TaskDao.DailyCompletionStats stat : dailyStats) {
            dailyMap.put(stat.date, stat.completed_count);
        }
        
        // 按30天生成完整数据（包括没有任务完成的天）
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat keyFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        
        for (int day = 0; day < 30; day++) {
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, day);
            Date dayStart = calendar.getTime();
            
            // 如果超出结束时间，停止统计
            if (dayStart.getTime() > endDate.getTime()) {
                break;
            }
            
            String dateKey = keyFormat.format(dayStart);
            String label = sdf.format(dayStart);
            int completedCount = dailyMap.getOrDefault(dateKey, 0);
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
        }
        
        return trendData;
    }
    
    /**
     * 获取自定义时间范围按天统计的任务完成趋势
     */
    private List<ChartData.CompletionTrend> getCustomDailyTrend(Date startDate, Date endDate, int totalDays) {
        List<ChartData.CompletionTrend> trendData = new ArrayList<>();
        
        // 使用SQL查询按天统计的数据
        List<TaskDao.DailyCompletionStats> dailyStats = taskRepository.getDailyCompletionStatsSync(
            startDate.getTime(), endDate.getTime());
        
        // 创建一个Map来存储每天的完成数量
        Map<String, Integer> dailyMap = new HashMap<>();
        for (TaskDao.DailyCompletionStats stat : dailyStats) {
            dailyMap.put(stat.date, stat.completed_count);
        }
        
        // 按天生成完整数据（包括没有任务完成的天）
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat keyFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        
        for (int day = 0; day <= totalDays; day++) {
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, day);
            Date dayStart = calendar.getTime();
            
            // 如果超出结束时间，停止统计
            if (dayStart.getTime() > endDate.getTime()) {
                break;
            }
            
            String dateKey = keyFormat.format(dayStart);
            String label = sdf.format(dayStart);
            int completedCount = dailyMap.getOrDefault(dateKey, 0);
            trendData.add(new ChartData.CompletionTrend(label, completedCount));
        }
        
        return trendData;
    }
}