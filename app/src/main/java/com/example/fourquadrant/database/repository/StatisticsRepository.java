package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.TaskDao;
import com.example.fourquadrant.database.dao.PomodoroDao;
import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.StatisticsData;

import java.util.List;
import java.util.Calendar;

/**
 * 统计数据仓库
 * 整合任务和番茄钟数据，提供统计功能
 */
public class StatisticsRepository {
    
    private TaskDao taskDao;
    private PomodoroDao pomodoroDao;
    private TaskRepository taskRepository;
    private PomodoroRepository pomodoroRepository;
    
    public StatisticsRepository(Application application) {
        AppDatabase database = null;
        try {
            if (application instanceof com.example.fourquadrant.FourQuadrantApplication) {
                // 使用Application中的单例数据库实例
                database = ((com.example.fourquadrant.FourQuadrantApplication) application).getDatabase();
            } else {
                // 备用方案：直接获取数据库实例
                database = AppDatabase.getDatabase(application);
            }
            
            if (database != null) {
                taskDao = database.taskDao();
                pomodoroDao = database.pomodoroDao();
            } else {
                android.util.Log.e("StatisticsRepository", "Database is null, StatisticsRepository will have limited functionality");
                taskDao = null;
                pomodoroDao = null;
            }
        } catch (Exception e) {
            android.util.Log.e("StatisticsRepository", "Error initializing StatisticsRepository", e);
            taskDao = null;
            pomodoroDao = null;
        }
        
        taskRepository = new TaskRepository(application);
        pomodoroRepository = new PomodoroRepository(application);
    }
    
    /**
     * 获取KPI统计数据
     */
    public LiveData<KpiData> getKpiData(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        long startTime = timeRangeMillis[0];
        long endTime = timeRangeMillis[1];
        
        MediatorLiveData<KpiData> result = new MediatorLiveData<>();
        
        // 使用新的SQL查询组合多个数据源
        LiveData<Integer> completedTasks = taskDao.getCompletedTaskCountByTimeRange(startTime, endTime);
        LiveData<Integer> pomodoroCount = pomodoroDao.getCompletedPomodoroCountByTimeRange(startTime, endTime);
        LiveData<Float> completionRate = taskDao.getCompletionRateByTimeRange(startTime, endTime);
        LiveData<Float> avgImportance = taskDao.getAverageImportanceByTimeRange(startTime, endTime);
        
        result.addSource(completedTasks, tasks -> {
            updateKpiDataNew(result, tasks, pomodoroCount.getValue(), 
                           completionRate.getValue(), avgImportance.getValue());
        });
        
        result.addSource(pomodoroCount, count -> {
            updateKpiDataNew(result, completedTasks.getValue(), count, 
                           completionRate.getValue(), avgImportance.getValue());
        });
        
        result.addSource(completionRate, rate -> {
            updateKpiDataNew(result, completedTasks.getValue(), pomodoroCount.getValue(), 
                           rate, avgImportance.getValue());
        });
        
        result.addSource(avgImportance, avg -> {
            updateKpiDataNew(result, completedTasks.getValue(), pomodoroCount.getValue(), 
                           completionRate.getValue(), avg);
        });
        
        return result;
    }
    
    /**
     * 同步获取KPI统计数据（确保数据准确性）
     */
    public KpiData getKpiDataSync(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        long startTime = timeRangeMillis[0];
        long endTime = timeRangeMillis[1];
        
        android.util.Log.d("StatisticsRepository", "Getting KPI data for range: " + timeRange +
            " (" + new java.util.Date(startTime) + " to " + new java.util.Date(endTime) + ")");
        
        KpiData kpiData = new KpiData();
        
        try {
            // 同步查询所有统计数据
            kpiData.completedTasks = taskDao.getCompletedTaskCountByTimeRangeSync(startTime, endTime);
            kpiData.pomodoroCount = pomodoroDao.getCompletedPomodoroCountByTimeRangeSync(startTime, endTime);
            
            Float completionRate = taskDao.getCompletionRateByTimeRangeSync(startTime, endTime);
            kpiData.completionRate = completionRate != null ? completionRate : 0.0f;
            
            Float avgImportance = taskDao.getAverageImportanceByTimeRangeSync(startTime, endTime);
            kpiData.avgImportance = avgImportance != null ? avgImportance : 0.0f;
            
            android.util.Log.d("StatisticsRepository", "KPI Data - Completed: " + kpiData.completedTasks +
                ", Pomodoro: " + kpiData.pomodoroCount + ", Rate: " + kpiData.completionRate + 
                ", Importance: " + kpiData.avgImportance);
                
        } catch (Exception e) {
            android.util.Log.e("StatisticsRepository", "Error getting sync KPI data", e);
        }
        
        return kpiData;
    }
    
