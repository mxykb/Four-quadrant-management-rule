// 声明包名，用于组织代码结构
package com.example.fourquadrant;

// 导入所需的类
import android.Manifest;                     // 权限相关类
import android.content.pm.PackageManager;     // 包管理相关类
import android.os.Bundle;                     // 用于保存Activity状态
import android.view.View;                     // 视图类
import android.widget.ImageButton;            // 图片按钮控件
import android.widget.TextView;               // 文本控件
import androidx.appcompat.widget.Toolbar;     // 工具栏控件
import androidx.drawerlayout.widget.DrawerLayout; // 抽屉布局
import androidx.core.view.GravityCompat;      // 重力相关常量
import com.google.android.material.navigation.NavigationView; // 导航视图
import com.google.android.material.appbar.AppBarLayout; // 应用栏布局
import androidx.appcompat.app.ActionBarDrawerToggle; // 抽屉切换按钮
import androidx.activity.OnBackPressedCallback; // 返回键回调

import androidx.activity.result.ActivityResultLauncher; // 活动结果启动器
import androidx.activity.result.contract.ActivityResultContracts; // 活动结果契约
import androidx.appcompat.app.AppCompatActivity; // 兼容的活动基类
import androidx.core.content.ContextCompat;     // 上下文兼容类
import androidx.viewpager2.widget.ViewPager2;   // 分页控件

import com.google.android.material.tabs.TabLayout; // 标签栏
import com.google.android.material.tabs.TabLayoutMediator; // 标签与分页的中介

import java.util.List;                         // 列表类
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.FrameLayout;
import com.example.fourquadrant.StatisticsFragment;
import com.example.fourquadrant.TomatoFragment;
import com.example.fourquadrant.TimerFragment;
import com.example.fourquadrant.UserFragment;

// 主活动类，继承自AppCompatActivity并实现TaskListFragment.TaskListListener接口
public class MainActivity extends AppCompatActivity implements TaskListFragment.TaskListListener {
    
    // 声明成员变量
    private TabLayout tabLayout;                // 标签栏控件
    private ViewPager2 viewPager;               // 分页容器控件
    private MainPagerAdapter pagerAdapter;      // 分页适配器
    private QuadrantChartFragment quadrantChartFragment; // 四象限图表碎片
    private DrawerLayout drawerLayout;          // 抽屉布局
    private NavigationView navigationView;      // 导航视图
    private Toolbar toolbar;                    // 工具栏
    private ImageButton floatingMenuButton;     // 悬浮菜单按钮
    private FrameLayout statisticsContainer;
    private StatisticsFragment statisticsFragment;
    private TomatoFragment tomatoFragment;
    private TimerFragment timerFragment;
    private UserFragment userFragment;
    
    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    // Activity创建时调用的生命周期方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类方法
        setContentView(R.layout.activity_main); // 设置Activity的布局文件
        
