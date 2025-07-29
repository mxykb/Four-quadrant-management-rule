package com.example.fourquadrant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UserFragment extends Fragment {
    
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIO = "bio";
    
    private EditText usernameEdit;
    private EditText emailEdit;
    private EditText bioEdit;
    private Button saveButton;
    private Button resetButton;
    private TextView statsText;
    
    private SharedPreferences preferences;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        
        initViews(view);
        setupPreferences();
        loadUserData();
        setupButtons();
        updateStatistics();
        
        return view;
    }
    
    private void initViews(View view) {
        usernameEdit = view.findViewById(R.id.username_edit);
        emailEdit = view.findViewById(R.id.email_edit);
        bioEdit = view.findViewById(R.id.bio_edit);
        saveButton = view.findViewById(R.id.save_button);
        resetButton = view.findViewById(R.id.reset_button);
        statsText = view.findViewById(R.id.stats_text);
    }
    
    private void setupPreferences() {
        preferences = requireContext().getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE);
    }
    
    private void loadUserData() {
        String username = preferences.getString(KEY_USERNAME, "");
        String email = preferences.getString(KEY_EMAIL, "");
        String bio = preferences.getString(KEY_BIO, "");
        
        usernameEdit.setText(username);
        emailEdit.setText(email);
        bioEdit.setText(bio);
    }
    
    private void setupButtons() {
        saveButton.setOnClickListener(v -> saveUserData());
        resetButton.setOnClickListener(v -> resetUserData());
    }
    
    private void saveUserData() {
        String username = usernameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String bio = bioEdit.getText().toString().trim();
        
        // 验证输入
        if (username.isEmpty()) {
            Toast.makeText(getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 保存数据
        preferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_BIO, bio)
                .apply();
        
        Toast.makeText(getContext(), "用户信息已保存", Toast.LENGTH_SHORT).show();
        updateStatistics();
    }
    
    private void resetUserData() {
        usernameEdit.setText("");
        emailEdit.setText("");
        bioEdit.setText("");
        
        preferences.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_EMAIL)
                .remove(KEY_BIO)
                .apply();
        
        Toast.makeText(getContext(), "用户信息已重置", Toast.LENGTH_SHORT).show();
        updateStatistics();
    }
    
    private void updateStatistics() {
        // 获取任务统计信息
        SharedPreferences taskPrefs = requireContext().getSharedPreferences("task_preferences", android.content.Context.MODE_PRIVATE);
        String tasksJson = taskPrefs.getString("tasks", "[]");
        
        // 简单统计（这里可以根据实际需要扩展）
        int totalTasks = 0;
        int completedTasks = 0;
        
        try {
            // 这里可以解析JSON来获取准确的任务数量
            // 为了简化，我们暂时显示基本信息
            if (!tasksJson.equals("[]")) {
                totalTasks = tasksJson.split("\"name\"").length - 1; // 粗略估算
                completedTasks = tasksJson.split("\"completed\":true").length - 1;
            }
        } catch (Exception e) {
            // 处理异常
        }
        
        String username = preferences.getString(KEY_USERNAME, "未设置");
        String statsInfo = String.format(
            "👤 用户：%s\n" +
            "📋 总任务数：%d\n" +
            "✅ 已完成：%d\n" +
            "⏱️ 使用时长：今日活跃\n" +
            "🎯 完成率：%.1f%%",
            username,
            totalTasks,
            completedTasks,
            totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0
        );
        
        statsText.setText(statsInfo);
    }
} 