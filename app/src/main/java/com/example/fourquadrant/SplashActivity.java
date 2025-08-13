package com.example.fourquadrant;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 启动欢迎页Activity
 * 显示应用Logo和名称，延迟几秒后自动跳转到主界面
 */
public class SplashActivity extends AppCompatActivity {
    
    // 欢迎页显示时长（毫秒）
    private static final int SPLASH_DELAY = 2500; // 2.5秒
    
    private Handler handler;
    private Runnable splashRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // 隐藏状态栏和导航栏，实现全屏效果
        setupFullScreen();
        
        // 设置版本信息
        setupVersionInfo();
        
        // 创建延迟跳转的Runnable
        splashRunnable = new Runnable() {
            @Override
            public void run() {
                // 跳转到主Activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // 添加淡入淡出动画效果
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // 结束当前Activity
            }
        };
        
        // 使用Handler延迟执行跳转
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(splashRunnable, SPLASH_DELAY);
    }
    
    /**
     * 设置全屏显示
     */
    private void setupFullScreen() {
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // 设置全屏模式
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler，避免内存泄漏
        if (handler != null && splashRunnable != null) {
            handler.removeCallbacks(splashRunnable);
        }
    }
    
    /**
     * 设置版本信息显示
     */
    private void setupVersionInfo() {
        TextView versionTextView = findViewById(R.id.splash_version);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            versionTextView.setText("v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setText("v1.0.0");
        }
    }
    
    @Override
    public void onBackPressed() {
        // 在欢迎页禁用返回键，避免用户意外退出
        // 不调用super.onBackPressed()，这样返回键就不会有任何效果
    }
}