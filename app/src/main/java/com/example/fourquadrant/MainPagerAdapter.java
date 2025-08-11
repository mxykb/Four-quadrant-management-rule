package com.example.fourquadrant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {
    
    private TaskListFragment taskListFragment;
    
    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                if (taskListFragment == null) {
                    taskListFragment = new TaskListFragment();
                }
                return taskListFragment;
            case 1:
                return new QuadrantChartFragment();
            case 2:
                return new CompletedTasksFragment();
            case 3:
                return new TaskSortFragment();
            default:
                return new TaskListFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return 4;
    }
    
    @Override
    public long getItemId(int position) {
        // 返回稳定的ID以帮助状态恢复
        return position;
    }
    
    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < getItemCount();
    }
    
    // 添加获取TaskListFragment的方法
    public TaskListFragment getTaskListFragment() {
        return taskListFragment;
    }
} 