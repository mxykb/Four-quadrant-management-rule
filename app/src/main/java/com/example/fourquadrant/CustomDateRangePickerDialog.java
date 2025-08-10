package com.example.fourquadrant;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义日期范围选择弹窗
 */
public class CustomDateRangePickerDialog {
    
    public interface OnDateRangeSelectedListener {
        void onDateRangeSelected(Date startDate, Date endDate);
    }
    
    public interface OnCancelListener {
        void onCancel();
    }
    
    private Context context;
    private OnDateRangeSelectedListener onDateRangeSelectedListener;
    private OnCancelListener onCancelListener;
    private AlertDialog dialog;
    
    private Date selectedStartDate;
    private Date selectedEndDate;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private Button btnStartDate;
    private Button btnEndDate;
    private Button btnConfirm;
    private Button btnCancel;
    
    public CustomDateRangePickerDialog(Context context, 
                                     OnDateRangeSelectedListener onDateRangeSelectedListener,
                                     OnCancelListener onCancelListener) {
        this.context = context;
        this.onDateRangeSelectedListener = onDateRangeSelectedListener;
        this.onCancelListener = onCancelListener;
        
        // 默认设置为今天
        Calendar calendar = Calendar.getInstance();
        selectedEndDate = calendar.getTime();
        
        // 开始日期默认为7天前
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        selectedStartDate = calendar.getTime();
        
        createDialog();
    }
    
    private void createDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_custom_date_range, null);
        
        initViews(view);
        setupListeners();
        updateDateDisplays();
        
        dialog = new AlertDialog.Builder(context)
            .setTitle("选择时间范围")
            .setView(view)
            .setCancelable(true)
            .setOnCancelListener(dialogInterface -> {
                if (onCancelListener != null) {
                    onCancelListener.onCancel();
                }
            })
            .create();
    }
    
    private void initViews(View view) {
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        btnStartDate = view.findViewById(R.id.btn_start_date);
        btnEndDate = view.findViewById(R.id.btn_end_date);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }
    
    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showStartDatePicker());
        btnEndDate.setOnClickListener(v -> showEndDatePicker());
        
        btnConfirm.setOnClickListener(v -> {
            if (onDateRangeSelectedListener != null) {
                onDateRangeSelectedListener.onDateRangeSelected(selectedStartDate, selectedEndDate);
            }
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> {
            if (onCancelListener != null) {
                onCancelListener.onCancel();
            }
            dialog.dismiss();
        });
    }
    
    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedStartDate);
        
        DatePickerDialog picker = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, dayOfMonth);
                selectedStartDate = newCalendar.getTime();
                
                // 如果开始日期晚于结束日期，自动调整结束日期
                if (selectedStartDate.after(selectedEndDate)) {
                    selectedEndDate = selectedStartDate;
                }
                
                updateDateDisplays();
                validateDateRange();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置最大日期为今天
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }
    
    private void showEndDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedEndDate);
        
        DatePickerDialog picker = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, dayOfMonth);
                selectedEndDate = newCalendar.getTime();
                
                // 如果结束日期早于开始日期，自动调整开始日期
                if (selectedEndDate.before(selectedStartDate)) {
                    selectedStartDate = selectedEndDate;
                }
                
                updateDateDisplays();
                validateDateRange();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置最大日期为今天
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }
    
    private void updateDateDisplays() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        tvStartDate.setText(sdf.format(selectedStartDate));
        tvEndDate.setText(sdf.format(selectedEndDate));
    }
    
    private void validateDateRange() {
        long diffInMillis = selectedEndDate.getTime() - selectedStartDate.getTime();
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
        
        if (diffInDays > 90) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("时间跨度超过90天");
        } else if (diffInDays < 0) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("日期范围无效");
        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setText("确定 (" + (diffInDays + 1) + "天)");
        }
    }
    
    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }
    
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
