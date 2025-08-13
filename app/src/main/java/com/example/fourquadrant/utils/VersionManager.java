package com.example.fourquadrant.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.example.fourquadrant.database.repository.SettingsRepository;
import com.example.fourquadrant.FourQuadrantApplication;

/**
 * 版本管理工具类
 * 负责管理应用版本信息的存储和更新
 */
public class VersionManager {
    
    private static final String VERSION_NAME_KEY = "app_version_name";
    private static final String VERSION_CODE_KEY = "app_version_code";
    private static final String INSTALL_TIME_KEY = "app_install_time";
    private static final String LAST_UPDATE_TIME_KEY = "app_last_update_time";
    private static final String APP_CATEGORY = "APP";
    
    private Context context;
    private SettingsRepository settingsRepository;
    
    public VersionManager(Application application) {
        this.context = application;
        this.settingsRepository = new SettingsRepository(application);
    }
    
    /**
     * 检查并更新版本信息
     * 在应用启动时调用
     */
    public void checkAndUpdateVersion() {
        try {
            // 获取当前应用版本信息
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String currentVersionName = packageInfo.versionName;
            int currentVersionCode = packageInfo.versionCode;
            
            // 获取数据库中存储的版本信息
            String storedVersionName = settingsRepository.getStringSettingSync(VERSION_NAME_KEY);
            Integer storedVersionCode = settingsRepository.getIntSettingSync(VERSION_CODE_KEY);
            
            long currentTime = System.currentTimeMillis();
            
            // 如果是首次安装或版本发生变化，更新版本信息
            if (storedVersionName == null || storedVersionCode == null || 
                !currentVersionName.equals(storedVersionName) || 
                currentVersionCode != storedVersionCode) {
                
                // 更新版本信息
                settingsRepository.setStringValue(VERSION_NAME_KEY, currentVersionName, APP_CATEGORY);
                settingsRepository.setIntValue(VERSION_CODE_KEY, currentVersionCode, APP_CATEGORY);
                settingsRepository.setLongSetting(LAST_UPDATE_TIME_KEY, currentTime, APP_CATEGORY);
                
                // 如果是首次安装，记录安装时间
                if (storedVersionName == null) {
                    settingsRepository.setLongSetting(INSTALL_TIME_KEY, currentTime, APP_CATEGORY);
                }
                
                // 记录版本更新日志
                logVersionUpdate(storedVersionName, currentVersionName, storedVersionCode, currentVersionCode);
            }
            
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前应用版本名称
     */
    public String getCurrentVersionName() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "未知版本";
        }
    }
    
    /**
     * 获取当前应用版本号
     */
    public int getCurrentVersionCode() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }
    
    /**
     * 获取应用安装时间
     */
    public long getInstallTime() {
        Long installTime = settingsRepository.getLongSettingSync(INSTALL_TIME_KEY);
        return installTime != null ? installTime : 0;
    }
    
    /**
     * 获取应用最后更新时间
     */
    public long getLastUpdateTime() {
        Long updateTime = settingsRepository.getLongSettingSync(LAST_UPDATE_TIME_KEY);
        return updateTime != null ? updateTime : getInstallTime();
    }
    
    /**
     * 获取存储在数据库中的版本信息
     */
    public String getStoredVersionName() {
        return settingsRepository.getStringSettingSync(VERSION_NAME_KEY);
    }
    
    /**
     * 获取存储在数据库中的版本号
     */
    public Integer getStoredVersionCode() {
        return settingsRepository.getIntSettingSync(VERSION_CODE_KEY);
    }
    
    /**
     * 检查是否是首次安装
     */
    public boolean isFirstInstall() {
        return getStoredVersionName() == null;
    }
    
    /**
     * 检查是否是版本更新
     */
    public boolean isVersionUpdated() {
        String storedVersion = getStoredVersionName();
        String currentVersion = getCurrentVersionName();
        
        if (storedVersion == null) {
            return false; // 首次安装不算更新
        }
        
        return !currentVersion.equals(storedVersion);
    }
    
    /**
     * 记录版本更新日志
     */
    private void logVersionUpdate(String oldVersionName, String newVersionName, 
                                 Integer oldVersionCode, int newVersionCode) {
        if (oldVersionName == null) {
            android.util.Log.i("VersionManager", 
                String.format("应用首次安装 - 版本: %s (代码: %d)", newVersionName, newVersionCode));
        } else {
            android.util.Log.i("VersionManager", 
                String.format("应用版本更新 - 从 %s (%d) 更新到 %s (%d)", 
                    oldVersionName, oldVersionCode != null ? oldVersionCode : 0, 
                    newVersionName, newVersionCode));
        }
    }
    
    /**
     * 获取版本信息摘要
     */
    public String getVersionSummary() {
        return String.format("版本 %s (代码: %d)", getCurrentVersionName(), getCurrentVersionCode());
    }
}