package com.example.fourquadrant;

import android.app.Application;
import android.util.Log;
import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.migration.DataMigrationManager;
import com.example.fourquadrant.utils.VersionManager;

/**
 * 应用程序类
 * 负责全局初始化工作
 */
public class FourQuadrantApplication extends Application {
    
    private static FourQuadrantApplication instance;
    private AppDatabase database;
    private DataMigrationManager dataMigrationManager;
    private VersionManager versionManager;
    private volatile boolean isDatabaseReady = false;
    private volatile boolean isMigrationCompleted = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 初始化数据库
        initializeDatabase();
    }
    
    /**
     * 获取应用程序实例
     */
    public static FourQuadrantApplication getInstance() {
        return instance;
    }
    
    /**
     * 获取数据库实例
     */
    public synchronized AppDatabase getDatabase() {
        if (database == null) {
            int retryCount = 0;
            int maxRetries = 3;
            
            while (retryCount < maxRetries) {
                try {
                    database = AppDatabase.getDatabase(this);
                    if (database != null) {
                        Log.d("FourQuadrantApplication", "Database initialized successfully on attempt " + (retryCount + 1));
                        break;
                    }
                } catch (Exception e) {
                    retryCount++;
                    Log.e("FourQuadrantApplication", "Database initialization failed, attempt " + retryCount + "/" + maxRetries, e);
                    
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(200 * retryCount); // 递增等待时间
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        // 最后一次尝试失败，记录错误但不抛出异常
                        Log.e("FourQuadrantApplication", "Database initialization failed after " + maxRetries + " attempts. App will continue with limited functionality.", e);
                        // 返回null，让调用者处理数据库不可用的情况
                        return null;
                    }
                }
            }
        }
        return database;
    }
    
    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        try {
            // 获取数据库实例，这会触发数据库创建
            database = AppDatabase.getDatabase(this);
            
            // 在后台线程中执行数据迁移和版本管理器初始化，避免阻塞主线程
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // 等待数据库完全初始化
                    Thread.sleep(300);
                    
                    // 初始化数据迁移管理器
                    dataMigrationManager = new DataMigrationManager(FourQuadrantApplication.this);
                    
                    // 执行数据迁移
                     if (dataMigrationManager.needsMigration()) {
                         Log.i("FourQuadrantApplication", "开始执行数据迁移...");
                         dataMigrationManager.performMigration();
                         Log.i("FourQuadrantApplication", "数据迁移完成");
                     }
                     isMigrationCompleted = true;
                     
                     // 初始化版本管理器并检查版本信息
                     initializeVersionManager();
                     
                     // 标记数据库完全就绪
                     isDatabaseReady = true;
                     Log.i("FourQuadrantApplication", "数据库完全就绪");
                    
                } catch (Exception e) {
                    Log.e("FourQuadrantApplication", "后台数据库初始化失败", e);
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            // 如果初始化失败，记录错误但不崩溃
            Log.e("FourQuadrantApplication", "数据库初始化失败", e);
            e.printStackTrace();
            // 重置数据库实例，下次获取时重新初始化
            database = null;
            isDatabaseReady = false;
        }
    }
    
    /**
     * 获取数据迁移管理器
     */
    public DataMigrationManager getDataMigrationManager() {
        return dataMigrationManager;
    }
    
    /**
     * 初始化版本管理器
     */
    private void initializeVersionManager() {
        try {
            // 等待数据库完全初始化
            Thread.sleep(100);
            
            versionManager = new VersionManager(FourQuadrantApplication.this);
            versionManager.checkAndUpdateVersion();
            
            Log.i("FourQuadrantApplication", 
                "版本管理器初始化完成: " + versionManager.getVersionSummary());
                
        } catch (Exception e) {
            Log.e("FourQuadrantApplication", "版本管理器初始化失败", e);
            e.printStackTrace();
        }
    }
    
    /**
     * 获取版本管理器
     */
    public VersionManager getVersionManager() {
        if (versionManager == null) {
            versionManager = new VersionManager(this);
        }
        return versionManager;
    }
    
    /**
     * 检查数据库是否完全就绪
     */
    public boolean isDatabaseReady() {
        return isDatabaseReady && database != null;
    }
    
    /**
     * 检查数据迁移是否完成
     */
    public boolean isMigrationCompleted() {
        return isMigrationCompleted;
    }
    
    /**
     * 等待数据库就绪
     * @param maxWaitTimeMs 最大等待时间（毫秒）
     * @return 数据库是否就绪
     */
    public boolean waitForDatabaseReady(long maxWaitTimeMs) {
        long startTime = System.currentTimeMillis();
        while (!isDatabaseReady() && (System.currentTimeMillis() - startTime) < maxWaitTimeMs) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isDatabaseReady();
    }
}
