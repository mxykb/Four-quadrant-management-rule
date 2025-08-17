package com.fourquadrant.ai.commands;

import android.content.Context;
import android.util.Log;

import com.fourquadrant.ai.AiExecutable;

import java.util.Map;

/**
 * 启动番茄钟功能实现
 */
public class StartPomodoro implements AiExecutable {
    private static final String TAG = "StartPomodoro";
    private Context context;
    
    public StartPomodoro(Context context) {
        this.context = context;
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 获取持续时间参数，默认25分钟
            int duration = 25;
            if (args.containsKey("duration")) {
                Object durationObj = args.get("duration");
                if (durationObj instanceof Integer) {
                    duration = (Integer) durationObj;
                } else if (durationObj instanceof String) {
                    duration = Integer.parseInt((String) durationObj);
                }
            }
            
            // 验证持续时间范围
            if (duration < 1 || duration > 120) {
                Log.w(TAG, "持续时间超出范围，使用默认值25分钟");
                duration = 25;
            }
            
            // 获取任务相关参数（必需参数）
            String taskName = (String) args.get("task_name");
            
            String taskId = null;
            if (args.containsKey("task_id")) {
                Object taskIdObj = args.get("task_id");
                if (taskIdObj instanceof String) {
                    taskId = (String) taskIdObj;
                }
            }
            
            // TODO: 调用番茄钟服务逻辑
            // 这里需要集成实际的番茄钟启动逻辑
            Log.i(TAG, "番茄钟开始：" + duration + " 分钟，任务：" + taskName);
            
            // 示例：启动番茄钟服务
            // Intent intent = new Intent(context, PomodoroService.class);
            // intent.putExtra("duration", duration);
            // intent.putExtra("task_name", taskName);
            // intent.putExtra("task_id", taskId);
            // context.startService(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "StartPomodoro 执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getDescription() {
        return "启动番茄钟计时器，支持指定时长和关联任务";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        // task_name 是必需参数
        if (!args.containsKey("task_name")) {
            return false;
        }
        Object taskName = args.get("task_name");
        if (!(taskName instanceof String)) {
            return false;
        }
        String name = (String) taskName;
        if (name.trim().isEmpty()) {
            return false;
        }
        
        // 验证duration参数
        if (args.containsKey("duration")) {
            Object duration = args.get("duration");
            if (duration instanceof Integer) {
                int dur = (Integer) duration;
                if (dur < 1 || dur > 120) {
                    return false;
                }
            } else if (duration instanceof String) {
                try {
                    int dur = Integer.parseInt((String) duration);
                    if (dur < 1 || dur > 120) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // 验证task_id参数
        if (args.containsKey("task_id")) {
            Object taskId = args.get("task_id");
            if (taskId != null && !(taskId instanceof String)) {
                return false;
            }
        }
        
        return true;
    }
}