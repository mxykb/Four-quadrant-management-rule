package com.example.fourquadrant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;

public class TomatoFragment extends Fragment implements IconPickerDialog.IconSelectedListener, TaskListFragment.TaskListListener {
    private TextView timerText;
    private Button startButton;
    private Button resumeButton;
    private Button pauseButton;
    private Button abandonButton;
    private Button sunButton;
    private Button reminderButton;
    private Button settingsButton;
    private Spinner taskSpinner;
    private TextView taskText;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isTimerPaused = false;
    private long remainingTime = 25 * 60 * 1000;
    
    // 番茄钟状态
    private boolean isBreakTime = false;
    private int currentTomatoCount = 0;
    private int totalTomatoCount = 4;
    
    // SharedPreferences相关
    private SharedPreferences prefs;
    private static final String PREF_NAME = "TomatoTimer";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_IS_PAUSED = "is_paused";
    private static final String KEY_REMAINING_TIME = "remaining_time";
    private static final String KEY_SELECTED_ICON = "selected_icon";
    private static final String KEY_IS_BREAK = "is_break";
    private static final String KEY_CURRENT_COUNT = "current_count";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tomato, container, false);
        
        timerText = view.findViewById(R.id.timer_text);
        startButton = view.findViewById(R.id.btn_start);
        resumeButton = view.findViewById(R.id.btn_resume);
        pauseButton = view.findViewById(R.id.btn_pause);
        abandonButton = view.findViewById(R.id.btn_abandon);
        sunButton = view.findViewById(R.id.btn_sun);
        reminderButton = view.findViewById(R.id.btn_reminder);
        settingsButton = view.findViewById(R.id.btn_tomato_setting);
        taskSpinner = view.findViewById(R.id.task_spinner);
        taskText = view.findViewById(R.id.task_text);
        
        // 初始化SharedPreferences
        prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        setupButtons();
        setupTaskSpinner();
        loadSavedIcon();
        
        // 检查是否需要恢复倒计时
        restoreTimerState();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 注册为任务监听器
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // 找到TaskListFragment并注册监听器
            for (androidx.fragment.app.Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof TaskListFragment) {
                    TaskListFragment taskListFragment = (TaskListFragment) fragment;
                    taskListFragment.addTaskListListener(this);
                    break;
                }
            }
        }
    }

    @Override
    public void onTasksUpdated(List<QuadrantView.Task> tasks) {
        // 任务更新时刷新下拉框
        if (taskSpinner != null) {
            setupTaskSpinner();
        }
    }

    private void setupButtons() {
        startButton.setOnClickListener(v -> {
            startTimer();
        });

        resumeButton.setOnClickListener(v -> {
            resumeTimer();
        });

        pauseButton.setOnClickListener(v -> {
            pauseTimer();
        });

        abandonButton.setOnClickListener(v -> {
            abandonTimer();
        });

        sunButton.setOnClickListener(v -> {
            showIconPicker();
        });

        reminderButton.setOnClickListener(v -> {
            showReminderSettings();
        });

        settingsButton.setOnClickListener(v -> {
            showTomatoSettings();
        });
    }

    private void setupTaskSpinner() {
        List<String> taskNames = getTaskNamesFromTaskManager();
        
        if (taskNames.isEmpty()) {
            taskNames.add("无任务可选");
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, taskNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(adapter);
    }
    
    // 显示任务选择下拉框（初始状态和结束后）
    private void showTaskSpinner() {
        taskSpinner.setVisibility(View.VISIBLE);
        taskText.setVisibility(View.GONE);
    }
    
    // 显示任务文本框（倒计时期间）
    private void showTaskText(String text) {
        taskSpinner.setVisibility(View.GONE);
        taskText.setVisibility(View.VISIBLE);
        taskText.setText(text);
    }
    
    // 获取当前选中的任务名称
    private String getSelectedTaskName() {
        if (taskSpinner.getSelectedItem() != null) {
            return taskSpinner.getSelectedItem().toString();
        }
        return "无任务";
    }
    
    // 更新任务显示（根据当前状态显示不同内容）
    private void updateTaskDisplay() {
        if (isTimerRunning || isTimerPaused) {
            // 倒计时期间，显示文本框
            if (isBreakTime) {
                showTaskText("休息");
            } else {
                showTaskText(getSelectedTaskName());
            }
        } else {
            // 非倒计时期间，显示下拉框
            showTaskSpinner();
        }
    }

    private List<String> getTaskNamesFromTaskManager() {
        List<String> taskNames = new ArrayList<>();
        
        if (getActivity() instanceof MainActivity) {
            // 遍历所有Fragment找到TaskListFragment
            for (androidx.fragment.app.Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof TaskListFragment) {
                    TaskListFragment taskListFragment = (TaskListFragment) fragment;
                    List<TaskListFragment.TaskItem> activeTasks = taskListFragment.getActiveTasks();
                    
                    for (TaskListFragment.TaskItem task : activeTasks) {
                        if (task != null && task.getName() != null && !task.getName().trim().isEmpty()) {
                            taskNames.add(task.getName());
                        }
                    }
                    break;
                }
            }
        }
        
        return taskNames;
    }

    private void showIconPicker() {
        IconPickerDialog dialog = IconPickerDialog.newInstance(this);
        dialog.show(getParentFragmentManager(), "icon_picker");
    }

    private void showReminderSettings() {
        ReminderSettingsDialog dialog = new ReminderSettingsDialog();
        dialog.show(getParentFragmentManager(), "reminder_settings");
    }

    private void showTomatoSettings() {
        TomatoSettingsDialog dialog = new TomatoSettingsDialog();
        dialog.setOnSettingsChangedListener(new TomatoSettingsDialog.OnSettingsChangedListener() {
            @Override
            public void onSettingsChanged() {
                // 设置变更后，如果倒计时没有运行，更新显示的时间
                if (!isTimerRunning && !isTimerPaused) {
                    long newDuration = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000L;
                    remainingTime = newDuration;
                    updateTimerDisplay(remainingTime);
                    
                    // 更新保存的剩余时间
                    prefs.edit()
                            .putLong(KEY_REMAINING_TIME, remainingTime)
                            .apply();
                    
                    updateTaskDisplay();
                }
            }
        });
        dialog.show(getParentFragmentManager(), "tomato_settings");
    }

    @Override
    public void onIconSelected(String icon) {
        sunButton.setText(icon);
        
        // 保存选中的图标
        prefs.edit().putString(KEY_SELECTED_ICON, icon).apply();
    }

    private void loadSavedIcon() {
        String savedIcon = prefs.getString(KEY_SELECTED_ICON, "🌞");
        sunButton.setText(savedIcon);
    }

    private void startTimer() {
        long currentTime = System.currentTimeMillis();
        
        // 获取设置的时间
        if (isBreakTime) {
            remainingTime = TomatoSettingsDialog.getBreakDuration(getContext()) * 60 * 1000L;
        } else {
            remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000L;
            currentTomatoCount = 1;
        }
        
        totalTomatoCount = TomatoSettingsDialog.getTomatoCount(getContext());
        
        // 保存倒计时状态到SharedPreferences
        prefs.edit()
                .putLong(KEY_START_TIME, currentTime)
                .putBoolean(KEY_IS_RUNNING, true)
                .putBoolean(KEY_IS_PAUSED, false)
                .putLong(KEY_REMAINING_TIME, remainingTime)
                .putBoolean(KEY_IS_BREAK, isBreakTime)
                .putInt(KEY_CURRENT_COUNT, currentTomatoCount)
                .apply();
        
        updateUIForCurrentState();
        startCountdown(remainingTime);
        showRunningButtons();
        
        // 切换到文本显示模式
        updateTaskDisplay();
    }

    private void updateUIForCurrentState() {
        if (isBreakTime) {
            sunButton.setText("😴"); // 休息图标
            timerText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            loadSavedIcon();
            timerText.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void resumeTimer() {
        long currentTime = System.currentTimeMillis();
        
        // 更新开始时间，但保持剩余时间
        prefs.edit()
                .putLong(KEY_START_TIME, currentTime)
                .putBoolean(KEY_IS_RUNNING, true)
                .putBoolean(KEY_IS_PAUSED, false)
                .putLong(KEY_REMAINING_TIME, remainingTime)
                .apply();
        
        startCountdown(remainingTime);
        showRunningButtons();
        updateTaskDisplay();
    }

    private void pauseTimer() {
        isTimerPaused = true;
        isTimerRunning = false;
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        // 保存暂停状态
        prefs.edit()
                .putBoolean(KEY_IS_RUNNING, false)
                .putBoolean(KEY_IS_PAUSED, true)
                .putLong(KEY_REMAINING_TIME, remainingTime)
                .apply();
        
        showPausedButtons();
        updateTaskDisplay();
    }

    private void abandonTimer() {
        resetTimer();
        updateTaskDisplay();
    }

    private void startCountdown(long timeRemaining) {
        isTimerRunning = true;
        isTimerPaused = false;
        
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                
                // 更新剩余时间到SharedPreferences
                prefs.edit()
                        .putLong(KEY_REMAINING_TIME, remainingTime)
                        .apply();
            }

            @Override
            public void onFinish() {
                onTimerFinished();
            }
        };
        
        countDownTimer.start();
    }

    private void updateTimerDisplay(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;
        
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    private void onTimerFinished() {
        playNotification();
        
        if (isBreakTime) {
            // 休息结束，开始下一个番茄钟
            isBreakTime = false;
            currentTomatoCount++;
            
            if (currentTomatoCount <= totalTomatoCount) {
                if (TomatoSettingsDialog.isAutoNextEnabled(getContext())) {
                    // 自动开始下一个番茄钟
                    Toast.makeText(getContext(), "休息结束，开始第" + currentTomatoCount + "个番茄钟", Toast.LENGTH_SHORT).show();
                    startTimer();
                    return;
                } else {
                    Toast.makeText(getContext(), "休息结束，点击开始进行第" + currentTomatoCount + "个番茄钟", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "所有番茄钟完成！", Toast.LENGTH_LONG).show();
                resetTimer();
                return;
            }
        } else {
            // 番茄钟结束，开始休息
            isBreakTime = true;
            
            if (currentTomatoCount < totalTomatoCount) {
                if (TomatoSettingsDialog.isAutoNextEnabled(getContext())) {
                    // 自动开始休息
                    Toast.makeText(getContext(), "第" + currentTomatoCount + "个番茄钟完成，开始休息", Toast.LENGTH_SHORT).show();
                    startTimer();
                    return;
                } else {
                    Toast.makeText(getContext(), "第" + currentTomatoCount + "个番茄钟完成，点击开始休息", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "所有番茄钟完成！", Toast.LENGTH_LONG).show();
                resetTimer();
                return;
            }
        }
        
        updateUIForCurrentState();
        updateTaskDisplay();
        resetTimer();
    }

    private void playNotification() {
        Context context = getContext();
        if (context == null) return;
        
        // 振动提醒
        if (ReminderSettingsDialog.isVibrateEnabled(context)) {
            try {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(1000); // 振动1秒
                }
            } catch (SecurityException e) {
                // 没有振动权限时静默处理
            }
        }
        
        // 响铃提醒
        if (ReminderSettingsDialog.isRingEnabled(context)) {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                }
            } catch (Exception e) {
                // 播放失败时静默处理
            }
        }
    }

    private void resetTimer() {
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000L;
        
        updateTimerDisplay(remainingTime);
        
        // 清除SharedPreferences中的倒计时状态
        prefs.edit()
                .remove(KEY_START_TIME)
                .putBoolean(KEY_IS_RUNNING, false)
                .putBoolean(KEY_IS_PAUSED, false)
                .putLong(KEY_REMAINING_TIME, remainingTime)
                .putBoolean(KEY_IS_BREAK, false)
                .putInt(KEY_CURRENT_COUNT, 0)
                .apply();
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        updateUIForCurrentState();
        showInitialButtons();
        updateTaskDisplay();
    }

    private void showInitialButtons() {
        startButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        abandonButton.setVisibility(View.GONE);
    }

    private void showRunningButtons() {
        startButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        abandonButton.setVisibility(View.VISIBLE);
    }

    private void showPausedButtons() {
        startButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        abandonButton.setVisibility(View.VISIBLE);
    }

    private void restoreTimerState() {
        boolean wasRunning = prefs.getBoolean(KEY_IS_RUNNING, false);
        boolean wasPaused = prefs.getBoolean(KEY_IS_PAUSED, false);
        remainingTime = prefs.getLong(KEY_REMAINING_TIME, TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000L);
        isBreakTime = prefs.getBoolean(KEY_IS_BREAK, false);
        currentTomatoCount = prefs.getInt(KEY_CURRENT_COUNT, 0);
        totalTomatoCount = TomatoSettingsDialog.getTomatoCount(getContext());
        
        if (wasRunning) {
            long startTime = prefs.getLong(KEY_START_TIME, 0);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            long actualRemainingTime = remainingTime - elapsedTime;
            
            if (actualRemainingTime > 0) {
                // 倒计时还在进行中，恢复倒计时
                remainingTime = actualRemainingTime;
                updateUIForCurrentState();
                startCountdown(remainingTime);
                showRunningButtons();
                updateTaskDisplay();
            } else {
                // 倒计时已结束，触发完成逻辑
                onTimerFinished();
            }
        } else if (wasPaused) {
            // 恢复暂停状态
            isTimerPaused = true;
            updateUIForCurrentState();
            updateTimerDisplay(remainingTime);
            showPausedButtons();
            updateTaskDisplay();
        } else {
            // 没有进行中的倒计时，显示初始状态
            resetTimer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 移除监听器
        if (getActivity() instanceof MainActivity) {
            for (androidx.fragment.app.Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof TaskListFragment) {
                    TaskListFragment taskListFragment = (TaskListFragment) fragment;
                    taskListFragment.removeTaskListListener(this);
                    break;
                }
            }
        }
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fragment暂停时取消当前计时器，但保持状态在SharedPreferences中
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复时刷新任务列表
        setupTaskSpinner();
    }
} 