        setupSystemUI();          // 设置系统UI（状态栏、导航栏等）
        setupPermissionLauncher(); // 设置权限请求启动器
        initViews();              // 初始化视图控件
        setupViewPager();         // 设置分页控件
        setupBackPressHandler();  // 设置返回键处理
    }
    
    // 初始化视图控件的方法
    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout); // 获取标签栏控件
        viewPager = findViewById(R.id.view_pager); // 获取分页容器控件
        
        // 初始化侧边栏相关控件
        drawerLayout = findViewById(R.id.drawer_layout); // 获取抽屉布局
        navigationView = findViewById(R.id.nav_view);   // 获取导航视图
        toolbar = findViewById(R.id.toolbar);           // 获取工具栏
        floatingMenuButton = findViewById(R.id.floating_menu_button); // 获取悬浮菜单按钮
        statisticsContainer = findViewById(R.id.statistics_container);
        
        // 设置工具栏
        setSupportActionBar(toolbar); // 设置工具栏为支持的操作栏
        if (getSupportActionBar() != null) { // 检查操作栏是否为空
            getSupportActionBar().setTitle(""); // 设置操作栏标题为空
        }
        
        // 设置悬浮菜单按钮的点击事件
        floatingMenuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) { // 检查抽屉是否打开
                drawerLayout.closeDrawer(GravityCompat.START); // 关闭抽屉
            } else {
                drawerLayout.openDrawer(GravityCompat.START); // 打开抽屉
            }
        });
        
        setupNavigationDrawer(); // 设置导航抽屉
    }
    
    // 设置返回键处理的方法
    private void setupBackPressHandler() {
        // 创建返回键回调
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) { // 检查抽屉是否打开
                    drawerLayout.closeDrawer(GravityCompat.START); // 关闭抽屉
                } else {
                    setEnabled(false); // 禁用当前回调
                    getOnBackPressedDispatcher().onBackPressed(); // 调用默认的返回键处理
                }
            }
        };
        
        getOnBackPressedDispatcher().addCallback(this, callback); // 注册回调
    }
    
    // 设置导航抽屉的方法
    private void setupNavigationDrawer() {
        // 设置导航项选中监听器
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId(); // 获取选中项的ID
            
            if (id == R.id.nav_task_management) { // 任务管理
                showTabs();
                drawerLayout.closeDrawers(); // 关闭抽屉
                return true;
            } else if (id == R.id.nav_statistics) { // 统计功能
                showStatisticsPage();
                drawerLayout.closeDrawers(); // 关闭抽屉
                return true;
            } else if (id == R.id.nav_tomato) {
                showTomatoPage();
                drawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_timer) {
                showTimerPage();
                drawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_user) { // 用户功能
                showUserPage(); // 显示用户页面
                drawerLayout.closeDrawers(); // 关闭抽屉
                return true;
            }
            
            return false;
        });
    }

    private void showStatisticsPage() {
        // 隐藏Tab和ViewPager，仅显示统计页面
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        if (statisticsFragment == null) {
            statisticsFragment = new StatisticsFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, statisticsFragment);
        ft.commit();
    }

    private void showTabs() {
        // 显示Tab和ViewPager，隐藏统计页面
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        statisticsContainer.setVisibility(View.GONE);
    }

    private void showTomatoPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        if (tomatoFragment == null) {
            tomatoFragment = new TomatoFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, tomatoFragment);
        ft.commit();
    }

    private void showTimerPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        if (timerFragment == null) {
            timerFragment = new TimerFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, timerFragment);
        ft.commit();
    }

    private void showUserPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        if (userFragment == null) {
            userFragment = new UserFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, userFragment);
        ft.commit();
    }
    
    // 显示统计对话框的方法
    private void showStatistics() {
        StatisticsDialog dialog = new StatisticsDialog(); // 创建统计对话框
        dialog.show(getSupportFragmentManager(), "statistics_dialog"); // 显示对话框
    }
    
    // 设置系统UI的方法
    private void setupSystemUI() {
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT); // 设置状态栏透明
        
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT); // 设置导航栏透明
        
        // 简化系统UI设置，减少闪烁
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 检查Android版本
            getWindow().setDecorFitsSystemWindows(false); // 设置装饰不适合系统窗口
        }
    }
    
    
    // 设置分页控件的方法
    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this); // 创建分页适配器
        viewPager.setAdapter(pagerAdapter); // 设置分页适配器
        
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
        }).attach(); // 附加到标签栏和分页控件
        
        // 监听页面切换
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position); // 调用父类方法
                
                switch (position) {
                    case 1: // 四象限图表
                        // 获取四象限图表Fragment
                        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof QuadrantChartFragment) {
                                quadrantChartFragment = (QuadrantChartFragment) fragment;
                                break;
                            }
                        }
                        
                        // 同步任务数据到四象限图表
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
    
    // 设置权限请求启动器的方法
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
    
    // 实现TaskListListener接口的方法，当任务列表更新时调用
    @Override
    public void onTasksUpdated(List<QuadrantView.Task> tasks) {
        // 通知四象限图表Fragment更新
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
    
    // 通知所有Fragment更新数据的方法
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
    
    
    
    // Activity恢复时调用的生命周期方法
    @Override
    protected void onResume() {
        super.onResume(); // 调用父类方法
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE); // 请求权限
        }
    }
}