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

import com.fourquadrant.ai.CommandRouter;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能工具Fragment
 * 提供AI功能的统一入口和管理界面
 */
public class AiToolsFragment extends Fragment {
    
    private TextView tvWelcome;
    private TextView tvDescription;
    private CardView cardPomodoroAi;
    private CardView cardStatisticsAi;
    private CardView cardTaskAi;
    private CardView cardSettingsAi;
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
        setupClickListeners();
        initializeAiSystem();
    }
    
    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvDescription = view.findViewById(R.id.tv_description);
        cardPomodoroAi = view.findViewById(R.id.card_pomodoro_ai);
        cardStatisticsAi = view.findViewById(R.id.card_statistics_ai);
        cardTaskAi = view.findViewById(R.id.card_task_ai);
        cardSettingsAi = view.findViewById(R.id.card_settings_ai);
        btnTestAi = view.findViewById(R.id.btn_test_ai);
        tvStatus = view.findViewById(R.id.tv_status);
    }
    
    private void setupClickListeners() {
        // 番茄钟AI功能
        cardPomodoroAi.setOnClickListener(v -> {
            showComingSoonToast("番茄钟AI助手");
            // TODO: 实现番茄钟AI功能
            // 例如：智能推荐番茄钟时长、分析专注模式等
        });
        
        // 统计AI功能
        cardStatisticsAi.setOnClickListener(v -> {
            showComingSoonToast("统计AI分析");
            // TODO: 实现统计AI功能
            // 例如：智能数据分析、趋势预测、个性化建议等
        });
        
        // 任务AI功能
        cardTaskAi.setOnClickListener(v -> {
            // 启动任务AI界面
            Intent intent = new Intent(getContext(), TaskAiActivity.class);
            startActivity(intent);
        });
        
        // 设置AI功能
        cardSettingsAi.setOnClickListener(v -> {
            showComingSoonToast("设置AI优化");
            // TODO: 实现设置AI功能
            // 例如：个性化设置推荐、使用习惯分析等
        });
        
        // 测试AI系统
        btnTestAi.setOnClickListener(v -> {
            testAiSystem();
        });
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
        
        updateStatus("正在测试AI系统...");
        
        // 测试启动番茄钟功能
        Map<String, Object> args = new HashMap<>();
        args.put("duration", 25);
        
        CommandRouter.ExecutionResult result = CommandRouter.executeCommand("start_pomodoro", args);
        
        if (result.isSuccess()) {
            updateStatus("✅ AI系统测试成功：" + result.getMessage());
            Toast.makeText(getContext(), "AI系统运行正常", Toast.LENGTH_SHORT).show();
        } else {
            updateStatus("❌ AI系统测试失败：" + result.getMessage());
            Toast.makeText(getContext(), "AI系统测试失败", Toast.LENGTH_SHORT).show();
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