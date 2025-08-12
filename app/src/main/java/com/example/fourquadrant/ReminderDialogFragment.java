package com.example.fourquadrant;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderDialogFragment extends DialogFragment {
    
    private static final String TAG = "ReminderDialogFragment";
    private static final String ARG_REMINDER_ID = "reminder_id";
    private static final String ARG_REMINDER_CONTENT = "reminder_content";
    private static final String ARG_REMINDER_TASK_NAME = "reminder_task_name";
    private static final String ARG_REMINDER_REPEAT = "reminder_repeat";
    
    private String reminderId;
    private String reminderContent;
    private String reminderTaskName;
    private boolean canRepeat;
    
    public static ReminderDialogFragment newInstance(String reminderId, String content, boolean canRepeat) {
        return newInstance(reminderId, content, null, canRepeat);
    }
    
    public static ReminderDialogFragment newInstance(String reminderId, String content, String taskName, boolean canRepeat) {
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REMINDER_ID, reminderId);
        args.putString(ARG_REMINDER_CONTENT, content);
        args.putString(ARG_REMINDER_TASK_NAME, taskName);
        args.putBoolean(ARG_REMINDER_REPEAT, canRepeat);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reminderId = getArguments().getString(ARG_REMINDER_ID);
            reminderContent = getArguments().getString(ARG_REMINDER_CONTENT);
            reminderTaskName = getArguments().getString(ARG_REMINDER_TASK_NAME);
            canRepeat = getArguments().getBoolean(ARG_REMINDER_REPEAT, false);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reminder_notification, null);
        
        // 初始化视图
        TextView tvReminderIcon = dialogView.findViewById(R.id.tv_reminder_icon);
        TextView tvReminderTitle = dialogView.findViewById(R.id.tv_reminder_title);
        TextView tvReminderContent = dialogView.findViewById(R.id.tv_reminder_content);
        TextView tvReminderTask = dialogView.findViewById(R.id.tv_reminder_task);
        TextView tvCurrentTime = dialogView.findViewById(R.id.tv_current_time);
        Button btnDismiss = dialogView.findViewById(R.id.btn_dismiss);
        Button btnSnooze = dialogView.findViewById(R.id.btn_snooze);
        
        // 设置内容
        tvReminderContent.setText(reminderContent);
        
        // 设置关联任务
        if (reminderTaskName != null && !reminderTaskName.trim().isEmpty()) {
            tvReminderTask.setText("📋 关联任务：" + reminderTaskName);
            tvReminderTask.setVisibility(View.VISIBLE);
            // 更新标题为任务提醒
            tvReminderTitle.setText("任务提醒");
        } else {
            tvReminderTask.setVisibility(View.GONE);
            // 保持默认标题
            tvReminderTitle.setText("定时提醒");
        }
        
        // 显示当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvCurrentTime.setText("当前时间：" + sdf.format(Calendar.getInstance().getTime()));
        
        // 设置按钮点击事件
        btnDismiss.setOnClickListener(v -> {
            handleDismiss();
            dismiss();
        });
        
        btnSnooze.setOnClickListener(v -> {
            handleSnooze();
            dismiss();
        });
        
        // 如果不能重复提醒，隐藏稍后提醒按钮
        if (!canRepeat) {
            btnSnooze.setVisibility(View.GONE);
        }
        
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setCancelable(true);
        
        AlertDialog dialog = builder.create();
        
        // 设置对话框样式
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
        
        return dialog;
    }
    
    private void handleDismiss() {
        Log.d(TAG, "用户在应用内选择知道了: " + reminderId);
        Toast.makeText(getContext(), "提醒已完成", Toast.LENGTH_SHORT).show();
        
        // 这里可以添加完成提醒的业务逻辑
        // 比如标记提醒为已完成、记录日志等
    }
    
    private void handleSnooze() {
        Log.d(TAG, "用户在应用内选择稍后提醒: " + reminderId);
        
        // 设置5分钟后的提醒
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, 5);
        
        try {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            intent.putExtra("reminder_id", reminderId + "_snooze_app");
            intent.putExtra("reminder_content", reminderContent);
            intent.putExtra("reminder_vibrate", true);
            intent.putExtra("reminder_sound", true);
            intent.putExtra("reminder_repeat", false); // 稍后提醒不再重复
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (reminderId + "_snooze_app").hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    pendingIntent
                );
                
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String snoozeTimeStr = sdf.format(snoozeTime.getTime());
                Toast.makeText(getContext(), "将在 " + snoozeTimeStr + " 再次提醒", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "设置稍后提醒失败", e);
            Toast.makeText(getContext(), "设置稍后提醒失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // 设置对话框大小
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                -2 // wrap_content
            );
        }
    }
}