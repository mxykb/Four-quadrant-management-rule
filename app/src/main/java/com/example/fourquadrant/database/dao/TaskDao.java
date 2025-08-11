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
    
    // 查询所有任务
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getAllTasks();
    
    // 查询活跃任务
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getActiveTasks();
    
    // 查询已完成任务
    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    LiveData<List<TaskEntity>> getCompletedTasks();
    
    // 根据ID查询任务
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<TaskEntity> getTaskById(String taskId);
    
    // 根据象限查询任务
    @Query("SELECT * FROM tasks WHERE quadrant = :quadrant AND is_completed = 0")
    LiveData<List<TaskEntity>> getTasksByQuadrant(int quadrant);
    
    // 查询任务总数
    @Query("SELECT COUNT(*) FROM tasks")
    LiveData<Integer> getTaskCount();
    
    // 查询已完成任务数
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    LiveData<Integer> getCompletedTaskCount();
    
    // 查询活跃任务数
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    LiveData<Integer> getActiveTaskCount();
    
    // 象限分布统计
    @Query("SELECT quadrant, COUNT(*) as count FROM tasks WHERE is_completed = 0 GROUP BY quadrant")
    LiveData<List<QuadrantCount>> getQuadrantDistribution();
    
    // 按时间范围查询任务
    @Query("SELECT * FROM tasks WHERE created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> getTasksByTimeRange(long startTime, long endTime);
    
    // 按时间范围查询已完成任务
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND completed_at BETWEEN :startTime AND :endTime ORDER BY completed_at DESC")
    LiveData<List<TaskEntity>> getCompletedTasksByTimeRange(long startTime, long endTime);
    
    // 获取平均重要性评分
    @Query("SELECT AVG(importance) FROM tasks WHERE is_completed = 1")
    LiveData<Float> getAverageImportance();
    
    // 获取高优先级任务（第一、二象限）
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND quadrant IN (1, 2) ORDER BY importance DESC, urgency DESC LIMIT :limit")
    LiveData<List<TaskEntity>> getHighPriorityTasks(int limit);
    
    // 按优先级排序任务
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY quadrant ASC, importance DESC, urgency DESC")
    LiveData<List<TaskEntity>> getTasksByPriority();
    
    // 搜索任务
    @Query("SELECT * FROM tasks WHERE name LIKE '%' || :searchQuery || '%' ORDER BY created_at DESC")
    LiveData<List<TaskEntity>> searchTasks(String searchQuery);
    
    // 任务完成率统计（按时间范围）
    @Query("SELECT " +
            "COUNT(*) as total_tasks, " +
            "SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as completed_tasks, " +
            "ROUND(SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM tasks WHERE created_at BETWEEN :startTime AND :endTime")
    LiveData<TaskCompletionStats> getTaskCompletionStats(long startTime, long endTime);
    
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
}
