package com.example.fourquadrant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class IconPickerDialog extends DialogFragment {
    
    public interface IconSelectedListener {
        void onIconSelected(String icon);
    }
    
    private IconSelectedListener listener;
    private String[] icons = {"🌞", "📚", "💻", "✏️", "🎯", "🏃", "🧘", "🎨", "🔬", "📖"};
    
    public static IconPickerDialog newInstance(IconSelectedListener listener) {
        IconPickerDialog dialog = new IconPickerDialog();
        dialog.listener = listener;
        return dialog;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_icon_picker, null);
        
        GridLayout gridLayout = view.findViewById(R.id.icon_grid);
        
        // 动态添加图标按钮
        for (String icon : icons) {
            Button iconButton = new Button(getContext());
            iconButton.setText(icon);
            iconButton.setTextSize(24);
            iconButton.setBackgroundResource(R.drawable.btn_circle_green);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120;
            params.height = 120;
            params.setMargins(16, 16, 16, 16);
            iconButton.setLayoutParams(params);
            
            iconButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconSelected(icon);
                }
                dismiss();
            });
            
            gridLayout.addView(iconButton);
        }
        
        builder.setView(view)
               .setTitle("选择图标")
               .setNegativeButton("取消", (dialog, which) -> dismiss());
        
        return builder.create();
    }
} 