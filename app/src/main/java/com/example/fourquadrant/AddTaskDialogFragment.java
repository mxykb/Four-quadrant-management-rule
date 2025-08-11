package com.example.fourquadrant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddTaskDialogFragment extends DialogFragment {
    
    public interface TaskAddListener {
        void addTask(String name, int importance, int urgency);
    }
    
    private TaskAddListener taskAddListener;
    private EditText etTaskName;
    private SeekBar sbImportance;
    private SeekBar sbUrgency;
    private TextView tvImportance;
    private TextView tvUrgency;
    
    public void setTaskAddListener(TaskAddListener listener) {
        this.taskAddListener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        
        View view = inflater.inflate(R.layout.dialog_add_task, null);
        
        initViews(view);
        setupSeekBars();
        
        builder.setView(view)
                .setTitle("添加新任务")
                .setPositiveButton("添加", null) // 稍后设置点击事件
                .setNegativeButton("取消", (dialog, id) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        
        // 在对话框显示后设置按钮点击事件，这样验证失败时不会关闭对话框
        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String taskName = etTaskName.getText().toString().trim();
                if (taskName.isEmpty()) {
                    // 显示错误提示并聚焦到输入框
                    etTaskName.setError("请输入任务名称");
                    etTaskName.requestFocus();
                    Toast.makeText(getContext(), "任务名称不能为空", Toast.LENGTH_SHORT).show();
                    return; // 不关闭对话框
                }
                
                int importance = sbImportance.getProgress() + 1; // 1-10
                int urgency = sbUrgency.getProgress() + 1; // 1-10
                
                if (taskAddListener != null) {
                    taskAddListener.addTask(taskName, importance, urgency);
                }
                dialog.dismiss(); // 添加成功后关闭对话框
            });
        });
        
        return dialog;
    }
    
    private void initViews(View view) {
        etTaskName = view.findViewById(R.id.et_task_name);
        sbImportance = view.findViewById(R.id.sb_importance);
        sbUrgency = view.findViewById(R.id.sb_urgency);
        tvImportance = view.findViewById(R.id.tv_importance);
        tvUrgency = view.findViewById(R.id.tv_urgency);
    }
    
    private void setupSeekBars() {
        sbImportance.setMax(9); // 0-9, 显示为1-10
        sbUrgency.setMax(9);
        
        sbImportance.setProgress(4); // 默认5
        sbUrgency.setProgress(4);
        
        updateImportanceText(5);
        updateUrgencyText(5);
        
        sbImportance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateImportanceText(progress + 1);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        sbUrgency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateUrgencyText(progress + 1);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void updateImportanceText(int value) {
        tvImportance.setText("重要性: " + value);
    }
    
    private void updateUrgencyText(int value) {
        tvUrgency.setText("紧急性: " + value);
    }
}