    private void updateKpiDataNew(MediatorLiveData<KpiData> result, 
                                 Integer completedTasks,
                                 Integer pomodoroCount,
                                 Float completionRate,
                                 Float avgImportance) {
        if (completedTasks != null || pomodoroCount != null || completionRate != null || avgImportance != null) {
            KpiData kpiData = new KpiData();
            
            kpiData.completedTasks = completedTasks != null ? completedTasks : 0;
            kpiData.pomodoroCount = pomodoroCount != null ? pomodoroCount : 0;
            kpiData.completionRate = completionRate != null ? completionRate : 0.0f;
            kpiData.avgImportance = avgImportance != null ? avgImportance : 0.0f;
            kpiData.totalFocusTime = 0; // 暂时设为0，可以后续添加
            
            result.setValue(kpiData);
        }
    }
    
    private void updateKpiData(MediatorLiveData<KpiData> result, 
                              TaskDao.TaskCompletionStats taskStats,
                              Integer pomodoroCount,
                              Float avgImportance,
                              Integer totalFocusTime) {
        if (taskStats != null || pomodoroCount != null || avgImportance != null || totalFocusTime != null) {
            KpiData kpiData = new KpiData();
            
            if (taskStats != null) {
                kpiData.completedTasks = taskStats.completed_tasks;
                kpiData.completionRate = taskStats.completion_rate;
            }
            
            kpiData.pomodoroCount = pomodoroCount != null ? pomodoroCount : 0;
            kpiData.avgImportance = avgImportance != null ? avgImportance : 0.0f;
            kpiData.totalFocusTime = totalFocusTime != null ? totalFocusTime : 0;
            
            result.setValue(kpiData);
        }
    }
    
    /**
     * 获取四象限分布数据（活跃任务）
     */
    public LiveData<List<TaskDao.QuadrantCount>> getActiveQuadrantDistribution() {
        if (taskDao == null) {
            android.util.Log.w("StatisticsRepository", "TaskDao is null, returning null for getActiveQuadrantDistribution");
            return null;
        }
        return taskDao.getActiveTaskQuadrantDistribution();
    }
    
    /**
     * 获取四象限分布数据（按时间范围的已完成任务）
     */
    public LiveData<List<TaskDao.QuadrantCount>> getCompletedQuadrantDistribution(String timeRange) {
        if (taskDao == null) {
            android.util.Log.w("StatisticsRepository", "TaskDao is null, returning null for getCompletedQuadrantDistribution");
            return null;
        }
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        long startTime = timeRangeMillis[0];
        long endTime = timeRangeMillis[1];
        return taskDao.getCompletedTaskQuadrantDistribution(startTime, endTime);
    }
    
