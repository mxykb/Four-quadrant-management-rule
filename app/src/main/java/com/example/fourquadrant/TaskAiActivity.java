package com.example.fourquadrant;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.adapter.TaskFunctionAdapter;
import com.example.fourquadrant.model.TaskFunction;
import com.example.fourquadrant.utils.ModulePermissionManager;
import com.example.fourquadrant.utils.TaskFunctionPermissionManager;
import com.fourquadrant.ai.AiExecutable;
import com.fourquadrant.ai.CommandRouter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务AI功能测试界面
 * 提供任务管理AI功能的参数配置和执行测试
 */
public class TaskAiActivity extends AppCompatActivity {
    
    // UI组件
    private ImageView btnBack;
    private RecyclerView recyclerTaskFunctions;
    private Spinner spinnerAction;
    private TextInputLayout layoutTaskName, layoutTaskId, layoutViewType;
    private TextInputEditText editTaskName, editTaskId, editViewType;
    private LinearLayout layoutImportance, layoutUrgency;
    private SeekBar seekbarImportance, seekbarUrgency;
    private TextView textImportanceValue, textUrgencyValue;
    private Button btnExecute;
    private CardView cardResult;
    private TextView textResult;
    
    // 任务功能权限相关
    private TaskFunctionAdapter functionAdapter;
    private List<TaskFunction> functionList;
    private TaskFunctionPermissionManager permissionManager;
    
    // 操作类型数组
    private final String[] actionTypes = {
        "create - 创建任务",
        "view - 查看任务", 
        "update - 更新任务",
        "delete - 删除任务",
        "complete - 完成任务"
    };
    
