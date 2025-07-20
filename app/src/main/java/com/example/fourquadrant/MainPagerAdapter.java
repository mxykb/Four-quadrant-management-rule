package com.example.fourquadrant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {
    
    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TaskListFragment();
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
} 