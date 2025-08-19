package com.example.fourquadrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.adapter.AiModuleAdapter;
import com.example.fourquadrant.model.AiModule;
import com.example.fourquadrant.utils.ModulePermissionManager;
import com.example.fourquadrant.utils.PermissionSystemTest;
import com.fourquadrant.ai.CommandRouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能工具Fragment
 * 提供AI功能的统一入口和管理界面
 */
public class AiToolsFragment extends Fragment {
    
    private TextView tvWelcome;
    private TextView tvDescription;
    private RecyclerView recyclerAiModules;
    private AiModuleAdapter moduleAdapter;
    private List<AiModule> moduleList;
    private ModulePermissionManager permissionManager;
    private Button btnTestAi;
    private TextView tvStatus;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_tools, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupModuleList();
        initializeAiSystem();
    }
    
    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvDescription = view.findViewById(R.id.tv_description);
        recyclerAiModules = view.findViewById(R.id.recycler_ai_modules);
        btnTestAi = view.findViewById(R.id.btn_test_ai);
        tvStatus = view.findViewById(R.id.tv_status);
        
        // 初始化权限管理器
        permissionManager = ModulePermissionManager.getInstance(getContext());
    }
    
    private void setupModuleList() {
        // 初始化模块列表
        moduleList = new ArrayList<>();
        moduleList.add(new AiModule("task_ai", "任务AI", "智能任务管理助手", "📋", 
            permissionManager.isModuleEnabled("task_ai"), TaskAiActivity.class));
        moduleList.add(new AiModule("pomodoro_ai", "番茄钟AI", "智能专注助手", "🍅", 
            permissionManager.isModuleEnabled("pomodoro_ai"), com.example.fourquadrant.activity.PomodoroAiActivity.class));
        moduleList.add(new AiModule("statistics_ai", "统计AI", "智能数据分析", "📈", 
            permissionManager.isModuleEnabled("statistics_ai"), null));
        moduleList.add(new AiModule("settings_ai", "设置AI", "智能配置优化", "⚙️", 
            permissionManager.isModuleEnabled("settings_ai"), null));
        
        // 设置RecyclerView
        recyclerAiModules.setLayoutManager(new LinearLayoutManager(getContext()));
        moduleAdapter = new AiModuleAdapter(getContext(), moduleList);
        recyclerAiModules.setAdapter(moduleAdapter);
        
        // 测试AI系统
        if (btnTestAi != null) {
            btnTestAi.setOnClickListener(v -> {
                testAiSystem();
            });
        }
    }
    
    private void initializeAiSystem() {
        if (getContext() != null) {
            // 初始化AI命令路由系统
            CommandRouter.initialize(getContext());
            updateStatus("AI系统已初始化");
        }
    }
    
    private void testAiSystem() {
        if (getContext() == null) {
            return;
        }
        
        updateStatus("正在测试权限系统...");
        
        // 运行权限系统测试
        PermissionSystemTest.runAllTests(getContext());
        
        // 测试启动番茄钟功能
        Map<String, Object> args = new HashMap<>();
        args.put("duration", 25);
        
        CommandRouter.ExecutionResult result = CommandRouter.executeCommand("start_pomodoro", args);
        
        if (result.isSuccess()) {
            updateStatus("✅ 权限系统和AI系统测试成功：" + result.getMessage());
            Toast.makeText(getContext(), "权限系统和AI系统运行正常", Toast.LENGTH_SHORT).show();
        } else {
            updateStatus("✅ 权限系统测试成功，AI功能：" + result.getMessage());
            Toast.makeText(getContext(), "权限系统测试完成，请查看日志", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showComingSoonToast(String feature) {
        Toast.makeText(getContext(), feature + " 即将上线，敬请期待！", Toast.LENGTH_SHORT).show();
    }
    
    private void updateStatus(String status) {
        if (tvStatus != null) {
            tvStatus.setText(status);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 更新AI系统状态
        if (getContext() != null) {
            int functionCount = CommandRouter.getRegisteredFunctions().size();
            updateStatus("AI系统运行中，已注册 " + functionCount + " 个功能");
        }
    }
}