package com.example.fourquadrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LongestDurationTaskAdapter extends RecyclerView.Adapter<LongestDurationTaskAdapter.ViewHolder> {
    
    private List<TaskAnalysisData.LongestDurationTask> tasks;
    private OnTaskClickListener listener;
    
    public interface OnTaskClickListener {
        void onTaskClick(TaskAnalysisData.LongestDurationTask task);
    }
    
    public LongestDurationTaskAdapter(List<TaskAnalysisData.LongestDurationTask> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_duration_task, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskAnalysisData.LongestDurationTask task = tasks.get(position);
        holder.bind(task);
    }
    
    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }
    
    public void updateTasks(List<TaskAnalysisData.LongestDurationTask> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskName;
        private TextView tvDurationDays;
        private TextView tvCompletionStatus;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvDurationDays = itemView.findViewById(R.id.tv_duration_days);
            tvCompletionStatus = itemView.findViewById(R.id.tv_completion_status);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }
        
        public void bind(TaskAnalysisData.LongestDurationTask task) {
            tvTaskName.setText(task.getTaskName());
            tvDurationDays.setText(task.getDurationDays() + " 天");
            
            // 设置完成状态
            if (task.isCompleted()) {
                tvCompletionStatus.setText("已完成");
                tvCompletionStatus.setBackgroundResource(R.drawable.bg_status_completed);
            } else {
                tvCompletionStatus.setText("进行中");
                tvCompletionStatus.setBackgroundResource(R.drawable.bg_status_pending);
            }
        }
    }
}
