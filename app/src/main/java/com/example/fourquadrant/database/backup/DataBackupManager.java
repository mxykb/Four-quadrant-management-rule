package com.example.fourquadrant.database.backup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.database.entity.ReminderEntity;
import com.example.fourquadrant.database.entity.UserEntity;
import com.example.fourquadrant.database.entity.SettingsEntity;
import com.example.fourquadrant.database.entity.TimerStateEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据备份管理器
 * 负责应用数据的导出和导入功能
 */
public class DataBackupManager {
    
    private static final String TAG = "DataBackupManager";
    private static final String BACKUP_FOLDER = "FourQuadrant";
    private static final String BACKUP_FILE_PREFIX = "backup_";
    private static final String BACKUP_FILE_EXTENSION = ".json";
    
    private Context context;
    private AppDatabase database;
    private Gson gson;
    private ExecutorService executor;
    
    public DataBackupManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 备份数据接口
     */
    public interface BackupCallback {
        void onSuccess(String filePath);
        void onError(String error);
        void onProgress(String message);
    }
    
    /**
     * 恢复数据接口
     */
    public interface RestoreCallback {
        void onSuccess();
        void onError(String error);
        void onProgress(String message);
    }
    
    /**
     * 备份数据模型
     */
    public static class BackupData {
        public String version = "1.0";
        public long timestamp;
        public String deviceInfo;
        public List<TaskEntity> tasks;
        public List<PomodoroSessionEntity> pomodoroSessions;
        public List<ReminderEntity> reminders;
        public List<UserEntity> users;
        public List<SettingsEntity> settings;
        public List<TimerStateEntity> timerStates;
        
        public BackupData() {
            this.timestamp = System.currentTimeMillis();
            this.deviceInfo = android.os.Build.MODEL + " (" + android.os.Build.VERSION.RELEASE + ")";
        }
    }
    
