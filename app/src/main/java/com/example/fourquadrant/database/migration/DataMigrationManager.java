package com.example.fourquadrant.database.migration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.database.entity.ReminderEntity;
import com.example.fourquadrant.database.entity.UserEntity;
import com.example.fourquadrant.database.entity.SettingsEntity;
import com.example.fourquadrant.database.repository.TaskRepository;
import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.repository.ReminderRepository;
import com.example.fourquadrant.database.repository.UserRepository;
import com.example.fourquadrant.database.repository.SettingsRepository;
import com.example.fourquadrant.PomodoroRecord;
import com.example.fourquadrant.ReminderItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 数据迁移管理器
 * 负责将SharedPreferences中的数据迁移到Room数据库
 */
public class DataMigrationManager {
    
    private static final String TAG = "DataMigrationManager";
    private static final String MIGRATION_PREF = "DataMigration";
    private static final String KEY_MIGRATION_COMPLETED = "migration_completed";
    private static final String KEY_MIGRATION_VERSION = "migration_version";
    private static final int CURRENT_MIGRATION_VERSION = 1;
    
    private Context context;
    private AppDatabase database;
    private Gson gson;
    
    public DataMigrationManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.gson = new Gson();
    }
    
    /**
     * 检查是否需要迁移数据
     */
    public boolean needsMigration() {
        SharedPreferences migrationPrefs = context.getSharedPreferences(MIGRATION_PREF, Context.MODE_PRIVATE);
        boolean migrationCompleted = migrationPrefs.getBoolean(KEY_MIGRATION_COMPLETED, false);
        int migrationVersion = migrationPrefs.getInt(KEY_MIGRATION_VERSION, 0);
        
        return !migrationCompleted || migrationVersion < CURRENT_MIGRATION_VERSION;
    }
    
    /**
     * 执行数据迁移
     */
    public void performMigration() {
        if (!needsMigration()) {
            Log.i(TAG, "数据迁移已完成，跳过迁移");
            return;
        }
        
        Log.i(TAG, "开始数据迁移...");
        
        try {
            // 迁移任务数据
            migrateTaskData();
            
            // 迁移番茄钟数据
            migratePomodoroData();
            
            // 迁移提醒数据
            migrateReminderData();
            
            // 迁移用户数据
            migrateUserData();
            
            // 迁移设置数据
            migrateSettingsData();
            
            // 标记迁移完成
            markMigrationCompleted();
            
            Log.i(TAG, "数据迁移完成");
            
        } catch (Exception e) {
            Log.e(TAG, "数据迁移失败", e);
        }
    }
    
    /**
     * 迁移任务数据
     */
    private void migrateTaskData() {
        Log.i(TAG, "迁移任务数据...");
        
        SharedPreferences taskPrefs = context.getSharedPreferences("TaskListPrefs", Context.MODE_PRIVATE);
        String savedTasksJson = taskPrefs.getString("saved_tasks", "[]");
        
        try {
            Type taskListType = new TypeToken<List<TaskItem>>(){}.getType();
            List<TaskItem> oldTasks = gson.fromJson(savedTasksJson, taskListType);
            
            if (oldTasks != null && !oldTasks.isEmpty()) {
                List<TaskEntity> newTasks = new ArrayList<>();
                
                for (TaskItem oldTask : oldTasks) {
                    TaskEntity newTask = new TaskEntity();
                    newTask.setId(oldTask.getId() != null ? oldTask.getId() : generateTaskId());
                    newTask.setName(oldTask.getName());
                    newTask.setImportance(oldTask.getImportance());
                    newTask.setUrgency(oldTask.getUrgency());
                    newTask.setCompleted(oldTask.isCompleted());
                    newTask.setCreatedAt(System.currentTimeMillis());
                    newTask.setUpdatedAt(System.currentTimeMillis());
                    
                    if (oldTask.isCompleted() && oldTask.getCompletedTime() != null) {
                        newTask.setCompletedAt(oldTask.getCompletedTime());
                    }
                    
                    newTasks.add(newTask);
                }
                
                // 批量插入任务
                database.taskDao().insertTasks(newTasks);
                Log.i(TAG, "成功迁移 " + newTasks.size() + " 个任务");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "迁移任务数据失败", e);
        }
    }
    
    /**
     * 迁移番茄钟数据
     */
    private void migratePomodoroData() {
        Log.i(TAG, "迁移番茄钟数据...");
        
        SharedPreferences pomodoroPrefs = context.getSharedPreferences("PomodoroRecords", Context.MODE_PRIVATE);
        String recordsJson = pomodoroPrefs.getString("pomodoro_records", "[]");
        
        try {
            Type recordListType = new TypeToken<List<PomodoroRecord>>(){}.getType();
            List<PomodoroRecord> oldRecords = gson.fromJson(recordsJson, recordListType);
            
            if (oldRecords != null && !oldRecords.isEmpty()) {
                List<PomodoroSessionEntity> newSessions = new ArrayList<>();
                
                for (PomodoroRecord oldRecord : oldRecords) {
                    PomodoroSessionEntity newSession = new PomodoroSessionEntity();
                    newSession.setId(generateSessionId());
                    newSession.setTaskId(oldRecord.getTaskId());
                    newSession.setTaskName(oldRecord.getTaskName());
                    newSession.setDurationMinutes(oldRecord.getDurationMinutes());
                    newSession.setCompleted(oldRecord.isCompleted());
                    newSession.setBreakSession(false); // 旧数据默认不是休息会话
                    newSession.setStartTime(oldRecord.getStartTime());
                    newSession.setCreatedAt(oldRecord.getStartTime());
                    
                    if (oldRecord.isCompleted()) {
                        newSession.setEndTime(oldRecord.getEndTime());
                    }
                    
                    newSessions.add(newSession);
                }
                
                // 批量插入番茄钟会话
                database.pomodoroDao().insertSessions(newSessions);
                Log.i(TAG, "成功迁移 " + newSessions.size() + " 个番茄钟记录");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "迁移番茄钟数据失败", e);
        }
    }
    
    /**
     * 迁移提醒数据
     */
    private void migrateReminderData() {
        Log.i(TAG, "迁移提醒数据...");
        
        SharedPreferences reminderPrefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);
        String remindersJson = reminderPrefs.getString("reminders", "[]");
        
        try {
            Type reminderListType = new TypeToken<List<ReminderItem>>(){}.getType();
            List<ReminderItem> oldReminders = gson.fromJson(remindersJson, reminderListType);
            
            if (oldReminders != null && !oldReminders.isEmpty()) {
                List<ReminderEntity> newReminders = new ArrayList<>();
                
                for (ReminderItem oldReminder : oldReminders) {
                    ReminderEntity newReminder = new ReminderEntity();
                    newReminder.setId(oldReminder.getId() != null ? oldReminder.getId() : generateReminderId());
                    newReminder.setContent(oldReminder.getContent());
                    newReminder.setReminderTime(oldReminder.getReminderTime());
                    newReminder.setActive(oldReminder.isActive());
                    newReminder.setVibrate(oldReminder.isVibrate());
                    newReminder.setSound(oldReminder.isSound());
                    newReminder.setRepeat(oldReminder.isRepeat());
                    newReminder.setRepeatCount(0); // 旧数据默认重复次数为0
                    
                    // 设置状态
                    if (oldReminder.isActive()) {
                        newReminder.setStatus("ACTIVE");
                    } else {
                        newReminder.setStatus("COMPLETED");
                    }
                    
                    // 关联任务（旧数据中没有taskId，使用taskName匹配）
                    if (oldReminder.getTaskName() != null && !oldReminder.getTaskName().trim().isEmpty()) {
                        newReminder.setTaskName(oldReminder.getTaskName());
                        // taskId在旧数据中不存在，设置为null
                        newReminder.setTaskId(null);
                    }
                    
                    newReminder.setCreatedAt(System.currentTimeMillis());
                    newReminder.setUpdatedAt(System.currentTimeMillis());
                    
                    newReminders.add(newReminder);
                }
                
                // 批量插入提醒
                database.reminderDao().insertReminders(newReminders);
                Log.i(TAG, "成功迁移 " + newReminders.size() + " 个提醒");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "迁移提醒数据失败", e);
        }
    }
    
    /**
     * 迁移用户数据
     */
    private void migrateUserData() {
        Log.i(TAG, "迁移用户数据...");
        
        SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = userPrefs.getString("username", "");
        String email = userPrefs.getString("email", "");
        String bio = userPrefs.getString("bio", "");
        
        if (!username.isEmpty() || !email.isEmpty() || !bio.isEmpty()) {
            UserEntity user = new UserEntity();
            user.setUsername(username.isEmpty() ? "用户" : username);
            user.setEmail(email);
            user.setBio(bio.isEmpty() ? "欢迎使用四象限任务管理工具" : bio);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            database.userDao().insertUser(user);
            Log.i(TAG, "成功迁移用户数据");
        }
    }
    
    /**
     * 迁移设置数据
     */
    private void migrateSettingsData() {
        Log.i(TAG, "迁移设置数据...");
        
        List<SettingsEntity> settings = new ArrayList<>();
        
        // 迁移番茄钟设置
        migratePomodoroSettings(settings);
        
        // 迁移提醒设置
        migrateReminderSettings(settings);
        
        // 迁移应用设置
        migrateAppSettings(settings);
        
        if (!settings.isEmpty()) {
            database.settingsDao().insertSettings(settings);
            Log.i(TAG, "成功迁移 " + settings.size() + " 个设置项");
        }
    }
    
    private void migratePomodoroSettings(List<SettingsEntity> settings) {
        SharedPreferences tomatoPrefs = context.getSharedPreferences("TomatoSettings", Context.MODE_PRIVATE);
        
        settings.add(SettingsEntity.createIntSetting(
            "pomodoro_count", 
            tomatoPrefs.getInt("tomato_count", 4), 
            "POMODORO"));
        
        settings.add(SettingsEntity.createIntSetting(
            "pomodoro_duration", 
            tomatoPrefs.getInt("tomato_duration", 25), 
            "POMODORO"));
        
        settings.add(SettingsEntity.createIntSetting(
            "break_duration", 
            tomatoPrefs.getInt("break_duration", 5), 
            "POMODORO"));
        
        settings.add(SettingsEntity.createBooleanSetting(
            "auto_next", 
            tomatoPrefs.getBoolean("auto_next", false), 
            "POMODORO"));
        
        SharedPreferences tomatoStatePrefs = context.getSharedPreferences("TomatoTimer", Context.MODE_PRIVATE);
        String selectedIcon = tomatoStatePrefs.getString("selected_icon", "🌞");
        settings.add(SettingsEntity.createStringSetting(
            "selected_icon", 
            selectedIcon, 
            "POMODORO"));
    }
    
    private void migrateReminderSettings(List<SettingsEntity> settings) {
        SharedPreferences reminderPrefs = context.getSharedPreferences("ReminderSettings", Context.MODE_PRIVATE);
        
        settings.add(SettingsEntity.createBooleanSetting(
            "vibrate_enabled", 
            reminderPrefs.getBoolean("vibrate_enabled", true), 
            "REMINDER"));
        
        settings.add(SettingsEntity.createBooleanSetting(
            "sound_enabled", 
            reminderPrefs.getBoolean("sound_enabled", true), 
            "REMINDER"));
        
        SharedPreferences timerPrefs = context.getSharedPreferences("TimerReminder", Context.MODE_PRIVATE);
        settings.add(SettingsEntity.createBooleanSetting(
            "repeat_enabled", 
            timerPrefs.getBoolean("repeat_enabled", false), 
            "REMINDER"));
    }
    
    private void migrateAppSettings(List<SettingsEntity> settings) {
        SharedPreferences appPrefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        
        settings.add(SettingsEntity.createIntSetting(
            "max_score", 
            appPrefs.getInt("max_score", 10), 
            "APP"));
        
        settings.add(SettingsEntity.createIntSetting(
            "chart_height", 
            appPrefs.getInt("chart_height", 300), 
            "APP"));
    }
    
    /**
     * 标记迁移完成
     */
    private void markMigrationCompleted() {
        SharedPreferences migrationPrefs = context.getSharedPreferences(MIGRATION_PREF, Context.MODE_PRIVATE);
        migrationPrefs.edit()
            .putBoolean(KEY_MIGRATION_COMPLETED, true)
            .putInt(KEY_MIGRATION_VERSION, CURRENT_MIGRATION_VERSION)
            .apply();
    }
    
    // 工具方法
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString();
    }
    
    private String generateSessionId() {
        return "pomodoro_" + UUID.randomUUID().toString();
    }
    
    private String generateReminderId() {
        return "reminder_" + UUID.randomUUID().toString();
    }
    
    // 临时的TaskItem类定义（用于反序列化旧数据）
    private static class TaskItem {
        private String id;
        private String name;
        private int importance;
        private int urgency;
        private boolean isCompleted;
        private Long completedTime;
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getImportance() { return importance; }
        public int getUrgency() { return urgency; }
        public boolean isCompleted() { return isCompleted; }
        public Long getCompletedTime() { return completedTime; }
    }
}
