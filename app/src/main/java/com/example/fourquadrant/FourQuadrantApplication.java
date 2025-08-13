package com.example.fourquadrant;

import android.app.Application;
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
            try {
                database = AppDatabase.getDatabase(this);
            } catch (Exception e) {
                e.printStackTrace();
                // 如果获取失败，等待一段时间后重试
                try {
                    Thread.sleep(100);
                    database = AppDatabase.getDatabase(this);
                } catch (Exception retryException) {
                    retryException.printStackTrace();
                    throw new RuntimeException("数据库初始化失败", retryException);
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
            
            // 初始化数据迁移管理器
            dataMigrationManager = new DataMigrationManager(this);
            
            // 执行数据迁移
            if (dataMigrationManager.needsMigration()) {
                dataMigrationManager.performMigration();
            }
            
            // 初始化版本管理器并检查版本信息
            initializeVersionManager();
            
        } catch (Exception e) {
            // 如果初始化失败，记录错误但不崩溃
            e.printStackTrace();
            // 重置数据库实例，下次获取时重新初始化
            database = null;
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
            // 在后台线程中初始化版本管理器，避免阻塞主线程
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // 等待数据库完全初始化
                    Thread.sleep(200);
                    
                    versionManager = new VersionManager(FourQuadrantApplication.this);
                    versionManager.checkAndUpdateVersion();
                    
                    android.util.Log.i("FourQuadrantApplication", 
                        "版本管理器初始化完成: " + versionManager.getVersionSummary());
                        
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("FourQuadrantApplication", "版本管理器初始化失败", e);
                }
            });
        } catch (Exception e) {
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
}
