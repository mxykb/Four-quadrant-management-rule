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
        AppDatabase database = AppDatabase.getDatabase(application);
        taskDao = database.taskDao();
        pomodoroDao = database.pomodoroDao();
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
        
        // 组合多个数据源
        LiveData<TaskDao.TaskCompletionStats> taskStats = taskDao.getTaskCompletionStats(startTime, endTime);
        LiveData<Integer> pomodoroCount = pomodoroDao.getCompletedPomodoroCountByTimeRange(startTime, endTime);
        LiveData<Float> avgImportance = taskDao.getAverageImportance();
        LiveData<Integer> totalFocusTime = pomodoroDao.getTotalFocusTimeByTimeRange(startTime, endTime);
        
        result.addSource(taskStats, stats -> {
            updateKpiData(result, taskStats.getValue(), pomodoroCount.getValue(), 
                         avgImportance.getValue(), totalFocusTime.getValue());
        });
        
        result.addSource(pomodoroCount, count -> {
            updateKpiData(result, taskStats.getValue(), count, 
                         avgImportance.getValue(), totalFocusTime.getValue());
        });
        
        result.addSource(avgImportance, avg -> {
            updateKpiData(result, taskStats.getValue(), pomodoroCount.getValue(), 
                         avg, totalFocusTime.getValue());
        });
        
        result.addSource(totalFocusTime, time -> {
            updateKpiData(result, taskStats.getValue(), pomodoroCount.getValue(), 
                         avgImportance.getValue(), time);
        });
        
        return result;
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
     * 获取四象限分布数据
     */
    public LiveData<List<TaskDao.QuadrantCount>> getQuadrantDistribution() {
        return taskDao.getQuadrantDistribution();
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
        long endTime = calendar.getTimeInMillis();
        long startTime;
        
        switch (timeRange) {
            case "today":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();
                break;
            case "week":
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startTime = calendar.getTimeInMillis();
                break;
            case "month":
                calendar.add(Calendar.MONTH, -1);
                startTime = calendar.getTimeInMillis();
                break;
            case "year":
                calendar.add(Calendar.YEAR, -1);
                startTime = calendar.getTimeInMillis();
                break;
            default:
                // 默认为本月
                calendar.add(Calendar.MONTH, -1);
                startTime = calendar.getTimeInMillis();
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
