package com.example.fourquadrant.database;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import com.example.fourquadrant.database.dao.TaskDao;
import com.example.fourquadrant.database.dao.PomodoroDao;
import com.example.fourquadrant.database.dao.ReminderDao;
import com.example.fourquadrant.database.dao.UserDao;
import com.example.fourquadrant.database.dao.SettingsDao;
import com.example.fourquadrant.database.dao.TimerStateDao;
import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.entity.PomodoroSessionEntity;
import com.example.fourquadrant.database.entity.ReminderEntity;
import com.example.fourquadrant.database.entity.UserEntity;
import com.example.fourquadrant.database.entity.SettingsEntity;
import com.example.fourquadrant.database.entity.TimerStateEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room数据库类
 */
@Database(
    entities = {
        TaskEntity.class,
        PomodoroSessionEntity.class,
        ReminderEntity.class,
        UserEntity.class,
        SettingsEntity.class,
        TimerStateEntity.class
    },
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // 数据库名称
    private static final String DATABASE_NAME = "four_quadrant_database";
    
    // 数据库实例
    private static volatile AppDatabase INSTANCE;
    
    // 线程池
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = 
        Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    // 抽象方法 - 获取DAO
    public abstract TaskDao taskDao();
    public abstract PomodoroDao pomodoroDao();
    public abstract ReminderDao reminderDao();
    public abstract UserDao userDao();
    public abstract SettingsDao settingsDao();
    public abstract TimerStateDao timerStateDao();
    
    // 数据库迁移：从版本1到版本2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加is_deleted字段到tasks表
            database.execSQL("ALTER TABLE tasks ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    // 数据库迁移：从版本2到版本3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建timer_state表
            database.execSQL("CREATE TABLE IF NOT EXISTS timer_state (" +
                "id TEXT NOT NULL PRIMARY KEY, " +
                "start_time INTEGER NOT NULL, " +
                "is_running INTEGER NOT NULL, " +
                "is_paused INTEGER NOT NULL, " +
                "remaining_time INTEGER NOT NULL, " +
                "is_break INTEGER NOT NULL, " +
                "current_count INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL)");
        }
    };
    
    // 数据库迁移：从版本3到版本4
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加新字段到timer_state表
            database.execSQL("ALTER TABLE timer_state ADD COLUMN is_completed_pending INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE timer_state ADD COLUMN completed_task_name TEXT");
            database.execSQL("ALTER TABLE timer_state ADD COLUMN total_count INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    /**
     * 获取数据库实例（单例模式）
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .addCallback(sRoomDatabaseCallback) // 添加回调
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // 添加迁移
                    .allowMainThreadQueries() // 允许主线程查询
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 数据库创建回调
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // 数据库创建后的初始化操作
            // 在后台线程中异步初始化默认设置，避免并发访问问题
            databaseWriteExecutor.execute(() -> {
                try {
                    // 等待数据库完全初始化
                    Thread.sleep(100);
                    if (INSTANCE != null) {
                        initializeDefaultSettings(INSTANCE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            
            // 数据库打开后的操作
            // 检查并迁移SharedPreferences数据
            // 这里可以添加数据迁移逻辑
        }
    };
    
    /**
     * 初始化默认设置
     */
    private static void initializeDefaultSettings(AppDatabase database) {
        try {
            SettingsDao settingsDao = database.settingsDao();
            long currentTime = System.currentTimeMillis();
            
            // 插入默认设置
            settingsDao.insertDefaultSettings(currentTime);
        } catch (Exception e) {
            // 如果初始化失败，记录错误但不崩溃
            e.printStackTrace();
        }
    }
    
    /**
     * 迁移SharedPreferences数据到数据库
     */
    public static void migrateSharedPreferencesData(Context context) {
        AppDatabase database = getDatabase(context);
        DataMigrationHelper migrationHelper = new DataMigrationHelper(context, database);
        
        // 执行数据迁移
        migrationHelper.migrateAllData();
    }
    
    /**
     * 关闭数据库连接
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    
    /**
     * 数据迁移辅助类
     */
    private static class DataMigrationHelper {
        private Context context;
        private AppDatabase database;
        
        public DataMigrationHelper(Context context, AppDatabase database) {
            this.context = context;
            this.database = database;
        }
        
        public void migrateAllData() {
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
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void migrateTaskData() {
            // 这里添加任务数据迁移逻辑
            // 从SharedPreferences读取任务数据，转换为TaskEntity并插入数据库
        }
        
        private void migratePomodoroData() {
            // 迁移番茄钟数据
        }
        
        private void migrateReminderData() {
            // 迁移提醒数据
        }
        
        private void migrateUserData() {
            // 迁移用户数据
        }
        
        private void migrateSettingsData() {
            // 迁移设置数据
        }
    }
}
