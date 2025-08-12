package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.ReminderDao;
import com.example.fourquadrant.database.entity.ReminderEntity;

import java.util.List;
import java.util.UUID;

/**
 * 提醒数据仓库
 */
public class ReminderRepository {
    
    private ReminderDao reminderDao;
    private LiveData<List<ReminderEntity>> allReminders;
    private LiveData<List<ReminderEntity>> activeReminders;
    private LiveData<List<ReminderEntity>> completedReminders;
    
    public ReminderRepository(Application application) {
        AppDatabase database;
        if (application instanceof com.example.fourquadrant.FourQuadrantApplication) {
            // 使用Application中的单例数据库实例
            database = ((com.example.fourquadrant.FourQuadrantApplication) application).getDatabase();
        } else {
            // 备用方案：直接获取数据库实例
            database = AppDatabase.getDatabase(application);
        }
        reminderDao = database.reminderDao();
        allReminders = reminderDao.getAllReminders();
        activeReminders = reminderDao.getActiveReminders();
        completedReminders = reminderDao.getCompletedReminders();
    }
    
    // 获取所有提醒
    public LiveData<List<ReminderEntity>> getAllReminders() {
        return allReminders;
    }
    
    // 获取活跃提醒
    public LiveData<List<ReminderEntity>> getActiveReminders() {
        return activeReminders;
    }
    
    // 获取已完成提醒
    public LiveData<List<ReminderEntity>> getCompletedReminders() {
        return completedReminders;
    }
    
    // 根据ID获取提醒
    public LiveData<ReminderEntity> getReminderById(String reminderId) {
        return reminderDao.getReminderById(reminderId);
    }
    
    // 根据任务ID获取提醒
    public LiveData<List<ReminderEntity>> getRemindersByTaskId(String taskId) {
        return reminderDao.getRemindersByTaskId(taskId);
    }
    
    // 按时间范围获取提醒
    public LiveData<List<ReminderEntity>> getRemindersByTimeRange(long startTime, long endTime) {
        return reminderDao.getRemindersByTimeRange(startTime, endTime);
    }
    
    // 按日期获取提醒
    public LiveData<List<ReminderEntity>> getRemindersByDate(String date) {
        return reminderDao.getRemindersByDate(date);
    }
    
    // 获取即将到期的提醒
    public LiveData<List<ReminderEntity>> getUpcomingReminders(long currentTime, long futureTime) {
        return reminderDao.getUpcomingReminders(currentTime, futureTime);
    }
    
    // 获取过期的提醒
    public LiveData<List<ReminderEntity>> getOverdueReminders(long currentTime) {
        return reminderDao.getOverdueReminders(currentTime);
    }
    
    // 根据状态获取提醒
    public LiveData<List<ReminderEntity>> getRemindersByStatus(String status) {
        return reminderDao.getRemindersByStatus(status);
    }
    
    // 搜索提醒
    public LiveData<List<ReminderEntity>> searchReminders(String searchQuery) {
        return reminderDao.searchReminders(searchQuery);
    }
    
    // 获取统计数据
    public LiveData<Integer> getReminderCount() {
        return reminderDao.getReminderCount();
    }
    
    public LiveData<Integer> getActiveReminderCount() {
        return reminderDao.getActiveReminderCount();
    }
    
    public LiveData<Integer> getCompletedReminderCount() {
        return reminderDao.getCompletedReminderCount();
    }
    
    public LiveData<ReminderDao.ReminderStats> getReminderStatsByTimeRange(long startTime, long endTime) {
        return reminderDao.getReminderStatsByTimeRange(startTime, endTime);
    }
    
    public LiveData<List<ReminderDao.DailyReminderStats>> getDailyReminderStats(long startTime, long endTime) {
        return reminderDao.getDailyReminderStats(startTime, endTime);
    }
    