    /**
     * 导出所有数据到JSON文件
     */
    public void exportData(BackupCallback callback) {
        executor.execute(() -> {
            try {
                callback.onProgress("开始导出数据...");
                
                // 创建备份数据对象
                BackupData backupData = new BackupData();
                
                // 导出任务数据
                callback.onProgress("导出任务数据...");
                backupData.tasks = database.taskDao().getAllTasksSync();
                Log.i(TAG, "导出任务数量: " + (backupData.tasks != null ? backupData.tasks.size() : 0));
                
                // 导出番茄钟会话数据
                callback.onProgress("导出番茄钟数据...");
                backupData.pomodoroSessions = database.pomodoroDao().getAllSessionsSync();
                Log.i(TAG, "导出番茄钟会话数量: " + (backupData.pomodoroSessions != null ? backupData.pomodoroSessions.size() : 0));
                
                // 导出提醒数据
                callback.onProgress("导出提醒数据...");
                backupData.reminders = database.reminderDao().getAllRemindersSync();
                Log.i(TAG, "导出提醒数量: " + (backupData.reminders != null ? backupData.reminders.size() : 0));
                
                // 导出用户数据
                callback.onProgress("导出用户数据...");
                backupData.users = database.userDao().getAllUsersSync();
                Log.i(TAG, "导出用户数量: " + (backupData.users != null ? backupData.users.size() : 0));
                
                // 导出设置数据
                callback.onProgress("导出设置数据...");
                backupData.settings = database.settingsDao().getAllSettingsSync();
                Log.i(TAG, "导出设置数量: " + (backupData.settings != null ? backupData.settings.size() : 0));
                
                // 导出计时器状态数据
                callback.onProgress("导出计时器状态...");
                TimerStateEntity timerState = database.timerStateDao().getTimerStateSync();
                if (timerState != null) {
                    backupData.timerStates = List.of(timerState);
                }
                
                // 生成备份文件
                callback.onProgress("生成备份文件...");
                String fileName = generateBackupFileName();
                File backupFile = createBackupFile(fileName);
                
                // 写入JSON数据
                try (FileWriter writer = new FileWriter(backupFile)) {
                    gson.toJson(backupData, writer);
                }
                
                Log.i(TAG, "数据导出成功: " + backupFile.getAbsolutePath());
                callback.onSuccess(backupFile.getAbsolutePath());
                
            } catch (Exception e) {
                Log.e(TAG, "数据导出失败", e);
                callback.onError("导出失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 从JSON文件导入数据
     */
    public void importData(String filePath, RestoreCallback callback) {
        executor.execute(() -> {
            try {
                callback.onProgress("开始导入数据...");
                
                // 读取备份文件
                File backupFile = new File(filePath);
                if (!backupFile.exists()) {
                    callback.onError("备份文件不存在: " + filePath);
                    return;
                }
                
                // 解析JSON数据
                callback.onProgress("解析备份文件...");
                BackupData backupData;
                try (FileReader reader = new FileReader(backupFile)) {
                    Type backupType = new TypeToken<BackupData>(){}.getType();
                    backupData = gson.fromJson(reader, backupType);
                }
                
                if (backupData == null) {
                    callback.onError("备份文件格式错误");
                    return;
                }
                
                // 清空现有数据（可选，根据需求决定）
                callback.onProgress("清理现有数据...");
                clearAllData();
                
                // 导入任务数据
                if (backupData.tasks != null && !backupData.tasks.isEmpty()) {
                    callback.onProgress("导入任务数据...");
                    database.taskDao().insertTasks(backupData.tasks);
                    Log.i(TAG, "导入任务数量: " + backupData.tasks.size());
                }
                
                // 导入番茄钟会话数据
                if (backupData.pomodoroSessions != null && !backupData.pomodoroSessions.isEmpty()) {
                    callback.onProgress("导入番茄钟数据...");
                    database.pomodoroDao().insertSessions(backupData.pomodoroSessions);
                    Log.i(TAG, "导入番茄钟会话数量: " + backupData.pomodoroSessions.size());
                }
                
                // 导入提醒数据
                if (backupData.reminders != null && !backupData.reminders.isEmpty()) {
                    callback.onProgress("导入提醒数据...");
                    database.reminderDao().insertReminders(backupData.reminders);
                    Log.i(TAG, "导入提醒数量: " + backupData.reminders.size());
                }
                
                // 导入用户数据
                if (backupData.users != null && !backupData.users.isEmpty()) {
                    callback.onProgress("导入用户数据...");
                    for (UserEntity user : backupData.users) {
                        database.userDao().insertUser(user);
                    }
                    Log.i(TAG, "导入用户数量: " + backupData.users.size());
                }
                
                // 导入设置数据
                if (backupData.settings != null && !backupData.settings.isEmpty()) {
                    callback.onProgress("导入设置数据...");
                    for (SettingsEntity setting : backupData.settings) {
                        database.settingsDao().insertSetting(setting);
                    }
                    Log.i(TAG, "导入设置数量: " + backupData.settings.size());
                }
                
                // 导入计时器状态数据
                if (backupData.timerStates != null && !backupData.timerStates.isEmpty()) {
                    callback.onProgress("导入计时器状态...");
                    database.timerStateDao().insertOrUpdateTimerState(backupData.timerStates.get(0));
                    Log.i(TAG, "导入计时器状态完成");
                }
                
                Log.i(TAG, "数据导入成功");
                callback.onSuccess();
                
            } catch (Exception e) {
                Log.e(TAG, "数据导入失败", e);
                callback.onError("导入失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 获取备份文件列表
     */
    public List<File> getBackupFiles() {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            return List.of();
        }
        
        File[] files = backupDir.listFiles((dir, name) -> 
            name.startsWith(BACKUP_FILE_PREFIX) && name.endsWith(BACKUP_FILE_EXTENSION));
        
        if (files == null) {
            return List.of();
        }
        
        return List.of(files);
    }
    
    /**
     * 删除备份文件
     */
    public boolean deleteBackupFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (Exception e) {
            Log.e(TAG, "删除备份文件失败", e);
            return false;
        }
    }
    
    /**
     * 清空所有数据
     */
    private void clearAllData() {
        database.taskDao().deleteAllTasks();
        database.pomodoroDao().deleteAllSessions();
        database.reminderDao().deleteAllReminders();
        database.userDao().deleteAllUsers();
        database.settingsDao().deleteAllSettings();
        database.timerStateDao().clearTimerState();
    }
    
    /**
     * 生成备份文件名
     */
    private String generateBackupFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return BACKUP_FILE_PREFIX + sdf.format(new Date()) + BACKUP_FILE_EXTENSION;
    }
    
    /**
     * 创建备份文件
     */
    private File createBackupFile(String fileName) throws IOException {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
            }
        }
        
        File backupFile = new File(backupDir, fileName);
        if (!backupFile.exists()) {
            if (!backupFile.createNewFile()) {
                throw new IOException("无法创建备份文件: " + backupFile.getAbsolutePath());
            }
        }
        
        return backupFile;
    }
    
    /**
     * 获取备份目录
     */
    private File getBackupDirectory() {
        // 使用外部存储的Documents目录
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(documentsDir, BACKUP_FOLDER);
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}