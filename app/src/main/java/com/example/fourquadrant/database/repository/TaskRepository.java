package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.TaskDao;
import com.example.fourquadrant.database.entity.TaskEntity;

import java.util.List;
import java.util.UUID;

/**
 * 任务数据仓库
 */
public class TaskRepository {
    
    private TaskDao taskDao;
    private LiveData<List<TaskEntity>> allTasks;
    private LiveData<List<TaskEntity>> activeTasks;
    private LiveData<List<TaskEntity>> completedTasks;
    
    public TaskRepository(Application application) {
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
                allTasks = taskDao.getAllTasks();
                activeTasks = taskDao.getActiveTasks();
                completedTasks = taskDao.getCompletedTasks();
            } else {
                android.util.Log.e("TaskRepository", "Database is null, TaskRepository will have limited functionality");
                // 初始化为null，调用者需要检查
                taskDao = null;
                allTasks = null;
                activeTasks = null;
                completedTasks = null;
            }
        } catch (Exception e) {
            android.util.Log.e("TaskRepository", "Error initializing TaskRepository", e);
            taskDao = null;
            allTasks = null;
            activeTasks = null;
            completedTasks = null;
        }
    }
    
    // 获取所有任务
    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }
    
    // 获取活跃任务
    public LiveData<List<TaskEntity>> getActiveTasks() {
        return activeTasks;
    }
    
    // 获取已完成任务
    public LiveData<List<TaskEntity>> getCompletedTasks() {
        return completedTasks;
    }
    
    // 根据ID获取任务
    public LiveData<TaskEntity> getTaskById(String taskId) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTaskById");
            return null;
        }
        return taskDao.getTaskById(taskId);
    }
    
    // 根据象限获取任务
    public LiveData<List<TaskEntity>> getTasksByQuadrant(int quadrant) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTasksByQuadrant");
            return null;
        }
        return taskDao.getTasksByQuadrant(quadrant);
    }
    
    // 获取高优先级任务
    public LiveData<List<TaskEntity>> getHighPriorityTasks(int limit) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getHighPriorityTasks");
            return null;
        }
        return taskDao.getHighPriorityTasks(limit);
    }
    
    // 按优先级排序获取任务
    public LiveData<List<TaskEntity>> getTasksByPriority() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTasksByPriority");
            return null;
        }
        return taskDao.getTasksByPriority();
    }
    
    // 搜索任务
    public LiveData<List<TaskEntity>> searchTasks(String searchQuery) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for searchTasks");
            return null;
        }
        return taskDao.searchTasks(searchQuery);
    }
    
    // 获取象限分布
    public LiveData<List<TaskDao.QuadrantCount>> getQuadrantDistribution() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getQuadrantDistribution");
            return null;
        }
        return taskDao.getQuadrantDistribution();
    }
    
    // 获取任务完成统计
    public LiveData<TaskDao.TaskCompletionStats> getTaskCompletionStats(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTaskCompletionStats");
            return null;
        }
        return taskDao.getTaskCompletionStats(startTime, endTime);
    }
    
    // 按时间范围获取任务
    public LiveData<List<TaskEntity>> getTasksByTimeRange(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTasksByTimeRange");
            return null;
        }
        return taskDao.getTasksByTimeRange(startTime, endTime);
    }
    
    // 按时间范围获取已完成任务
    public LiveData<List<TaskEntity>> getCompletedTasksByTimeRange(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getCompletedTasksByTimeRange");
            return null;
        }
        return taskDao.getCompletedTasksByTimeRange(startTime, endTime);
    }
    
    // 获取统计数据
    public LiveData<Integer> getTaskCount() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTaskCount");
            return null;
        }
        return taskDao.getTaskCount();
    }
    
    public LiveData<Integer> getCompletedTaskCount() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getCompletedTaskCount");
            return null;
        }
        return taskDao.getCompletedTaskCount();
    }
    
    public LiveData<Integer> getActiveTaskCount() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getActiveTaskCount");
            return null;
        }
        return taskDao.getActiveTaskCount();
    }
    
    public LiveData<Float> getAverageImportance() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getAverageImportance");
            return null;
        }
        return taskDao.getAverageImportance();
    }
    
    // 插入任务（同步）
    public void insertTask(TaskEntity task) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot insert task");
            return;
        }
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(generateTaskId());
        }
        taskDao.insertTask(task);
    }
    
    // 便捷方法：创建新任务
    public void createTask(String name, int importance, int urgency) {
        TaskEntity task = new TaskEntity(generateTaskId(), name, importance, urgency);
        insertTask(task);
    }
    
    // 更新任务（同步）
    public void updateTask(TaskEntity task) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot update task");
            return;
        }
        task.setUpdatedAt(System.currentTimeMillis());
        taskDao.updateTask(task);
    }
    
    // 完成任务
    public void completeTask(TaskEntity task) {
        task.setCompleted(true);
        updateTask(task);
    }
    
    // 取消完成任务
    public void uncompleteTask(TaskEntity task) {
        task.setCompleted(false);
        updateTask(task);
    }
    
    // 删除任务（同步）
    public void deleteTask(TaskEntity task) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot delete task");
            return;
        }
        taskDao.deleteTask(task);
    }
    
    // 根据ID删除任务（同步）
    public void deleteTaskById(String taskId) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot delete task by id");
            return;
        }
        taskDao.deleteTaskById(taskId);
    }
    
    // 删除所有任务（同步）
    public void deleteAllTasks() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot delete all tasks");
            return;
        }
        taskDao.deleteAllTasks();
    }
    
    // 软删除所有活跃任务（同步）
    public void softDeleteActiveTasks() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot soft delete active tasks");
            return;
        }
        taskDao.softDeleteActiveTasks(System.currentTimeMillis());
    }
    
    // 批量插入任务（同步）
    public void insertTasks(List<TaskEntity> tasks) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, cannot insert tasks");
            return;
        }
        taskDao.insertTasks(tasks);
    }
    
    // 生成任务ID
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString();
    }
    
    // 时间范围常量
    public static class TimeRange {
        public static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
        public static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;
        public static final long MONTH_IN_MILLIS = 30 * DAY_IN_MILLIS;
        public static final long YEAR_IN_MILLIS = 365 * DAY_IN_MILLIS;
        
        public static long[] getToday() {
            long now = System.currentTimeMillis();
            long startOfDay = now - (now % DAY_IN_MILLIS);
            return new long[]{startOfDay, startOfDay + DAY_IN_MILLIS};
        }
        
        public static long[] getThisWeek() {
            long now = System.currentTimeMillis();
            long startOfWeek = now - (6 * DAY_IN_MILLIS);
            return new long[]{startOfWeek, now};
        }
        
        public static long[] getThisMonth() {
            long now = System.currentTimeMillis();
            long startOfMonth = now - (29 * DAY_IN_MILLIS);
            return new long[]{startOfMonth, now};
        }
        
        public static long[] getThisYear() {
            long now = System.currentTimeMillis();
            long startOfYear = now - (364 * DAY_IN_MILLIS);
            return new long[]{startOfYear, now};
        }
    }
    
    // 添加缺失的统计方法
    public LiveData<Integer> getTotalTaskCount() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTotalTaskCount");
            return null;
        }
        return taskDao.getTaskCount(); // 使用现有方法
    }
    
    // ==================== 同步查询方法 ====================
    // 这些方法提供准确的即时数据查询，不使用LiveData
    
    // 同步获取所有任务
    public List<TaskEntity> getAllTasksSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getAllTasksSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getAllTasksSync();
    }
    
    // 同步获取活跃任务
    public List<TaskEntity> getActiveTasksSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getActiveTasksSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getActiveTasksSync();
    }
    
    // 同步获取已完成任务
    public List<TaskEntity> getCompletedTasksSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getCompletedTasksSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getCompletedTasksSync();
    }
    
    // 同步根据ID获取任务
    public TaskEntity getTaskByIdSync(String taskId) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getTaskByIdSync");
            return null;
        }
        return taskDao.getTaskByIdSync(taskId);
    }
    
    // 同步根据象限获取任务
    public List<TaskEntity> getTasksByQuadrantSync(int quadrant) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getTasksByQuadrantSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getTasksByQuadrantSync(quadrant);
    }
    
    // 同步获取统计数据
    public int getTaskCountSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning 0 for getTaskCountSync");
            return 0;
        }
        return taskDao.getTaskCountSync();
    }
    
    public int getCompletedTaskCountSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning 0 for getCompletedTaskCountSync");
            return 0;
        }
        return taskDao.getCompletedTaskCountSync();
    }
    
    public int getActiveTaskCountSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning 0 for getActiveTaskCountSync");
            return 0;
        }
        return taskDao.getActiveTaskCountSync();
    }
    
    // 同步按时间范围获取统计数据
    public int getCompletedTaskCountByTimeRangeSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning 0 for getCompletedTaskCountByTimeRangeSync");
            return 0;
        }
        return taskDao.getCompletedTaskCountByTimeRangeSync(startTime, endTime);
    }
    
    public Float getAverageImportanceByTimeRangeSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getAverageImportanceByTimeRangeSync");
            return null;
        }
        return taskDao.getAverageImportanceByTimeRangeSync(startTime, endTime);
    }
    
    public Float getCompletionRateByTimeRangeSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning null for getCompletionRateByTimeRangeSync");
            return null;
        }
        return taskDao.getCompletionRateByTimeRangeSync(startTime, endTime);
    }
    
    // 同步获取象限分布
    public List<TaskDao.QuadrantCount> getActiveTaskQuadrantDistributionSync() {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getActiveTaskQuadrantDistributionSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getActiveTaskQuadrantDistributionSync();
    }
    
    // 同步按时间范围获取象限分布
    public List<TaskDao.QuadrantCount> getCompletedTaskQuadrantDistributionSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getCompletedTaskQuadrantDistributionSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getCompletedTaskQuadrantDistributionSync(startTime, endTime);
    }
    
    // 同步按时间范围获取任务
    public List<TaskEntity> getTasksByTimeRangeSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getTasksByTimeRangeSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getTasksByTimeRangeSync(startTime, endTime);
    }
    
    public List<TaskEntity> getCompletedTasksByTimeRangeSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getCompletedTasksByTimeRangeSync");
            return new java.util.ArrayList<>();
        }
        return taskDao.getCompletedTasksByTimeRangeSync(startTime, endTime);
    }
    
    // 同步按小时获取任务完成统计
    public List<TaskDao.HourlyCompletionStats> getHourlyCompletionStatsSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getHourlyCompletionStatsSync");
            return new java.util.ArrayList<>();
        }
        android.util.Log.d("TaskRepository", "开始查询小时统计数据，时间范围: " + startTime + " 到 " + endTime);
        List<TaskDao.HourlyCompletionStats> result = taskDao.getHourlyCompletionStatsSync(startTime, endTime);
        android.util.Log.d("TaskRepository", "小时统计查询完成，返回 " + result.size() + " 条记录");
        return result;
    }
    
    // 同步按天获取任务完成统计
    public List<TaskDao.DailyCompletionStats> getDailyCompletionStatsSync(long startTime, long endTime) {
        if (taskDao == null) {
            android.util.Log.w("TaskRepository", "TaskDao is null, returning empty list for getDailyCompletionStatsSync");
            return new java.util.ArrayList<>();
        }
        android.util.Log.d("TaskRepository", "开始查询每日统计数据，时间范围: " + startTime + " 到 " + endTime);
        List<TaskDao.DailyCompletionStats> result = taskDao.getDailyCompletionStatsSync(startTime, endTime);
        android.util.Log.d("TaskRepository", "每日统计查询完成，返回 " + result.size() + " 条记录");
        return result;
    }
}
