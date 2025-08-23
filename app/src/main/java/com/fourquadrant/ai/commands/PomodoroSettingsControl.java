package com.fourquadrant.ai.commands;

import android.content.Context;
import android.util.Log;

import com.example.fourquadrant.TomatoSettingsDialog;
import com.example.fourquadrant.database.repository.SettingsRepository;
import com.fourquadrant.ai.AiExecutable;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 番茄钟设置管理功能实现
 * 支持修改工作时长、休息时长、番茄钟数量和自动开始设置
 */
public class PomodoroSettingsControl implements AiExecutable {
    private static final String TAG = "PomodoroSettingsControl";
    private Context context;
    private SettingsRepository settingsRepository;
    private ExecutorService executor;
    
    // 设置键名常量
    private static final String KEY_TOMATO_COUNT = "tomato_count";
    private static final String KEY_TOMATO_DURATION = "tomato_duration";
    private static final String KEY_BREAK_DURATION = "break_duration";
    private static final String KEY_AUTO_NEXT = "auto_next";
    
    public PomodoroSettingsControl(Context context) {
        this.context = context;
        // 使用ApplicationContext来创建Repository
        if (context.getApplicationContext() instanceof android.app.Application) {
            this.settingsRepository = new SettingsRepository((android.app.Application) context.getApplicationContext());
        } else {
            // 如果无法获取Application，则使用null，后续操作会跳过
            this.settingsRepository = null;
            Log.w(TAG, "无法获取Application实例，某些功能可能不可用");
        }
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 检查Repository是否可用
            if (settingsRepository == null) {
                Log.e(TAG, "SettingsRepository不可用，无法执行操作");
                return;
            }
            
            String action = (String) args.get("action");
            if (action == null || action.trim().isEmpty()) {
                Log.w(TAG, "设置操作类型不能为空");
                return;
            }
            
            switch (action.toLowerCase()) {
                case "set":
                    setSettings(args);
                    break;
                case "get":
                    getSettings(args);
                    break;
                case "reset":
                    resetSettings();
                    break;
                default:
                    Log.w(TAG, "不支持的设置操作: " + action);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "番茄钟设置操作失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 设置番茄钟配置
     */
    private void setSettings(Map<String, Object> args) {
        try {
            // 获取设置参数
            Integer tomatoCount = getIntegerValue(args, "tomato_count");
            Integer tomatoDuration = getIntegerValue(args, "tomato_duration");
            Integer breakDuration = getIntegerValue(args, "break_duration");
            Boolean autoNext = getBooleanValue(args, "auto_next");
            
            // 验证参数范围
            if (tomatoCount != null && (tomatoCount < 1 || tomatoCount > 10)) {
                Log.w(TAG, "番茄钟数量超出范围(1-10)，使用默认值4");
                tomatoCount = 4;
            }
            
            if (tomatoDuration != null && (tomatoDuration < 1 || tomatoDuration > 120)) {
                Log.w(TAG, "工作时长超出范围(1-120分钟)，使用默认值25");
                tomatoDuration = 25;
            }
            
            if (breakDuration != null && (breakDuration < 1 || breakDuration > 60)) {
                Log.w(TAG, "休息时长超出范围(1-60分钟)，使用默认值5");
                breakDuration = 5;
            }
            
            // 异步保存设置
            final Integer finalTomatoCount = tomatoCount;
            final Integer finalTomatoDuration = tomatoDuration;
            final Integer finalBreakDuration = breakDuration;
            final Boolean finalAutoNext = autoNext;
            
            executor.execute(() -> {
                try {
                    if (finalTomatoCount != null) {
                        settingsRepository.saveIntSetting(KEY_TOMATO_COUNT, finalTomatoCount);
                        Log.i(TAG, "番茄钟数量已设置为: " + finalTomatoCount);
                    }
                    
                    if (finalTomatoDuration != null) {
                        settingsRepository.saveIntSetting(KEY_TOMATO_DURATION, finalTomatoDuration);
                        Log.i(TAG, "工作时长已设置为: " + finalTomatoDuration + "分钟");
                    }
                    
                    if (finalBreakDuration != null) {
                        settingsRepository.saveIntSetting(KEY_BREAK_DURATION, finalBreakDuration);
                        Log.i(TAG, "休息时长已设置为: " + finalBreakDuration + "分钟");
                    }
                    
                    if (finalAutoNext != null) {
                        settingsRepository.saveBooleanSetting(KEY_AUTO_NEXT, finalAutoNext);
                        Log.i(TAG, "自动开始已设置为: " + finalAutoNext);
                    }
                    
                    Log.i(TAG, "番茄钟设置保存成功");
                } catch (Exception e) {
                    Log.e(TAG, "保存番茄钟设置失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "设置番茄钟配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取番茄钟配置
     */
    private void getSettings(Map<String, Object> args) {
        try {
            executor.execute(() -> {
                try {
                    // 获取当前设置值
                    Integer tomatoCount = settingsRepository.getIntSettingSync(KEY_TOMATO_COUNT);
                    Integer tomatoDuration = settingsRepository.getIntSettingSync(KEY_TOMATO_DURATION);
                    Integer breakDuration = settingsRepository.getIntSettingSync(KEY_BREAK_DURATION);
                    Boolean autoNext = settingsRepository.getBooleanSettingSync(KEY_AUTO_NEXT);
                    
                    // 使用默认值
                    tomatoCount = tomatoCount != null ? tomatoCount : 4;
                    tomatoDuration = tomatoDuration != null ? tomatoDuration : 25;
                    breakDuration = breakDuration != null ? breakDuration : 5;
                    autoNext = autoNext != null ? autoNext : false;
                    
                    // 构建设置信息
                    StringBuilder settingsInfo = new StringBuilder();
                    settingsInfo.append("当前番茄钟设置:\n");
                    settingsInfo.append("番茄钟数量: ").append(tomatoCount).append("个\n");
                    settingsInfo.append("工作时长: ").append(tomatoDuration).append("分钟\n");
                    settingsInfo.append("休息时长: ").append(breakDuration).append("分钟\n");
                    settingsInfo.append("自动开始: ").append(autoNext ? "开启" : "关闭");
                    
                    Log.i(TAG, settingsInfo.toString());
                    
                } catch (Exception e) {
                    Log.e(TAG, "获取番茄钟设置失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取番茄钟配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重置番茄钟设置为默认值
     */
    private void resetSettings() {
        try {
            executor.execute(() -> {
                try {
                    // 重置为默认值
                    settingsRepository.saveIntSetting(KEY_TOMATO_COUNT, 4);
                    settingsRepository.saveIntSetting(KEY_TOMATO_DURATION, 25);
                    settingsRepository.saveIntSetting(KEY_BREAK_DURATION, 5);
                    settingsRepository.saveBooleanSetting(KEY_AUTO_NEXT, false);
                    
                    Log.i(TAG, "番茄钟设置已重置为默认值");
                } catch (Exception e) {
                    Log.e(TAG, "重置番茄钟设置失败: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "重置番茄钟设置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取整数值
     */
    private Integer getIntegerValue(Map<String, Object> args, String key) {
        if (!args.containsKey(key)) {
            return null;
        }
        
        Object value = args.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "无法解析整数值: " + value);
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取布尔值
     */
    private Boolean getBooleanValue(Map<String, Object> args, String key) {
        if (!args.containsKey(key)) {
            return null;
        }
        
        Object value = args.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String strValue = (String) value;
            return "true".equalsIgnoreCase(strValue) || "1".equals(strValue);
        }
        return null;
    }
    
    @Override
    public String getDescription() {
        return "番茄钟设置管理，支持设置(set)、查询(get)和重置(reset)配置";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        if (args == null) {
            return false;
        }
        
        String action = (String) args.get("action");
        if (action == null || action.isEmpty()) {
            return false;
        }
        
        // 验证操作类型
        if (!action.equalsIgnoreCase("set") && 
            !action.equalsIgnoreCase("get") && 
            !action.equalsIgnoreCase("reset")) {
            return false;
        }
        
        // 如果是设置操作，验证参数
        if (action.equalsIgnoreCase("set")) {
            // 至少需要一个设置参数
            return args.containsKey("tomato_count") || 
                   args.containsKey("tomato_duration") || 
                   args.containsKey("break_duration") || 
                   args.containsKey("auto_next");
        }
        
        return true;
    }
    
    /**
     * 获取支持的参数
     */
    public static Map<String, String> getSupportedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "操作类型：set(设置)、get(查询)、reset(重置)");
        params.put("tomato_count", "番茄钟数量(1-10)");
        params.put("tomato_duration", "工作时长(1-120分钟)");
        params.put("break_duration", "休息时长(1-60分钟)");
        params.put("auto_next", "自动开始下一个(布尔值)");
        return params;
    }
}
