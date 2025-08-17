package com.fourquadrant.ai.commands;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fourquadrant.ai.AiExecutable;

import java.util.Map;

/**
 * 切换深色模式功能实现
 */
public class ToggleDarkMode implements AiExecutable {
    private static final String TAG = "ToggleDarkMode";
    private static final String PREFS_NAME = "app_settings";
    private static final String DARK_MODE_KEY = "dark_mode";
    
    private Context context;
    
    public ToggleDarkMode(Context context) {
        this.context = context;
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // 获取当前深色模式状态
            boolean currentDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);
            
            // 检查是否指定了特定的模式
            Boolean targetMode = null;
            if (args.containsKey("enable")) {
                Object enableObj = args.get("enable");
                if (enableObj instanceof Boolean) {
                    targetMode = (Boolean) enableObj;
                } else if (enableObj instanceof String) {
                    String enableStr = (String) enableObj;
                    targetMode = "true".equalsIgnoreCase(enableStr) || "1".equals(enableStr);
                }
            }
            
            // 确定新的模式状态
            boolean newDarkMode = targetMode != null ? targetMode : !currentDarkMode;
            
            // 保存新的设置
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(DARK_MODE_KEY, newDarkMode);
            editor.apply();
            
            Log.i(TAG, "深色模式已" + (newDarkMode ? "开启" : "关闭"));
            
            // TODO: 切换深色模式逻辑
            // 这里需要集成实际的主题切换逻辑
            
            // 示例：应用主题变更
            // if (context instanceof Activity) {
            //     Activity activity = (Activity) context;
            //     if (newDarkMode) {
            //         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            //     } else {
            //         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            //     }
            //     activity.recreate(); // 重新创建Activity以应用新主题
            // }
            
        } catch (Exception e) {
            Log.e(TAG, "ToggleDarkMode 执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getDescription() {
        return "切换深色模式";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        if (args.containsKey("enable")) {
            Object enable = args.get("enable");
            if (enable instanceof Boolean) {
                return true;
            } else if (enable instanceof String) {
                String enableStr = (String) enable;
                return "true".equalsIgnoreCase(enableStr) || 
                       "false".equalsIgnoreCase(enableStr) ||
                       "1".equals(enableStr) || 
                       "0".equals(enableStr);
            }
            return false;
        }
        return true; // 没有enable参数时进行切换
    }
}