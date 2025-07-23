package com.example.fourquadrant;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
    private SharedPreferences prefs;
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
        
        prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
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
        etTomatoCount.setText(String.valueOf(prefs.getInt(KEY_TOMATO_COUNT, 4)));
        etTomatoDuration.setText(String.valueOf(prefs.getInt(KEY_TOMATO_DURATION, 25)));
        etBreakDuration.setText(String.valueOf(prefs.getInt(KEY_BREAK_DURATION, 5)));
        cbAutoNext.setChecked(prefs.getBoolean(KEY_AUTO_NEXT, false));
    }
    
    private void saveSettings() {
        try {
            int tomatoCount = Integer.parseInt(etTomatoCount.getText().toString());
            int tomatoDuration = Integer.parseInt(etTomatoDuration.getText().toString());
            int breakDuration = Integer.parseInt(etBreakDuration.getText().toString());
            
            prefs.edit()
                    .putInt(KEY_TOMATO_COUNT, tomatoCount)
                    .putInt(KEY_TOMATO_DURATION, tomatoDuration)
                    .putInt(KEY_BREAK_DURATION, breakDuration)
                    .putBoolean(KEY_AUTO_NEXT, cbAutoNext.isChecked())
                    .apply();
        } catch (NumberFormatException e) {
            // 输入错误时不保存
        }
    }
    
    public static int getTomatoCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TOMATO_COUNT, 4);
    }
    
    public static int getTomatoDuration(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TOMATO_DURATION, 25);
    }
    
    public static int getBreakDuration(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_BREAK_DURATION, 5);
    }
    
    public static boolean isAutoNextEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_NEXT, false);
    }
} 