package com.fourquadrant.ai.commands;

import android.content.Context;
import android.util.Log;

import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.fourquadrant.ai.AiExecutable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 番茄钟历史记录查询功能实现
 * 支持查询会话记录、统计数据和完成情况
 */
public class PomodoroHistoryControl implements AiExecutable {
    private static final String TAG = "PomodoroHistoryControl";
    private Context context;
    private PomodoroRepository pomodoroRepository;
    private ExecutorService executor;
    private SimpleDateFormat dateFormat;
    
    public PomodoroHistoryControl(Context context) {
        this.context = context;
        // 使用ApplicationContext来创建Repository
        if (context.getApplicationContext() instanceof android.app.Application) {
            this.pomodoroRepository = new PomodoroRepository((android.app.Application) context.getApplicationContext());
        } else {
            // 如果无法获取Application，则使用null，后续操作会跳过
            this.pomodoroRepository = null;
            Log.w(TAG, "无法获取Application实例，某些功能可能不可用");
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 检查Repository是否可用
            if (pomodoroRepository == null) {
                Log.e(TAG, "PomodoroRepository不可用，无法执行操作");
                return;
            }
            
            String action = (String) args.get("action");
            if (action == null || action.trim().isEmpty()) {
                Log.w(TAG, "历史记录操作类型不能为空");
                return;
            }
            
            switch (action.toLowerCase()) {
                case "recent":
                    getRecentSessions(args);
                    break;
                case "today":
                    getTodaySessions(args);
                    break;
                case "week":
                    getWeekSessions(args);
                    break;
                case "month":
                    getMonthSessions(args);
                    break;
                case "stats":
                    getStatistics(args);
                    break;
                case "task":
                    getTaskSessions(args);
                    break;
                default:
                    Log.w(TAG, "不支持的历史记录操作: " + action);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "番茄钟历史记录操作失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取最近的会话记录
     */
    private void getRecentSessions(Map<String, Object> args) {
        try {
            int limit = getIntegerValue(args, "limit", 10);
            
            executor.execute(() -> {
                try {
                    // 使用实际存在的方法
                    List<PomodoroSessionEntity> allSessions = pomodoroRepository.getAllSessionsSync();
                    if (allSessions != null && !allSessions.isEmpty()) {
                        // 限制数量
                        int actualLimit = Math.min(limit, allSessions.size());
                        List<PomodoroSessionEntity> recentSessions = allSessions.subList(0, actualLimit);
                        
                        StringBuilder info = new StringBuilder();
                        info.append("最近").append(recentSessions.size()).append("个番茄钟会话:\n");
                        
                        for (PomodoroSessionEntity session : recentSessions) {
                            info.append("• ").append(session.getTaskName())
                                .append(" - ").append(session.getDurationMinutes()).append("分钟")
                                .append(" - ").append(dateFormat.format(new Date(session.getStartTime())))
                                .append(" - ").append(session.isCompleted() ? "已完成" : "未完成")
                                .append("\n");
                        }
                        
                        Log.i(TAG, info.toString());
                    } else {
                        Log.i(TAG, "暂无番茄钟会话记录");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取最近会话记录失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取最近会话记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取今天的会话记录
     */
    private void getTodaySessions(Map<String, Object> args) {
        try {
            executor.execute(() -> {
                try {
                    long todayStart = getTodayStartTime();
                    long todayEnd = getTodayEndTime();
                    
                    List<PomodoroSessionEntity> sessions = pomodoroRepository.getSessionsByTimeRangeSync(todayStart, todayEnd);
                    if (sessions != null && !sessions.isEmpty()) {
                        StringBuilder info = new StringBuilder();
                        info.append("今天的番茄钟会话(").append(sessions.size()).append("个):\n");
                        
                        int totalWorkTime = 0;
                        int completedCount = 0;
                        
                        for (PomodoroSessionEntity session : sessions) {
                            if (!session.isBreakSession()) {
                                totalWorkTime += session.getDurationMinutes();
                                if (session.isCompleted()) {
                                    completedCount++;
                                }
                            }
                        }
                        
                        info.append("总工作时长: ").append(totalWorkTime).append("分钟\n");
                        info.append("完成数量: ").append(completedCount).append("个\n");
                        info.append("完成率: ").append(String.format("%.1f%%", (double) completedCount / sessions.size() * 100));
                        
                        Log.i(TAG, info.toString());
                    } else {
                        Log.i(TAG, "今天暂无番茄钟会话记录");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取今天会话记录失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取今天会话记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取本周的会话记录
     */
    private void getWeekSessions(Map<String, Object> args) {
        try {
            executor.execute(() -> {
                try {
                    long weekStart = getWeekStartTime();
                    long weekEnd = getWeekEndTime();
                    
                    List<PomodoroSessionEntity> sessions = pomodoroRepository.getSessionsByTimeRangeSync(weekStart, weekEnd);
                    if (sessions != null && !sessions.isEmpty()) {
                        StringBuilder info = new StringBuilder();
                        info.append("本周的番茄钟会话(").append(sessions.size()).append("个):\n");
                        
                        int totalWorkTime = 0;
                        int completedCount = 0;
                        
                        for (PomodoroSessionEntity session : sessions) {
                            if (!session.isBreakSession()) {
                                totalWorkTime += session.getDurationMinutes();
                                if (session.isCompleted()) {
                                    completedCount++;
                                }
                            }
                        }
                        
                        info.append("总工作时长: ").append(totalWorkTime).append("分钟\n");
                        info.append("完成数量: ").append(completedCount).append("个\n");
                        info.append("平均每天: ").append(String.format("%.1f", (double) totalWorkTime / 7)).append("分钟");
                        
                        Log.i(TAG, info.toString());
                    } else {
                        Log.i(TAG, "本周暂无番茄钟会话记录");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取本周会话记录失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取本周会话记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取本月的会话记录
     */
    private void getMonthSessions(Map<String, Object> args) {
        try {
            executor.execute(() -> {
                try {
                    long monthStart = getMonthStartTime();
                    long monthEnd = getMonthEndTime();
                    
                    List<PomodoroSessionEntity> sessions = pomodoroRepository.getSessionsByTimeRangeSync(monthStart, monthEnd);
                    if (sessions != null && !sessions.isEmpty()) {
                        StringBuilder info = new StringBuilder();
                        info.append("本月的番茄钟会话(").append(sessions.size()).append("个):\n");
                        
                        int totalWorkTime = 0;
                        int completedCount = 0;
                        
                        for (PomodoroSessionEntity session : sessions) {
                            if (!session.isBreakSession()) {
                                totalWorkTime += session.getDurationMinutes();
                                if (session.isCompleted()) {
                                    completedCount++;
                                }
                            }
                        }
                        
                        info.append("总工作时长: ").append(totalWorkTime).append("分钟\n");
                        info.append("完成数量: ").append(completedCount).append("个\n");
                        info.append("平均每天: ").append(String.format("%.1f", (double) totalWorkTime / 30)).append("分钟");
                        
                        Log.i(TAG, info.toString());
                    } else {
                        Log.i(TAG, "本月暂无番茄钟会话记录");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取本月会话记录失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取本月会话记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取统计数据
     */
    private void getStatistics(Map<String, Object> args) {
        try {
            executor.execute(() -> {
                try {
                    // 使用实际存在的方法
                    List<PomodoroSessionEntity> allSessions = pomodoroRepository.getAllSessionsSync();
                    List<PomodoroSessionEntity> completedSessions = pomodoroRepository.getCompletedSessionsSync();
                    Integer totalWorkTime = pomodoroRepository.getTotalFocusTimeSync();
                    
                    StringBuilder info = new StringBuilder();
                    info.append("番茄钟总体统计:\n");
                    info.append("总会话数: ").append(allSessions != null ? allSessions.size() : 0).append("个\n");
                    info.append("完成会话数: ").append(completedSessions != null ? completedSessions.size() : 0).append("个\n");
                    info.append("总工作时长: ").append(totalWorkTime != null ? totalWorkTime : 0).append("分钟\n");
                    
                    if (allSessions != null && !allSessions.isEmpty()) {
                        double completionRate = (double) (completedSessions != null ? completedSessions.size() : 0) / allSessions.size() * 100;
                        info.append("总体完成率: ").append(String.format("%.1f%%", completionRate));
                    }
                    
                    Log.i(TAG, info.toString());
                } catch (Exception e) {
                    Log.e(TAG, "获取统计数据失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取统计数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取特定任务的会话记录
     */
    private void getTaskSessions(Map<String, Object> args) {
        try {
            String taskName = (String) args.get("task_name");
            if (taskName == null || taskName.trim().isEmpty()) {
                Log.w(TAG, "任务名称不能为空");
                return;
            }
            
            executor.execute(() -> {
                try {
                    // 获取所有会话，然后按任务名称过滤
                    List<PomodoroSessionEntity> allSessions = pomodoroRepository.getAllSessionsSync();
                    if (allSessions != null && !allSessions.isEmpty()) {
                        List<PomodoroSessionEntity> taskSessions = new java.util.ArrayList<>();
                        for (PomodoroSessionEntity session : allSessions) {
                            if (taskName.equals(session.getTaskName())) {
                                taskSessions.add(session);
                            }
                        }
                        
                        if (!taskSessions.isEmpty()) {
                            StringBuilder info = new StringBuilder();
                            info.append("任务「").append(taskName).append("」的番茄钟会话(").append(taskSessions.size()).append("个):\n");
                            
                            int totalWorkTime = 0;
                            int completedCount = 0;
                            
                            for (PomodoroSessionEntity session : taskSessions) {
                                if (!session.isBreakSession()) {
                                    totalWorkTime += session.getDurationMinutes();
                                    if (session.isCompleted()) {
                                        completedCount++;
                                    }
                                }
                            }
                            
                            info.append("总工作时长: ").append(totalWorkTime).append("分钟\n");
                            info.append("完成数量: ").append(completedCount).append("个\n");
                            info.append("完成率: ").append(String.format("%.1f%%", (double) completedCount / taskSessions.size() * 100));
                            
                            Log.i(TAG, info.toString());
                        } else {
                            Log.i(TAG, "任务「" + taskName + "」暂无番茄钟会话记录");
                        }
                    } else {
                        Log.i(TAG, "暂无番茄钟会话记录");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取任务会话记录失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取任务会话记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取整数值
     */
    private int getIntegerValue(Map<String, Object> args, String key, int defaultValue) {
        if (!args.containsKey(key)) {
            return defaultValue;
        }
        
        Object value = args.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "无法解析整数值: " + value + "，使用默认值: " + defaultValue);
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取今天开始时间
     */
    private long getTodayStartTime() {
        long currentTime = System.currentTimeMillis();
        return currentTime - (currentTime % (24 * 60 * 60 * 1000));
    }
    
    /**
     * 获取今天结束时间
     */
    private long getTodayEndTime() {
        return getTodayStartTime() + 24 * 60 * 60 * 1000 - 1;
    }
    
    /**
     * 获取本周开始时间
     */
    private long getWeekStartTime() {
        long currentTime = System.currentTimeMillis();
        long todayStart = getTodayStartTime();
        // 简单计算，实际应该考虑周几
        return todayStart - 7 * 24 * 60 * 60 * 1000;
    }
    
    /**
     * 获取本周结束时间
     */
    private long getWeekEndTime() {
        return getTodayEndTime();
    }
    
    /**
     * 获取本月开始时间
     */
    private long getMonthStartTime() {
        long currentTime = System.currentTimeMillis();
        long todayStart = getTodayStartTime();
        // 简单计算，实际应该考虑月份天数
        return todayStart - 30 * 24 * 60 * 60 * 1000L;
    }
    
    /**
     * 获取本月结束时间
     */
    private long getMonthEndTime() {
        return getTodayEndTime();
    }
    
    @Override
    public String getDescription() {
        return "番茄钟历史记录查询，支持查询最近(recent)、今天(today)、本周(week)、本月(month)、统计(stats)和任务(task)记录";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        if (args == null) {
            return false;
        }
        
        String action = (String) args.get("action");
        if (action == null || action.isEmpty()) {
            return false;
        }
        
        // 验证操作类型
        if (!action.equalsIgnoreCase("recent") && 
            !action.equalsIgnoreCase("today") && 
            !action.equalsIgnoreCase("week") && 
            !action.equalsIgnoreCase("month") && 
            !action.equalsIgnoreCase("stats") && 
            !action.equalsIgnoreCase("task")) {
            return false;
        }
        
        // 如果是查询特定任务，需要任务名称
        if (action.equalsIgnoreCase("task")) {
            return args.containsKey("task_name");
        }
        
        return true;
    }
    
    /**
     * 获取支持的参数
     */
    public static Map<String, String> getSupportedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "操作类型：recent(最近)、today(今天)、week(本周)、month(本月)、stats(统计)、task(任务)");
        params.put("limit", "限制数量(仅用于recent操作，默认10)");
        params.put("task_name", "任务名称(仅用于task操作)");
        return params;
    }
}
