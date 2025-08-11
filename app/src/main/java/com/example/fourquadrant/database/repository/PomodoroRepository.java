package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.PomodoroDao;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;

import java.util.List;
import java.util.UUID;

/**
 * 番茄钟数据仓库
 */
public class PomodoroRepository {
    
    private PomodoroDao pomodoroDao;
    private LiveData<List<PomodoroSessionEntity>> allSessions;
    private LiveData<List<PomodoroSessionEntity>> completedSessions;
    
    public PomodoroRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        pomodoroDao = database.pomodoroDao();
        allSessions = pomodoroDao.getAllSessions();
        completedSessions = pomodoroDao.getCompletedSessions();
    }
    
    // 获取所有会话
    public LiveData<List<PomodoroSessionEntity>> getAllSessions() {
        return allSessions;
    }
    
    // 获取已完成会话
    public LiveData<List<PomodoroSessionEntity>> getCompletedSessions() {
        return completedSessions;
    }
    
    // 根据任务ID获取会话
    public LiveData<List<PomodoroSessionEntity>> getSessionsByTaskId(String taskId) {
        return pomodoroDao.getSessionsByTaskId(taskId);
    }
    
    // 按时间范围获取会话
    public LiveData<List<PomodoroSessionEntity>> getSessionsByTimeRange(long startTime, long endTime) {
        return pomodoroDao.getSessionsByTimeRange(startTime, endTime);
    }
    
    // 获取统计数据
    public LiveData<Integer> getCompletedPomodoroCount() {
        return pomodoroDao.getCompletedPomodoroCount();
    }
    
    public LiveData<Integer> getCompletedPomodoroCountByTimeRange(long startTime, long endTime) {
        return pomodoroDao.getCompletedPomodoroCountByTimeRange(startTime, endTime);
    }
    
    public LiveData<Integer> getTotalFocusTime() {
        return pomodoroDao.getTotalFocusTime();
    }
    
    public LiveData<Integer> getTotalFocusTimeByTimeRange(long startTime, long endTime) {
        return pomodoroDao.getTotalFocusTimeByTimeRange(startTime, endTime);
    }
    
    // 获取每日统计
    public LiveData<List<PomodoroDao.DailyPomodoroStats>> getDailyPomodoroStats(long startTime, long endTime) {
        return pomodoroDao.getDailyPomodoroStats(startTime, endTime);
    }
    
    // 获取任务番茄钟统计
    public LiveData<List<PomodoroDao.TaskPomodoroStats>> getTaskPomodoroStats() {
        return pomodoroDao.getTaskPomodoroStats();
    }
    
    public LiveData<List<PomodoroDao.TaskPomodoroStats>> getTaskPomodoroStatsByTimeRange(long startTime, long endTime) {
        return pomodoroDao.getTaskPomodoroStatsByTimeRange(startTime, endTime);
    }
    
    // 获取最长专注时间的任务
    public LiveData<List<PomodoroDao.TaskPomodoroStats>> getLongestFocusTaskStats(int limit) {
        return pomodoroDao.getLongestFocusTaskStats(limit);
    }
    
    // 开始番茄钟会话
    public PomodoroSessionEntity startPomodoroSession(String taskId, String taskName, int durationMinutes) {
        String sessionId = generateSessionId();
        PomodoroSessionEntity session = new PomodoroSessionEntity(
            sessionId, taskId, taskName, durationMinutes, false);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.insertSession(session);
        });
        
        return session;
    }
    
    // 开始休息会话
    public PomodoroSessionEntity startBreakSession(int durationMinutes) {
        String sessionId = generateSessionId();
        PomodoroSessionEntity session = new PomodoroSessionEntity(
            sessionId, null, "休息", durationMinutes, true);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.insertSession(session);
        });
        
        return session;
    }
    
    // 完成会话
    public void completeSession(PomodoroSessionEntity session) {
        session.setCompleted(true);
        session.setEndTime(System.currentTimeMillis());
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.updateSession(session);
        });
    }
    
    // 取消会话
    public void cancelSession(PomodoroSessionEntity session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.deleteSession(session);
        });
    }
    
    // 插入会话
    public void insertSession(PomodoroSessionEntity session) {
        if (session.getId() == null || session.getId().isEmpty()) {
            session.setId(generateSessionId());
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.insertSession(session);
        });
    }
    
    // 更新会话
    public void updateSession(PomodoroSessionEntity session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.updateSession(session);
        });
    }
    
    // 删除会话
    public void deleteSession(PomodoroSessionEntity session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.deleteSession(session);
        });
    }
    
    // 根据ID删除会话
    public void deleteSessionById(String sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.deleteSessionById(sessionId);
        });
    }
    
    // 删除所有会话
    public void deleteAllSessions() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.deleteAllSessions();
        });
    }
    
    // 批量插入会话
    public void insertSessions(List<PomodoroSessionEntity> sessions) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pomodoroDao.insertSessions(sessions);
        });
    }
    
    // 记录番茄钟完成（便捷方法）
    public void recordPomodoroCompletion(String taskId, String taskName, int durationMinutes) {
        String sessionId = generateSessionId();
        PomodoroSessionEntity session = new PomodoroSessionEntity(
            sessionId, taskId, taskName, durationMinutes, false);
        session.setCompleted(true);
        session.setEndTime(System.currentTimeMillis());
        
        insertSession(session);
    }
    
    // 记录休息完成（便捷方法）
    public void recordBreakCompletion(int durationMinutes) {
        String sessionId = generateSessionId();
        PomodoroSessionEntity session = new PomodoroSessionEntity(
            sessionId, null, "休息", durationMinutes, true);
        session.setCompleted(true);
        session.setEndTime(System.currentTimeMillis());
        
        insertSession(session);
    }
    
    // 生成会话ID
    private String generateSessionId() {
        return "pomodoro_" + UUID.randomUUID().toString();
    }
    
    // 添加缺失的统计方法
    public LiveData<Integer> getTotalPomodoroCount() {
        return pomodoroDao.getTotalPomodoroCount();
    }
    
    // ==================== 同步查询方法 ====================
    // 这些方法提供准确的即时数据查询，不使用LiveData
    
    // 同步获取已完成的番茄钟总数
    public int getCompletedPomodoroCountSync() {
        return pomodoroDao.getCompletedPomodoroCountSync();
    }
    
    // 同步按时间范围获取已完成的番茄钟数
    public int getCompletedPomodoroCountByTimeRangeSync(long startTime, long endTime) {
        return pomodoroDao.getCompletedPomodoroCountByTimeRangeSync(startTime, endTime);
    }
    
    // 同步获取总专注时间
    public Integer getTotalFocusTimeSync() {
        return pomodoroDao.getTotalFocusTimeSync();
    }
    
    // 同步按时间范围获取总专注时间
    public Integer getTotalFocusTimeByTimeRangeSync(long startTime, long endTime) {
        return pomodoroDao.getTotalFocusTimeByTimeRangeSync(startTime, endTime);
    }
    
    // 同步获取所有会话
    public List<PomodoroSessionEntity> getAllSessionsSync() {
        return pomodoroDao.getAllSessionsSync();
    }
    
    // 同步获取已完成会话
    public List<PomodoroSessionEntity> getCompletedSessionsSync() {
        return pomodoroDao.getCompletedSessionsSync();
    }
    
    // 同步按时间范围获取会话
    public List<PomodoroSessionEntity> getSessionsByTimeRangeSync(long startTime, long endTime) {
        return pomodoroDao.getSessionsByTimeRangeSync(startTime, endTime);
    }
}
