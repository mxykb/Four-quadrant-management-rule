package com.example.fourquadrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HighPriorityTaskAdapter extends RecyclerView.Adapter<HighPriorityTaskAdapter.ViewHolder> {
    
    private List<TaskAnalysisData.HighPriorityTask> tasks;
    private OnTaskClickListener listener;
    
    public interface OnTaskClickListener {
        void onTaskClick(TaskAnalysisData.HighPriorityTask task);
    }
    
    public HighPriorityTaskAdapter(List<TaskAnalysisData.HighPriorityTask> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_high_priority_task, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskAnalysisData.HighPriorityTask task = tasks.get(position);
        holder.bind(task);
    }
    
    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }
    
    public void updateTasks(List<TaskAnalysisData.HighPriorityTask> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCompletionStatus;
        private TextView tvTaskName;
        private TextView tvImportance;
        private TextView tvUrgency;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompletionStatus = itemView.findViewById(R.id.iv_completion_status);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvImportance = itemView.findViewById(R.id.tv_importance);
            tvUrgency = itemView.findViewById(R.id.tv_urgency);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }
        
        public void bind(TaskAnalysisData.HighPriorityTask task) {
            tvTaskName.setText(task.getTaskName());
            tvImportance.setText(String.valueOf(task.getImportance()));
            tvUrgency.setText(String.valueOf(task.getUrgency()));
            
            // 设置完成状态图标
            if (task.isCompleted()) {
                ivCompletionStatus.setImageResource(R.drawable.ic_task_completed);
                ivCompletionStatus.setColorFilter(itemView.getContext().getColor(R.color.accent_color));
            } else {
                ivCompletionStatus.setImageResource(R.drawable.ic_task);
                ivCompletionStatus.setColorFilter(itemView.getContext().getColor(R.color.text_secondary));
            }
        }
    }
}
