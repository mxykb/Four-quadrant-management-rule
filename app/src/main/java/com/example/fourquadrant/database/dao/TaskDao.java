package com.example.fourquadrant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.entity.TaskEntity;

import java.util.List;

/**
 * 任务数据访问对象
 */
@Dao
public interface TaskDao {
    
    // 插入任务
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(TaskEntity task);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskEntity> tasks);
    
    // 更新任务
    @Update
    void updateTask(TaskEntity task);
    
    // 删除任务
    @Delete
    void deleteTask(TaskEntity task);
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTaskById(String taskId);
    
    @Query("DELETE FROM tasks")
    void deleteAllTasks();
    
    // 软删除正在进行中的任务
    @Query("UPDATE tasks SET is_deleted = 1, updated_at = :currentTime WHERE is_completed = 0 AND is_deleted = 0")
    void softDeleteActiveTasks(long currentTime);
    
    // 查询所有任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getAllTasks();
    
    // 查询活跃任务（未删除且未完成）
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_deleted = 0 ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getActiveTasks();
    
    // 查询已完成任务（未删除且已完成）
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND is_deleted = 0 ORDER BY completed_at DESC")
    LiveData<List<TaskEntity>> getCompletedTasks();
    
    // 根据ID查询任务（未删除）
    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_deleted = 0")
    LiveData<TaskEntity> getTaskById(String taskId);
    
    // 根据象限查询任务（未删除且未完成）
    @Query("SELECT * FROM tasks WHERE quadrant = :quadrant AND is_completed = 0 AND is_deleted = 0")
    LiveData<List<TaskEntity>> getTasksByQuadrant(int quadrant);
    
    // 统计方法（未删除任务）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_deleted = 0")
    LiveData<Integer> getTaskCount();
    
    // 统计已完成任务数量（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND is_deleted = 0")
    LiveData<Integer> getCompletedTaskCount();
    
    // 统计活跃任务数量（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND is_deleted = 0")
    LiveData<Integer> getActiveTaskCount();
    
    // 象限分布统计（未删除且未完成）
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 0 AND is_deleted = 0 GROUP BY quadrant")
    LiveData<List<QuadrantCount>> getQuadrantDistribution();
    
    // 按时间范围查询任务（未删除）
    @Query("SELECT * FROM tasks WHERE created_at BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getTasksByTimeRange(long startTime, long endTime);
    
    // 按时间范围查询已完成任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY completed_at DESC")
    LiveData<List<TaskEntity>> getCompletedTasksByTimeRange(long startTime, long endTime);
    
    // 获取平均重要性评分（未删除且已完成）
    @Query("SELECT AVG(importance) FROM tasks WHERE is_completed = 1 AND is_deleted = 0")
    LiveData<Float> getAverageImportance();
    
    // 获取高优先级任务（第一、二象限，未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_deleted = 0 AND quadrant IN (1, 2) ORDER BY importance DESC, urgency DESC LIMIT :limit")
    LiveData<List<TaskEntity>> getHighPriorityTasks(int limit);
    
    // 按优先级排序任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_deleted = 0 ORDER BY quadrant ASC, importance DESC, urgency DESC")
    LiveData<List<TaskEntity>> getTasksByPriority();
    
    // 搜索任务（未删除）
    @Query("SELECT * FROM tasks WHERE name LIKE '%' || :searchQuery || '%' AND is_deleted = 0 ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> searchTasks(String searchQuery);
    
    // 任务完成率统计（按时间范围，未删除）
    @Query("SELECT " +
            "COUNT(*) as total_tasks, " +
            "SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as completed_tasks, " +
            "ROUND(SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks WHERE created_at BETWEEN :startTime AND :endTime AND is_deleted = 0")
    LiveData<TaskCompletionStats> getTaskCompletionStats(long startTime, long endTime);
    
    // 按时间范围获取已完成任务数（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0")
    LiveData<Integer> getCompletedTaskCountByTimeRange(long startTime, long endTime);
    
    // 按时间范围获取平均重要性（未删除）
    @Query("SELECT AVG(importance) FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0")
    LiveData<Float> getAverageImportanceByTimeRange(long startTime, long endTime);
    
    // 按时间范围获取任务完成率（未删除）
    @Query("SELECT " +
            "CASE WHEN COUNT(*) = 0 THEN 0.0 " +
            "ELSE ROUND(SUM(CASE WHEN is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime THEN 1 ELSE 0 END) * 100.0 / " +
            "COUNT(*), 2) END as completion_rate " +
            "FROM tasks WHERE created_at <= :endTime AND is_deleted = 0")
    LiveData<Float> getCompletionRateByTimeRange(long startTime, long endTime);
    
    // 每日任务完成趋势（未删除）
    @Query("SELECT " +
            "DATE(completed_at/1000, 'unixepoch', 'localtime') as date, " +
            "COUNT(*) as completed_count " +
            "FROM tasks " +
            "WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 " +
            "GROUP BY DATE(completed_at/1000, 'unixepoch', 'localtime') " +
            "ORDER BY date ASC")
    LiveData<List<DailyCompletionStats>> getDailyCompletionStats(long startTime, long endTime);
    
    // 按小时统计任务完成趋势（未删除）
    @Query("SELECT " +
            "strftime('%H', datetime(completed_at/1000, 'unixepoch', 'localtime')) as hour, " +
            "COUNT(*) as completed_count " +
            "FROM tasks " +
            "WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 " +
            "GROUP BY strftime('%H', datetime(completed_at/1000, 'unixepoch', 'localtime')) " +
            "ORDER BY hour ASC")
    List<HourlyCompletionStats> getHourlyCompletionStatsSync(long startTime, long endTime);
    
    // 同步按天统计任务完成趋势（未删除）
    @Query("SELECT " +
            "DATE(completed_at/1000, 'unixepoch', 'localtime') as date, " +
            "COUNT(*) as completed_count " +
            "FROM tasks " +
            "WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 " +
            "GROUP BY DATE(completed_at/1000, 'unixepoch', 'localtime') " +
            "ORDER BY date ASC")
    List<DailyCompletionStats> getDailyCompletionStatsSync(long startTime, long endTime);
    
    // 象限分布统计（只统计活跃任务，未删除）
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 0 AND is_deleted = 0 GROUP BY quadrant ORDER BY quadrant")
    LiveData<List<QuadrantCount>> getActiveTaskQuadrantDistribution();
    
    // 按时间范围获取象限分布统计（已完成任务，未删除）
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 GROUP BY quadrant ORDER BY quadrant")
    LiveData<List<QuadrantCount>> getCompletedTaskQuadrantDistribution(long startTime, long endTime);
    
    // ==================== 同步查询方法 ====================
    // 这些方法返回实际数据而不是LiveData，确保数据查询的准确性
    
    // 同步查询方法（用于后台任务，未删除）
    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY created_at DESC")
    List<TaskEntity> getAllTasksSync();
    
    // 同步查询活跃任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_deleted = 0 ORDER BY created_at DESC")
    List<TaskEntity> getActiveTasksSync();
    
    // 同步查询已完成任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND is_deleted = 0 ORDER BY completed_at DESC")
    List<TaskEntity> getCompletedTasksSync();
    
    // 同步根据ID查询任务（未删除）
    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_deleted = 0")
    TaskEntity getTaskByIdSync(String taskId);
    
    // 同步根据象限查询任务（未删除）
    @Query("SELECT * FROM tasks WHERE quadrant = :quadrant AND is_completed = 0 AND is_deleted = 0")
    List<TaskEntity> getTasksByQuadrantSync(int quadrant);
    
    // 同步查询任务总数（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_deleted = 0")
    int getTaskCountSync();
    
    // 同步查询已完成任务数（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND is_deleted = 0")
    int getCompletedTaskCountSync();
    
    // 同步查询活跃任务数（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND is_deleted = 0")
    int getActiveTaskCountSync();
    
    // 同步按时间范围查询已完成任务数（未删除）
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0")
    int getCompletedTaskCountByTimeRangeSync(long startTime, long endTime);
    
    // 同步按时间范围获取平均重要性（未删除）
    @Query("SELECT AVG(importance) FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0")
    Float getAverageImportanceByTimeRangeSync(long startTime, long endTime);
    
    // 同步按时间范围获取任务完成率（未删除）
    @Query("SELECT " +
            "CASE WHEN COUNT(*) = 0 THEN 0.0 " +
            "ELSE ROUND(SUM(CASE WHEN is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime THEN 1 ELSE 0 END) * 100.0 / " +
            "COUNT(*), 2) END as completion_rate " +
            "FROM tasks WHERE created_at <= :endTime AND is_deleted = 0")
    Float getCompletionRateByTimeRangeSync(long startTime, long endTime);
    
    // 同步象限分布统计（未删除）
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 0 AND is_deleted = 0 GROUP BY quadrant ORDER BY quadrant")
    List<QuadrantCount> getActiveTaskQuadrantDistributionSync();
    
    // 同步按时间范围获取象限分布统计（已完成任务，未删除）
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 GROUP BY quadrant ORDER BY quadrant")
    List<QuadrantCount> getCompletedTaskQuadrantDistributionSync(long startTime, long endTime);
    
    // 同步按时间范围查询任务（未删除）
    @Query("SELECT * FROM tasks WHERE created_at BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY created_at DESC")
    List<TaskEntity> getTasksByTimeRangeSync(long startTime, long endTime);
    
    // 同步按时间范围查询已完成任务（未删除）
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY completed_at DESC")
    List<TaskEntity> getCompletedTasksByTimeRangeSync(long startTime, long endTime);
    
    // 内部类定义统计结果
    class QuadrantCount {
        public int quadrant;
        public int count;
    }
    
    class TaskCompletionStats {
        public int total_tasks;
        public int completed_tasks;
        public float completion_rate;
    }
    
    class DailyCompletionStats {
        public String date;
        public int completed_count;
    }
    
    class HourlyCompletionStats {
        public String hour;
        public int completed_count;
    }
}
