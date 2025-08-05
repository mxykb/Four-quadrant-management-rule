package com.example.fourquadrant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends Fragment implements TaskListFragment.TaskListListener {
    
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
    private SharedPreferences prefs;
    private Gson gson;
    
    // 常量
    private static final String PREF_NAME = "TimerReminder";
    private static final String KEY_REMINDERS = "saved_reminders";
    private static final String KEY_VIBRATE = "vibrate_enabled";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_REPEAT = "repeat_enabled";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initData();
        setupListeners();
        loadTaskList();
        loadSettings();
    }
    
    private void initViews(View view) {
        etReminderContent = view.findViewById(R.id.et_reminder_content);
        spinnerTaskSelect = view.findViewById(R.id.spinner_task_select);
        btn5min = view.findViewById(R.id.btn_5min);
        btn15min = view.findViewById(R.id.btn_15min);
        btn30min = view.findViewById(R.id.btn_30min);
        btnSelectTime = view.findViewById(R.id.btn_select_time);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSetReminder = view.findViewById(R.id.btn_set_reminder);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);
        cbVibrate = view.findViewById(R.id.cb_vibrate);
        cbSound = view.findViewById(R.id.cb_sound);
        cbRepeat = view.findViewById(R.id.cb_repeat);
    }
    
    private void initData() {
        prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        taskNames = new ArrayList<>();
        taskNames.add("无关联任务");
        
        taskAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, taskNames);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskSelect.setAdapter(taskAdapter);
        
        selectedDateTime = Calendar.getInstance();
    }
    
    private void setupListeners() {
        btn5min.setOnClickListener(v -> setQuickTime(5));
        btn15min.setOnClickListener(v -> setQuickTime(15));
        btn30min.setOnClickListener(v -> setQuickTime(30));
        
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());
        
        btnCancel.setOnClickListener(v -> clearForm());
        
        btnSetReminder.setOnClickListener(v -> setReminder());
        
        // 设置变更时保存
        cbVibrate.setOnCheckedChangeListener((button, isChecked) -> saveSettings());
        cbSound.setOnCheckedChangeListener((button, isChecked) -> saveSettings());
        cbRepeat.setOnCheckedChangeListener((button, isChecked) -> saveSettings());
    }
    
    private void setQuickTime(int minutes) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes);
        selectedDateTime = now;
        updateTimeDisplay();
        resetQuickButtons();
        highlightButton(minutes == 5 ? btn5min : minutes == 15 ? btn15min : btn30min);
    }
    
    private void showTimePickerDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selected.set(Calendar.MINUTE, minute);
                selected.set(Calendar.SECOND, 0);
                
                // 如果选择的时间已经过了，设置为明天
                if (selected.before(Calendar.getInstance())) {
                    selected.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                selectedDateTime = selected;
                updateTimeDisplay();
                resetQuickButtons();
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }
    
    private void updateTimeDisplay() {
        if (selectedDateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            tvSelectedTime.setText(sdf.format(selectedDateTime.getTime()));
            tvSelectedTime.setTextColor(getResources().getColor(R.color.purple_primary, null));
        }
    }
    
    private void resetQuickButtons() {
        btn5min.setBackgroundResource(R.drawable.button_quick_time);
        btn15min.setBackgroundResource(R.drawable.button_quick_time);
        btn30min.setBackgroundResource(R.drawable.button_quick_time);
    }
    
    private void highlightButton(Button button) {
        button.setBackgroundResource(R.drawable.button_primary);
    }
    
    private void setReminder() {
        String content = etReminderContent.getText().toString().trim();
        
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "请输入提醒内容", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDateTime == null || selectedDateTime.before(Calendar.getInstance())) {
            Toast.makeText(getContext(), "请选择有效的提醒时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建提醒对象
        ReminderItem reminder = new ReminderItem();
        reminder.id = generateUniqueId();
        reminder.content = content;
        reminder.taskName = spinnerTaskSelect.getSelectedItem().toString();
        reminder.reminderTime = selectedDateTime.getTimeInMillis();
        reminder.isVibrate = cbVibrate.isChecked();
        reminder.isSound = cbSound.isChecked();
        reminder.isRepeat = cbRepeat.isChecked();
        reminder.isActive = true;
        
        // 保存提醒
        saveReminder(reminder);
        
        // 设置系统通知
        scheduleNotification(reminder);
        
        // 显示确认消息
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        String timeStr = sdf.format(new Date(reminder.reminderTime));
        Toast.makeText(getContext(), "提醒已设置：" + timeStr, Toast.LENGTH_LONG).show();
        
        // 清空表单
        clearForm();
    }
    
    private void saveReminder(ReminderItem reminder) {
        List<ReminderItem> reminders = loadReminders();
        reminders.add(reminder);
        
        String json = gson.toJson(reminders);
        prefs.edit().putString(KEY_REMINDERS, json).apply();
    }
    
    private List<ReminderItem> loadReminders() {
        String json = prefs.getString(KEY_REMINDERS, "[]");
        Type type = new TypeToken<List<ReminderItem>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    private void scheduleNotification(ReminderItem reminder) {
        try {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            intent.putExtra("reminder_id", reminder.id);
            intent.putExtra("reminder_content", reminder.content);
            intent.putExtra("reminder_vibrate", reminder.isVibrate);
            intent.putExtra("reminder_sound", reminder.isSound);
            intent.putExtra("reminder_repeat", reminder.isRepeat);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.reminderTime,
                    pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "设置提醒失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearForm() {
        etReminderContent.setText("");
        spinnerTaskSelect.setSelection(0);
        selectedDateTime = null;
        tvSelectedTime.setText("未设置");
        tvSelectedTime.setTextColor(getResources().getColor(R.color.text_secondary, null));
        resetQuickButtons();
    }
    
    private void loadTaskList() {
        // 尝试从TaskListFragment获取任务列表
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.addTaskListListener(this);
        }
        
        // 从其他Fragment获取任务
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                for (androidx.fragment.app.Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof TaskListFragment) {
                        TaskListFragment taskListFragment = (TaskListFragment) fragment;
                        List<TaskListFragment.TaskItem> activeTasks = taskListFragment.getActiveTasks();
                        updateTaskSpinner(activeTasks);
                        break;
                    }
                }
            }
        }, 100);
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
    
    private void loadSettings() {
        cbVibrate.setChecked(prefs.getBoolean(KEY_VIBRATE, true));
        cbSound.setChecked(prefs.getBoolean(KEY_SOUND, true));
        cbRepeat.setChecked(prefs.getBoolean(KEY_REPEAT, false));
    }
    
    private void saveSettings() {
        prefs.edit()
            .putBoolean(KEY_VIBRATE, cbVibrate.isChecked())
            .putBoolean(KEY_SOUND, cbSound.isChecked())
            .putBoolean(KEY_REPEAT, cbRepeat.isChecked())
            .apply();
    }
    
    private String generateUniqueId() {
        return "reminder_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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
    
    // 提醒项数据类
    public static class ReminderItem {
        public String id;
        public String content;
        public String taskName;
        public long reminderTime;
        public boolean isVibrate;
        public boolean isSound;
        public boolean isRepeat;
        public boolean isActive;
    }
} 