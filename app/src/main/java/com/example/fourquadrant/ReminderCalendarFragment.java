package com.example.fourquadrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日历视图Fragment
 */
public class ReminderCalendarFragment extends Fragment implements ReminderManager.ReminderManagerListener {
    
    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView selectedDateReminders;
    private ReminderListAdapter adapter;
    private ReminderManager reminderManager;
    private List<ReminderItem> selectedDateReminderList;
    private Date selectedDate;
    private boolean useSharedManager = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_calendar, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupCalendar();
        setupRecyclerView();
        
        // 默认显示今天的提醒
        selectedDate = new Date();
        updateSelectedDateReminders();
    }
    
    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        selectedDateText = view.findViewById(R.id.selected_date_text);
        selectedDateReminders = view.findViewById(R.id.selected_date_reminders);
        
        // 如果没有设置共享的ReminderManager，则创建新实例
        if (reminderManager == null && !useSharedManager) {
            reminderManager = new ReminderManager(requireContext());
            reminderManager.addListener(this);
        }
        selectedDateReminderList = new ArrayList<>();
    }
    
    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();
            updateSelectedDateReminders();
        });
    }
    
    private void setupRecyclerView() {
        adapter = new ReminderListAdapter(selectedDateReminderList, new ReminderListAdapter.ReminderActionListener() {
            @Override
            public void onEditReminder(ReminderItem reminder) {
                // 编辑提醒 - 从日历页面进入
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showEditReminderPage(reminder, "calendar");
                }
            }
            
            @Override
            public void onDeleteReminder(ReminderItem reminder) {
                reminderManager.deleteReminder(reminder.getId());
            }
            
            @Override
            public void onSnoozeReminder(ReminderItem reminder) {
                long newTime = reminder.getReminderTime() + (5 * 60 * 1000);
                reminder.setReminderTime(newTime);
                reminderManager.updateReminder(reminder);
            }
            
            @Override
            public void onToggleReminder(ReminderItem reminder) {
                // 使用ReminderManager的toggleReminderActive方法
                // 这会自动处理闹钟的设置和取消
                boolean success = reminderManager.toggleReminderActive(reminder);
                if (!success) {
                    // 操作失败时刷新列表以恢复开关状态
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        
        selectedDateReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedDateReminders.setAdapter(adapter);
    }
    
    private void updateSelectedDateReminders() {
        if (selectedDate == null) return;
        
        selectedDateReminderList.clear();
        selectedDateReminderList.addAll(reminderManager.getRemindersForDate(selectedDate));
        
        // 更新标题
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        String dateStr = String.format("%d年%d月%d日", 
            cal.get(Calendar.YEAR), 
            cal.get(Calendar.MONTH) + 1, 
            cal.get(Calendar.DAY_OF_MONTH));
        selectedDateText.setText(dateStr + " 的提醒 (" + selectedDateReminderList.size() + "条)");
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reminderManager != null) {
            reminderManager.removeListener(this);
        }
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // 禁用状态保存以避免ViewPager2恢复问题
        // 不调用super.onSaveInstanceState(outState)
    }
    
    // ReminderManager.ReminderManagerListener 实现
    @Override
    public void onRemindersChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::updateSelectedDateReminders);
        }
    }
    
    @Override
    public void onReminderAdded(ReminderItem reminder) {
        onRemindersChanged();
    }
    
    @Override
    public void onReminderUpdated(ReminderItem reminder) {
        onRemindersChanged();
    }
    
    @Override
    public void onReminderDeleted(ReminderItem reminder) {
        onRemindersChanged();
    }
    
    /**
     * 设置共享的ReminderManager（从ReminderMainFragment传入）
     */
    public void setSharedReminderManager(ReminderManager sharedManager) {
        // 如果已经有监听器，先移除
        if (reminderManager != null) {
            reminderManager.removeListener(this);
        }
        
        this.reminderManager = sharedManager;
        this.useSharedManager = true;
        
        if (reminderManager != null) {
            reminderManager.addListener(this);
            System.out.println("ReminderCalendarFragment: 使用共享的ReminderManager");
        }
    }
}