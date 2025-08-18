package com.fourquadrant.ai.commands;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.repository.TaskRepository;
import com.fourquadrant.ai.AiExecutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务管理AI功能实现类
 * 支持创建任务、查看任务、更新任务、删除任务等操作
 */
public class TaskManagement implements AiExecutable {
    private static final String TAG = "TaskManagement";
    private final Context context;
    private final TaskRepository taskRepository;
    private final ExecutorService executor;
    
    public TaskManagement(Context context) {
        this.context = context;
        this.taskRepository = new TaskRepository((Application) context.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            String action = (String) args.get("action");
            if (action == null || action.trim().isEmpty()) {
                Log.w(TAG, "任务操作类型不能为空");
                return;
            }
            
            switch (action.toLowerCase()) {
                case "create":
                    createTask(args);
                    break;
                case "view":
                    viewTasks(args);
                    break;
                case "update":
                    updateTask(args);
                    break;
                case "delete":
                    deleteTask(args);
                    break;
                case "complete":
                    completeTask(args);
                    break;
                default:
                    Log.w(TAG, "不支持的任务操作: " + action);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "任务管理操作失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建新任务
     */
    private void createTask(Map<String, Object> args) {
        String taskName = (String) args.get("task_name");
        Integer importance = getIntegerValue(args, "importance", 5);
        Integer urgency = getIntegerValue(args, "urgency", 5);
        
        if (taskName == null || taskName.trim().isEmpty()) {
            Log.w(TAG, "任务名称不能为空");
            return;
        }
        
        // 验证重要性和紧急性范围
        final Integer finalImportance = Math.max(1, Math.min(10, importance));
        final Integer finalUrgency = Math.max(1, Math.min(10, urgency));
        final String finalTaskName = taskName.trim();
        
        executor.execute(() -> {
            try {
                taskRepository.createTask(finalTaskName, finalImportance, finalUrgency);
                Log.i(TAG, String.format("成功创建任务: %s (重要性:%d, 紧急性:%d)", 
                    finalTaskName, finalImportance, finalUrgency));
            } catch (Exception e) {
                Log.e(TAG, "创建任务失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 查看任务列表
     */
    private void viewTasks(Map<String, Object> args) {
        String type = (String) args.get("type");
        if (type == null) {
            type = "active"; // 默认查看活跃任务
        }
        
        final String finalType = type;
        executor.execute(() -> {
            try {
                switch (finalType.toLowerCase()) {
                    case "all":
                        // 这里可以添加获取所有任务的逻辑
                        Log.i(TAG, "查看所有任务");
                        break;
                    case "completed":
                        // 这里可以添加获取已完成任务的逻辑
                        Log.i(TAG, "查看已完成任务");
                        break;
                    case "active":
                    default:
                        // 这里可以添加获取活跃任务的逻辑
                        Log.i(TAG, "查看活跃任务");
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "查看任务失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 更新任务
     */
    private void updateTask(Map<String, Object> args) {
        String taskId = (String) args.get("task_id");
        String taskName = (String) args.get("task_name");
        Integer importance = getIntegerValue(args, "importance", null);
        Integer urgency = getIntegerValue(args, "urgency", null);
        
        if (taskId == null || taskId.trim().isEmpty()) {
            Log.w(TAG, "任务ID不能为空");
            return;
        }
        
        executor.execute(() -> {
            try {
                // 这里需要实现具体的任务更新逻辑
                // 由于TaskRepository没有直接的updateTask方法，这里先记录日志
                Log.i(TAG, String.format("更新任务: ID=%s, 名称=%s, 重要性=%s, 紧急性=%s", 
                    taskId, taskName, importance, urgency));
            } catch (Exception e) {
                Log.e(TAG, "更新任务失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 删除任务
     */
    private void deleteTask(Map<String, Object> args) {
        String taskId = (String) args.get("task_id");
        
        if (taskId == null || taskId.trim().isEmpty()) {
            Log.w(TAG, "任务ID不能为空");
            return;
        }
        
        executor.execute(() -> {
            try {
                taskRepository.deleteTaskById(taskId);
                Log.i(TAG, "成功删除任务: " + taskId);
            } catch (Exception e) {
                Log.e(TAG, "删除任务失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 完成任务
     */
    private void completeTask(Map<String, Object> args) {
        String taskId = (String) args.get("task_id");
        
        if (taskId == null || taskId.trim().isEmpty()) {
            Log.w(TAG, "任务ID不能为空");
            return;
        }
        
        executor.execute(() -> {
            try {
                // 这里需要实现具体的任务完成逻辑
                // 由于需要先获取任务实体，这里先记录日志
                Log.i(TAG, "完成任务: " + taskId);
            } catch (Exception e) {
                Log.e(TAG, "完成任务失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 安全获取整数值
     */
    private Integer getIntegerValue(Map<String, Object> args, String key, Integer defaultValue) {
        Object value = args.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "无法解析整数值: " + value);
                return defaultValue;
            }
        }
        
        return defaultValue;
    }
    
    @Override
    public String getDescription() {
        return "任务管理AI助手 - 支持创建、查看、更新、删除和完成任务";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            Log.w(TAG, "参数不能为空");
            return false;
        }
        
        String action = (String) args.get("action");
        if (action == null || action.trim().isEmpty()) {
            Log.w(TAG, "action参数是必需的");
            return false;
        }
        
        switch (action.toLowerCase()) {
            case "create":
                String taskName = (String) args.get("task_name");
                if (taskName == null || taskName.trim().isEmpty()) {
                    Log.w(TAG, "创建任务时task_name参数是必需的");
                    return false;
                }
                break;
            case "update":
            case "delete":
            case "complete":
                String taskId = (String) args.get("task_id");
                if (taskId == null || taskId.trim().isEmpty()) {
                    Log.w(TAG, action + "操作时task_id参数是必需的");
                    return false;
                }
                break;
            case "view":
                // view操作不需要必需参数
                break;
            default:
                Log.w(TAG, "不支持的操作类型: " + action);
                return false;
        }
        
        return true;
    }
}