package com.example.fourquadrant.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fourquadrant.R;
import com.example.fourquadrant.adapter.PomodoroFunctionAdapter;
import com.example.fourquadrant.utils.PomodoroFunctionPermissionManager;
import com.example.fourquadrant.model.PomodoroFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;

/**
 * 番茄钟AI功能测试界面
 */
public class PomodoroAiActivity extends AppCompatActivity {
    
    private ImageView btnBack;
    private RecyclerView recyclerPomodoroFunctions;
    private Spinner spinnerAction;
    private EditText etTaskName, etDuration;
    private Button btnExecute;
    private TextView tvResult;
    
    private PomodoroFunctionPermissionManager permissionManager;
    private PomodoroFunctionAdapter functionAdapter;
    private List<PomodoroFunction> pomodoroFunctions;
    
    // 操作类型数组 - 包含所有18个番茄钟AI功能
    private String[] actionTypes = {
        // 基础番茄钟控制功能
        "启动番茄钟",
        "暂停番茄钟", 
        "恢复番茄钟",
        "停止番茄钟",
        "查询状态",
        
        // 番茄钟休息流程控制功能
        "开始休息",
        "跳过休息",
        
        // 番茄钟完成流程控制功能
        "完成番茄钟",
        "关闭番茄钟",
        "重置番茄钟",
        
        // 番茄钟设置管理功能
        "设置番茄钟",
        "查询设置",
        "重置设置",
        
        // 番茄钟历史记录查询功能
        "查询历史",
        "统计分析",
        
        // 原有功能
        "番茄钟设置",
        "番茄钟分析"
    };
    
    // 操作类型到AI命令的映射表
    private String[] aiCommands = {
        // 基础番茄钟控制功能
        "start_pomodoro",
        "pause_pomodoro", 
        "resume_pomodoro",
        "stop_pomodoro",
        "get_pomodoro_status",
        
        // 番茄钟休息流程控制功能
        "start_break",
        "skip_break",
        
        // 番茄钟完成流程控制功能
        "complete_pomodoro",
        "close_pomodoro",
        "reset_pomodoro",
        
        // 番茄钟设置管理功能
        "set_pomodoro_settings",
        "get_pomodoro_settings",
        "reset_pomodoro_settings",
        
        // 番茄钟历史记录查询功能
        "get_pomodoro_history",
        "get_pomodoro_stats",
        
        // 原有功能
        "pomodoro_settings",
        "pomodoro_analytics"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_ai);
        
        initViews();
        initPermissionManager();
        setupSpinner();
        loadPomodoroFunctions();
        setupListeners();
        
