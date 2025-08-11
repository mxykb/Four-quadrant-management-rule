package com.example.fourquadrant;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
    
    private com.example.fourquadrant.database.repository.UserRepository userRepository;
    private com.example.fourquadrant.database.repository.TaskRepository taskRepository;
    private com.example.fourquadrant.database.repository.PomodoroRepository pomodoroRepository;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        
        initViews(view);
        initRepositories();
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
    
    private void initRepositories() {
        userRepository = new com.example.fourquadrant.database.repository.UserRepository(requireActivity().getApplication());
        taskRepository = new com.example.fourquadrant.database.repository.TaskRepository(requireActivity().getApplication());
        pomodoroRepository = new com.example.fourquadrant.database.repository.PomodoroRepository(requireActivity().getApplication());
    }
    
    private void loadUserData() {
        if (userRepository != null) {
            LiveData<com.example.fourquadrant.database.entity.UserEntity> userLiveData = userRepository.getCurrentUser();
            userLiveData.observe(getViewLifecycleOwner(), new Observer<com.example.fourquadrant.database.entity.UserEntity>() {
                @Override
                public void onChanged(com.example.fourquadrant.database.entity.UserEntity user) {
                    if (user != null) {
                        usernameEdit.setText(user.getUsername());
                        emailEdit.setText(user.getEmail());
                        bioEdit.setText(user.getBio());
                    } else {
                        // 用户不存在，设置默认值
                        usernameEdit.setText("");
                        emailEdit.setText("");
                        bioEdit.setText("");
                    }
                    userLiveData.removeObserver(this);
                }
            });
        }
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
        
        // 保存数据到数据库
        if (userRepository != null) {
            com.example.fourquadrant.database.entity.UserEntity user = new com.example.fourquadrant.database.entity.UserEntity();
            user.setUsername(username);
            user.setEmail(email);
            user.setBio(bio);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            
            userRepository.insertOrUpdateUser(user);
        }
        
        Toast.makeText(getContext(), "用户信息已保存", Toast.LENGTH_SHORT).show();
        updateStatistics();
    }
    
    private void resetUserData() {
        usernameEdit.setText("");
        emailEdit.setText("");
        bioEdit.setText("");
        
        if (userRepository != null) {
            userRepository.deleteAllUsers();
        }
        
        Toast.makeText(getContext(), "用户信息已重置", Toast.LENGTH_SHORT).show();
        updateStatistics();
    }
    
    private void updateStatistics() {
        // 使用数据库获取统计信息
        if (taskRepository != null && pomodoroRepository != null) {
            LiveData<Integer> totalTasksLiveData = taskRepository.getTotalTaskCount();
            LiveData<Integer> completedTasksLiveData = taskRepository.getCompletedTaskCount();
            LiveData<Integer> pomodoroCountLiveData = pomodoroRepository.getTotalPomodoroCount();
            
            // 观察总任务数
            totalTasksLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer totalTasks) {
                    if (totalTasks == null) totalTasks = 0;
                    final int finalTotalTasks = totalTasks;
                    
                    // 观察已完成任务数
                    completedTasksLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer completedTasks) {
                            if (completedTasks == null) completedTasks = 0;
                            final int finalCompletedTasks = completedTasks;
                            
                            // 观察番茄钟数量
                            pomodoroCountLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                                @Override
                                public void onChanged(Integer pomodoroCount) {
                                    if (pomodoroCount == null) pomodoroCount = 0;
                                    
                                    String statsInfo = String.format(
                                        "📋 总任务数：%d\n" +
                                        "✅ 已完成：%d\n" +
                                        "🎯 完成率：%.1f%%\n" +
                                        "🍅 番茄钟：%d个",
                                        finalTotalTasks,
                                        finalCompletedTasks,
                                        finalTotalTasks > 0 ? (finalCompletedTasks * 100.0 / finalTotalTasks) : 0.0,
                                        pomodoroCount
                                    );
                                    
                                    statsText.setText(statsInfo);
                                    
                                    // 移除观察者防止内存泄漏
                                    pomodoroCountLiveData.removeObserver(this);
                                    completedTasksLiveData.removeObserver(this);
                                    totalTasksLiveData.removeObserver(this);
                                }
                            });
                        }
                    });
                }
            });
        } else {
            // 数据库未初始化，显示默认信息
            statsText.setText("📊 使用统计：\n暂无数据");
        }
    }
} 