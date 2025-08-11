package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.SettingsDao;
import com.example.fourquadrant.database.entity.SettingsEntity;

import java.util.List;

/**
 * 设置数据仓库
 */
public class SettingsRepository {
    
    private SettingsDao settingsDao;
    private LiveData<List<SettingsEntity>> allSettings;
    
    public SettingsRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        settingsDao = database.settingsDao();
        allSettings = settingsDao.getAllSettings();
    }
    
    // 获取所有设置
    public LiveData<List<SettingsEntity>> getAllSettings() {
        return allSettings;
    }
    
    // 根据key获取设置
    public LiveData<SettingsEntity> getSettingByKey(String key) {
        return settingsDao.getSettingByKey(key);
    }
    
    // 同步获取设置
    public SettingsEntity getSettingByKeySync(String key) {
        return settingsDao.getSettingByKeySync(key);
    }
    
    // 根据分类获取设置
    public LiveData<List<SettingsEntity>> getSettingsByCategory(String category) {
        return settingsDao.getSettingsByCategory(category);
    }
    
    // 插入或更新设置
    public void insertSetting(SettingsEntity setting) {
        settingsDao.insertSetting(setting);
    }
    
    // 批量插入设置
    public void insertSettings(List<SettingsEntity> settings) {
        settingsDao.insertSettings(settings);
    }
    
    // 更新设置
    public void updateSetting(SettingsEntity setting) {
        setting.setUpdatedAt(System.currentTimeMillis());
        settingsDao.updateSetting(setting);
    }
    
    // 删除设置
    public void deleteSettingByKey(String key) {
        settingsDao.deleteSettingByKey(key);
    }
    
    // 根据分类删除设置
    public void deleteSettingsByCategory(String category) {
        settingsDao.deleteSettingsByCategory(category);
    }
    
    // 删除所有设置
    public void deleteAllSettings() {
        settingsDao.deleteAllSettings();
    }
    
    // 便捷方法：设置字符串值
    public void setStringValue(String key, String value, String category) {
        long currentTime = System.currentTimeMillis();
        settingsDao.setStringValue(key, value, category, currentTime);
    }
    
    // 便捷方法：设置整数值
    public void setIntValue(String key, int value, String category) {
        long currentTime = System.currentTimeMillis();
        settingsDao.setIntValue(key, String.valueOf(value), category, currentTime);
    }
    
    // 便捷方法：设置布尔值
    public void setBooleanValue(String key, boolean value, String category) {
        long currentTime = System.currentTimeMillis();
        settingsDao.setBooleanValue(key, String.valueOf(value), category, currentTime);
    }
    
    // 便捷方法：获取字符串值
    public String getStringValue(String key, String defaultValue) {
        String value = settingsDao.getStringValueSync(key);
        return value != null ? value : defaultValue;
    }
    
    // 便捷方法：获取整数值
    public int getIntValue(String key, int defaultValue) {
        String value = settingsDao.getIntValueString(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // 便捷方法：获取布尔值
    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = settingsDao.getBooleanValueString(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    // 更新设置值
    public void updateSettingValue(String key, String value) {
        settingsDao.updateSettingValue(key, value, System.currentTimeMillis());
    }
    
    // 检查设置是否存在
    public boolean isSettingExists(String key) {
        return settingsDao.getSettingCount(key) > 0;
    }
    
    // 初始化默认设置
    public void initializeDefaultSettings() {
        long currentTime = System.currentTimeMillis();
        settingsDao.insertDefaultSettings(currentTime);
    }
    
    // 番茄钟设置
    public static class PomodoroSettings {
        public static final String CATEGORY = "POMODORO";
        public static final String POMODORO_COUNT = "pomodoro_count";
        public static final String POMODORO_DURATION = "pomodoro_duration";
        public static final String BREAK_DURATION = "break_duration";
        public static final String AUTO_NEXT = "auto_next";
        public static final String SELECTED_ICON = "selected_icon";
    }
    
    // 提醒设置
    public static class ReminderSettings {
        public static final String CATEGORY = "REMINDER";
        public static final String VIBRATE_ENABLED = "vibrate_enabled";
        public static final String SOUND_ENABLED = "sound_enabled";
        public static final String REPEAT_ENABLED = "repeat_enabled";
    }
    
    // 应用设置
    public static class AppSettings {
        public static final String CATEGORY = "APP";
        public static final String MAX_SCORE = "max_score";
        public static final String CHART_HEIGHT = "chart_height";
        public static final String THEME_MODE = "theme_mode";
    }
    
    // 用户设置
    public static class UserSettings {
        public static final String CATEGORY = "USER";
        public static final String FIRST_LAUNCH = "first_launch";
        public static final String TUTORIAL_COMPLETED = "tutorial_completed";
    }
    
    // 番茄钟设置便捷方法
    public int getPomodoroCount() {
        return getIntValue(PomodoroSettings.POMODORO_COUNT, 4);
    }
    
    public void setPomodoroCount(int count) {
        setIntValue(PomodoroSettings.POMODORO_COUNT, count, PomodoroSettings.CATEGORY);
    }
    
    public int getPomodoroDuration() {
        return getIntValue(PomodoroSettings.POMODORO_DURATION, 25);
    }
    
    public void setPomodoroDuration(int duration) {
        setIntValue(PomodoroSettings.POMODORO_DURATION, duration, PomodoroSettings.CATEGORY);
    }
    
    public int getBreakDuration() {
        return getIntValue(PomodoroSettings.BREAK_DURATION, 5);
    }
    
    public void setBreakDuration(int duration) {
        setIntValue(PomodoroSettings.BREAK_DURATION, duration, PomodoroSettings.CATEGORY);
    }
    
    public boolean isAutoNextEnabled() {
        return getBooleanValue(PomodoroSettings.AUTO_NEXT, false);
    }
    
    public void setAutoNextEnabled(boolean enabled) {
        setBooleanValue(PomodoroSettings.AUTO_NEXT, enabled, PomodoroSettings.CATEGORY);
    }
    
    public String getSelectedIcon() {
        return getStringValue(PomodoroSettings.SELECTED_ICON, "🌞");
    }
    
    public void setSelectedIcon(String icon) {
        setStringValue(PomodoroSettings.SELECTED_ICON, icon, PomodoroSettings.CATEGORY);
    }
    
    // 提醒设置便捷方法
    public boolean isVibrateEnabled() {
        return getBooleanValue(ReminderSettings.VIBRATE_ENABLED, true);
    }
    
    public void setVibrateEnabled(boolean enabled) {
        setBooleanValue(ReminderSettings.VIBRATE_ENABLED, enabled, ReminderSettings.CATEGORY);
    }
    
    public boolean isSoundEnabled() {
        return getBooleanValue(ReminderSettings.SOUND_ENABLED, true);
    }
    
    public void setSoundEnabled(boolean enabled) {
        setBooleanValue(ReminderSettings.SOUND_ENABLED, enabled, ReminderSettings.CATEGORY);
    }
    
    public boolean isRepeatEnabled() {
        return getBooleanValue(ReminderSettings.REPEAT_ENABLED, false);
    }
    
    public void setRepeatEnabled(boolean enabled) {
        setBooleanValue(ReminderSettings.REPEAT_ENABLED, enabled, ReminderSettings.CATEGORY);
    }
    
    // 同步获取方法 - 用于静态方法调用
    public Integer getIntSettingSync(String key) {
        try {
            SettingsEntity entity = settingsDao.getSettingSync(key);
            if (entity != null && entity.getValue() != null) {
                return entity.getIntValue();
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }
    
    public Boolean getBooleanSettingSync(String key) {
        try {
            SettingsEntity entity = settingsDao.getSettingSync(key);
            if (entity != null && entity.getValue() != null) {
                return entity.getBooleanValue();
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }
    
    public String getStringSettingSync(String key) {
        try {
            SettingsEntity entity = settingsDao.getSettingSync(key);
            if (entity != null && entity.getStringValue() != null) {
                return entity.getStringValue();
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }
    
    // 添加缺失的便捷方法
    public LiveData<Integer> getIntSetting(String key) {
        return settingsDao.getIntValue(key);
    }
    
    public LiveData<Boolean> getBooleanSetting(String key) {
        return settingsDao.getBooleanValue(key);
    }
    
    public LiveData<String> getStringSetting(String key) {
        return settingsDao.getStringValue(key);
    }
    
    public LiveData<Long> getLongSetting(String key) {
        return settingsDao.getLongValue(key);
    }
    
    public void saveIntSetting(String key, int value) {
        setIntValue(key, value, "GENERAL");
    }
    
    public void saveBooleanSetting(String key, boolean value) {
        setBooleanValue(key, value, "GENERAL");
    }
    
    public void saveStringSetting(String key, String value) {
        setStringValue(key, value, "GENERAL");
    }
    
    public void saveLongSetting(String key, long value) {
        long currentTime = System.currentTimeMillis();
        settingsDao.setLongValue(key, value, "GENERAL", currentTime);
    }
    
    public void deleteSetting(String key) {
        deleteSettingByKey(key);
    }
}
