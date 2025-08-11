package com.example.fourquadrant;

import android.content.Context;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;

import com.example.fourquadrant.database.repository.SettingsRepository;
import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.entity.SettingsEntity;

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
    
    // 数据库仓库
    private SettingsRepository settingsRepository;
    private PomodoroRepository pomodoroRepository;
    
    // 设置键名常量
    private static final String KEY_SELECTED_ICON = "tomato_selected_icon";
    private static final String KEY_IS_RUNNING = "tomato_is_running";
    private static final String KEY_IS_PAUSED = "tomato_is_paused";
    private static final String KEY_REMAINING_TIME = "tomato_remaining_time";
    private static final String KEY_IS_BREAK = "tomato_is_break";
    private static final String KEY_CURRENT_COUNT = "tomato_current_count";
    private static final String KEY_START_TIME = "tomato_start_time";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tomato, container, false);
        
        initViews(view);
        initDatabase();
        setupButtons();
        setupTaskSpinner();
        loadSettings();
        
        // 检查是否需要恢复倒计时
        restoreTimerState();
        
        return view;
    }
    
    private void initViews(View view) {
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
    }
    
    private void initDatabase() {
        settingsRepository = new SettingsRepository(requireActivity().getApplication());
        pomodoroRepository = new PomodoroRepository(requireActivity().getApplication());
    }
    
    private void setupButtons() {
        startButton.setOnClickListener(v -> startTimer());
        resumeButton.setOnClickListener(v -> resumeTimer());
        pauseButton.setOnClickListener(v -> pauseTimer());
        abandonButton.setOnClickListener(v -> abandonTimer());
        sunButton.setOnClickListener(v -> showIconPicker());
        reminderButton.setOnClickListener(v -> showReminderSettings());
        settingsButton.setOnClickListener(v -> showTomatoSettings());
        
        // 初始化按钮状态
        updateButtonStates();
    }
    
    private void setupTaskSpinner() {
        // 首先直接从数据库加载任务列表
        loadTasksFromDatabase();
        
        // 然后注册为任务列表监听器以获取后续更新
        if (getParentFragment() != null && getParentFragment() instanceof TaskListFragment) {
            ((TaskListFragment) getParentFragment()).addTaskListListener(this);
        } else {
            // 从MainActivity获取TaskListFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                TaskListFragment taskListFragment = mainActivity.getTaskListFragment();
                if (taskListFragment != null) {
                    taskListFragment.addTaskListListener(this);
                }
            }
        }
        
        updateTaskSpinnerVisibility();
    }
    
    /**
     * 直接从数据库加载任务列表
     */
    private void loadTasksFromDatabase() {
        if (getActivity() != null) {
            new Thread(() -> {
                try {
                    // 初始化TaskRepository
                    com.example.fourquadrant.database.repository.TaskRepository taskRepository = 
                        new com.example.fourquadrant.database.repository.TaskRepository(getActivity().getApplication());
                    
                    // 同步查询活跃任务
                    List<com.example.fourquadrant.database.entity.TaskEntity> taskEntities = 
                        taskRepository.getActiveTasksSync();
                    
                    // 转换为QuadrantView.Task列表（TomatoFragment使用的格式）
                    List<QuadrantView.Task> tasks = new ArrayList<>();
                    for (com.example.fourquadrant.database.entity.TaskEntity entity : taskEntities) {
                        QuadrantView.Task task = new QuadrantView.Task(
                            entity.getName(),
                            entity.getImportance(),
                            entity.getUrgency()
                        );
                        tasks.add(task);
                    }
                    
                    android.util.Log.d("TomatoFragment", "Loaded " + tasks.size() + " tasks from database");
                    
                    // 在主线程更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            onTasksUpdated(tasks);
                        });
                    }
                    
                } catch (Exception e) {
                    android.util.Log.e("TomatoFragment", "Error loading tasks from database", e);
                    // 在主线程设置空列表
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            onTasksUpdated(new ArrayList<>());
                        });
                    }
                }
            }).start();
        }
    }
    
    private void loadSettings() {
        // 加载图标设置
        LiveData<String> iconSetting = settingsRepository.getStringSetting(KEY_SELECTED_ICON);
        iconSetting.observe(getViewLifecycleOwner(), icon -> {
            if (icon != null && !icon.isEmpty()) {
                sunButton.setText(icon);
            } else {
                sunButton.setText("🌞"); // 默认图标
            }
        });
        
        // 加载番茄钟设置
        loadTomatoSettings();
    }
    
    private void loadTomatoSettings() {
        totalTomatoCount = TomatoSettingsDialog.getTomatoCount(getContext());
        int tomato_duration = TomatoSettingsDialog.getTomatoDuration(getContext());
        
        // 只有在不是运行状态时才更新剩余时间
        if (!isTimerRunning && !isTimerPaused) {
            if (isBreakTime) {
                int break_duration = TomatoSettingsDialog.getBreakDuration(getContext());
                remainingTime = break_duration * 60 * 1000;
            } else {
                remainingTime = tomato_duration * 60 * 1000;
            }
            updateTimerDisplay(remainingTime);
        }
    }
    
    private void restoreTimerState() {
        // 从数据库恢复状态
        LiveData<Boolean> isRunningLiveData = settingsRepository.getBooleanSetting(KEY_IS_RUNNING);
        isRunningLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRunning) {
                if (isRunning == null) isRunning = false;
                
                if (isRunning) {
                    // 获取其他状态信息
                    restoreFullTimerState();
                }
                isRunningLiveData.removeObserver(this);
            }
        });
    }
    
    private void restoreFullTimerState() {
        LiveData<Boolean> isPausedLiveData = settingsRepository.getBooleanSetting(KEY_IS_PAUSED);
        LiveData<Long> remainingTimeLiveData = settingsRepository.getLongSetting(KEY_REMAINING_TIME);
        LiveData<Long> startTimeLiveData = settingsRepository.getLongSetting(KEY_START_TIME);
        LiveData<Boolean> isBreakLiveData = settingsRepository.getBooleanSetting(KEY_IS_BREAK);
        LiveData<Integer> currentCountLiveData = settingsRepository.getIntSetting(KEY_CURRENT_COUNT);
        
        // 创建单独的Observer引用
        Observer<Boolean> isPausedObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPaused) {
                if (isPaused == null) isPaused = false;
                isTimerPaused = isPaused;
                isPausedLiveData.removeObserver(this);
            }
        };
        
        Observer<Long> remainingTimeObserver = new Observer<Long>() {
            @Override
            public void onChanged(Long remaining) {
                if (remaining != null && remaining > 0) {
                    remainingTime = remaining;
                }
                remainingTimeLiveData.removeObserver(this);
            }
        };
        
        Observer<Long> startTimeObserver = new Observer<Long>() {
            @Override
            public void onChanged(Long startTime) {
                if (startTime != null && startTime > 0) {
                    restoreTimerFromStartTime(startTime);
                } else if (!isTimerPaused) {
                    continueTimer();
                }
                startTimeLiveData.removeObserver(this);
            }
        };
        
        Observer<Boolean> isBreakObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBreak) {
                if (isBreak != null) isBreakTime = isBreak;
                updateTaskSpinnerVisibility();
                isBreakLiveData.removeObserver(this);
            }
        };
        
        Observer<Integer> currentCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (count != null) currentTomatoCount = count;
                updateButtonStates();
                currentCountLiveData.removeObserver(this);
            }
        };
        
        // 观察所有状态
        isPausedLiveData.observe(getViewLifecycleOwner(), isPausedObserver);
        remainingTimeLiveData.observe(getViewLifecycleOwner(), remainingTimeObserver);
        startTimeLiveData.observe(getViewLifecycleOwner(), startTimeObserver);
        isBreakLiveData.observe(getViewLifecycleOwner(), isBreakObserver);
        currentCountLiveData.observe(getViewLifecycleOwner(), currentCountObserver);
    }
    
    private void restoreTimerFromStartTime(long startTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        
        long originalDuration;
        if (isBreakTime) {
            originalDuration = TomatoSettingsDialog.getBreakDuration(getContext()) * 60 * 1000;
        } else {
            originalDuration = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        }
        
        remainingTime = originalDuration - elapsedTime;
        
        if (remainingTime <= 0) {
            // 时间已经结束，触发完成事件
            onTimerFinished();
        } else if (!isTimerPaused) {
            // 继续倒计时
            isTimerRunning = true;
            continueTimer();
        }
    }
    
    private void startTimer() {
        if (!isTimerRunning && !isTimerPaused) {
            isTimerRunning = true;
            
            long currentTime = System.currentTimeMillis();
            totalTomatoCount = TomatoSettingsDialog.getTomatoCount(getContext());
            
            // 保存倒计时状态到数据库
            saveTimerState(currentTime, true, false, remainingTime, isBreakTime, currentTomatoCount);
            
            continueTimer();
            updateButtonStates();
            updateTaskSpinnerVisibility();
        }
    }
    
    private void continueTimer() {
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                
                // 更新剩余时间到数据库
                settingsRepository.saveLongSetting(KEY_REMAINING_TIME, remainingTime);
            }
            
            @Override
            public void onFinish() {
                onTimerFinished();
            }
        };
        countDownTimer.start();
    }
    
    private void pauseTimer() {
        if (isTimerRunning && !isTimerPaused) {
            isTimerPaused = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            
            // 保存暂停状态
            saveTimerState(0, true, true, remainingTime, isBreakTime, currentTomatoCount);
            updateButtonStates();
        }
    }
    
    private void resumeTimer() {
        if (isTimerRunning && isTimerPaused) {
            isTimerPaused = false;
            
            // 保存恢复状态
            saveTimerState(System.currentTimeMillis(), true, false, remainingTime, isBreakTime, currentTomatoCount);
            
            continueTimer();
            updateButtonStates();
        }
    }
    
    private void abandonTimer() {
        // 停止并重置计时器
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        
        // 重置为默认时间
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        updateTimerDisplay(remainingTime);
        
        // 清除数据库中的倒计时状态
        clearTimerState();
        
        updateButtonStates();
        updateTaskSpinnerVisibility();
        
        Toast.makeText(getContext(), "番茄钟已重置", Toast.LENGTH_SHORT).show();
    }
    
    private void onTimerFinished() {
        isTimerRunning = false;
        isTimerPaused = false;
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        // 记录番茄钟完成
        recordPomodoroCompletion();
        
        // 播放提醒
        playReminder();
        
        // 切换状态
        if (!isBreakTime) {
            // 完成一个番茄钟，增加计数
            currentTomatoCount++;
            
            if (currentTomatoCount >= totalTomatoCount) {
                // 完成所有番茄钟
                finishAllPomodoros();
            } else {
                // 进入休息时间
                startBreakTime();
            }
        } else {
            // 完成休息，检查是否自动开始下一个番茄钟
            finishBreakTime();
        }
    }
    
    private void startBreakTime() {
        isBreakTime = true;
        remainingTime = TomatoSettingsDialog.getBreakDuration(getContext()) * 60 * 1000;
        updateTimerDisplay(remainingTime);
        updateTaskSpinnerVisibility();
        
        boolean autoNext = TomatoSettingsDialog.getAutoNext(getContext());
        if (autoNext) {
            startTimer();
        } else {
            updateButtonStates();
            Toast.makeText(getContext(), "休息时间开始，点击开始继续", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void finishBreakTime() {
        isBreakTime = false;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        updateTimerDisplay(remainingTime);
        updateTaskSpinnerVisibility();
        
        boolean autoNext = TomatoSettingsDialog.getAutoNext(getContext());
        if (autoNext) {
            startTimer();
        } else {
            updateButtonStates();
            Toast.makeText(getContext(), "休息结束，点击开始继续番茄钟", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void finishAllPomodoros() {
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        updateTimerDisplay(remainingTime);
        
        clearTimerState();
        updateButtonStates();
        updateTaskSpinnerVisibility();
        
        Toast.makeText(getContext(), "恭喜！完成了所有番茄钟！", Toast.LENGTH_LONG).show();
    }
    
    private void recordPomodoroCompletion() {
        if (!isBreakTime && pomodoroRepository != null) {
            String taskName = getCurrentTaskName();
            String taskId = getCurrentTaskId();
            int duration = TomatoSettingsDialog.getTomatoDuration(getContext());
            
            // 使用数据库记录番茄钟完成
            pomodoroRepository.recordPomodoroCompletion(taskId, taskName, duration);
        }
    }
    
    private String getCurrentTaskName() {
        if (isBreakTime) {
            return "休息";
        }
        
        if (taskSpinner.getVisibility() == View.VISIBLE && taskSpinner.getSelectedItem() != null) {
            QuadrantView.Task selectedTask = (QuadrantView.Task) taskSpinner.getSelectedItem();
            return selectedTask.getName();
        }
        
        if (taskText.getVisibility() == View.VISIBLE) {
            return taskText.getText().toString();
        }
        
        return "未指定任务";
    }
    
    private String getCurrentTaskId() {
        // TODO: 需要从任务中获取ID，目前返回null
        return null;
    }
    
    private void playReminder() {
        boolean shouldVibrate = ReminderSettingsDialog.getVibrateEnabled(getContext());
        boolean shouldRing = ReminderSettingsDialog.getRingEnabled(getContext());
        
        if (shouldVibrate) {
            try {
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(1000);
                }
            } catch (SecurityException e) {
                // 没有振动权限
            }
        }
        
        if (shouldRing) {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                }
            } catch (Exception e) {
                // 播放失败
            }
        }
    }
    
    private void updateTimerDisplay(long millisUntilFinished) {
        long minutes = millisUntilFinished / 60000;
        long seconds = (millisUntilFinished % 60000) / 1000;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeLeftFormatted);
    }
    
    private void updateButtonStates() {
        if (!isTimerRunning && !isTimerPaused) {
            // 初始状态
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.GONE);
            abandonButton.setVisibility(View.GONE);
        } else if (isTimerRunning && !isTimerPaused) {
            // 运行状态
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            resumeButton.setVisibility(View.GONE);
            abandonButton.setVisibility(View.VISIBLE);
        } else if (isTimerPaused) {
            // 暂停状态
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
            abandonButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateTaskSpinnerVisibility() {
        if (isTimerRunning || isTimerPaused) {
            // 计时器运行时，显示文本框，隐藏下拉框
            taskSpinner.setVisibility(View.GONE);
            taskText.setVisibility(View.VISIBLE);
            
            if (isBreakTime) {
                taskText.setText("休息");
            } else {
                // 显示当前选中的任务
                if (taskSpinner.getSelectedItem() != null) {
                    QuadrantView.Task selectedTask = (QuadrantView.Task) taskSpinner.getSelectedItem();
                    taskText.setText(selectedTask.getName());
                } else {
                    taskText.setText("未选择任务");
                }
            }
        } else {
            // 计时器停止时，显示下拉框，隐藏文本框
            taskSpinner.setVisibility(View.VISIBLE);
            taskText.setVisibility(View.GONE);
        }
    }
    
    private void showIconPicker() {
        IconPickerDialog dialog = IconPickerDialog.newInstance(this);
        dialog.show(getParentFragmentManager(), "IconPicker");
    }
    
    private void showReminderSettings() {
        ReminderSettingsDialog dialog = new ReminderSettingsDialog();
        dialog.show(getParentFragmentManager(), "ReminderSettings");
    }
    
    private void showTomatoSettings() {
        TomatoSettingsDialog dialog = new TomatoSettingsDialog();
        dialog.setOnSettingsChangedListener(() -> loadTomatoSettings());
        dialog.show(getParentFragmentManager(), "TomatoSettings");
    }
    
    @Override
    public void onIconSelected(String icon) {
        sunButton.setText(icon);
        
        // 保存选中的图标到数据库
        settingsRepository.saveStringSetting(KEY_SELECTED_ICON, icon);
    }
    
    @Override
    public void onTasksUpdated(List<QuadrantView.Task> tasks) {
        // 更新任务下拉框
        List<QuadrantView.Task> taskList = new ArrayList<>(tasks);
        
        ArrayAdapter<QuadrantView.Task> adapter = new ArrayAdapter<QuadrantView.Task>(
            getContext(), android.R.layout.simple_spinner_item, taskList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(getItem(position).getName());
                return textView;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setText(getItem(position).getName());
                return textView;
            }
        };
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(adapter);
        
        // 选择第一个任务（如果有的话）
        if (!taskList.isEmpty()) {
            taskSpinner.setSelection(0);
        }
    }
    
    private void saveTimerState(long startTime, boolean isRunning, boolean isPaused, 
                               long remaining, boolean isBreak, int count) {
        new Thread(() -> {
            settingsRepository.saveLongSetting(KEY_START_TIME, startTime);
            settingsRepository.saveBooleanSetting(KEY_IS_RUNNING, isRunning);
            settingsRepository.saveBooleanSetting(KEY_IS_PAUSED, isPaused);
            settingsRepository.saveLongSetting(KEY_REMAINING_TIME, remaining);
            settingsRepository.saveBooleanSetting(KEY_IS_BREAK, isBreak);
            settingsRepository.saveIntSetting(KEY_CURRENT_COUNT, count);
        }).start();
    }
    
    private void clearTimerState() {
        new Thread(() -> {
            settingsRepository.deleteSetting(KEY_START_TIME);
            settingsRepository.deleteSetting(KEY_IS_RUNNING);
            settingsRepository.deleteSetting(KEY_IS_PAUSED);
            settingsRepository.deleteSetting(KEY_REMAINING_TIME);
            settingsRepository.deleteSetting(KEY_IS_BREAK);
            settingsRepository.deleteSetting(KEY_CURRENT_COUNT);
        }).start();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Fragment暂停时取消当前计时器，但保持状态在数据库中
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Fragment恢复时检查是否需要恢复计时器
        if (isTimerRunning && !isTimerPaused && countDownTimer == null) {
            continueTimer();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        // 移除任务列表监听器
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            TaskListFragment taskListFragment = mainActivity.getTaskListFragment();
            if (taskListFragment != null) {
                taskListFragment.removeTaskListListener(this);
            }
        }
    }
}