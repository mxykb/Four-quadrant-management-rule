package com.example.fourquadrant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskListFragment.TaskListListener {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MainPagerAdapter pagerAdapter;
    private QuadrantChartFragment quadrantChartFragment;
    
    // 权限请求
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupPermissionLauncher();
        initViews();
        setupViewPager();
    }
    
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }
    
    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // 设置标签页标题
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("任务列表");
                    break;
                case 1:
                    tab.setText("四象限图表");
                    break;
                case 2:
                    tab.setText("已完成");
                    break;
                case 3:
                    tab.setText("任务排序");
                    break;
            }
        }).attach();
        
        // 监听页面切换，获取Fragment引用并同步数据
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                
                switch (position) {
                    case 1: // 四象限图表
                        // 获取四象限图表Fragment
                        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof QuadrantChartFragment) {
                                quadrantChartFragment = (QuadrantChartFragment) fragment;
                                break;
                            }
                        }
                        
                        // 立即同步任务数据到四象限图表
                        if (quadrantChartFragment != null) {
                            // 从TaskListFragment获取当前任务数据
                            TaskListFragment taskListFragment = null;
                            for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                                if (fragment instanceof TaskListFragment) {
                                    taskListFragment = (TaskListFragment) fragment;
                                    break;
                                }
                            }
                            if (taskListFragment != null) {
                                List<QuadrantView.Task> currentTasks = taskListFragment.getCurrentTasks();
                                quadrantChartFragment.updateTasks(currentTasks);
                            }
                        }
                        break;
                        
                    case 2: // 已完成任务
                        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof CompletedTasksFragment) {
                                CompletedTasksFragment completedTasksFragment = (CompletedTasksFragment) fragment;
                                completedTasksFragment.loadCompletedTasks();
                                break;
                            }
                        }
                        break;
                        
                    case 3: // 任务排序
                        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof TaskSortFragment) {
                                TaskSortFragment taskSortFragment = (TaskSortFragment) fragment;
                                taskSortFragment.loadAndSortTasks();
                                break;
                            }
                        }
                        break;
                }
            }
        });
    }
    
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    // 权限被拒绝的处理
                }
            }
        );
    }
    
    @Override
    public void onTasksUpdated(List<QuadrantView.Task> tasks) {
        // 当任务列表更新时，通知四象限图表Fragment
        if (quadrantChartFragment != null) {
            quadrantChartFragment.updateTasks(tasks);
        }
        
        // 如果当前在四象限图表页面，立即更新显示
        if (viewPager.getCurrentItem() == 1) {
            viewPager.post(() -> {
                if (quadrantChartFragment != null) {
                    quadrantChartFragment.updateTasks(tasks);
                }
            });
        }
    }
    
    public void notifyFragmentsUpdate() {
        System.out.println("notifyFragmentsUpdate: starting...");
        
        // 通知已完成任务Fragment更新
        boolean foundCompleted = false;
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof CompletedTasksFragment) {
                CompletedTasksFragment completedTasksFragment = (CompletedTasksFragment) fragment;
                completedTasksFragment.loadCompletedTasks();
                foundCompleted = true;
                System.out.println("notifyFragmentsUpdate: found and updated CompletedTasksFragment");
                break;
            }
        }
        if (!foundCompleted) {
            System.out.println("notifyFragmentsUpdate: CompletedTasksFragment not found");
        }
        
        // 通知任务排序Fragment更新
        boolean foundSort = false;
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof TaskSortFragment) {
                TaskSortFragment taskSortFragment = (TaskSortFragment) fragment;
                taskSortFragment.loadAndSortTasks();
                foundSort = true;
                System.out.println("notifyFragmentsUpdate: found and updated TaskSortFragment");
                break;
            }
        }
        if (!foundSort) {
            System.out.println("notifyFragmentsUpdate: TaskSortFragment not found");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}