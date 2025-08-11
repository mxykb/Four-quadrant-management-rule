package com.example.fourquadrant;

import android.app.Dialog;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class TomatoSettingsDialog extends DialogFragment {
    
    public interface OnSettingsChangedListener {
        void onSettingsChanged();
    }
    
    private EditText etTomatoCount;
    private EditText etTomatoDuration;
    private EditText etBreakDuration;
    private CheckBox cbAutoNext;
    private com.example.fourquadrant.database.repository.SettingsRepository settingsRepository;
    private OnSettingsChangedListener listener;
    
    private static final String PREF_NAME = "TomatoSettings";
    private static final String KEY_TOMATO_COUNT = "tomato_count";
    private static final String KEY_TOMATO_DURATION = "tomato_duration";
    private static final String KEY_BREAK_DURATION = "break_duration";
    private static final String KEY_AUTO_NEXT = "auto_next";
    
    public void setOnSettingsChangedListener(OnSettingsChangedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tomato_settings, null);
        
        etTomatoCount = view.findViewById(R.id.et_tomato_count);
        etTomatoDuration = view.findViewById(R.id.et_tomato_duration);
        etBreakDuration = view.findViewById(R.id.et_break_duration);
        cbAutoNext = view.findViewById(R.id.cb_auto_next);
        Button saveButton = view.findViewById(R.id.btn_save_tomato);
        
        settingsRepository = new com.example.fourquadrant.database.repository.SettingsRepository(requireActivity().getApplication());
        
        // 加载保存的设置
        loadSettings();
        
        saveButton.setOnClickListener(v -> {
            saveSettings();
            if (listener != null) {
                listener.onSettingsChanged();
            }
            dismiss();
        });
        
        builder.setView(view);
        return builder.create();
    }
    
    private void loadSettings() {
        if (settingsRepository != null) {
            LiveData<Integer> tomatoCountLiveData = settingsRepository.getIntSetting(KEY_TOMATO_COUNT);
            tomatoCountLiveData.observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer value) {
                    etTomatoCount.setText(String.valueOf(value != null ? value : 4));
                    tomatoCountLiveData.removeObserver(this);
                }
            });
            
            LiveData<Integer> tomatoDurationLiveData = settingsRepository.getIntSetting(KEY_TOMATO_DURATION);
            tomatoDurationLiveData.observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer value) {
                    etTomatoDuration.setText(String.valueOf(value != null ? value : 25));
                    tomatoDurationLiveData.removeObserver(this);
                }
            });
            
            LiveData<Integer> breakDurationLiveData = settingsRepository.getIntSetting(KEY_BREAK_DURATION);
            breakDurationLiveData.observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer value) {
                    etBreakDuration.setText(String.valueOf(value != null ? value : 5));
                    breakDurationLiveData.removeObserver(this);
                }
            });
            
            LiveData<Boolean> autoNextLiveData = settingsRepository.getBooleanSetting(KEY_AUTO_NEXT);
            autoNextLiveData.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean value) {
                    cbAutoNext.setChecked(value != null ? value : false);
                    autoNextLiveData.removeObserver(this);
                }
            });
        } else {
            // 设置默认值
            etTomatoCount.setText("4");
            etTomatoDuration.setText("25");
            etBreakDuration.setText("5");
            cbAutoNext.setChecked(false);
        }
    }
    
    private void saveSettings() {
        try {
            int tomatoCount = Integer.parseInt(etTomatoCount.getText().toString());
            int tomatoDuration = Integer.parseInt(etTomatoDuration.getText().toString());
            int breakDuration = Integer.parseInt(etBreakDuration.getText().toString());
            
            if (settingsRepository != null) {
                settingsRepository.saveIntSetting(KEY_TOMATO_COUNT, tomatoCount);
                settingsRepository.saveIntSetting(KEY_TOMATO_DURATION, tomatoDuration);
                settingsRepository.saveIntSetting(KEY_BREAK_DURATION, breakDuration);
                settingsRepository.saveBooleanSetting(KEY_AUTO_NEXT, cbAutoNext.isChecked());
            }
        } catch (NumberFormatException e) {
            // 输入错误时不保存
        }
    }
    
    public static int getTomatoCount(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                // 同步获取，可能返回null
                Integer value = repo.getIntSettingSync(KEY_TOMATO_COUNT);
                return value != null ? value : 4;
            } catch (Exception e) {
                return 4; // 默认值
            }
        }
        return 4; // 默认值
    }
    
    public static int getTomatoDuration(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                Integer value = repo.getIntSettingSync(KEY_TOMATO_DURATION);
                return value != null ? value : 25;
            } catch (Exception e) {
                return 25; // 默认值
            }
        }
        return 25; // 默认值
    }
    
    public static int getBreakDuration(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                Integer value = repo.getIntSettingSync(KEY_BREAK_DURATION);
                return value != null ? value : 5;
            } catch (Exception e) {
                return 5; // 默认值
            }
        }
        return 5; // 默认值
    }
    
    public static boolean getAutoNext(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                Boolean value = repo.getBooleanSettingSync(KEY_AUTO_NEXT);
                return value != null ? value : false;
            } catch (Exception e) {
                return false; // 默认值
            }
        }
        return false; // 默认值
    }
} 