    // 创建新提醒
    public ReminderEntity createReminder(String content, long reminderTime) {
        String reminderId = generateReminderId();
        ReminderEntity reminder = new ReminderEntity(reminderId, content, reminderTime);
        insertReminder(reminder);
        return reminder;
    }
    
    // 创建关联任务的提醒
    public ReminderEntity createReminderWithTask(String content, long reminderTime, 
                                                 String taskId, String taskName) {
        String reminderId = generateReminderId();
        ReminderEntity reminder = new ReminderEntity(reminderId, content, reminderTime);
        reminder.setTaskId(taskId);
        reminder.setTaskName(taskName);
        insertReminder(reminder);
        return reminder;
    }
    
    // 插入提醒（同步）
    public void insertReminder(ReminderEntity reminder) {
        if (reminder.getId() == null || reminder.getId().isEmpty()) {
            reminder.setId(generateReminderId());
        }
        reminderDao.insertReminder(reminder);
    }
    
    // 更新提醒（同步）
    public void updateReminder(ReminderEntity reminder) {
        reminder.setUpdatedAt(System.currentTimeMillis());
        reminderDao.updateReminder(reminder);
    }
    
    // 完成提醒
    public void completeReminder(ReminderEntity reminder) {
        reminder.setStatus("COMPLETED");
        reminder.setActive(false);
        updateReminder(reminder);
    }
    
    // 延迟提醒
    public void snoozeReminder(ReminderEntity reminder, long snoozeMinutes) {
        long newReminderTime = reminder.getReminderTime() + (snoozeMinutes * 60 * 1000);
        reminder.setReminderTime(newReminderTime);
        reminder.setStatus("SNOOZED");
        reminder.setRepeatCount(reminder.getRepeatCount() + 1);
        updateReminder(reminder);
    }
    
    // 取消提醒
    public void cancelReminder(ReminderEntity reminder) {
        reminder.setStatus("CANCELLED");
        reminder.setActive(false);
        updateReminder(reminder);
    }
    
    // 激活提醒
    public void activateReminder(ReminderEntity reminder) {
        reminder.setStatus("ACTIVE");
        reminder.setActive(true);
        updateReminder(reminder);
    }
    
    // 删除提醒（同步）
    public void deleteReminder(ReminderEntity reminder) {
        reminderDao.deleteReminder(reminder);
    }
    
    // 根据ID删除提醒（同步）
    public void deleteReminderById(String reminderId) {
        reminderDao.deleteReminderById(reminderId);
    }
    
    // 删除所有提醒（同步）
    public void deleteAllReminders() {
        reminderDao.deleteAllReminders();
    }
    
    // 批量插入提醒（同步）
    public void insertReminders(List<ReminderEntity> reminders) {
        reminderDao.insertReminders(reminders);
    }
    
    // 更新提醒状态（同步）
    public void updateReminderStatus(String reminderId, String status) {
        reminderDao.updateReminderStatus(reminderId, status, System.currentTimeMillis());
    }
    
    // 标记过期提醒（同步）
    public void markOverdueRemindersAsExpired() {
        long currentTime = System.currentTimeMillis();
        reminderDao.markOverdueRemindersAsExpired(currentTime, currentTime);
    }
    
    // 获取即将到期的提醒（下一小时）
    public LiveData<List<ReminderEntity>> getUpcomingRemindersNextHour() {
        long currentTime = System.currentTimeMillis();
        long oneHourLater = currentTime + (60 * 60 * 1000); // 1小时后
        return getUpcomingReminders(currentTime, oneHourLater);
    }
    
    // 生成提醒ID
    private String generateReminderId() {
        return "reminder_" + UUID.randomUUID().toString();
    }
    
    // 提醒状态常量
    public static class ReminderStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String COMPLETED = "COMPLETED";
        public static final String SNOOZED = "SNOOZED";
        public static final String CANCELLED = "CANCELLED";
        public static final String EXPIRED = "EXPIRED";
    }
}
