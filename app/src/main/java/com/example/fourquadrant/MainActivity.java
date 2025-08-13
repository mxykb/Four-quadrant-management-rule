// 声明包名，用于组织代码结构
package com.example.fourquadrant;

// 导入所需的类
import android.Manifest;                     // 权限相关类
import android.content.pm.PackageManager;     // 包管理相关类
import android.content.SharedPreferences;     // 共享偏好设置
import android.os.Bundle;                     // 用于保存Activity状态
import android.os.Build;
import android.os.PowerManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
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
import com.example.fourquadrant.SettingsFragment;
import com.example.fourquadrant.database.migration.DataMigrationManager;
import com.example.fourquadrant.utils.VersionManager;
import android.util.Log;

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
    private ReminderMainFragment reminderMainFragment;
    private NewReminderFragment newReminderFragment;
    private UserFragment userFragment;
    private SettingsFragment settingsFragment;
    
    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    // 当前页面状态管理
    private String currentPageState = "main"; // main, statistics, tomato, reminder, user, settings
    private boolean isFirstResume = true; // 标记是否是第一次onResume
    
    // 数据迁移管理器
    // DataMigrationManager已移至Application中管理
    
    // Activity创建时调用的生命周期方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类方法
        setContentView(R.layout.activity_main); // 设置Activity的布局文件
        
        // 如果有状态恢复问题，清除Fragment状态
        if (savedInstanceState != null) {
            try {
                // 尝试正常恢复状态
                setupSystemUI();          // 设置系统UI（状态栏、导航栏等）
                setupPermissionLauncher(); // 设置权限请求启动器
                initViews();              // 初始化视图控件
                setupViewPager();         // 设置分页控件
                setupBackPressHandler();  // 设置返回键处理
            } catch (Exception e) {
                // 如果恢复失败，清除Fragment状态并重新创建
                getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .commitNow();
                recreate();
                return;
            }
        } else {
            setupSystemUI();          // 设置系统UI（状态栏、导航栏等）
            setupPermissionLauncher(); // 设置权限请求启动器
            initViews();              // 初始化视图控件
            setupViewPager();         // 设置分页控件
            setupBackPressHandler();  // 设置返回键处理
        }
        
        // 数据库初始化已在Application中完成，这里不再重复初始化
        
        // 验证版本信息是否正确存储到数据库
        verifyVersionInfo();
        
        // 检查是否需要显示提醒弹窗
        handleReminderIntent(getIntent());
        
        // 只有在从后台恢复时才恢复页面状态，新启动时不恢复
        // restorePageState() 只在 onResume 中调用
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
    
    // 数据库初始化方法已移至Application中
    
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
                showReminderMainPage();
                drawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_user) { // 用户功能
                showUserPage(); // 显示用户页面
                drawerLayout.closeDrawers(); // 关闭抽屉
                return true;
            } else if (id == R.id.nav_settings) { // 设置功能
                showSettingsPage(); // 显示设置页面
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
        currentPageState = "statistics";
        
        // 每次都创建新的StatisticsFragment实例，确保数据刷新
        statisticsFragment = new StatisticsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setReorderingAllowed(true);
        ft.replace(R.id.statistics_container, statisticsFragment);
        ft.commitNow();
    }

    public void showTabs() {
        // 显示Tab和ViewPager，隐藏统计页面
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        statisticsContainer.setVisibility(View.GONE);
        currentPageState = "main";
    }

    private void showTomatoPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        currentPageState = "tomato";
        if (tomatoFragment == null) {
            tomatoFragment = new TomatoFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, tomatoFragment);
        ft.commit();
    }

    public void showReminderMainPage() {
        showReminderMainPageWithTab(0); // 默认显示列表tab
    }

    public void showReminderMainPageWithTab(int tabIndex) {
        try {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            statisticsContainer.setVisibility(View.VISIBLE);
            currentPageState = "reminder";
            
            // 清除之前的Fragment状态
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setReorderingAllowed(true);
            
            // 每次都创建新的ReminderMainFragment实例
            reminderMainFragment = new ReminderMainFragment();
            Bundle args = new Bundle();
            args.putInt("initial_tab", tabIndex);
            reminderMainFragment.setArguments(args);
            
            ft.replace(R.id.statistics_container, reminderMainFragment);
            ft.commitNow();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，尝试重新创建
            recreate();
        }
    }

    public void showNewReminderPage() {
        showNewReminderPage("list"); // 默认从列表进入
    }

    public void showNewReminderPage(String sourceTab) {
        try {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            statisticsContainer.setVisibility(View.VISIBLE);
            
            // 清除之前的Fragment状态
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setReorderingAllowed(true);
            
            newReminderFragment = NewReminderFragment.newInstance(sourceTab);
            ft.replace(R.id.statistics_container, newReminderFragment);
            ft.commitNow();
        } catch (Exception e) {
            e.printStackTrace();
            showReminderMainPage();
        }
    }

    public void showEditReminderPage(ReminderItem reminder) {
        showEditReminderPage(reminder, "list"); // 默认从列表进入
    }

    public void showEditReminderPage(ReminderItem reminder, String sourceTab) {
        try {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            statisticsContainer.setVisibility(View.VISIBLE);
            
            // 清除之前的Fragment状态
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setReorderingAllowed(true);
            
            newReminderFragment = NewReminderFragment.newInstance(reminder, sourceTab);
            ft.replace(R.id.statistics_container, newReminderFragment);
            ft.commitNow();
        } catch (Exception e) {
            e.printStackTrace();
            showReminderMainPage();
        }
    }

    private void showUserPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        currentPageState = "user";
        if (userFragment == null) {
            userFragment = new UserFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, userFragment);
        ft.commit();
    }
    
    private void showSettingsPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        currentPageState = "settings";
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, settingsFragment);
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
        
        // 设置保存策略，减少状态恢复问题
        viewPager.setSaveEnabled(false);
        viewPager.setOffscreenPageLimit(1);
        
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
                    android.widget.Toast.makeText(this, "通知权限被拒绝，可能无法正常显示通知", android.widget.Toast.LENGTH_LONG).show();
                }
            }
        );
        
        // 检查并请求通知权限
        checkAndRequestNotificationPermission();
        
        // 检查电池优化设置
        checkBatteryOptimization();
    }
    
    private void checkAndRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog();
            }
        }
    }
    
    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("电池优化设置")
                .setMessage("为了确保提醒功能在后台正常运行，请将此应用添加到电池优化白名单中。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        // 如果上述方法失败，尝试打开电池优化设置页面
                        try {
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            startActivity(intent);
                        } catch (Exception ex) {
                            // 如果都失败了，打开应用设置页面
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("稍后", null)
                .show();
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
    
    // 添加任务列表监听器的方法
    public void addTaskListListener(TaskListFragment.TaskListListener listener) {
        // 查找TaskListFragment并添加监听器
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof TaskListFragment) {
                TaskListFragment taskListFragment = (TaskListFragment) fragment;
                taskListFragment.addTaskListListener(listener);
                break;
            }
        }
    }
    
    // 通知所有Fragment更新数据的方法
    public void notifyFragmentsUpdate() {
        System.out.println("notifyFragmentsUpdate: starting...");
        
        // 通知任务列表Fragment更新
        boolean foundTaskList = false;
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof TaskListFragment) {
                TaskListFragment taskListFragment = (TaskListFragment) fragment;
                taskListFragment.refreshTasksFromDatabase();
                foundTaskList = true;
                System.out.println("notifyFragmentsUpdate: found and updated TaskListFragment");
                break;
            }
        }
        if (!foundTaskList) {
            System.out.println("notifyFragmentsUpdate: TaskListFragment not found");
        }
        
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
        
        // 通知四象限图表Fragment更新
        if (quadrantChartFragment != null) {
            // 四象限图表会通过TaskListListener自动更新，这里不需要额外操作
            System.out.println("notifyFragmentsUpdate: QuadrantChartFragment will be updated via TaskListListener");
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
        
        // 只有从后台恢复时才恢复页面状态，避免在onCreate后重复恢复
        if (!isFirstResume) {
            restorePageState();
        }
        isFirstResume = false;
    }
    
    /**
     * 恢复页面状态
     */
    private void restorePageState() {
        // 从SharedPreferences恢复页面状态
        SharedPreferences prefs = getSharedPreferences("MainActivity", MODE_PRIVATE);
        currentPageState = prefs.getString("currentPageState", "main");
        
        // 检查是否是应用重新启动（而不是从后台恢复）
        // 如果是重新启动，应该清除状态并显示主页面
        boolean isAppRestart = prefs.getBoolean("isAppRestart", true);
        
        if (isAppRestart) {
            // 应用重新启动，清除状态并显示主页面
            currentPageState = "main";
            prefs.edit()
                .putString("currentPageState", "main")
                .putBoolean("isAppRestart", false)
                .apply();
            showTabs();
            return;
        }
        
        // 检查是否需要恢复非主页面状态
        if (!"main".equals(currentPageState)) {
            // 检查当前Fragment是否正确
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.statistics_container);
            boolean needRestore = false;
            
            if ("reminder".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof ReminderMainFragment);
            } else if ("statistics".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof StatisticsFragment);
            } else if ("tomato".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof TomatoFragment);
            } else if ("user".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof UserFragment);
            } else if ("settings".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof SettingsFragment);
            }
            
            if (needRestore) {
                // 延迟恢复，确保Activity完全准备好
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    switch (currentPageState) {
                        case "reminder":
                            showReminderMainPage();
                            break;
                        case "statistics":
                            showStatisticsPage();
                            break;
                        case "tomato":
                            showTomatoPage();
                            break;
                        case "user":
                            showUserPage();
                            break;
                        case "settings":
                            showSettingsPage();
                            break;
                        default:
                            showTabs();
                            break;
                    }
                }, 100);
            }
        }
    }
    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleReminderIntent(intent);
    }
    
    private void handleReminderIntent(android.content.Intent intent) {
        if (intent != null && intent.getBooleanExtra("show_reminder_dialog", false)) {
            String reminderId = intent.getStringExtra("reminder_id");
            String reminderContent = intent.getStringExtra("reminder_content");
            String reminderTaskName = intent.getStringExtra("reminder_task_name");
            boolean canRepeat = intent.getBooleanExtra("reminder_repeat", false);
            
            if (reminderId != null && reminderContent != null) {
                // 延迟显示弹窗，确保Activity完全加载
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    showReminderDialog(reminderId, reminderContent, reminderTaskName, canRepeat);
                }, 500);
            }
        }
    }
    
    private void showReminderDialog(String reminderId, String content, String taskName, boolean canRepeat) {
        try {
            ReminderDialogFragment dialog = ReminderDialogFragment.newInstance(reminderId, content, taskName, canRepeat);
            dialog.show(getSupportFragmentManager(), "ReminderDialog");
        } catch (Exception e) {
            e.printStackTrace();
            String displayText = content;
            if (taskName != null && !taskName.trim().isEmpty()) {
                displayText = "任务：" + taskName + "\n" + content;
            }
            android.widget.Toast.makeText(this, "提醒：" + displayText, android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 保存当前页面状态到SharedPreferences
        savePageState();
        
        // 清除所有Fragment状态以避免恢复问题
        try {
            // 清除主ViewPager的状态
            outState.clear();
            
            // 清除统计容器中的Fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.statistics_container);
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .remove(currentFragment)
                    .commitNowAllowingStateLoss();
            }
            
            // 清除所有子Fragment的状态
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment != null) {
                    fragment.onSaveInstanceState(new Bundle()); // 传入空Bundle
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        super.onSaveInstanceState(outState);
    }
    
    /**
     * 保存当前页面状态
     */
    private void savePageState() {
        SharedPreferences prefs = getSharedPreferences("MainActivity", MODE_PRIVATE);
        prefs.edit().putString("currentPageState", currentPageState).apply();
    }
    
    @Override
    protected void onDestroy() {
        // 如果应用真正被销毁（不是屏幕旋转等），设置重启标记
        if (isFinishing()) {
            SharedPreferences prefs = getSharedPreferences("MainActivity", MODE_PRIVATE);
            prefs.edit().putBoolean("isAppRestart", true).apply();
        }
        
        // 清理ViewPager适配器
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        super.onDestroy();
    }
    
    // 添加缺失的方法
    public TaskListFragment getTaskListFragment() {
        if (pagerAdapter != null) {
            return pagerAdapter.getTaskListFragment();
        }
        return null;
    }
    
    /**
     * 验证版本信息是否正确存储到数据库
     */
    private void verifyVersionInfo() {
        try {
            FourQuadrantApplication app = (FourQuadrantApplication) getApplication();
            VersionManager versionManager = app.getVersionManager();
            
            if (versionManager != null) {
                // 在后台线程中验证版本信息
                new Thread(() -> {
                    try {
                        // 等待一下确保版本信息已经初始化完成
                        Thread.sleep(1000);
                        
                        String currentVersion = versionManager.getCurrentVersionName();
                        int currentVersionCode = versionManager.getCurrentVersionCode();
                        String storedVersion = versionManager.getStoredVersionName();
                        Integer storedVersionCode = versionManager.getStoredVersionCode();
                        long installTime = versionManager.getInstallTime();
                        long lastUpdateTime = versionManager.getLastUpdateTime();
                        boolean isFirstInstall = versionManager.isFirstInstall();
                        boolean isVersionUpdated = versionManager.isVersionUpdated();
                        
                        Log.d("VersionInfo", "=== 版本信息验证 ===");
                        Log.d("VersionInfo", "当前版本名称: " + currentVersion);
                        Log.d("VersionInfo", "当前版本代码: " + currentVersionCode);
                        Log.d("VersionInfo", "存储的版本名称: " + storedVersion);
                        Log.d("VersionInfo", "存储的版本代码: " + storedVersionCode);
                        Log.d("VersionInfo", "安装时间: " + new java.util.Date(installTime));
                        Log.d("VersionInfo", "最后更新时间: " + new java.util.Date(lastUpdateTime));
                        Log.d("VersionInfo", "是否首次安装: " + isFirstInstall);
                        Log.d("VersionInfo", "是否版本更新: " + isVersionUpdated);
                        Log.d("VersionInfo", "版本摘要: " + versionManager.getVersionSummary());
                        Log.d("VersionInfo", "=== 版本信息验证完成 ===");
                        
                    } catch (Exception e) {
                        Log.e("VersionInfo", "验证版本信息时出错: " + e.getMessage(), e);
                    }
                }).start();
            } else {
                Log.w("VersionInfo", "VersionManager 尚未初始化");
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "获取版本信息时出错: " + e.getMessage(), e);
        }
    }
}