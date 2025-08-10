package com.example.fourquadrant;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * 提醒列表适配器
 */
public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ReminderViewHolder> {
    
    private List<ReminderItem> reminders;
    private ReminderActionListener listener;
    
    public interface ReminderActionListener {
        void onEditReminder(ReminderItem reminder);
        void onDeleteReminder(ReminderItem reminder);
        void onSnoozeReminder(ReminderItem reminder);
        void onToggleReminder(ReminderItem reminder);
    }
    
    public ReminderListAdapter(List<ReminderItem> reminders, ReminderActionListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderItem reminder = reminders.get(position);
        holder.bind(reminder);
    }
    
    @Override
    public int getItemCount() {
        return reminders.size();
    }
    
    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView timeText;
        private TextView contentText;
        private TextView taskText;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private ImageButton snoozeButton;
        private Switch activeSwitch;
        private View statusIndicator;
        
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.reminder_card);
            timeText = itemView.findViewById(R.id.reminder_time);
            contentText = itemView.findViewById(R.id.reminder_content);
            taskText = itemView.findViewById(R.id.reminder_task);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            snoozeButton = itemView.findViewById(R.id.btn_snooze);
            activeSwitch = itemView.findViewById(R.id.switch_active);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }
        
        public void bind(ReminderItem reminder) {
            // 显示时间
            timeText.setText(reminder.getFormattedTime());
            
            // 显示内容
            contentText.setText(reminder.getContentSummary());
            
            // 显示关联任务
            if (reminder.getTaskName() != null && !reminder.getTaskName().isEmpty() 
                && !"无关联任务".equals(reminder.getTaskName())) {
                taskText.setText("📋 " + reminder.getTaskName());
                taskText.setVisibility(View.VISIBLE);
            } else {
                taskText.setVisibility(View.GONE);
            }
            
            // 设置激活状态 - 先清除监听器避免触发回调
            activeSwitch.setOnCheckedChangeListener(null);
            activeSwitch.setChecked(reminder.isActive());
            activeSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                if (listener != null && isChecked != reminder.isActive()) {
                    // 只有在状态真正改变时才调用
                    listener.onToggleReminder(reminder);
                }
            });
            
            // 设置状态指示器
            updateStatusIndicator(reminder);
            
            // 设置卡片透明度和颜色
            if (!reminder.isActive()) {
                cardView.setAlpha(0.6f);
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            } else if (reminder.isPast()) {
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            } else {
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(Color.WHITE);
            }
            
            // 设置按钮点击事件
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditReminder(reminder);
                }
            });
            
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReminder(reminder);
                }
            });
            
            snoozeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSnoozeReminder(reminder);
                }
            });
            
            // 稍后提醒按钮只在过期时显示
            snoozeButton.setVisibility(reminder.isPast() && reminder.isActive() ? View.VISIBLE : View.GONE);
            
            // 卡片点击事件 - 编辑提醒
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditReminder(reminder);
                }
            });
        }
        
        private void updateStatusIndicator(ReminderItem reminder) {
            if (!reminder.isActive()) {
                // 已禁用
                statusIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"));
            } else if (reminder.isPast()) {
                // 已过期
                statusIndicator.setBackgroundColor(Color.parseColor("#F44336"));
            } else if (reminder.isToday()) {
                // 今天
                statusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
            } else {
                // 未来
                statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
        }
    }
} 