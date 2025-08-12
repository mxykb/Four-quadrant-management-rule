package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.PomodoroDao;
import com.example.fourquadrant.database.dao.TimerStateDao;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.database.entity.TimerStateEntity;

import java.util.List;
import java.util.UUID;

/**
 * 番茄钟数据仓库
 */
public class PomodoroRepository {
    
    private PomodoroDao pomodoroDao;
    private TimerStateDao timerStateDao;
    private LiveData<List<PomodoroSessionEntity>> allSessions;
    private LiveData<List<PomodoroSessionEntity>> completedSessions;
    
    public PomodoroRepository(Application application) {
        AppDatabase database;
        if (application instanceof com.example.fourquadrant.FourQuadrantApplication) {
            // 使用Application中的单例数据库实例
            database = ((com.example.fourquadrant.FourQuadrantApplication) application).getDatabase();
        } else {
            // 备用方案：直接获取数据库实例
            database = AppDatabase.getDatabase(application);
        }
        pomodoroDao = database.pomodoroDao();
        timerStateDao = database.timerStateDao();
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
        
        pomodoroDao.insertSession(session);
        
        return session;
    }
    
    // 开始休息会话
    public PomodoroSessionEntity startBreakSession(int durationMinutes) {
        String sessionId = generateSessionId();
        PomodoroSessionEntity session = new PomodoroSessionEntity(
            sessionId, null, "休息", durationMinutes, true);
        
        pomodoroDao.insertSession(session);
        
        return session;
    }
    
    // 完成会话
    public void completeSession(PomodoroSessionEntity session) {
        session.setCompleted(true);
        session.setEndTime(System.currentTimeMillis());
        
        pomodoroDao.updateSession(session);
    }
    
    // 取消会话
    public void cancelSession(PomodoroSessionEntity session) {
        pomodoroDao.deleteSession(session);
    }
    
    // 插入会话
    public void insertSession(PomodoroSessionEntity session) {
        if (session.getId() == null || session.getId().isEmpty()) {
            session.setId(generateSessionId());
        }
        pomodoroDao.insertSession(session);
    }
    
    // 更新会话
    public void updateSession(PomodoroSessionEntity session) {
        pomodoroDao.updateSession(session);
    }
    
    // 删除会话
    public void deleteSession(PomodoroSessionEntity session) {
        pomodoroDao.deleteSession(session);
    }
    
    // 根据ID删除会话
    public void deleteSessionById(String sessionId) {
        pomodoroDao.deleteSessionById(sessionId);
    }
    
    // 删除所有会话
    public void deleteAllSessions() {
        pomodoroDao.deleteAllSessions();
    }
    
    // 批量插入会话
    public void insertSessions(List<PomodoroSessionEntity> sessions) {
        pomodoroDao.insertSessions(sessions);
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
    
    // ==================== 计时器状态相关方法 ====================
    
    // 获取计时器状态
    public LiveData<TimerStateEntity> getTimerState() {
        return timerStateDao.getTimerState();
    }
    
    // 同步获取计时器状态
    public TimerStateEntity getTimerStateSync() {
        return timerStateDao.getTimerStateSync();
    }
    
    // 保存计时器状态
    public void saveTimerState(long startTime, boolean isRunning, boolean isPaused, 
                              long remainingTime, boolean isBreak, int currentCount) {
        new Thread(() -> {
            TimerStateEntity timerState = new TimerStateEntity(startTime, isRunning, isPaused, 
                                                              remainingTime, isBreak, currentCount);
            timerStateDao.insertOrUpdateTimerState(timerState);
        }).start();
    }
    
    // 同步保存计时器状态
    public void saveTimerStateSync(long startTime, boolean isRunning, boolean isPaused, 
                                  long remainingTime, boolean isBreak, int currentCount) {
        TimerStateEntity timerState = new TimerStateEntity(startTime, isRunning, isPaused, 
                                                          remainingTime, isBreak, currentCount);
        timerStateDao.insertOrUpdateTimerState(timerState);
    }
    
    // 保存番茄钟完成待确认状态
    public void savePomodoroCompletionPending(String taskName, int currentCount, int totalCount) {
        new Thread(() -> {
            TimerStateEntity timerState = getTimerStateSync();
            if (timerState == null) {
                timerState = new TimerStateEntity();
            }
            timerState.setCompletedPending(true);
            timerState.setCompletedTaskName(taskName);
            timerState.setCurrentCount(currentCount);
            timerState.setTotalCount(totalCount);
            timerState.setRunning(false);
            timerState.setPaused(false);
            timerState.setRemainingTime(0);
            timerStateDao.insertOrUpdateTimerState(timerState);
        }).start();
    }
    
    // 清除番茄钟完成待确认状态
    public void clearPomodoroCompletionPending() {
        new Thread(() -> {
            TimerStateEntity timerState = getTimerStateSync();
            if (timerState != null) {
                timerState.setCompletedPending(false);
                timerState.setCompletedTaskName(null);
                timerStateDao.insertOrUpdateTimerState(timerState);
            }
        }).start();
    }
    
    // 清除计时器状态
    public void clearTimerState() {
        new Thread(() -> {
            timerStateDao.clearTimerState();
        }).start();
    }
    
    // 同步清除计时器状态
    public void clearTimerStateSync() {
        timerStateDao.clearTimerState();
    }
}
