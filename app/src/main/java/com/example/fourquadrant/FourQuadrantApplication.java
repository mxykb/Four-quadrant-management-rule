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
    public AppDatabase getDatabase() {
        if (database == null) {
            database = AppDatabase.getDatabase(this);
        }
        return database;
    }
    
    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        // 获取数据库实例，这会触发数据库创建
        database = AppDatabase.getDatabase(this);
        
        // 初始化数据迁移管理器
        dataMigrationManager = new DataMigrationManager(this);
        
        // 在后台线程中执行数据迁移
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (dataMigrationManager.needsMigration()) {
                dataMigrationManager.performMigration();
            }
        });
    }
    
    /**
     * 获取数据迁移管理器
     */
    public DataMigrationManager getDataMigrationManager() {
        return dataMigrationManager;
    }
}
