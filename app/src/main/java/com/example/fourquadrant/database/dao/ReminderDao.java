package com.example.fourquadrant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.entity.ReminderEntity;

import java.util.List;

/**
 * 提醒数据访问对象
 */
@Dao
public interface ReminderDao {
    
    // 插入提醒
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminder(ReminderEntity reminder);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminders(List<ReminderEntity> reminders);
    
    // 更新提醒
    @Update
    void updateReminder(ReminderEntity reminder);
    
    // 删除提醒
    @Delete
    void deleteReminder(ReminderEntity reminder);
    
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    void deleteReminderById(String reminderId);
    
    @Query("DELETE FROM reminders")
    void deleteAllReminders();
    
    // 查询所有提醒
    @Query("SELECT * FROM reminders ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getAllReminders();
    
    // 查询活跃提醒
    @Query("SELECT * FROM reminders WHERE is_active = 1 ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getActiveReminders();
    
    // 查询已完成提醒
    @Query("SELECT * FROM reminders WHERE status = 'COMPLETED' ORDER BY reminder_time DESC")
    LiveData<List<ReminderEntity>> getCompletedReminders();
    
    // 根据ID查询提醒
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    LiveData<ReminderEntity> getReminderById(String reminderId);
    
    // 根据任务ID查询提醒
    @Query("SELECT * FROM reminders WHERE task_id = :taskId ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getRemindersByTaskId(String taskId);
    
    // 查询指定时间范围的提醒
    @Query("SELECT * FROM reminders WHERE reminder_time BETWEEN :startTime AND :endTime ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getRemindersByTimeRange(long startTime, long endTime);
    
    // 查询某一天的提醒
    @Query("SELECT * FROM reminders WHERE DATE(reminder_time/1000, 'unixepoch', 'localtime') = :date ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getRemindersByDate(String date);
    
    // 查询即将到期的提醒（未来1小时内）
    @Query("SELECT * FROM reminders WHERE is_active = 1 AND reminder_time BETWEEN :currentTime AND :futureTime ORDER BY reminder_time ASC")
    LiveData<List<ReminderEntity>> getUpcomingReminders(long currentTime, long futureTime);
    
    // 查询过期的提醒
    @Query("SELECT * FROM reminders WHERE is_active = 1 AND reminder_time < :currentTime ORDER BY reminder_time DESC")
    LiveData<List<ReminderEntity>> getOverdueReminders(long currentTime);
    
    // 根据状态查询提醒
    @Query("SELECT * FROM reminders WHERE status = :status ORDER BY reminder_time DESC")
    LiveData<List<ReminderEntity>> getRemindersByStatus(String status);
    
    // 查询提醒总数
    @Query("SELECT COUNT(*) FROM reminders")
    LiveData<Integer> getReminderCount();
    
    // 查询活跃提醒数
    @Query("SELECT COUNT(*) FROM reminders WHERE is_active = 1")
    LiveData<Integer> getActiveReminderCount();
    
    // 查询已完成提醒数
    @Query("SELECT COUNT(*) FROM reminders WHERE status = 'COMPLETED'")
    LiveData<Integer> getCompletedReminderCount();
    
    // 按时间范围查询提醒统计
    @Query("SELECT " +
            "COUNT(*) as total_reminders, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_reminders, " +
            "SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_reminders " +
            "FROM reminders WHERE created_at BETWEEN :startTime AND :endTime")
    LiveData<ReminderStats> getReminderStatsByTimeRange(long startTime, long endTime);
    
    // 每日提醒统计
    @Query("SELECT " +
            "DATE(reminder_time/1000, 'unixepoch', 'localtime') as date, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_count " +
            "FROM reminders " +
            "WHERE reminder_time BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(reminder_time/1000, 'unixepoch', 'localtime') " +
            "ORDER BY date DESC")
    LiveData<List<DailyReminderStats>> getDailyReminderStats(long startTime, long endTime);
    
    // 搜索提醒
    @Query("SELECT * FROM reminders WHERE content LIKE '%' || :searchQuery || '%' ORDER BY reminder_time DESC")
    LiveData<List<ReminderEntity>> searchReminders(String searchQuery);
    
    // ==================== 同步查询方法 ====================
    // 这些方法返回实际数据而不是LiveData，用于数据导出等场景
    
    // 同步查询所有提醒
    @Query("SELECT * FROM reminders ORDER BY reminder_time ASC")
    List<ReminderEntity> getAllRemindersSync();
    
    // 同步查询活跃提醒
    @Query("SELECT * FROM reminders WHERE is_active = 1 ORDER BY reminder_time ASC")
    List<ReminderEntity> getActiveRemindersSync();
    
    // 同步查询已完成提醒
    @Query("SELECT * FROM reminders WHERE status = 'COMPLETED' ORDER BY reminder_time DESC")
    List<ReminderEntity> getCompletedRemindersSync();
    
    // 更新提醒状态
    @Query("UPDATE reminders SET status = :status, updated_at = :updateTime WHERE id = :reminderId")
    void updateReminderStatus(String reminderId, String status, long updateTime);
    
    // 批量更新过期提醒状态
    @Query("UPDATE reminders SET is_active = 0, status = 'EXPIRED', updated_at = :updateTime WHERE is_active = 1 AND reminder_time < :currentTime")
    void markOverdueRemindersAsExpired(long currentTime, long updateTime);
    
    // 内部类定义统计结果
    class ReminderStats {
        public int total_reminders;
        public int completed_reminders;
        public int active_reminders;
    }
    
    class DailyReminderStats {
        public String date;
        public int total_count;
        public int completed_count;
    }
}
