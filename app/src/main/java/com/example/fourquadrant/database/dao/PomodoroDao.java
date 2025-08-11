package com.example.fourquadrant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.entity.PomodoroSessionEntity;

import java.util.List;

/**
 * 番茄钟会话数据访问对象
 */
@Dao
public interface PomodoroDao {
    
    // 插入番茄钟会话
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(PomodoroSessionEntity session);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSessions(List<PomodoroSessionEntity> sessions);
    
    // 更新番茄钟会话
    @Update
    void updateSession(PomodoroSessionEntity session);
    
    // 删除番茄钟会话
    @Delete
    void deleteSession(PomodoroSessionEntity session);
    
    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    void deleteSessionById(String sessionId);
    
    @Query("DELETE FROM pomodoro_sessions")
    void deleteAllSessions();
    
    // 查询所有会话
    @Query("SELECT * FROM pomodoro_sessions ORDER BY start_time DESC")
    LiveData<List<PomodoroSessionEntity>> getAllSessions();
    
    // 查询已完成的会话
    @Query("SELECT * FROM pomodoro_sessions WHERE is_completed = 1 ORDER BY start_time DESC")
    LiveData<List<PomodoroSessionEntity>> getCompletedSessions();
    
    // 根据任务ID查询会话
    @Query("SELECT * FROM pomodoro_sessions WHERE task_id = :taskId ORDER BY start_time DESC")
    LiveData<List<PomodoroSessionEntity>> getSessionsByTaskId(String taskId);
    
    // 按时间范围查询会话
    @Query("SELECT * FROM pomodoro_sessions WHERE start_time BETWEEN :startTime AND :endTime ORDER BY start_time DESC")
    LiveData<List<PomodoroSessionEntity>> getSessionsByTimeRange(long startTime, long endTime);
    
    // 查询已完成的番茄钟总数
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0")
    LiveData<Integer> getCompletedPomodoroCount();
    
    // 按时间范围查询已完成的番茄钟数
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime")
    LiveData<Integer> getCompletedPomodoroCountByTimeRange(long startTime, long endTime);
    
    // 查询总专注时间（分钟）
    @Query("SELECT SUM(duration_minutes) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0")
    LiveData<Integer> getTotalFocusTime();
    
    // 按时间范围查询总专注时间
    @Query("SELECT SUM(duration_minutes) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime")
    LiveData<Integer> getTotalFocusTimeByTimeRange(long startTime, long endTime);
    
    // 每日番茄钟完成统计
    @Query("SELECT " +
            "DATE(start_time/1000, 'unixepoch', 'localtime') as date, " +
            "COUNT(*) as count, " +
            "SUM(duration_minutes) as total_minutes " +
            "FROM pomodoro_sessions " +
            "WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(start_time/1000, 'unixepoch', 'localtime') " +
            "ORDER BY date DESC")
    LiveData<List<DailyPomodoroStats>> getDailyPomodoroStats(long startTime, long endTime);
    
    // 任务番茄钟使用统计
    @Query("SELECT " +
            "task_name, " +
            "COUNT(*) as session_count, " +
            "SUM(duration_minutes) as total_minutes " +
            "FROM pomodoro_sessions " +
            "WHERE is_completed = 1 AND is_break_session = 0 AND task_name IS NOT NULL " +
            "GROUP BY task_name " +
            "ORDER BY session_count DESC")
    LiveData<List<TaskPomodoroStats>> getTaskPomodoroStats();
    
    // 按时间范围查询任务番茄钟统计
    @Query("SELECT " +
            "task_name, " +
            "COUNT(*) as session_count, " +
            "SUM(duration_minutes) as total_minutes " +
            "FROM pomodoro_sessions " +
            "WHERE is_completed = 1 AND is_break_session = 0 AND task_name IS NOT NULL " +
            "AND start_time BETWEEN :startTime AND :endTime " +
            "GROUP BY task_name " +
            "ORDER BY session_count DESC")
    LiveData<List<TaskPomodoroStats>> getTaskPomodoroStatsByTimeRange(long startTime, long endTime);
    
    // 获取最长专注时间的任务
    @Query("SELECT " +
            "task_name, " +
            "SUM(duration_minutes) as total_minutes, " +
            "COUNT(*) as session_count " +
            "FROM pomodoro_sessions " +
            "WHERE is_completed = 1 AND is_break_session = 0 AND task_name IS NOT NULL " +
            "GROUP BY task_name " +
            "ORDER BY total_minutes DESC " +
            "LIMIT :limit")
    LiveData<List<TaskPomodoroStats>> getLongestFocusTaskStats(int limit);
    
    // 按时间段分布统计番茄钟（上午、下午、晚上）
    @Query("SELECT " +
            "CASE " +
            "  WHEN CAST(strftime('%H', datetime(start_time/1000, 'unixepoch', 'localtime')) AS INTEGER) BETWEEN 6 AND 11 THEN '上午' " +
            "  WHEN CAST(strftime('%H', datetime(start_time/1000, 'unixepoch', 'localtime')) AS INTEGER) BETWEEN 12 AND 17 THEN '下午' " +
            "  ELSE '晚上' " +
            "END as time_period, " +
            "COUNT(*) as count " +
            "FROM pomodoro_sessions " +
            "WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime " +
            "GROUP BY time_period " +
            "ORDER BY " +
            "  CASE time_period " +
            "    WHEN '上午' THEN 1 " +
            "    WHEN '下午' THEN 2 " +
            "    WHEN '晚上' THEN 3 " +
            "  END")
    LiveData<List<TimePeriodStats>> getTimePeriodStats(long startTime, long endTime);
    
    // 内部类定义统计结果
    class DailyPomodoroStats {
        public String date;
        public int count;
        public int total_minutes;
    }
    
    class TaskPomodoroStats {
        public String task_name;
        public int session_count;
        public int total_minutes;
    }
    
    class TimePeriodStats {
        public String time_period;
        public int count;
    }
    
    // 添加总番茄钟数统计
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0")
    LiveData<Integer> getTotalPomodoroCount();
    
    // ==================== 同步查询方法 ====================
    // 这些方法返回实际数据而不是LiveData，确保数据查询的准确性
    
    // 同步查询已完成的番茄钟总数
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0")
    int getCompletedPomodoroCountSync();
    
    // 同步按时间范围查询已完成的番茄钟数
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime")
    int getCompletedPomodoroCountByTimeRangeSync(long startTime, long endTime);
    
    // 同步查询总专注时间（分钟）
    @Query("SELECT SUM(duration_minutes) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0")
    Integer getTotalFocusTimeSync();
    
    // 同步按时间范围查询总专注时间
    @Query("SELECT SUM(duration_minutes) FROM pomodoro_sessions WHERE is_completed = 1 AND is_break_session = 0 AND start_time BETWEEN :startTime AND :endTime")
    Integer getTotalFocusTimeByTimeRangeSync(long startTime, long endTime);
    
    // 同步查询所有会话
    @Query("SELECT * FROM pomodoro_sessions ORDER BY start_time DESC")
    List<PomodoroSessionEntity> getAllSessionsSync();
    
    // 同步查询已完成会话
    @Query("SELECT * FROM pomodoro_sessions WHERE is_completed = 1 ORDER BY start_time DESC")
    List<PomodoroSessionEntity> getCompletedSessionsSync();
    
    // 同步按时间范围查询会话
    @Query("SELECT * FROM pomodoro_sessions WHERE start_time BETWEEN :startTime AND :endTime ORDER BY start_time DESC")
    List<PomodoroSessionEntity> getSessionsByTimeRangeSync(long startTime, long endTime);
}
