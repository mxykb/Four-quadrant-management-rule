package com.example.fourquadrant;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class StatisticsDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // 创建自定义布局
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_statistics, null);
        
        builder.setView(view)
               .setTitle("任务统计")
               .setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        
        return builder.create();
    }
} 