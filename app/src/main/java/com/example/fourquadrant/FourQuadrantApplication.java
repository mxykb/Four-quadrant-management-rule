package com.example.fourquadrant;

import android.app.Application;
import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.migration.DataMigrationManager;

/**
 * 应用程序类
 * 负责全局初始化工作
 */
public class FourQuadrantApplication extends Application {
    
    private static FourQuadrantApplication instance;
    private AppDatabase database;
    private DataMigrationManager dataMigrationManager;
    
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
}
