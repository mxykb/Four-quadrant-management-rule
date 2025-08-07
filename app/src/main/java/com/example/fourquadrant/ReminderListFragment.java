package com.example.fourquadrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * 提醒列表Fragment
 */
public class ReminderListFragment extends Fragment implements ReminderManager.ReminderManagerListener {
    
    private RecyclerView recyclerView;
    private ReminderListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private ReminderManager reminderManager;
    private List<ReminderItem> reminders;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadReminders();
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.reminder_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyView = view.findViewById(R.id.empty_view);
        
        reminderManager = new ReminderManager(requireContext());
        reminderManager.addListener(this);
        reminders = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        adapter = new ReminderListAdapter(reminders, new ReminderListAdapter.ReminderActionListener() {
            @Override
            public void onEditReminder(ReminderItem reminder) {
                // 编辑提醒
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showEditReminderPage(reminder);
                }
            }
            
            @Override
            public void onDeleteReminder(ReminderItem reminder) {
                // 删除提醒
                reminderManager.deleteReminder(reminder.getId());
            }
            
            @Override
            public void onSnoozeReminder(ReminderItem reminder) {
                // 稍后提醒（延迟5分钟）
                long newTime = reminder.getReminderTime() + (5 * 60 * 1000);
                reminder.setReminderTime(newTime);
                reminderManager.updateReminder(reminder);
            }
            
            @Override
            public void onToggleReminder(ReminderItem reminder) {
                // 切换提醒激活状态
                reminder.setActive(!reminder.isActive());
                reminderManager.updateReminder(reminder);
            }
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
            getResources().getColor(R.color.purple_primary, null)
        );
        swipeRefreshLayout.setOnRefreshListener(this::refreshReminders);
    }
    
    private void loadReminders() {
        reminders.clear();
        reminders.addAll(reminderManager.getActiveReminders());
        updateUI();
    }
    
    private void refreshReminders() {
        // 清理过期提醒
        reminderManager.cleanupPastReminders();
        loadReminders();
        swipeRefreshLayout.setRefreshing(false);
    }
    
    private void updateUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        // 显示/隐藏空状态
        if (reminders.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadReminders();
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
            getActivity().runOnUiThread(this::loadReminders);
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
} 