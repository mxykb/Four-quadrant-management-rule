package com.example.fourquadrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * 提醒功能主页面，包含提醒列表和日历视图两个标签
 */
public class ReminderMainFragment extends Fragment {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabNewReminder;
    private ReminderPagerAdapter pagerAdapter;
    private TabLayoutMediator mediator;
    
    // Fragment实例缓存
    private ReminderListFragment reminderListFragment;
    private ReminderCalendarFragment reminderCalendarFragment;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_main, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViewPager();
        setupFab();
        setInitialTab();
    }
    
    @Override
    public void onDestroy() {
        // 清理ViewPager2的适配器和mediator
        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        super.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // 完全禁用状态保存
        // 不调用super.onSaveInstanceState(outState)
    }
    
    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.reminder_tab_layout);
        viewPager = view.findViewById(R.id.reminder_view_pager);
        fabNewReminder = view.findViewById(R.id.fab_new_reminder);
        
        // 预创建Fragment实例
        if (reminderListFragment == null) {
            reminderListFragment = new ReminderListFragment();
        }
        if (reminderCalendarFragment == null) {
            reminderCalendarFragment = new ReminderCalendarFragment();
        }
    }
    
    private void setupViewPager() {
        pagerAdapter = new ReminderPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // 完全禁用状态保存和恢复
        viewPager.setSaveEnabled(false);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setSaveFromParentEnabled(false);
        
        // 连接TabLayout和ViewPager2
        mediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("提醒列表");
                    tab.setIcon(R.drawable.ic_notification);
                    break;
                case 1:
                    tab.setText("日历视图");
                    tab.setIcon(R.drawable.ic_calendar);
                    break;
            }
        });
        mediator.attach();
    }
    
    private void setupFab() {
        fabNewReminder.setOnClickListener(v -> {
            // 根据当前选中的tab跳转到新建提醒页面
            if (getActivity() instanceof MainActivity) {
                String sourceTab = getCurrentSourceTab();
                ((MainActivity) getActivity()).showNewReminderPage(sourceTab);
            }
        });
    }
    
    /**
     * 获取当前页面的来源标识
     * @return "list" 或 "calendar"
     */
    private String getCurrentSourceTab() {
        if (viewPager != null) {
            int currentPosition = viewPager.getCurrentItem();
            return currentPosition == 1 ? "calendar" : "list";
        }
        return "list";
    }
    
    /**
     * 设置初始选中的tab
     */
    private void setInitialTab() {
        Bundle args = getArguments();
        if (args != null && viewPager != null) {
            int initialTab = args.getInt("initial_tab", 0);
            // 延迟设置，确保ViewPager完全初始化
            viewPager.post(() -> {
                if (initialTab >= 0 && initialTab < 2) {
                    viewPager.setCurrentItem(initialTab, false);
                }
            });
        }
    }
    
    /**
     * 提醒页面的ViewPager适配器
     * 使用预创建的Fragment实例，避免状态恢复问题
     */
    private class ReminderPagerAdapter extends FragmentStateAdapter {
        
        public ReminderPagerAdapter(@NonNull Fragment fragment) {
            super(fragment.getChildFragmentManager(), fragment.getViewLifecycleOwner().getLifecycle());
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // 返回预创建的Fragment实例
            switch (position) {
                case 0:
                    return reminderListFragment;
                case 1:
                    return reminderCalendarFragment;
                default:
                    return reminderListFragment;
            }
        }
        
        @Override
        public int getItemCount() {
            return 2;
        }
        
        @Override
        public long getItemId(int position) {
            // 返回稳定的ID
            return position + 1000; // 使用唯一的基数避免冲突
        }
        
        @Override
        public boolean containsItem(long itemId) {
            return itemId >= 1000 && itemId < 1000 + getItemCount();
        }
    }
} 