    /**
     * 同步获取四象限分布数据（活跃任务）
     */
    public List<TaskDao.QuadrantCount> getActiveQuadrantDistributionSync() {
        if (taskDao == null) {
            android.util.Log.w("StatisticsRepository", "TaskDao is null, returning empty list for getActiveQuadrantDistributionSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getActiveTaskQuadrantDistributionSync();
    }
    
    /**
     * 同步获取四象限分布数据（按时间范围的已完成任务）
     */
    public List<TaskDao.QuadrantCount> getCompletedQuadrantDistributionSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("StatisticsRepository", "TaskDao is null, returning empty list for getCompletedQuadrantDistributionSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getCompletedTaskQuadrantDistributionSync(startTime, endTime);
    }
    
    /**
     * 获取番茄钟时间段分布数据
     */
    public LiveData<List<PomodoroDao.TimePeriodStats>> getTimePeriodDistribution(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        long startTime = timeRangeMillis[0];
        long endTime = timeRangeMillis[1];
        return pomodoroDao.getTimePeriodStats(startTime, endTime);
    }
    
    /**
     * 获取每日任务完成趋势
     */
    public LiveData<List<TaskDao.DailyCompletionStats>> getDailyCompletionTrend(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        long startTime = timeRangeMillis[0];
        long endTime = timeRangeMillis[1];
        return taskDao.getDailyCompletionStats(startTime, endTime);
    }
    
    /**
     * 获取每日番茄钟统计
     */
    public LiveData<List<PomodoroDao.DailyPomodoroStats>> getDailyPomodoroStats(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        return pomodoroDao.getDailyPomodoroStats(timeRangeMillis[0], timeRangeMillis[1]);
    }
    
    /**
     * 获取任务番茄钟统计
     */
    public LiveData<List<PomodoroDao.TaskPomodoroStats>> getTaskPomodoroStats(String timeRange) {
        long[] timeRangeMillis = getTimeRangeMillis(timeRange);
        return pomodoroDao.getTaskPomodoroStatsByTimeRange(timeRangeMillis[0], timeRangeMillis[1]);
    }
    
    /**
     * 获取高优先级任务
     */
    public LiveData<List<TaskEntity>> getHighPriorityTasks(int limit) {
        return taskDao.getHighPriorityTasks(limit);
    }
    
    /**
     * 获取最长专注时间的任务
     */
    public LiveData<List<PomodoroDao.TaskPomodoroStats>> getLongestFocusTaskStats(int limit) {
        return pomodoroDao.getLongestFocusTaskStats(limit);
    }
    
    /**
     * 记录番茄钟完成
     */
    public void recordPomodoroCompletion(String taskId, String taskName, int durationMinutes, boolean completed) {
        pomodoroRepository.recordPomodoroCompletion(taskId, taskName, durationMinutes);
    }
    
    /**
     * 获取时间范围的毫秒值
     */
    private long[] getTimeRangeMillis(String timeRange) {
        Calendar calendar = Calendar.getInstance();
        long endTime;
        long startTime;
        
        switch (timeRange) {
            case "today":
                // 今天的开始时间（00:00:00）
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();
                
                // 今天的结束时间（23:59:59.999）
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                endTime = calendar.getTimeInMillis();
                
                android.util.Log.d("StatisticsRepository", "Today range: " + 
                    new java.util.Date(startTime) + " to " + new java.util.Date(endTime));
                break;
                
            case "week":
                // 当前时间作为结束时间
                endTime = System.currentTimeMillis();
                // 7天前作为开始时间
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startTime = calendar.getTimeInMillis();
                break;
                
            case "month":
                // 当前时间作为结束时间
                endTime = System.currentTimeMillis();
                // 30天前作为开始时间
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                startTime = calendar.getTimeInMillis();
                break;
                
            case "year":
                // 当前时间作为结束时间
                endTime = System.currentTimeMillis();
                // 365天前作为开始时间
                calendar.add(Calendar.DAY_OF_YEAR, -365);
                startTime = calendar.getTimeInMillis();
                break;
                
            default:
                // 默认为今天
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();
                
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                endTime = calendar.getTimeInMillis();
                break;
        }
        
        return new long[]{startTime, endTime};
    }
    
    /**
     * KPI数据类
     */
    public static class KpiData {
        public int completedTasks;
        public int pomodoroCount;
        public float completionRate;
        public float avgImportance;
        public int totalFocusTime;
        
        public KpiData() {
            this.completedTasks = 0;
            this.pomodoroCount = 0;
            this.completionRate = 0.0f;
            this.avgImportance = 0.0f;
            this.totalFocusTime = 0;
        }
    }
}
