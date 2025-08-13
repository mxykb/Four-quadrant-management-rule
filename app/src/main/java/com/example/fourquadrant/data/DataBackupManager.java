package com.example.fourquadrant.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.entity.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据备份和恢复管理器
 * 支持将所有应用数据导出为JSON格式，并能重新导入
 */
public class DataBackupManager {
    private static final String TAG = "DataBackupManager";
    private static final String BACKUP_FOLDER = "FourQuadrant_Backups";
    private static final String BACKUP_FILE_PREFIX = "backup_";
    private static final String BACKUP_FILE_EXTENSION = ".json";
    
    private final Context context;
    private final AppDatabase database;
    private final Gson gson;
    private final ExecutorService executor;
    
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
     * 备份数据内部类
     * 包含所有需要备份的实体数据
     */
    public static class BackupData {
        public List<TaskEntity> tasks;
        public List<PomodoroSessionEntity> pomodoroSessions;
        public List<ReminderEntity> reminders;
        public UserEntity user;
        public List<SettingsEntity> settings;
        public TimerStateEntity timerState;
        public long backupTime;
        public String appVersion;
        
        public BackupData() {
            this.backupTime = System.currentTimeMillis();
            this.appVersion = "1.0.0"; // 可以从BuildConfig获取
        }
    }
    
    /**
     * 导出数据到JSON文件
     * @param callback 导出结果回调
     */
    public void exportData(ExportCallback callback) {
        executor.execute(() -> {
            try {
                // 创建备份数据对象
                BackupData backupData = new BackupData();
                
                // 从数据库同步获取所有数据
                backupData.tasks = database.taskDao().getAllTasksSync();
                backupData.pomodoroSessions = database.pomodoroDao().getAllSessionsSync();
                backupData.reminders = database.reminderDao().getAllRemindersSync();
                backupData.user = database.userDao().getUserSync();
                backupData.settings = database.settingsDao().getAllSettingsSync();
                backupData.timerState = database.timerStateDao().getTimerStateSync();
                
                // 转换为JSON
                String jsonData = gson.toJson(backupData);
                
                // 生成备份文件名
                String fileName = generateBackupFileName();
                File backupFile = new File(getBackupDirectory(), fileName);
                
                // 写入文件
                try (FileWriter writer = new FileWriter(backupFile)) {
                    writer.write(jsonData);
                }
                
                Log.i(TAG, "数据导出成功: " + backupFile.getAbsolutePath());
                if (callback != null) {
                    callback.onSuccess(backupFile.getAbsolutePath());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "数据导出失败", e);
                if (callback != null) {
                    callback.onError("导出失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 从JSON文件导入数据
     * @param filePath 备份文件路径
     * @param callback 导入结果回调
     */
    public void importData(String filePath, ImportCallback callback) {
        executor.execute(() -> {
            try {
                // 读取JSON文件
                File backupFile = new File(filePath);
                if (!backupFile.exists()) {
                    if (callback != null) {
                        callback.onError("备份文件不存在");
                    }
                    return;
                }
                
                StringBuilder jsonBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                }
                
                // 解析JSON数据
                BackupData backupData = gson.fromJson(jsonBuilder.toString(), BackupData.class);
                
                // 清空现有数据（可选，根据需求决定）
                // clearAllData();
                
                // 导入数据到数据库
                if (backupData.tasks != null && !backupData.tasks.isEmpty()) {
                    database.taskDao().insertTasks(backupData.tasks);
                }
                
                if (backupData.pomodoroSessions != null && !backupData.pomodoroSessions.isEmpty()) {
                    database.pomodoroDao().insertSessions(backupData.pomodoroSessions);
                }
                
                if (backupData.reminders != null && !backupData.reminders.isEmpty()) {
                    database.reminderDao().insertReminders(backupData.reminders);
                }
                
                if (backupData.user != null) {
                    database.userDao().insertUser(backupData.user);
                }
                
                if (backupData.settings != null && !backupData.settings.isEmpty()) {
                    for (SettingsEntity setting : backupData.settings) {
                        database.settingsDao().insertSetting(setting);
                    }
                }
                
                if (backupData.timerState != null) {
                    database.timerStateDao().insertOrUpdateTimerState(backupData.timerState);
                }
                
                Log.i(TAG, "数据导入成功");
                if (callback != null) {
                    callback.onSuccess();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "数据导入失败", e);
                if (callback != null) {
                    callback.onError("导入失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取备份文件列表
     * @return 备份文件列表
     */
    public List<File> getBackupFiles() {
        List<File> backupFiles = new ArrayList<>();
        File backupDir = getBackupDirectory();
        
        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles((dir, name) -> 
                name.startsWith(BACKUP_FILE_PREFIX) && name.endsWith(BACKUP_FILE_EXTENSION));
            
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                backupFiles.addAll(Arrays.asList(files));
            }
        }
        
        return backupFiles;
    }
    
    /**
     * 删除备份文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteBackupFile(String filePath) {
        try {
            File file = new File(filePath);
            boolean deleted = file.delete();
            if (deleted) {
                Log.i(TAG, "备份文件删除成功: " + filePath);
            }
            return deleted;
        } catch (Exception e) {
            Log.e(TAG, "删除备份文件失败", e);
            return false;
        }
    }
    
    /**
     * 清空所有数据（谨慎使用）
     */
    public void clearAllData() {
        executor.execute(() -> {
            try {
                database.taskDao().deleteAllTasks();
                database.pomodoroDao().deleteAllSessions();
                database.reminderDao().deleteAllReminders();
                database.settingsDao().deleteAllSettings();
                database.timerStateDao().clearTimerState();
                Log.i(TAG, "所有数据已清空");
            } catch (Exception e) {
                Log.e(TAG, "清空数据失败", e);
            }
        });
    }
    
    /**
     * 清空所有数据（带回调）
     */
    public void clearAllData(ClearDataCallback callback) {
        executor.execute(() -> {
            try {
                database.taskDao().deleteAllTasks();
                database.pomodoroDao().deleteAllSessions();
                database.reminderDao().deleteAllReminders();
                database.settingsDao().deleteAllSettings();
                database.timerStateDao().clearTimerState();
                Log.i(TAG, "所有数据已清空");
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "清空数据失败", e);
                if (callback != null) {
                    callback.onError("清空数据失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 生成备份文件名
     * @return 文件名
     */
    private String generateBackupFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return BACKUP_FILE_PREFIX + sdf.format(new Date()) + BACKUP_FILE_EXTENSION;
    }
    
    /**
     * 获取备份目录
     * @return 备份目录
     */
    private File getBackupDirectory() {
        File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }
    
    /**
     * 导出回调接口
     */
    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
    
    /**
     * 导入回调接口
     */
    public interface ImportCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * 清空数据回调接口
     */
    public interface ClearDataCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * 释放资源
     */
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}