    private String selectedAction = "create";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_ai);
        
        initViews();
        setupListeners();
        setupSpinner();
        setupFunctionList();
        updateUIForAction(selectedAction);
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerTaskFunctions = findViewById(R.id.recycler_task_functions);
        spinnerAction = findViewById(R.id.spinner_action);
        layoutTaskName = findViewById(R.id.layout_task_name);
        layoutTaskId = findViewById(R.id.layout_task_id);
        layoutViewType = findViewById(R.id.layout_view_type);
        editTaskName = findViewById(R.id.edit_task_name);
        editTaskId = findViewById(R.id.edit_task_id);
        editViewType = findViewById(R.id.edit_view_type);
        layoutImportance = findViewById(R.id.layout_importance);
        layoutUrgency = findViewById(R.id.layout_urgency);
        seekbarImportance = findViewById(R.id.seekbar_importance);
        seekbarUrgency = findViewById(R.id.seekbar_urgency);
        textImportanceValue = findViewById(R.id.text_importance_value);
        textUrgencyValue = findViewById(R.id.text_urgency_value);
        btnExecute = findViewById(R.id.btn_execute);
        cardResult = findViewById(R.id.card_result);
        textResult = findViewById(R.id.text_result);
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 重要性滑块
        seekbarImportance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textImportanceValue.setText(String.valueOf(progress + 1));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 紧急性滑块
        seekbarUrgency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textUrgencyValue.setText(String.valueOf(progress + 1));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 执行按钮
        btnExecute.setOnClickListener(v -> executeTaskAi());
    }
    
    /**
     * 设置操作类型下拉框
     */
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, actionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(adapter);
        
        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = actionTypes[position];
                selectedAction = selected.split(" - ")[0];
                updateUIForAction(selectedAction);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    /**
     * 根据选择的操作类型更新UI显示
     */
    private void updateUIForAction(String action) {
        // 隐藏所有可选字段
        layoutTaskName.setVisibility(View.GONE);
        layoutTaskId.setVisibility(View.GONE);
        layoutViewType.setVisibility(View.GONE);
        layoutImportance.setVisibility(View.GONE);
        layoutUrgency.setVisibility(View.GONE);
        
        switch (action) {
            case "create":
                layoutTaskName.setVisibility(View.VISIBLE);
                layoutImportance.setVisibility(View.VISIBLE);
                layoutUrgency.setVisibility(View.VISIBLE);
                break;
            case "view":
                layoutViewType.setVisibility(View.VISIBLE);
                editViewType.setText("active"); // 设置默认值
                break;
            case "update":
                layoutTaskId.setVisibility(View.VISIBLE);
                layoutTaskName.setVisibility(View.VISIBLE);
                layoutImportance.setVisibility(View.VISIBLE);
                layoutUrgency.setVisibility(View.VISIBLE);
                break;
            case "delete":
            case "complete":
                layoutTaskId.setVisibility(View.VISIBLE);
                break;
        }
    }
    
    /**
     * 执行任务AI功能
     */
    private void executeTaskAi() {
        try {
            // 检查模块权限
            ModulePermissionManager modulePermissionManager = ModulePermissionManager.getInstance(this);
            if (!modulePermissionManager.isModuleEnabled("task_ai")) {
                showError("任务AI模块已被禁用，请在主页权限管理中启用");
                return;
            }
            
            // 检查功能权限
            String functionId = getFunctionIdByAction(selectedAction);
            if (!permissionManager.isFunctionEnabled(functionId)) {
                showError("该功能已被禁用，请在权限管理中启用");
                return;
            }
            
            // 构建参数映射
            Map<String, Object> args = new HashMap<>();
            args.put("action", selectedAction);
            
            // 根据操作类型添加相应参数
            switch (selectedAction) {
                case "create":
                    String taskName = getTextFromEditText(editTaskName);
                    if (taskName.isEmpty()) {
                        showError("请输入任务名称");
                        return;
                    }
                    args.put("task_name", taskName);
                    args.put("importance", seekbarImportance.getProgress() + 1);
                    args.put("urgency", seekbarUrgency.getProgress() + 1);
                    break;
                    
                case "view":
                    String viewType = getTextFromEditText(editViewType);
                    if (!viewType.isEmpty()) {
                        args.put("type", viewType);
                    }
                    break;
                    
                case "update":
                    String updateTaskId = getTextFromEditText(editTaskId);
                    if (updateTaskId.isEmpty()) {
                        showError("请输入任务ID");
                        return;
                    }
                    args.put("task_id", updateTaskId);
                    
                    String updateTaskName = getTextFromEditText(editTaskName);
                    if (!updateTaskName.isEmpty()) {
                        args.put("task_name", updateTaskName);
                    }
                    args.put("importance", seekbarImportance.getProgress() + 1);
                    args.put("urgency", seekbarUrgency.getProgress() + 1);
                    break;
                    
                case "delete":
                case "complete":
                    String taskId = getTextFromEditText(editTaskId);
                    if (taskId.isEmpty()) {
                        showError("请输入任务ID");
                        return;
                    }
                    args.put("task_id", taskId);
                    break;
            }
            
            // 执行命令
            CommandRouter.ExecutionResult result = CommandRouter.executeCommand("task_management", args);
            
            // 显示结果
            showResult(result);
            
        } catch (Exception e) {
            showError("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 从EditText获取文本内容
     */
    private String getTextFromEditText(TextInputEditText editText) {
        if (editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }
    
    /**
     * 显示执行结果
     */
    private void showResult(CommandRouter.ExecutionResult result) {
        cardResult.setVisibility(View.VISIBLE);
        
        String resultText = "操作: " + selectedAction + "\n";
        resultText += "状态: " + (result.isSuccess() ? "成功" : "失败") + "\n";
        resultText += "消息: " + result.getMessage();
        
        textResult.setText(resultText);
        
        // 滚动到结果区域
        cardResult.post(() -> {
            int[] location = new int[2];
            cardResult.getLocationOnScreen(location);
            // 这里可以添加滚动逻辑
        });
    }
    
    /**
     * 设置任务功能权限列表
     */
    private void setupFunctionList() {
        // 初始化权限管理器
        permissionManager = TaskFunctionPermissionManager.getInstance(this);
        
        // 初始化功能列表
        functionList = new ArrayList<>();
        loadTaskFunctions();
        
        // 设置RecyclerView
        functionAdapter = new TaskFunctionAdapter(functionList);
        recyclerTaskFunctions.setLayoutManager(new LinearLayoutManager(this));
        recyclerTaskFunctions.setAdapter(functionAdapter);
        
        // 设置权限切换监听器
        functionAdapter.setOnFunctionPermissionChangeListener((function, enabled) -> {
            permissionManager.setFunctionEnabled(function.getId(), enabled);
            showResult(CommandRouter.ExecutionResult.success(
                "功能 " + function.getName() + " 已" + (enabled ? "启用" : "禁用")));
        });
    }
    
    /**
     * 加载任务AI功能
     */
    private void loadTaskFunctions() {
        functionList.clear();
        
        // 创建任务
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_CREATE_TASK,
            "创建任务",
            "允许创建新的任务项目",
            R.drawable.ic_add,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_CREATE_TASK)
        ));
        
        // 查看任务
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_VIEW_TASK,
            "查看任务",
            "允许查看现有任务详情",
            R.drawable.ic_task,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_VIEW_TASK)
        ));
        
        // 编辑任务
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_EDIT_TASK,
            "编辑任务",
            "允许修改任务内容和属性",
            R.drawable.ic_edit,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_EDIT_TASK)
        ));
        
        // 删除任务
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_DELETE_TASK,
            "删除任务",
            "允许删除不需要的任务",
            R.drawable.ic_delete,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_DELETE_TASK)
        ));
        
        // 任务分析
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_TASK_ANALYSIS,
            "任务分析",
            "提供任务完成情况的智能分析",
            R.drawable.ic_statistics,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_TASK_ANALYSIS)
        ));
        
        // 任务提醒
        functionList.add(new TaskFunction(
            TaskFunctionPermissionManager.FUNCTION_TASK_REMINDER,
            "任务提醒",
            "设置和管理任务提醒通知",
            R.drawable.ic_notification,
            permissionManager.isFunctionEnabled(TaskFunctionPermissionManager.FUNCTION_TASK_REMINDER)
        ));
    }
    
    /**
     * 根据操作类型获取对应的功能ID
     */
    private String getFunctionIdByAction(String action) {
        switch (action) {
            case "create":
                return TaskFunctionPermissionManager.FUNCTION_CREATE_TASK;
            case "view":
                return TaskFunctionPermissionManager.FUNCTION_VIEW_TASK;
            case "update":
                return TaskFunctionPermissionManager.FUNCTION_EDIT_TASK;
            case "delete":
                return TaskFunctionPermissionManager.FUNCTION_DELETE_TASK;
            case "complete":
                return TaskFunctionPermissionManager.FUNCTION_EDIT_TASK; // 完成任务也属于编辑功能
            default:
                return TaskFunctionPermissionManager.FUNCTION_VIEW_TASK; // 默认为查看权限
        }
    }
 
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}