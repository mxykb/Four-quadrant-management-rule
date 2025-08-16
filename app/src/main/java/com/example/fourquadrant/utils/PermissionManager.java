package com.example.fourquadrant.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    
    public static class PermissionInfo {
        public String permission;
        public String displayName;
        public String description;
        public boolean isRequired;
        
        public PermissionInfo(String permission, String displayName, String description, boolean isRequired) {
            this.permission = permission;
            this.displayName = displayName;
            this.description = description;
            this.isRequired = isRequired;
        }
    }
    
    private static final PermissionInfo[] REQUIRED_PERMISSIONS = {
        new PermissionInfo(Manifest.permission.POST_NOTIFICATIONS, "通知权限", "用于显示任务提醒和番茄钟通知", true),
        new PermissionInfo(Manifest.permission.VIBRATE, "震动权限", "用于提醒时的震动反馈", false),
        new PermissionInfo(Manifest.permission.SCHEDULE_EXACT_ALARM, "精确闹钟权限", "用于准时的任务提醒", true),
        new PermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储权限", "用于保存四象限图表图片", false),
        new PermissionInfo(Manifest.permission.SYSTEM_ALERT_WINDOW, "悬浮窗权限", "用于显示番茄钟悬浮窗", false)
    };
    
    /**
     * 检查所有权限状态
     */
    public static List<PermissionInfo> checkAllPermissions(Context context) {
        List<PermissionInfo> deniedPermissions = new ArrayList<>();
        
        for (PermissionInfo permissionInfo : REQUIRED_PERMISSIONS) {
            if (!isPermissionGranted(context, permissionInfo.permission)) {
                deniedPermissions.add(permissionInfo);
            }
        }
        
        // 检查电池优化
        if (!isBatteryOptimizationIgnored(context)) {
            deniedPermissions.add(new PermissionInfo(
                "BATTERY_OPTIMIZATION", 
                "电池优化白名单", 
                "确保应用在后台正常运行", 
                true
            ));
        }
        
        return deniedPermissions;
    }
    
    /**
     * 检查单个权限是否已授予
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        // 特殊处理一些权限
        switch (permission) {
            case Manifest.permission.POST_NOTIFICATIONS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                }
                return true; // Android 13以下默认有通知权限
                
            case Manifest.permission.SCHEDULE_EXACT_ALARM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    return alarmManager != null && alarmManager.canScheduleExactAlarms();
                }
                return true; // Android 12以下默认有精确闹钟权限
                
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return Settings.canDrawOverlays(context);
                }
                return true;
                
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return true; // Android 10以上不需要此权限
                }
                return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                
            default:
                return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * 检查是否在电池优化白名单中
     */
    public static boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager != null && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }
    
    /**
     * 生成权限提示文本
     */
    public static String generatePermissionText(List<PermissionInfo> deniedPermissions) {
        if (deniedPermissions.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("应用需要以下权限以正常工作：");
        
        for (int i = 0; i < deniedPermissions.size(); i++) {
            PermissionInfo info = deniedPermissions.get(i);
            if (i > 0) {
                sb.append("、");
            }
            sb.append(info.displayName);
        }
        
        sb.append("。点击此处前往设置。");
        return sb.toString();
    }
    
    /**
     * 打开应用设置页面
     */
    public static void openAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (Exception e) {
            // 如果失败，尝试打开设置主页
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivity(intent);
            } catch (Exception ex) {
                // 忽略异常
            }
        }
    }
}