        // 在界面创建完成后立即显示调试信息
        new android.os.Handler().postDelayed(() -> {
            showDebugInfo();
        }, 1000); // 延迟1秒显示，确保界面完全加载
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerPomodoroFunctions = findViewById(R.id.recycler_pomodoro_functions);
        spinnerAction = findViewById(R.id.spinner_action);
        etTaskName = findViewById(R.id.edit_task_name);
        etDuration = findViewById(R.id.edit_duration);
        btnExecute = findViewById(R.id.btn_execute);
        tvResult = findViewById(R.id.text_result);
    }
    
    private void initPermissionManager() {
        permissionManager = PomodoroFunctionPermissionManager.getInstance(this);
    }
    
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, actionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(adapter);
    }
    
    private void loadPomodoroFunctions() {
        pomodoroFunctions = new ArrayList<>();
        
        // 基础番茄钟控制功能 - 与actionTypes[0-4]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_START_POMODORO,
            "启动番茄钟",
            "开始新的番茄钟计时",
            R.drawable.ic_timer,  // 计时器图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_START_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_PAUSE_POMODORO,
            "暂停番茄钟",
            "暂停当前运行的番茄钟计时",
            R.drawable.ic_snooze,  // 暂停/休息图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_PAUSE_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_RESUME_POMODORO,
            "恢复番茄钟",
            "恢复暂停的番茄钟计时",
            R.drawable.ic_timer,  // 计时器图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_RESUME_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_STOP_POMODORO,
            "停止番茄钟",
            "停止并重置番茄钟计时",
            R.drawable.ic_close,  // 关闭图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_STOP_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_GET_STATUS,
            "查询状态",
            "获取当前番茄钟运行状态",
            R.drawable.ic_progress,  // 进度/状态图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_GET_STATUS)
        ));
        
        // 番茄钟休息流程控制功能 - 与actionTypes[5-6]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_START_BREAK,
            "开始休息",
            "手动开始番茄钟休息时间",
            R.drawable.ic_snooze,  // 休息图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_START_BREAK)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_SKIP_BREAK,
            "跳过休息",
            "跳过当前休息时间，直接开始下一个番茄钟",
            R.drawable.ic_chevron_right,  // 跳过/前进图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_SKIP_BREAK)
        ));
        
        // 番茄钟完成流程控制功能 - 与actionTypes[7-9]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_COMPLETE_POMODORO,
            "完成番茄钟",
            "强制完成当前番茄钟或跳过休息",
            R.drawable.ic_check,  // 完成/勾选图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_COMPLETE_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_CLOSE_POMODORO,
            "关闭番茄钟",
            "关闭并重置番茄钟计时器",
            R.drawable.ic_close,  // 关闭图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_CLOSE_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_RESET_POMODORO,
            "重置番茄钟",
            "放弃当前番茄钟，重置计时器",
            R.drawable.ic_warning,  // 警告/重置图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_RESET_POMODORO)
        ));
        
        // 番茄钟设置管理功能 - 与actionTypes[10-12]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_SET_POMODORO_SETTINGS,
            "设置番茄钟",
            "配置番茄钟工作时长、休息时长等参数",
            R.drawable.ic_settings,  // 设置图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_SET_POMODORO_SETTINGS)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_SETTINGS,
            "查询设置",
            "查看当前番茄钟配置参数",
            R.drawable.ic_menu,  // 菜单/查询图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_SETTINGS)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_RESET_POMODORO_SETTINGS,
            "重置设置",
            "将番茄钟设置恢复为默认值",
            R.drawable.ic_warning,  // 警告/重置图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_RESET_POMODORO_SETTINGS)
        ));
        
        // 番茄钟历史记录查询功能 - 与actionTypes[13-14]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_HISTORY,
            "查询历史",
            "查看番茄钟使用历史记录",
            R.drawable.ic_calendar,  // 日历/历史图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_HISTORY)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_STATS,
            "统计分析",
            "查看番茄钟使用统计数据",
            R.drawable.ic_statistics,  // 统计图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_GET_POMODORO_STATS)
        ));
        
        // 原有功能 - 与actionTypes[15-16]对应
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_POMODORO_SETTINGS,
            "番茄钟设置",
            "配置番茄钟相关参数",
            R.drawable.ic_settings,  // 设置图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_POMODORO_SETTINGS)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_POMODORO_ANALYTICS,
            "番茄钟分析",
            "分析番茄钟使用数据",
            R.drawable.ic_statistics,  // 统计图标
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_POMODORO_ANALYTICS)
        ));
        
        // 设置适配器
        functionAdapter = new PomodoroFunctionAdapter(pomodoroFunctions, this::onFunctionPermissionChanged);
        recyclerPomodoroFunctions.setLayoutManager(new LinearLayoutManager(this));
        recyclerPomodoroFunctions.setAdapter(functionAdapter);
        
        // 优化RecyclerView设置，确保能显示所有功能
        recyclerPomodoroFunctions.setHasFixedSize(true);
        recyclerPomodoroFunctions.setNestedScrollingEnabled(true);
        
        // 添加调试信息
        Log.d("PomodoroAiActivity", "实际加载的功能数量: " + pomodoroFunctions.size());
        Log.d("PomodoroAiActivity", "操作类型数量: " + actionTypes.length);
        
        // 打印所有功能名称用于调试
        for (int i = 0; i < pomodoroFunctions.size(); i++) {
            PomodoroFunction function = pomodoroFunctions.get(i);
            Log.d("PomodoroAiActivity", "功能[" + i + "]: " + function.getName() + " (ID: " + function.getId() + ")");
        }
        
        // 显示功能数量提示
        if (pomodoroFunctions.size() > 7) {
            Log.i("PomodoroAiActivity", "功能列表包含 " + pomodoroFunctions.size() + " 个功能，请上下滚动查看所有功能");
        }
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnExecute.setOnClickListener(v -> {
            // 添加按钮点击反馈效果
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
            
            // 显示点击反馈Toast
            android.widget.Toast.makeText(this, "正在执行操作...", android.widget.Toast.LENGTH_SHORT).show();
            
            // 执行操作
            executePomodoroAction();
        });
        
        // 添加调试按钮点击事件
        if (btnExecute != null) {
            btnExecute.setOnLongClickListener(v -> {
                // 长按执行按钮显示调试信息
                showDebugInfo();
                return true;
            });
        }
        
        // 根据选择的操作更新UI显示
        spinnerAction.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateUIForSelectedAction(position);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    
    /**
     * 显示调试信息
     */
    private void showDebugInfo() {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== 调试信息 ===\n");
        debugInfo.append("功能列表大小: ").append(pomodoroFunctions.size()).append("\n");
        debugInfo.append("操作类型数量: ").append(actionTypes.length).append("\n");
        debugInfo.append("适配器项目数: ").append(functionAdapter.getItemCount()).append("\n\n");
        
        debugInfo.append("功能列表内容:\n");
        for (int i = 0; i < pomodoroFunctions.size(); i++) {
            PomodoroFunction function = pomodoroFunctions.get(i);
            debugInfo.append(i).append(": ").append(function.getName())
                    .append(" (ID: ").append(function.getId()).append(")")
                    .append(" [").append(function.isEnabled() ? "启用" : "禁用").append("]\n");
        }
        
        debugInfo.append("\n操作类型列表:\n");
        for (int i = 0; i < actionTypes.length; i++) {
            debugInfo.append(i).append(": ").append(actionTypes[i]).append("\n");
        }
        
        tvResult.setText(debugInfo.toString());
        Log.d("PomodoroAiActivity", debugInfo.toString());
    }
    
    private void updateUIForSelectedAction(int position) {
        // 根据选择的操作类型显示/隐藏相关输入框
        switch (position) {
            case 0: // 启动番茄钟
                etTaskName.setVisibility(View.VISIBLE);
                etDuration.setVisibility(View.GONE);  // 启动番茄钟不需要时长参数
                break;
                
            case 1: // 暂停番茄钟
            case 2: // 恢复番茄钟
            case 3: // 停止番茄钟
            case 4: // 查询状态
            case 7: // 完成番茄钟
            case 8: // 关闭番茄钟
            case 9: // 重置番茄钟
            case 11: // 查询设置
            case 12: // 重置设置
            case 14: // 统计分析
            case 15: // 番茄钟设置
            case 16: // 番茄钟分析
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.GONE);
                break;
                
            case 5: // 开始休息
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.VISIBLE);  // 需要休息时长
                break;
                
            case 6: // 跳过休息
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.GONE);
                break;
                
            case 10: // 设置番茄钟
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.VISIBLE);  // 需要工作时长
                break;
                
            case 13: // 查询历史
                etTaskName.setVisibility(View.VISIBLE);  // 可以按任务名称查询
                etDuration.setVisibility(View.VISIBLE);  // 限制查询数量
                break;
                
            default:
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.GONE);
                break;
        }
    }
    
    private void executePomodoroAction() {
        int selectedPosition = spinnerAction.getSelectedItemPosition();
        String selectedAction = actionTypes[selectedPosition];
        String aiCommand = aiCommands[selectedPosition];
        String taskName = etTaskName.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        
        // 显示执行结果
        String result = "执行操作: " + selectedAction;
        
        // 根据操作类型显示相应的参数信息
        switch (selectedPosition) {
            case 0: // 启动番茄钟
                if (!taskName.isEmpty()) {
                    result += "\n任务名称: " + taskName;
                }
                result += "\n时长: 使用当前设置中的番茄钟时长";
                break;
                
            case 5: // 开始休息
                if (!duration.isEmpty()) {
                    result += "\n休息时长: " + duration + " 分钟";
                } else {
                    result += "\n休息时长: 使用默认值 5 分钟";
                }
                break;
                
            case 10: // 设置番茄钟
                if (!duration.isEmpty()) {
                    result += "\n工作时长: " + duration + " 分钟";
                } else {
                    result += "\n工作时长: 使用默认值 25 分钟";
                }
                break;
                
            case 13: // 查询历史
                if (!taskName.isEmpty()) {
                    result += "\n查询任务: " + taskName;
                }
                if (!duration.isEmpty()) {
                    result += "\n查询数量: " + duration + " 条";
                } else {
                    result += "\n查询数量: 默认 10 条";
                }
                break;
                
            default:
                // 其他操作不需要额外参数
                break;
        }
        
        tvResult.setText(result);
        
        // 调用相应的AI功能
        try {
            // 构建参数
            Map<String, Object> args = new HashMap<>();
            
            // 根据操作类型设置不同的参数
            switch (selectedPosition) {
                case 0: // 启动番茄钟
                    if (!taskName.isEmpty()) {
                        args.put("task_name", taskName);
                    }
                    // 启动番茄钟使用当前设置中的时长，不在这里硬编码
                    // 时长会从SettingsRepository中读取当前配置
                    break;
                    
                case 5: // 开始休息
                    if (!duration.isEmpty()) {
                        try {
                            int breakDuration = Integer.parseInt(duration);
                            args.put("action", "start");
                            args.put("break_duration", breakDuration);
                        } catch (NumberFormatException e) {
                            args.put("action", "start");
                            args.put("break_duration", 5); // 默认5分钟休息
                        }
                    } else {
                        args.put("action", "start");
                        args.put("break_duration", 5);
                    }
                    break;
                    
                case 6: // 跳过休息
                    args.put("action", "skip");
                    break;
                    
                case 7: // 完成番茄钟
                    args.put("action", "complete");
                    break;
                    
                case 8: // 关闭番茄钟
                    args.put("action", "close");
                    break;
                    
                case 9: // 重置番茄钟
                    args.put("action", "reset");
                    break;
                    
                case 10: // 设置番茄钟
                    if (!duration.isEmpty()) {
                        try {
                            int durationValue = Integer.parseInt(duration);
                            args.put("action", "set");
                            args.put("tomato_duration", durationValue);
                        } catch (NumberFormatException e) {
                            args.put("action", "set");
                            args.put("tomato_duration", 25);
                        }
                    } else {
                        args.put("action", "set");
                        args.put("tomato_duration", 25);
                    }
                    break;
                    
                case 11: // 查询设置
                    args.put("action", "get");
                    break;
                    
                case 12: // 重置设置
                    args.put("action", "reset");
                    break;
                    
                case 13: // 查询历史
                    if (!taskName.isEmpty()) {
                        args.put("action", "task");
                        args.put("task_name", taskName);
                    } else if (!duration.isEmpty()) {
                        try {
                            int limit = Integer.parseInt(duration);
                            args.put("action", "recent");
                            args.put("limit", limit);
                        } catch (NumberFormatException e) {
                            args.put("action", "recent");
                            args.put("limit", 10);
                        }
                    } else {
                        args.put("action", "recent");
                        args.put("limit", 10);
                    }
                    break;
                    
                case 14: // 统计分析
                    args.put("action", "stats");
                    break;
                    
                default:
                    // 其他操作不需要额外参数
                    break;
            }
            
            // 执行AI命令
            com.fourquadrant.ai.CommandRouter.ExecutionResult executionResult = 
                com.fourquadrant.ai.CommandRouter.executeCommand(aiCommand, args);
            
            // 显示执行结果
            if (executionResult.isSuccess()) {
                result += "\n\n✅ 执行成功: " + executionResult.getMessage();
                
                // 显示成功Toast
                android.widget.Toast.makeText(this, 
                    "✅ " + selectedAction + " 执行成功!", 
                    android.widget.Toast.LENGTH_LONG).show();
                    
                // 成功时的特殊提示
                if (selectedPosition == 0) { // 启动番茄钟
                    android.widget.Toast.makeText(this, 
                        "🍅 番茄钟已启动，请查看番茄钟页面", 
                        android.widget.Toast.LENGTH_LONG).show();
                }
            } else {
                result += "\n\n❌ 执行失败: " + executionResult.getMessage();
                
                // 显示失败Toast
                android.widget.Toast.makeText(this, 
                    "❌ " + selectedAction + " 执行失败: " + executionResult.getMessage(), 
                    android.widget.Toast.LENGTH_LONG).show();
            }
            
            // 添加执行状态提示
            String statusMessage = "正在执行: " + selectedAction;
            if (selectedPosition == 0) { // 启动番茄钟
                statusMessage += "\n请稍候，番茄钟正在启动...";
            }
            
            // 合并所有信息并显示
            String finalResult = result + "\n\n" + statusMessage;
            tvResult.setText(finalResult);
            
            // 确保结果卡片可见并添加动画效果
            android.view.View resultCard = findViewById(R.id.card_result);
            if (resultCard != null) {
                resultCard.setVisibility(android.view.View.VISIBLE);
                
                // 添加淡入动画效果
                resultCard.setAlpha(0f);
                resultCard.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            }
            
        } catch (Exception e) {
            String errorResult = result + "\n\n执行出错: " + e.getMessage();
            tvResult.setText(errorResult);
            
            // 显示错误Toast
            android.widget.Toast.makeText(this, 
                "⚠️ 执行出错: " + e.getMessage(), 
                android.widget.Toast.LENGTH_LONG).show();
                
            // 确保结果卡片可见
            android.view.View resultCard = findViewById(R.id.card_result);
            if (resultCard != null) {
                resultCard.setVisibility(android.view.View.VISIBLE);
            }
        }
    }
    
    private void onFunctionPermissionChanged(PomodoroFunction function, boolean enabled) {
        // 更新权限管理器
        permissionManager.setFunctionEnabled(function.getId(), enabled);
        
        // 显示权限变更提示
        String message = function.getName() + (enabled ? " 已启用" : " 已禁用");
        tvResult.setText(message);
    }
}