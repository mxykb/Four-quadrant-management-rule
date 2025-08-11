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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ReminderSettingsDialog extends DialogFragment {
    
    private CheckBox cbVibrate;
    private CheckBox cbRing;
    private com.example.fourquadrant.database.repository.SettingsRepository settingsRepository;
    
    private static final String PREF_NAME = "ReminderSettings";
    private static final String KEY_VIBRATE = "vibrate_enabled";
    private static final String KEY_RING = "ring_enabled";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reminder_settings, null);
        
        cbVibrate = view.findViewById(R.id.cb_vibrate);
        cbRing = view.findViewById(R.id.cb_ring);
        Button saveButton = view.findViewById(R.id.btn_save_reminder);
        
        settingsRepository = new com.example.fourquadrant.database.repository.SettingsRepository(requireActivity().getApplication());
        
        // 加载保存的设置
        loadSettings();
        
        saveButton.setOnClickListener(v -> {
            saveSettings();
            dismiss();
        });
        
        builder.setView(view);
        return builder.create();
    }
    
    private void loadSettings() {
        if (settingsRepository != null) {
            LiveData<Boolean> vibrateLiveData = settingsRepository.getBooleanSetting(KEY_VIBRATE);
            vibrateLiveData.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean value) {
                    cbVibrate.setChecked(value != null ? value : true);
                    vibrateLiveData.removeObserver(this);
                }
            });
            
            LiveData<Boolean> ringLiveData = settingsRepository.getBooleanSetting(KEY_RING);
            ringLiveData.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean value) {
                    cbRing.setChecked(value != null ? value : true);
                    ringLiveData.removeObserver(this);
                }
            });
        } else {
            // 设置默认值
            cbVibrate.setChecked(true);
            cbRing.setChecked(true);
        }
    }
    
    private void saveSettings() {
        if (settingsRepository != null) {
            settingsRepository.saveBooleanSetting(KEY_VIBRATE, cbVibrate.isChecked());
            settingsRepository.saveBooleanSetting(KEY_RING, cbRing.isChecked());
        }
    }
    
    public static boolean getVibrateEnabled(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                Boolean value = repo.getBooleanSettingSync(KEY_VIBRATE);
                return value != null ? value : true;
            } catch (Exception e) {
                return true; // 默认启用振动
            }
        }
        return true; // 默认启用振动
    }
    
    public static boolean getRingEnabled(Context context) {
        if (context.getApplicationContext() instanceof android.app.Application) {
            com.example.fourquadrant.database.repository.SettingsRepository repo = 
                new com.example.fourquadrant.database.repository.SettingsRepository((android.app.Application) context.getApplicationContext());
            try {
                Boolean value = repo.getBooleanSettingSync(KEY_RING);
                return value != null ? value : true;
            } catch (Exception e) {
                return true; // 默认启用响铃
            }
        }
        return true; // 默认启用响铃
    }
} 