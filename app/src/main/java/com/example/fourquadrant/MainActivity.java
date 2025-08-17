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
import com.example.fourquadrant.AiToolsFragment;
import com.example.fourquadrant.database.migration.DataMigrationManager;
import com.example.fourquadrant.utils.VersionManager;
import com.example.fourquadrant.utils.PermissionManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
    private AiToolsFragment aiToolsFragment;
    
    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    // 权限提示栏相关
    private LinearLayout permissionNotificationBar;
    private TextView permissionText;
    private ImageButton permissionCloseBtn;
    private Handler permissionHandler;
    private Runnable scrollRunnable;
    private boolean isPermissionBarVisible = false;
    private long lastPermissionCheckTime = 0;
    private static final long PERMISSION_CHECK_INTERVAL = 2000; // 2秒间隔
    
    // 当前页面状态管理
    private String currentPageState = "main"; // main, statistics, tomato, reminder, user, settings
    private boolean isFirstResume = true; // 标记是否是第一次onResume
    
    // 对话框管理
    private AlertDialog batteryOptimizationDialog;
    
    // 数据迁移管理器
    // DataMigrationManager已移至Application中管理
    
    // Activity创建时调用的生命周期方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类方法
        setContentView(R.layout.activity_main); // 设置Activity的布局文件
        
        // 恢复保存的状态
        if (savedInstanceState != null) {
            try {
                currentPageState = savedInstanceState.getString("currentPageState", "main");
                isFirstResume = savedInstanceState.getBoolean("isFirstResume", true);
                Log.d("MainActivity", "Restored state: currentPageState=" + currentPageState + ", isFirstResume=" + isFirstResume);
            } catch (Exception e) {
                Log.e("MainActivity", "Error restoring saved state", e);
            }
        }
        
        try {
            // 统一初始化流程
            setupSystemUI();          // 设置系统UI（状态栏、导航栏等）
            setupPermissionLauncher(); // 设置权限请求启动器
            initViews();              // 初始化视图控件
            setupViewPager();         // 设置分页控件
            setupBackPressHandler();  // 设置返回键处理
            
            // 数据库初始化已在Application中完成，这里不再重复初始化
            
            // 验证版本信息是否正确存储到数据库
            verifyVersionInfo();
            
            // 检查是否需要显示提醒弹窗
            handleReminderIntent(getIntent());
            
        } catch (Exception e) {
            // 记录异常但不重新创建Activity，避免无限循环
            Log.e("MainActivity", "Error during onCreate initialization", e);
            
            // 尝试基本的初始化，确保应用能够启动
            try {
                if (tabLayout == null || viewPager == null) {
                    initViews();
                }
            } catch (Exception fallbackException) {
                Log.e("MainActivity", "Fallback initialization also failed", fallbackException);
            }
        }
        
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
        
        // 初始化权限提示栏
        initPermissionNotificationBar();
        
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
            } else if (id == R.id.nav_ai_tools) { // 智能工具功能
                showAiToolsPage(); // 显示智能工具页面
                drawerLayout.closeDrawers(); // 关闭抽屉
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

    private void showAiToolsPage() {
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        statisticsContainer.setVisibility(View.VISIBLE);
        currentPageState = "ai_tools";
        if (aiToolsFragment == null) {
            aiToolsFragment = new AiToolsFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.statistics_container, aiToolsFragment);
        ft.commit();
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
                // 权限结果处理，更新权限提示栏状态
                checkPermissionsAndShowBar();
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
        // 检查Activity是否已经销毁或正在销毁
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        // 如果已有对话框在显示，先关闭
        if (batteryOptimizationDialog != null && batteryOptimizationDialog.isShowing()) {
            batteryOptimizationDialog.dismiss();
        }
        
        try {
            // 使用自定义布局创建对话框
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_battery_optimization, null);
            
            batteryOptimizationDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setOnDismissListener(dialog -> batteryOptimizationDialog = null)
                    .create();
            
            // 设置按钮点击事件
            android.widget.Button btnGoSettings = dialogView.findViewById(R.id.btn_go_settings);
            android.widget.Button btnLater = dialogView.findViewById(R.id.btn_later);
            
            btnGoSettings.setOnClickListener(v -> {
                Intent intent = null;
                Log.d("MainActivity", "Battery optimization dialog clicked");
                
                // 优先尝试直接请求忽略电池优化
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    // 添加Intent标志确保新任务启动
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Log.d("MainActivity", "Checking ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS availability");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        Log.d("MainActivity", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS available, starting...");
                        try {
                            startActivity(intent);
                            Log.d("MainActivity", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS started successfully");
                            // 关闭对话框并添加延迟确保Intent完全启动
                            batteryOptimizationDialog.dismiss();
                            return;
                        } catch (Exception e) {
                            Log.w("MainActivity", "Failed to start ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", e);
                        }
                    } else {
                        Log.d("MainActivity", "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS not available");
                    }
                }
                
                // 尝试打开电池优化设置页面
                intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.d("MainActivity", "Checking ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS availability");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    Log.d("MainActivity", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS available, starting...");
                    try {
                        startActivity(intent);
                        Log.d("MainActivity", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS started successfully");
                        batteryOptimizationDialog.dismiss();
                        return;
                    } catch (Exception e) {
                        Log.w("MainActivity", "Failed to start ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS", e);
                    }
                } else {
                    Log.d("MainActivity", "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS not available");
                }
                
                // 最后尝试打开应用设置页面
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.d("MainActivity", "Starting ACTION_APPLICATION_DETAILS_SETTINGS as fallback");
                try {
                    startActivity(intent);
                    Log.d("MainActivity", "ACTION_APPLICATION_DETAILS_SETTINGS started successfully");
                    batteryOptimizationDialog.dismiss();
                } catch (Exception e) {
                    Log.e("MainActivity", "Failed to start any settings activity", e);
                    Toast.makeText(MainActivity.this, "无法打开设置页面，请手动前往应用设置", Toast.LENGTH_LONG).show();
                }
            });
            
            btnLater.setOnClickListener(v -> {
                Log.d("MainActivity", "User clicked later button");
                batteryOptimizationDialog.dismiss();
            });
            
            batteryOptimizationDialog.show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error showing battery optimization dialog", e);
            batteryOptimizationDialog = null;
        }
    }
    
    /**
     * 初始化权限提示栏
     */
    private void initPermissionNotificationBar() {
        permissionNotificationBar = findViewById(R.id.permission_notification_bar);
        permissionText = permissionNotificationBar.findViewById(R.id.permission_text);
        permissionCloseBtn = permissionNotificationBar.findViewById(R.id.permission_close_btn);
        permissionHandler = new Handler(Looper.getMainLooper());
        
        // 设置点击事件
        permissionNotificationBar.setOnClickListener(v -> {
            // 点击提示栏打开应用设置
            PermissionManager.openAppSettings(this);
        });
        
        permissionCloseBtn.setOnClickListener(v -> {
            hidePermissionNotificationBar();
        });
        
        // 检查权限并显示提示栏
        checkPermissionsAndShowBar();
    }
    
    /**
     * 检查权限并显示提示栏
     */
    private void checkPermissionsAndShowBar() {
        long currentTime = System.currentTimeMillis();
        
        // 防止频繁检查，间隔时间内不重复检查
        if (currentTime - lastPermissionCheckTime < PERMISSION_CHECK_INTERVAL) {
            return;
        }
        
        lastPermissionCheckTime = currentTime;
        
        List<PermissionManager.PermissionInfo> deniedPermissions = PermissionManager.checkAllPermissions(this);
        
        if (!deniedPermissions.isEmpty()) {
            String permissionText = PermissionManager.generatePermissionText(deniedPermissions);
            showPermissionNotificationBar(permissionText);
        } else {
            hidePermissionNotificationBar();
        }
    }
    
    /**
     * 显示权限提示栏
     */
    private void showPermissionNotificationBar(String text) {
        if (isPermissionBarVisible) {
            // 如果已经显示，只更新文本
            permissionText.setText(text);
            startScrollAnimation();
            return;
        }
        
        permissionText.setText(text);
        permissionNotificationBar.setVisibility(View.VISIBLE);
        isPermissionBarVisible = true;
        
        // 添加滑入动画
        permissionNotificationBar.setTranslationY(-permissionNotificationBar.getHeight());
        permissionNotificationBar.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // 动画结束后开始滚动文本
                    startScrollAnimation();
                })
                .start();
    }
    
    /**
     * 隐藏权限提示栏
     */
    private void hidePermissionNotificationBar() {
        if (!isPermissionBarVisible) {
            return;
        }
        
        stopScrollAnimation();
        
        permissionNotificationBar.animate()
                .translationY(-permissionNotificationBar.getHeight())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    permissionNotificationBar.setVisibility(View.GONE);
                    isPermissionBarVisible = false;
                })
                .start();
    }
    
    /**
     * 开始滚动动画
     */
    private void startScrollAnimation() {
        stopScrollAnimation(); // 先停止之前的动画
        
        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (permissionText != null && isPermissionBarVisible) {
                    // 获取文本宽度和容器宽度
                    int textWidth = (int) permissionText.getPaint().measureText(permissionText.getText().toString());
                    int containerWidth = permissionText.getParent() != null ? 
                            ((View) permissionText.getParent()).getWidth() - permissionText.getPaddingLeft() - permissionText.getPaddingRight() : 0;
                    
                    if (textWidth > containerWidth) {
                        // 需要滚动
                        ObjectAnimator animator = ObjectAnimator.ofFloat(permissionText, "translationX", 0, -(textWidth - containerWidth + 50));
                        animator.setDuration(3000 + (textWidth - containerWidth) * 10); // 根据文本长度调整滚动速度
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.setRepeatCount(ValueAnimator.INFINITE);
                        animator.setRepeatMode(ValueAnimator.RESTART);
                        animator.start();
                        
                        // 延迟后重新开始
                        permissionHandler.postDelayed(this, animator.getDuration() + 1000);
                    } else {
                        // 不需要滚动，延迟后重新检查
                        permissionHandler.postDelayed(this, 5000);
                    }
                }
            }
        };
        
        permissionHandler.postDelayed(scrollRunnable, 1000); // 延迟1秒开始滚动
    }
    
    /**
     * 停止滚动动画
     */
    private void stopScrollAnimation() {
        if (scrollRunnable != null) {
            permissionHandler.removeCallbacks(scrollRunnable);
            scrollRunnable = null;
        }
        
        if (permissionText != null) {
            permissionText.clearAnimation();
            permissionText.setTranslationX(0);
        }
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
        
        // 检查权限并更新提示栏
        checkPermissionsAndShowBar();
        
        // 重新检查电池优化权限状态（从设置页面返回时）
        if (!isFirstResume) {
            checkBatteryOptimization();
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
            } else if ("ai_tools".equals(currentPageState)) {
                needRestore = !(currentFragment instanceof AiToolsFragment);
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
                        case "ai_tools":
                            showAiToolsPage();
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
        
        try {
            // 只保存必要的状态信息，避免过度清除
            outState.putString("currentPageState", currentPageState);
            outState.putBoolean("isFirstResume", isFirstResume);
            
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            Log.e("MainActivity", "Error saving instance state", e);
            // 即使保存状态失败，也要调用父类方法
            super.onSaveInstanceState(outState);
        }
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
        
        // 清理对话框资源，防止窗口泄漏
        if (batteryOptimizationDialog != null && batteryOptimizationDialog.isShowing()) {
            try {
                batteryOptimizationDialog.dismiss();
            } catch (Exception e) {
                Log.e("MainActivity", "Error dismissing battery optimization dialog", e);
            }
            batteryOptimizationDialog = null;
        }
        
        // 清理权限提示栏相关资源
        stopScrollAnimation();
        if (permissionHandler != null) {
            permissionHandler.removeCallbacksAndMessages(null);
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