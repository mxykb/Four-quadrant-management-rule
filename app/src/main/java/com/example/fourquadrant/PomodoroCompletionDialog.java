package com.example.fourquadrant;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;

/**
 * 番茄钟完成提醒弹窗
 */
public class PomodoroCompletionDialog extends Dialog {
    
    public interface OnActionListener {
        void onStartBreak();
        void onSkipBreak();
        void onClose();
    }
    
    private TextView tvTitle;
    private TextView tvTaskName;
    private TextView tvPomodoroCount;
    private Button btnStartBreak;
    private Button btnSkipBreak;

    private Button btnClose;
    
    private OnActionListener actionListener;
    private Context context;
    
    public PomodoroCompletionDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initDialog();
    }
    
    private void initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pomodoro_completion);
        setCancelable(false); // 不允许点击外部关闭
        
        initViews();
        setupClickListeners();
        
        // 设置弹窗居中显示
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvTaskName = findViewById(R.id.tv_task_name);
        tvPomodoroCount = findViewById(R.id.tv_pomodoro_count);
        btnStartBreak = findViewById(R.id.btn_start_break);
        btnSkipBreak = findViewById(R.id.btn_skip_break);

        btnClose = findViewById(R.id.btn_close);
    }
    
    private void setupClickListeners() {
        btnStartBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onStartBreak();
                }
                dismiss();
            }
        });
        
        btnSkipBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onSkipBreak();
                }
                dismiss();
            }
        });
        

        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onClose();
                }
                dismiss();
            }
        });
    }
    
    /**
     * 设置弹窗内容
     * @param taskName 任务名称
     * @param currentCount 当前番茄钟轮次
     * @param totalCount 总番茄钟数量
     */
    public void setContent(String taskName, int currentCount, int totalCount) {
        if (tvTaskName != null) {
            tvTaskName.setText(taskName != null ? taskName : "未选择任务");
        }
        if (tvPomodoroCount != null) {
            tvPomodoroCount.setText("第" + currentCount + "个番茄钟完成");
        }
        
        // 如果已完成所有番茄钟，禁用开始休息和跳过休息按钮
        boolean hasMorePomodoros = currentCount < totalCount;
        
        if (btnStartBreak != null) {
            btnStartBreak.setEnabled(hasMorePomodoros);
            btnStartBreak.setAlpha(hasMorePomodoros ? 1.0f : 0.5f);
        }
        
        if (btnSkipBreak != null) {
            btnSkipBreak.setEnabled(hasMorePomodoros);
            btnSkipBreak.setAlpha(hasMorePomodoros ? 1.0f : 0.5f);
        }
    }
    
    /**
     * 设置操作监听器
     */
    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }
    
    /**
     * 播放提醒声音和震动
     */
    public void playReminder() {
        // 播放提醒声音
        playReminderSound();
        
        // 震动提醒
        playVibration();
    }
    
    private void playReminderSound() {
        try {
            // 检查是否启用声音提醒
            boolean soundEnabled = ReminderSettingsDialog.getRingEnabled(context);
            if (soundEnabled) {
                // 使用系统默认提示音
                android.media.RingtoneManager.getRingtone(
                    context, 
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                ).play();
            }
        } catch (Exception e) {
            // 忽略播放错误
        }
    }
    
    private void playVibration() {
        try {
            // 检查是否启用震动提醒
            boolean vibrationEnabled = ReminderSettingsDialog.getVibrateEnabled(context);
            if (vibrationEnabled) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // 震动模式：短震-停顿-短震-停顿-长震
                    long[] pattern = {0, 200, 100, 200, 100, 500};
                    vibrator.vibrate(pattern, -1); // -1表示不重复
                }
            }
        } catch (Exception e) {
            // 忽略震动错误
        }
    }
    
    @Override
    public void show() {
        super.show();
        // 显示弹窗时播放提醒
        playReminder();
    }
}