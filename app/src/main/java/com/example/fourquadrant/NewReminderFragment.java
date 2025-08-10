package com.example.fourquadrant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewReminderFragment extends Fragment implements TaskListFragment.TaskListListener {
    
    // UI 组件
    private EditText etReminderContent;
    private Spinner spinnerTaskSelect;
    private Button btn5min, btn15min, btn30min, btnSelectTime, btnCancel, btnSetReminder;
    private TextView tvSelectedTime;
    private CheckBox cbVibrate, cbSound, cbRepeat;
    
    // 数据相关
    private List<String> taskNames;
    private ArrayAdapter<String> taskAdapter;
    private Calendar selectedDateTime;
    private ReminderManager reminderManager;
    private ReminderItem editingReminder; // 编辑模式下的提醒项
    private boolean isEditMode = false;
    private String sourceTab = "list"; // 来源页面：list(列表) 或 calendar(日历)
    
    public static NewReminderFragment newInstance() {
        return newInstance("list");
    }
    
    public static NewReminderFragment newInstance(String sourceTab) {
        NewReminderFragment fragment = new NewReminderFragment();
        Bundle args = new Bundle();
        args.putString("source_tab", sourceTab);
        fragment.setArguments(args);
        return fragment;
    }
    
    public static NewReminderFragment newInstance(ReminderItem reminder, String sourceTab) {
        NewReminderFragment fragment = new NewReminderFragment();
        Bundle args = new Bundle();
        args.putString("reminder_id", reminder.getId());
        args.putString("source_tab", sourceTab);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_reminder, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initData();
        setupListeners();
        checkEditMode();
        loadTaskList();
    }
    
    private void initViews(View view) {
        etReminderContent = view.findViewById(R.id.et_reminder_content);
        spinnerTaskSelect = view.findViewById(R.id.spinner_task_select);
        btn5min = view.findViewById(R.id.btn_5min);
        btn15min = view.findViewById(R.id.btn_15min);
        btn30min = view.findViewById(R.id.btn_30min);
        btnSelectTime = view.findViewById(R.id.btn_select_time);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);
        cbVibrate = view.findViewById(R.id.cb_vibrate);
        cbSound = view.findViewById(R.id.cb_sound);
        cbRepeat = view.findViewById(R.id.cb_repeat);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSetReminder = view.findViewById(R.id.btn_set_reminder);
        
        // 尝试获取共享的ReminderManager
        reminderManager = getSharedReminderManager();
        if (reminderManager == null) {
            reminderManager = new ReminderManager(requireContext());
        }
    }
    
    private void initData() {
        taskNames = new ArrayList<>();
        taskNames.add("无关联任务");
        
        taskAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, taskNames);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskSelect.setAdapter(taskAdapter);
        
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.add(Calendar.MINUTE, 5); // 默认5分钟后
        updateSelectedTimeDisplay();
    }
    
    private void checkEditMode() {
        Bundle args = getArguments();
        if (args != null) {
            // 获取来源页面
            sourceTab = args.getString("source_tab", "list");
            
            // 检查是否是编辑模式
            if (args.containsKey("reminder_id")) {
                String reminderId = args.getString("reminder_id");
                editingReminder = reminderManager.getReminderById(reminderId);
                if (editingReminder != null) {
                    isEditMode = true;
                    loadReminderData();
                    btnSetReminder.setText("更新提醒");
                }
            }
        }
    }
    
    private void loadReminderData() {
        if (editingReminder == null) return;
        
        etReminderContent.setText(editingReminder.getContent());
        
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTimeInMillis(editingReminder.getReminderTime());
        updateSelectedTimeDisplay();
        
        cbVibrate.setChecked(editingReminder.isVibrate());
        cbSound.setChecked(editingReminder.isSound());
        cbRepeat.setChecked(editingReminder.isRepeat());
        
        // 设置任务关联
        String taskName = editingReminder.getTaskName();
        if (taskName != null && !taskName.isEmpty()) {
            for (int i = 0; i < taskNames.size(); i++) {
                if (taskNames.get(i).equals(taskName)) {
                    spinnerTaskSelect.setSelection(i);
                    break;
                }
            }
        }
    }
    
    private void setupListeners() {
        btn5min.setOnClickListener(v -> setQuickTime(5));
        btn15min.setOnClickListener(v -> setQuickTime(15));
        btn30min.setOnClickListener(v -> setQuickTime(30));
        
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        
        btnCancel.setOnClickListener(v -> navigateBack());
        
        btnSetReminder.setOnClickListener(v -> saveReminder());
    }
    
    private void setQuickTime(int minutes) {
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.add(Calendar.MINUTE, minutes);
        updateSelectedTimeDisplay();
    }
    
    private void showTimePicker() {
        Calendar current = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) -> {
                selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);
                
                // 如果选择的时间已经过了今天，则设置为明天
                if (selectedDateTime.before(Calendar.getInstance())) {
                    selectedDateTime.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                updateSelectedTimeDisplay();
            },
            current.get(Calendar.HOUR_OF_DAY),
            current.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }
    
    private void updateSelectedTimeDisplay() {
        if (selectedDateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            tvSelectedTime.setText(sdf.format(selectedDateTime.getTime()));
            tvSelectedTime.setTextColor(getResources().getColor(R.color.purple_primary, null));
        }
    }
    
    private void loadTaskList() {
        // 直接从SharedPreferences加载任务列表，而不是依赖TaskListFragment
        loadTasksFromPreferences();
        
        // 同时注册监听器以获取后续更新
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).addTaskListListener(this);
        }
    }
    
    /**
     * 直接从SharedPreferences加载任务列表
     */
    private void loadTasksFromPreferences() {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("TaskListPrefs", Context.MODE_PRIVATE);
            String tasksJson = prefs.getString("saved_tasks", "[]");
            
            Gson gson = new Gson();
            Type type = new TypeToken<List<TaskListFragment.TaskItem>>(){}.getType();
            List<TaskListFragment.TaskItem> allTasks = gson.fromJson(tasksJson, type);
            
            if (allTasks != null) {
                // 过滤出活跃的任务（未完成的任务）
                List<TaskListFragment.TaskItem> activeTasks = new ArrayList<>();
                for (TaskListFragment.TaskItem task : allTasks) {
                    if (!task.isCompleted()) {
                        activeTasks.add(task);
                    }
                }
                updateTaskSpinner(activeTasks);
            } else {
                updateTaskSpinner(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，至少保证有"无关联任务"选项
            updateTaskSpinner(new ArrayList<>());
        }
    }
    
    private void saveReminder() {
        String content = etReminderContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "请输入提醒内容", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDateTime.before(Calendar.getInstance())) {
            Toast.makeText(getContext(), "提醒时间不能是过去的时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查权限
        if (!hasAlarmPermission()) {
            Toast.makeText(getContext(), "需要闹钟权限来设置提醒", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            ReminderItem reminder;
            if (isEditMode && editingReminder != null) {
                // 编辑模式
                reminder = editingReminder;
                reminder.setContent(content);
                reminder.setReminderTime(selectedDateTime.getTimeInMillis());
            } else {
                // 新建模式
                reminder = new ReminderItem(content, selectedDateTime.getTimeInMillis());
            }
            
            // 设置关联任务
            String selectedTask = (String) spinnerTaskSelect.getSelectedItem();
            if (selectedTask != null && !"无关联任务".equals(selectedTask)) {
                reminder.setTaskName(selectedTask);
            } else {
                reminder.setTaskName(null);
            }
            
            // 设置选项
            reminder.setVibrate(cbVibrate.isChecked());
            reminder.setSound(cbSound.isChecked());
            reminder.setRepeat(cbRepeat.isChecked());
            
            // 保存到数据库
            if (isEditMode) {
                reminderManager.updateReminder(reminder);
            } else {
                reminderManager.addReminder(reminder);
            }
            
            // 使用ReminderManager统一管理闹钟设置
            // 只有激活状态的提醒才设置系统闹钟
            if (reminder.isActive()) {
                reminderManager.scheduleAlarm(reminder);
            }
            
            Toast.makeText(getContext(), 
                isEditMode ? "提醒已更新" : "提醒已设置", 
                Toast.LENGTH_SHORT).show();
            
            // 返回正确的页面
            navigateBack();
            
        } catch (Exception e) {
            Toast.makeText(getContext(), "设置提醒失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean hasAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
    
    /**
     * 根据来源页面返回到正确的位置
     */
    private void navigateBack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if ("calendar".equals(sourceTab)) {
                // 从日历页面进入，返回到日历页面并切换到日历tab
                activity.showReminderMainPageWithTab(1); // 1 = 日历tab
            } else {
                // 从列表页面进入，返回到列表页面并切换到列表tab
                activity.showReminderMainPageWithTab(0); // 0 = 列表tab
            }
        }
    }
    
    private void scheduleAlarm(ReminderItem reminder) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_content", reminder.getContent());
        intent.putExtra("vibrate", reminder.isVibrate());
        intent.putExtra("sound", reminder.isSound());
        intent.putExtra("repeat", reminder.isRepeat());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            getContext(),
            reminder.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTime(),
                    pendingIntent
                );
            } catch (SecurityException e) {
                Toast.makeText(getContext(), "无法设置精确闹钟，请检查权限设置", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void updateTaskSpinner(List<TaskListFragment.TaskItem> tasks) {
        taskNames.clear();
        taskNames.add("无关联任务");
        
        if (tasks != null) {
            for (TaskListFragment.TaskItem task : tasks) {
                taskNames.add(task.getName());
            }
        }
        
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onTasksUpdated(List<QuadrantView.Task> tasks) {
        if (tasks != null && getActivity() != null) {
            List<TaskListFragment.TaskItem> taskItems = new ArrayList<>();
            for (QuadrantView.Task task : tasks) {
                TaskListFragment.TaskItem item = new TaskListFragment.TaskItem();
                item.setName(task.getName());
                item.setImportance(task.getImportance());
                item.setUrgency(task.getUrgency());
                taskItems.add(item);
            }
            updateTaskSpinner(taskItems);
        }
    }
    
    /**
     * 获取共享的ReminderManager
     * 尝试从当前Activity的ReminderMainFragment获取
     */
    private ReminderManager getSharedReminderManager() {
        try {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // 从Fragment管理器中查找ReminderMainFragment
                for (Fragment fragment : mainActivity.getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof ReminderMainFragment) {
                        ReminderMainFragment mainFragment = (ReminderMainFragment) fragment;
                        ReminderManager manager = mainFragment.getSharedReminderManager();
                        if (manager != null) {
                            System.out.println("NewReminderFragment: 成功获取共享的ReminderManager");
                            return manager;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("NewReminderFragment: 获取共享ReminderManager失败: " + e.getMessage());
        }
        
        System.out.println("NewReminderFragment: 未找到共享的ReminderManager，将创建新实例");
        return null;
    }
} 