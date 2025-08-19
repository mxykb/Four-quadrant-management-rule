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
    
    // 操作类型数组
    private String[] actionTypes = {
        "启动番茄钟",
        "暂停番茄钟", 
        "恢复番茄钟",
        "停止番茄钟",
        "查询状态"
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
        
        // 添加番茄钟AI功能项
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_PAUSE_POMODORO,
            "暂停番茄钟",
            "暂停当前运行的番茄钟计时",
            R.drawable.ic_snooze,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_PAUSE_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_RESUME_POMODORO,
            "恢复番茄钟",
            "恢复暂停的番茄钟计时",
            R.drawable.ic_timer,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_RESUME_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_STOP_POMODORO,
            "停止番茄钟",
            "停止并重置番茄钟计时",
            R.drawable.ic_close,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_STOP_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_GET_STATUS,
            "查询状态",
            "获取当前番茄钟运行状态",
            R.drawable.ic_progress,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_GET_STATUS)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_START_POMODORO,
            "启动番茄钟",
            "开始新的番茄钟计时",
            R.drawable.ic_timer,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_START_POMODORO)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_POMODORO_SETTINGS,
            "番茄钟设置",
            "配置番茄钟相关参数",
            R.drawable.ic_settings,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_POMODORO_SETTINGS)
        ));
        
        pomodoroFunctions.add(new PomodoroFunction(
            PomodoroFunctionPermissionManager.FUNCTION_POMODORO_ANALYTICS,
            "番茄钟分析",
            "分析番茄钟使用数据",
            R.drawable.ic_statistics,
            permissionManager.isFunctionEnabled(PomodoroFunctionPermissionManager.FUNCTION_POMODORO_ANALYTICS)
        ));
        
        // 设置适配器
        functionAdapter = new PomodoroFunctionAdapter(pomodoroFunctions, this::onFunctionPermissionChanged);
        recyclerPomodoroFunctions.setLayoutManager(new LinearLayoutManager(this));
        recyclerPomodoroFunctions.setAdapter(functionAdapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnExecute.setOnClickListener(v -> executePomodoroAction());
        
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
    
    private void updateUIForSelectedAction(int position) {
        // 根据选择的操作类型显示/隐藏相关输入框
        switch (position) {
            case 0: // 启动番茄钟
                etTaskName.setVisibility(View.VISIBLE);
                etDuration.setVisibility(View.VISIBLE);
                break;
            case 1: // 暂停番茄钟
            case 2: // 恢复番茄钟
            case 3: // 停止番茄钟
            case 4: // 查询状态
                etTaskName.setVisibility(View.GONE);
                etDuration.setVisibility(View.GONE);
                break;
        }
    }
    
    private void executePomodoroAction() {
        String selectedAction = actionTypes[spinnerAction.getSelectedItemPosition()];
        String taskName = etTaskName.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        
        // 显示执行结果
        String result = "执行操作: " + selectedAction;
        if (!taskName.isEmpty()) {
            result += "\n任务名称: " + taskName;
        }
        if (!duration.isEmpty()) {
            result += "\n时长: " + duration + " 分钟";
        }
        
        tvResult.setText(result);
        
        // TODO: 实际调用番茄钟AI功能
        // 这里可以集成实际的AI功能调用逻辑
    }
    
    private void onFunctionPermissionChanged(PomodoroFunction function, boolean enabled) {
        // 更新权限管理器
        permissionManager.setFunctionEnabled(function.getId(), enabled);
        
        // 显示权限变更提示
        String message = function.getName() + (enabled ? " 已启用" : " 已禁用");
        tvResult.setText(message);
    }
}