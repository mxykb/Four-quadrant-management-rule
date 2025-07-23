package com.example.fourquadrant;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
    private SharedPreferences prefs;
    
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
        
        prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
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
        cbVibrate.setChecked(prefs.getBoolean(KEY_VIBRATE, true));
        cbRing.setChecked(prefs.getBoolean(KEY_RING, true));
    }
    
    private void saveSettings() {
        prefs.edit()
                .putBoolean(KEY_VIBRATE, cbVibrate.isChecked())
                .putBoolean(KEY_RING, cbRing.isChecked())
                .apply();
    }
    
    public static boolean isVibrateEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_VIBRATE, true);
    }
    
    public static boolean isRingEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_RING, true);
    }
} 