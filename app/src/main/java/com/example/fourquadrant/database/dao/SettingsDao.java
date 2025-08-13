package com.example.fourquadrant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.entity.SettingsEntity;

import java.util.List;

/**
 * 设置数据访问对象
 */
@Dao
public interface SettingsDao {
    
    // 插入或更新设置
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSetting(SettingsEntity setting);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSettings(List<SettingsEntity> settings);
    
    // 更新设置
    @Update
    void updateSetting(SettingsEntity setting);
    
    // 查询所有设置
    @Query("SELECT * FROM settings ORDER BY category, key")
    LiveData<List<SettingsEntity>> getAllSettings();
    
    // 根据key查询设置
    @Query("SELECT * FROM settings WHERE key = :key")
    LiveData<SettingsEntity> getSettingByKey(String key);
    
    // 同步查询设置
    @Query("SELECT * FROM settings WHERE key = :key")
    SettingsEntity getSettingByKeySync(String key);
    
    // 根据分类查询设置
    @Query("SELECT * FROM settings WHERE category = :category ORDER BY key")
    LiveData<List<SettingsEntity>> getSettingsByCategory(String category);
    
    // 删除设置
    @Query("DELETE FROM settings WHERE key = :key")
    void deleteSettingByKey(String key);
    
    @Query("DELETE FROM settings WHERE category = :category")
    void deleteSettingsByCategory(String category);
    
    @Query("DELETE FROM settings")
    void deleteAllSettings();
    
    // 同步获取设置（不使用LiveData）
    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    SettingsEntity getSettingSync(String key);
    
    // 同步获取所有设置（用于数据导出）
    @Query("SELECT * FROM settings ORDER BY category, key")
    List<SettingsEntity> getAllSettingsSync();
    
    // LiveData版本的获取方法
    @Query("SELECT CASE WHEN value IS NOT NULL THEN CAST(value AS INTEGER) ELSE NULL END FROM settings WHERE key = :key AND type = 'INT'")
    LiveData<Integer> getIntValue(String key);
    
    @Query("SELECT CASE WHEN value IS NOT NULL THEN CAST(value AS INTEGER) ELSE NULL END FROM settings WHERE key = :key AND type = 'BOOLEAN'")
    LiveData<Boolean> getBooleanValue(String key);
    
    @Query("SELECT value FROM settings WHERE key = :key AND type = 'STRING'")
    LiveData<String> getStringValue(String key);
    
    @Query("SELECT CASE WHEN value IS NOT NULL THEN CAST(value AS INTEGER) ELSE NULL END FROM settings WHERE key = :key AND type = 'LONG'")
    LiveData<Long> getLongValue(String key);
    
    // 便捷方法：获取字符串值（同步）
    @Query("SELECT value FROM settings WHERE key = :key AND type = 'STRING'")
    String getStringValueSync(String key);
    
    // 便捷方法：获取整数值
    @Query("SELECT value FROM settings WHERE key = :key AND type = 'INT'")
    String getIntValueString(String key);
    
    // 便捷方法：获取布尔值
    @Query("SELECT value FROM settings WHERE key = :key AND type = 'BOOLEAN'")
    String getBooleanValueString(String key);
    
    // 便捷方法：设置字符串值
    @Query("INSERT OR REPLACE INTO settings (key, value, type, category, created_at, updated_at) " +
           "VALUES (:key, :value, 'STRING', :category, :time, :time)")
    void setStringValue(String key, String value, String category, long time);
    
    // 便捷方法：设置整数值
    @Query("INSERT OR REPLACE INTO settings (key, value, type, category, created_at, updated_at) " +
           "VALUES (:key, :value, 'INT', :category, :time, :time)")
    void setIntValue(String key, String value, String category, long time);
    
    // 便捷方法：设置布尔值
    @Query("INSERT OR REPLACE INTO settings (key, value, type, category, created_at, updated_at) " +
           "VALUES (:key, :value, 'BOOLEAN', :category, :time, :time)")
    void setBooleanValue(String key, String value, String category, long time);
    
    // 便捷方法：设置长整型值
    @Query("INSERT OR REPLACE INTO settings (key, value, type, category, created_at, updated_at) " +
           "VALUES (:key, :value, 'LONG', :category, :time, :time)")
    void setLongValue(String key, long value, String category, long time);
    
    // 检查设置是否存在
    @Query("SELECT COUNT(*) FROM settings WHERE key = :key")
    int getSettingCount(String key);
    
    // 更新设置值
    @Query("UPDATE settings SET value = :value, updated_at = :updateTime WHERE key = :key")
    void updateSettingValue(String key, String value, long updateTime);
    
    // 批量设置默认值
    @Query("INSERT OR IGNORE INTO settings (key, value, type, category, created_at, updated_at) " +
           "VALUES " +
           "('pomodoro_count', '4', 'INT', 'POMODORO', :time, :time), " +
           "('pomodoro_duration', '25', 'INT', 'POMODORO', :time, :time), " +
           "('break_duration', '5', 'INT', 'POMODORO', :time, :time), " +
           "('auto_next', 'false', 'BOOLEAN', 'POMODORO', :time, :time), " +
           "('vibrate_enabled', 'true', 'BOOLEAN', 'REMINDER', :time, :time), " +
           "('sound_enabled', 'true', 'BOOLEAN', 'REMINDER', :time, :time), " +
           "('repeat_enabled', 'false', 'BOOLEAN', 'REMINDER', :time, :time), " +
           "('max_score', '10', 'INT', 'APP', :time, :time), " +
           "('chart_height', '300', 'INT', 'APP', :time, :time), " +
           "('app_version_name', '1.0', 'STRING', 'APP', :time, :time), " +
           "('app_version_code', '1', 'INT', 'APP', :time, :time), " +
           "('app_install_time', :time, 'LONG', 'APP', :time, :time)")
    void insertDefaultSettings(long time);
}
