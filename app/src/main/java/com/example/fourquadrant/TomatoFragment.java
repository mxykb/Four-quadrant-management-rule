package com.example.fourquadrant;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;
import android.util.Log;

import com.example.fourquadrant.database.repository.SettingsRepository;
import com.example.fourquadrant.database.repository.PomodoroRepository;
import com.example.fourquadrant.database.entity.SettingsEntity;
import com.example.fourquadrant.database.entity.TimerStateEntity;
import com.example.fourquadrant.PomodoroService;

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
    
    // 服务相关
    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;
    private BroadcastReceiver timerReceiver;
    
    // 对话框管理
    private AlertDialog batteryOptimizationDialog;
    
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
        setupServiceConnection();
        setupBroadcastReceiver();
        
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
    
    private void setupServiceConnection() {
        Intent serviceIntent = new Intent(getContext(), PomodoroService.class);
        // 先启动服务以确保前台通知显示
        getContext().startService(serviceIntent);
        // 然后绑定服务以获取服务实例
        getContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void syncFromService() {
        if (isServiceBound && pomodoroService != null) {
            Log.d("TomatoFragment", "Syncing with service - before sync: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            
            boolean serviceRunning = pomodoroService.isTimerRunning();
            boolean servicePaused = pomodoroService.isTimerPaused();
            long serviceRemaining = pomodoroService.getRemainingTime();
            boolean serviceBreak = pomodoroService.isBreakTime();
            int serviceCount = pomodoroService.getCurrentTomatoCount();
            
            Log.d("TomatoFragment", "Service state: isTimerRunning=" + serviceRunning + ", isTimerPaused=" + servicePaused + ", remainingTime=" + serviceRemaining + ", isBreakTime=" + serviceBreak + ", currentCount=" + serviceCount);
            
            isTimerRunning = serviceRunning;
            isTimerPaused = servicePaused;
            remainingTime = serviceRemaining;
            isBreakTime = serviceBreak;
            currentTomatoCount = serviceCount;
            
            Log.d("TomatoFragment", "After sync: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            
            // 强制更新UI显示
            getActivity().runOnUiThread(() -> {
                updateTimerDisplay(remainingTime);
                updateButtonStates();
                updateTaskSpinnerVisibility();
            });
        } else {
            Log.d("TomatoFragment", "Cannot sync with service - service is null or not bound");
        }
    }
    
    private void setupBroadcastReceiver() {
        timerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (PomodoroService.ACTION_TIMER_UPDATE.equals(action)) {
                    long remainingTime = intent.getLongExtra(PomodoroService.EXTRA_REMAINING_TIME, 0);
                    boolean isBreak = intent.getBooleanExtra(PomodoroService.EXTRA_IS_BREAK, false);
                    int currentCount = intent.getIntExtra(PomodoroService.EXTRA_CURRENT_COUNT, 0);
                    
                    Log.d("TomatoFragment", "Received timer update broadcast: remainingTime=" + remainingTime + ", isBreak=" + isBreak + ", currentCount=" + currentCount);
                    
                    // 更新UI和状态
                    TomatoFragment.this.remainingTime = remainingTime;
                    TomatoFragment.this.isBreakTime = isBreak;
                    TomatoFragment.this.currentTomatoCount = currentCount;
                    
                    // 从服务同步状态
                    if (isServiceBound && pomodoroService != null) {
                        TomatoFragment.this.isTimerRunning = pomodoroService.isTimerRunning();
                        TomatoFragment.this.isTimerPaused = pomodoroService.isTimerPaused();
                        Log.d("TomatoFragment", "Synced running state from service: isTimerRunning=" + TomatoFragment.this.isTimerRunning + ", isTimerPaused=" + TomatoFragment.this.isTimerPaused);
                    }
                    
                    updateTimerDisplay(remainingTime);
                    updateButtonStates();
                    updateTaskSpinnerVisibility();
                    
                } else if (PomodoroService.ACTION_TIMER_FINISHED.equals(action)) {
                    boolean isBreak = intent.getBooleanExtra(PomodoroService.EXTRA_IS_BREAK, false);
                    int currentCount = intent.getIntExtra(PomodoroService.EXTRA_CURRENT_COUNT, 0);
                    
                    Log.d("TomatoFragment", "Received timer finished broadcast: isBreak=" + isBreak + ", currentCount=" + currentCount);
                    
                    // 更新Fragment状态
                    TomatoFragment.this.isBreakTime = isBreak;
                    TomatoFragment.this.currentTomatoCount = currentCount;
                    
                    if (!isBreak) {
                        // 番茄钟完成，记录完成并显示弹窗
                        recordPomodoroCompletion();
                        Log.d("TomatoFragment", "Pomodoro completion recorded");
                        
                        // 显示番茄钟完成弹窗
                        showPomodoroCompletionDialog();
                    } else {
                        // 休息结束，播放提醒
                        playReminder();
                    }
                    
                    updateButtonStates();
                    updateTaskSpinnerVisibility();
                } else if ("com.example.fourquadrant.BATTERY_OPTIMIZATION_WARNING".equals(action)) {
                    // 显示电池优化警告
                    showBatteryOptimizationDialog();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(PomodoroService.ACTION_TIMER_UPDATE);
        filter.addAction(PomodoroService.ACTION_TIMER_FINISHED);
        filter.addAction("com.example.fourquadrant.BATTERY_OPTIMIZATION_WARNING");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(timerReceiver, filter);
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("TomatoFragment", "Service connected");
            PomodoroService.PomodoroServiceBinder binder = (PomodoroService.PomodoroServiceBinder) service;
            pomodoroService = binder.getService();
            isServiceBound = true;
            
            Log.d("TomatoFragment", "Service bound, current fragment state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            
            // 优先从服务获取当前状态
            if (pomodoroService.isTimerRunning()) {
                // 服务中有运行的计时器，直接同步服务状态
                Log.d("TomatoFragment", "Service has running timer, syncing from service");
                syncFromService();
            } else {
                // 服务中没有运行的计时器，尝试从数据库恢复状态
                Log.d("TomatoFragment", "Service has no running timer, restoring from database");
                restoreTimerState();
                
                // 如果Fragment恢复的状态显示计时器正在运行，需要将状态同步到服务
                if (isTimerRunning) {
                    Log.d("TomatoFragment", "Fragment has running timer state, syncing to service");
                    pomodoroService.syncState(isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
                }
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("TomatoFragment", "Service disconnected");
            pomodoroService = null;
            isServiceBound = false;
        }
    };
    
    private void setupButtons() {
        startButton.setOnClickListener(v -> {
            Log.d("TomatoFragment", "Start button clicked - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            if (isServiceBound && pomodoroService != null) {
                Log.d("TomatoFragment", "Starting timer through service");
                pomodoroService.startTimer();
            } else {
                Log.d("TomatoFragment", "Service not bound, starting timer directly");
                startTimer();
            }
        });
        resumeButton.setOnClickListener(v -> {
            Log.d("TomatoFragment", "Resume button clicked - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            if (isServiceBound && pomodoroService != null) {
                Log.d("TomatoFragment", "Resuming timer through service");
                pomodoroService.resumeTimer();
            } else {
                Log.d("TomatoFragment", "Service not bound, resuming timer directly");
                resumeTimer();
            }
        });
        pauseButton.setOnClickListener(v -> {
            Log.d("TomatoFragment", "Pause button clicked - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            if (isServiceBound && pomodoroService != null) {
                Log.d("TomatoFragment", "Pausing timer through service");
                pomodoroService.pauseTimer();
            } else {
                Log.d("TomatoFragment", "Service not bound, pausing timer directly");
                pauseTimer();
            }
        });
        abandonButton.setOnClickListener(v -> {
            Log.d("TomatoFragment", "Abandon button clicked - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
            if (isServiceBound && pomodoroService != null) {
                Log.d("TomatoFragment", "Abandoning timer through service");
                pomodoroService.abandonTimer();
            } else {
                Log.d("TomatoFragment", "Service not bound, abandoning timer directly");
                abandonTimer();
            }
        });
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
            // 使用数据库线程池执行查询，避免并发访问问题
            com.example.fourquadrant.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // 检查数据库是否就绪
                    FourQuadrantApplication app = (FourQuadrantApplication) getActivity().getApplication();
                    if (!app.waitForDatabaseReady(3000)) { // 最多等待3秒
                        android.util.Log.w("TomatoFragment", "数据库未就绪，跳过任务加载");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                onTasksUpdated(new ArrayList<>());
                            });
                        }
                        return;
                    }
                    
                    // 等待数据库完全初始化
                    Thread.sleep(200);
                    
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
            });
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
        // 使用计数器确保所有状态都加载完成后再恢复计时器
        final int[] loadedCount = {0};
        final int totalStates = 5;
        
        LiveData<Boolean> isPausedLiveData = settingsRepository.getBooleanSetting(KEY_IS_PAUSED);
        LiveData<Long> remainingTimeLiveData = settingsRepository.getLongSetting(KEY_REMAINING_TIME);
        LiveData<Long> startTimeLiveData = settingsRepository.getLongSetting(KEY_START_TIME);
        LiveData<Boolean> isBreakLiveData = settingsRepository.getBooleanSetting(KEY_IS_BREAK);
        LiveData<Integer> currentCountLiveData = settingsRepository.getIntSetting(KEY_CURRENT_COUNT);
        
        final long[] savedStartTime = {0};
        
        Observer<Boolean> isPausedObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPaused) {
                if (isPaused == null) isPaused = false;
                isTimerPaused = isPaused;
                isTimerRunning = true; // 如果有状态需要恢复，说明计时器曾经在运行
                loadedCount[0]++;
                checkAndRestoreTimer(loadedCount[0], totalStates, savedStartTime[0]);
                isPausedLiveData.removeObserver(this);
            }
        };
        
        Observer<Long> remainingTimeObserver = new Observer<Long>() {
            @Override
            public void onChanged(Long remaining) {
                if (remaining != null && remaining > 0) {
                    remainingTime = remaining;
                }
                loadedCount[0]++;
                checkAndRestoreTimer(loadedCount[0], totalStates, savedStartTime[0]);
                remainingTimeLiveData.removeObserver(this);
            }
        };
        
        Observer<Long> startTimeObserver = new Observer<Long>() {
            @Override
            public void onChanged(Long startTime) {
                if (startTime != null && startTime > 0) {
                    savedStartTime[0] = startTime;
                }
                loadedCount[0]++;
                checkAndRestoreTimer(loadedCount[0], totalStates, savedStartTime[0]);
                startTimeLiveData.removeObserver(this);
            }
        };
        
        Observer<Boolean> isBreakObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBreak) {
                if (isBreak != null) isBreakTime = isBreak;
                loadedCount[0]++;
                checkAndRestoreTimer(loadedCount[0], totalStates, savedStartTime[0]);
                isBreakLiveData.removeObserver(this);
            }
        };
        
        Observer<Integer> currentCountObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (count != null) currentTomatoCount = count;
                loadedCount[0]++;
                checkAndRestoreTimer(loadedCount[0], totalStates, savedStartTime[0]);
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
    
    private void checkAndRestoreTimer(int loadedCount, int totalStates, long startTime) {
        if (loadedCount == totalStates) {
            // 所有状态都加载完成，现在恢复计时器
            
            if (startTime > 0) {
                restoreTimerFromStartTime(startTime);
            } else if (isTimerRunning) {
                // 如果没有开始时间但计时器在运行，通过服务同步状态
                if (isServiceBound && pomodoroService != null) {
                    pomodoroService.syncState(isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
                } else {
                    // 如果服务未绑定，启动服务并同步状态
                    Intent serviceIntent = new Intent(getContext(), PomodoroService.class);
                    getContext().startService(serviceIntent);
                    setupServiceConnection();
                }
            }
            
            // 更新UI状态
            updateTimerDisplay(remainingTime);
            updateButtonStates();
            updateTaskSpinnerVisibility();
        }
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
            // 时间已经结束，清除状态
            clearTimerState();
            remainingTime = originalDuration;
            isTimerRunning = false;
            isTimerPaused = false;
        } else {
            // 通过服务同步状态并继续计时
            if (isServiceBound && pomodoroService != null) {
                pomodoroService.syncState(isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
            } else {
                // 如果服务未绑定，启动服务
                Intent serviceIntent = new Intent(getContext(), PomodoroService.class);
                getContext().startService(serviceIntent);
                setupServiceConnection();
            }
        }
    }
    
    private void startTimer() {
        if (!isTimerRunning && !isTimerPaused) {
            long currentTime = System.currentTimeMillis();
            totalTomatoCount = TomatoSettingsDialog.getTomatoCount(getContext());
            
            // 获取番茄钟时长
            long duration = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
            
            // 通过服务启动计时器
            if (isServiceBound && pomodoroService != null) {
                // 设置当前任务名称
                String taskName = getCurrentTaskName();
                pomodoroService.setCurrentTaskName(taskName);
                pomodoroService.startTimer(duration, isBreakTime, currentTomatoCount, totalTomatoCount);
            } else {
                // 如果服务未绑定，启动服务
                Intent serviceIntent = new Intent(getContext(), PomodoroService.class);
                getContext().startService(serviceIntent);
                setupServiceConnection();
            }
            
            // 保存倒计时状态到数据库
            saveTimerState(currentTime, true, false, duration, isBreakTime, currentTomatoCount);
        }
    }
    
    // 注意：continueTimer方法已废弃，计时器现在由PomodoroService管理
    // 保留此方法仅为兼容性，实际不应被调用
    private void continueTimer() {
        // 此方法已废弃，所有计时器操作应通过PomodoroService进行
        // 如果意外调用此方法，尝试通过服务启动计时器
        if (isServiceBound && pomodoroService != null && isTimerRunning && !isTimerPaused) {
            pomodoroService.syncState(isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
        }
    }
    
    // 注意：pauseTimer方法已废弃，计时器现在由PomodoroService管理
    // 保留此方法仅为兼容性，实际不应被调用
    private void pauseTimer() {
        // 此方法已废弃，所有计时器操作应通过PomodoroService进行
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.pauseTimer();
        }
    }
    
    // 注意：resumeTimer方法已废弃，计时器现在由PomodoroService管理
    // 保留此方法仅为兼容性，实际不应被调用
    private void resumeTimer() {
        // 此方法已废弃，所有计时器操作应通过PomodoroService进行
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.resumeTimer();
        }
    }
    
    // 注意：abandonTimer方法已废弃，计时器现在由PomodoroService管理
    // 保留此方法仅为兼容性，实际不应被调用
    private void abandonTimer() {
        // 此方法已废弃，所有计时器操作应通过PomodoroService进行
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.abandonTimer();
        }
        
        // 重置Fragment的状态变量
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        
        // 清除数据库中的倒计时状态
        clearTimerState();
        
        // 更新UI显示
        updateTimerDisplay(remainingTime);
        updateButtonStates();
        updateTaskSpinnerVisibility();
        
        Toast.makeText(getContext(), "番茄钟已重置", Toast.LENGTH_SHORT).show();
    }
    
    // 注意：onTimerFinished方法已废弃，计时器完成逻辑现在由PomodoroService管理
    // 保留此方法仅为兼容性，实际不应被调用
    private void onTimerFinished() {
        // 此方法已废弃，计时器完成逻辑应通过PomodoroService和广播接收器处理
        // 如果意外调用此方法，只处理UI相关操作
        
        // 记录番茄钟完成
        recordPomodoroCompletion();
        
        // 播放提醒
        playReminder();
    }
    
    // 注意：以下方法已废弃，状态转换逻辑现在由PomodoroService管理
    // 保留这些方法仅为兼容性，实际不应被调用
    
    private void startBreakTime() {
        // 此方法已废弃，状态转换应通过PomodoroService处理
        // 如果意外调用此方法，只处理UI更新
        updateTaskSpinnerVisibility();
        updateButtonStates();
        Toast.makeText(getContext(), "休息时间开始", Toast.LENGTH_SHORT).show();
    }
    
    private void finishBreakTime() {
        // 此方法已废弃，状态转换应通过PomodoroService处理
        // 如果意外调用此方法，只处理UI更新
        updateTaskSpinnerVisibility();
        updateButtonStates();
        Toast.makeText(getContext(), "休息结束", Toast.LENGTH_SHORT).show();
    }
    
    private void finishAllPomodoros() {
        // 此方法已废弃，状态转换应通过PomodoroService处理
        // 如果意外调用此方法，只处理UI更新
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
        Log.d("TomatoFragment", "Updating timer display: " + timeLeftFormatted + " (" + millisUntilFinished + "ms)");
        timerText.setText(timeLeftFormatted);
    }
    
    private void updateButtonStates() {
        Log.d("TomatoFragment", "Updating button states - isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused);
        
        if (!isTimerRunning && !isTimerPaused) {
            // 初始状态
            Log.d("TomatoFragment", "Setting buttons for initial state");
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.GONE);
            abandonButton.setVisibility(View.GONE);
        } else if (isTimerRunning && !isTimerPaused) {
            // 运行状态
            Log.d("TomatoFragment", "Setting buttons for running state");
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            resumeButton.setVisibility(View.GONE);
            abandonButton.setVisibility(View.VISIBLE);
        } else if (isTimerPaused) {
            // 暂停状态
            Log.d("TomatoFragment", "Setting buttons for paused state");
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
    
    /**
     * 显示番茄钟完成弹窗
     */
    private void showPomodoroCompletionDialog() {
        if (getContext() == null) return;
        
        PomodoroCompletionDialog dialog = new PomodoroCompletionDialog(getContext());
        
        // 设置弹窗内容
        String taskName = getCurrentTaskName();
        dialog.setContent(taskName, currentTomatoCount, totalTomatoCount);
        
        // 设置操作监听器
        dialog.setOnActionListener(new PomodoroCompletionDialog.OnActionListener() {
            @Override
            public void onStartBreak() {
                handleStartBreak();
            }
            
            @Override
            public void onSkipBreak() {
                handleSkipBreak();
            }
            
            @Override
            public void onClose() {
                handleClose();
            }
        });
        
        // 显示弹窗
        dialog.show();
    }
    
    /**
     * 处理开始休息
     */
    private void handleStartBreak() {
        Log.d("TomatoFragment", "User chose to start break");
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.startBreakByUser();
        }
        updateButtonStates();
        updateTaskSpinnerVisibility();
    }
    
    /**
     * 处理跳过休息，继续工作
     */
    private void handleSkipBreak() {
        Log.d("TomatoFragment", "User chose to skip break");
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.skipBreakByUser();
        }
        updateButtonStates();
        updateTaskSpinnerVisibility();
    }
    

    
    /**
     * 处理关闭，重置番茄钟
     */
    private void handleClose() {
        Log.d("TomatoFragment", "User chose to close and reset");
        if (isServiceBound && pomodoroService != null) {
            pomodoroService.closeByUser();
        }
        
        // 停止服务以确保通知完全清除
        Intent serviceIntent = new Intent(getContext(), PomodoroService.class);
        getContext().stopService(serviceIntent);
        
        // 解绑服务
        if (isServiceBound) {
            getContext().unbindService(serviceConnection);
            isServiceBound = false;
            pomodoroService = null;
        }
        
        // 重置Fragment状态
        isTimerRunning = false;
        isTimerPaused = false;
        isBreakTime = false;
        currentTomatoCount = 0;
        remainingTime = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
        
        // 更新UI
        updateTimerDisplay(remainingTime);
        updateButtonStates();
        updateTaskSpinnerVisibility();
        
        Toast.makeText(getContext(), "番茄钟已重置", Toast.LENGTH_SHORT).show();
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
        
        Log.d("TomatoFragment", "onPause called - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        // Fragment暂停时不再取消计时器，因为计时由服务管理
        // 只需要保存当前状态到数据库
        if (isTimerRunning) {
            // 从服务获取最新状态
            if (isServiceBound && pomodoroService != null) {
                remainingTime = pomodoroService.getRemainingTime();
                isTimerPaused = pomodoroService.isTimerPaused();
                isBreakTime = pomodoroService.isBreakTime();
                currentTomatoCount = pomodoroService.getCurrentTomatoCount();
                
                Log.d("TomatoFragment", "Updated state from service: isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime + ", isBreakTime=" + isBreakTime + ", currentCount=" + currentTomatoCount);
            }
            
            // 计算当前应该保存的开始时间
            long currentTime = System.currentTimeMillis();
            long startTime;
            
            if (isTimerPaused) {
                // 如果已暂停，保存0作为开始时间，依赖剩余时间恢复
                startTime = 0;
                Log.d("TomatoFragment", "Timer is paused, saving startTime as 0");
            } else {
                // 如果正在运行，计算开始时间
                long originalDuration;
                if (isBreakTime) {
                    originalDuration = TomatoSettingsDialog.getBreakDuration(getContext()) * 60 * 1000;
                } else {
                    originalDuration = TomatoSettingsDialog.getTomatoDuration(getContext()) * 60 * 1000;
                }
                startTime = currentTime - (originalDuration - remainingTime);
                Log.d("TomatoFragment", "Timer is running, calculated startTime=" + startTime);
            }
            
            // 保存完整状态
            saveTimerState(startTime, isTimerRunning, isTimerPaused, remainingTime, isBreakTime, currentTomatoCount);
            Log.d("TomatoFragment", "Timer state saved to database");
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        Log.d("TomatoFragment", "onResume called - current state: isTimerRunning=" + isTimerRunning + ", isTimerPaused=" + isTimerPaused + ", remainingTime=" + remainingTime);
        
        // Fragment恢复时重新绑定服务
        setupServiceConnection();
        
        // 检查是否有待确认的番茄钟完成状态
        checkPendingPomodoroCompletion();
        
        // 重新检查电池优化权限状态（从设置页面返回时）
        checkBatteryOptimization();
        
        Log.d("TomatoFragment", "Service binding initiated");
        // 注意：不在这里调用restoreTimerState()，而是在服务连接成功后处理状态同步
    }
    
    /**
     * 检查是否有待确认的番茄钟完成状态
     */
    private void checkPendingPomodoroCompletion() {
        if (pomodoroRepository != null) {
            new Thread(() -> {
                try {
                    TimerStateEntity timerState = pomodoroRepository.getTimerStateSync();
                    if (timerState != null && timerState.isCompletedPending()) {
                        // 有待确认的完成状态，需要在主线程显示弹窗
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // 更新当前状态
                                currentTomatoCount = timerState.getCurrentCount();
                                totalTomatoCount = timerState.getTotalCount();
                                
                                // 显示完成弹窗
                                showPomodoroCompletionDialog();
                                
                                Log.d("TomatoFragment", "Restored pending completion dialog for count: " + currentTomatoCount);
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("TomatoFragment", "Failed to check pending completion", e);
                }
            }).start();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 不再取消计时器，因为计时器由服务管理
        
        // 移除任务列表监听器
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            TaskListFragment taskListFragment = mainActivity.getTaskListFragment();
            if (taskListFragment != null) {
                taskListFragment.removeTaskListListener(this);
            }
        }
        
        // 解绑服务
        if (isServiceBound) {
            getContext().unbindService(serviceConnection);
            isServiceBound = false;
        }
        
        // 注销广播接收器
        if (timerReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(timerReceiver);
        }
        
        // 关闭对话框以防止窗口泄漏
        if (batteryOptimizationDialog != null && batteryOptimizationDialog.isShowing()) {
            batteryOptimizationDialog.dismiss();
            batteryOptimizationDialog = null;
        }
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext() != null) {
            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            String packageName = getContext().getPackageName();
            
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog();
            }
        }
    }

    private void showBatteryOptimizationDialog() {
        // 检查Fragment是否还在活动状态
        if (!isAdded() || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed() || getContext() == null) {
            return;
        }
        
        // 如果已有对话框在显示，先关闭
        if (batteryOptimizationDialog != null && batteryOptimizationDialog.isShowing()) {
            batteryOptimizationDialog.dismiss();
        }
        
        try {
            // 使用自定义布局创建对话框
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_battery_optimization, null);
            
            batteryOptimizationDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setOnDismissListener(dialog -> batteryOptimizationDialog = null)
                    .create();
            
            // 设置按钮点击事件
            Button btnGoSettings = dialogView.findViewById(R.id.btn_go_settings);
            Button btnLater = dialogView.findViewById(R.id.btn_later);
            
            btnGoSettings.setOnClickListener(v -> {
                        Intent intent = null;
                        Log.d("TomatoFragment", "Battery optimization dialog clicked");
                        
                        // 优先尝试直接请求忽略电池优化
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext() != null) {
                            intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                            // 添加Intent标志确保新任务启动
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.d("TomatoFragment", "Checking ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS availability");
                            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                Log.d("TomatoFragment", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS available, starting...");
                                try {
                                    startActivity(intent);
                                    Log.d("TomatoFragment", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS started successfully");
                                    // 关闭对话框并添加延迟确保Intent完全启动
                                    batteryOptimizationDialog.dismiss();
                                    return;
                                } catch (Exception e) {
                                    Log.w("TomatoFragment", "Failed to start ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", e);
                                }
                            } else {
                                Log.d("TomatoFragment", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS not available");
                            }
                        }
                        
                        // 尝试打开电池优化设置页面
                        if (getContext() != null) {
                            intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.d("TomatoFragment", "Checking ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS availability");
                            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                Log.d("TomatoFragment", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS available, starting...");
                                try {
                                    startActivity(intent);
                                    Log.d("TomatoFragment", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS started successfully");
                                    batteryOptimizationDialog.dismiss();
                                    return;
                                } catch (Exception e) {
                                    Log.w("TomatoFragment", "Failed to start ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS", e);
                                }
                            } else {
                                Log.d("TomatoFragment", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS not available");
                            }
                        }
                        
                        // 最后尝试打开应用设置页面
                        if (getContext() != null) {
                            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.d("TomatoFragment", "Starting ACTION_APPLICATION_DETAILS_SETTINGS as fallback");
                            try {
                                startActivity(intent);
                                Log.d("TomatoFragment", "ACTION_APPLICATION_DETAILS_SETTINGS started successfully");
                                batteryOptimizationDialog.dismiss();
                            } catch (Exception e) {
                                Log.e("TomatoFragment", "Failed to start any settings activity", e);
                                Toast.makeText(getContext(), "无法打开设置页面，请手动前往应用设置", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            
            btnLater.setOnClickListener(v -> {
                Log.d("TomatoFragment", "用户点击了稍后按钮");
                batteryOptimizationDialog.dismiss();
            });
            
            batteryOptimizationDialog.show();
        } catch (Exception e) {
            Log.e("TomatoFragment", "显示电池优化对话框时出错", e);
            batteryOptimizationDialog = null;
        }
    }
}