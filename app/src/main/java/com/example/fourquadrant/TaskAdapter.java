package com.example.fourquadrant;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    
    private List<TaskListFragment.TaskItem> taskList;
    private TaskAdapterListener listener;
    
    public interface TaskAdapterListener {
        void onTaskChanged();
        void onTaskDeleted(int position);
        void onTaskCompleted(int position);
    }
    
    public TaskAdapter(List<TaskListFragment.TaskItem> taskList, TaskAdapterListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskListFragment.TaskItem task = taskList.get(position);
        holder.bind(task, position);
    }
    
    @Override
    public int getItemCount() {
        return taskList.size();
    }
    
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private EditText taskNameEdit;
        private SeekBar importanceSeekBar;
        private SeekBar urgencySeekBar;
        private TextView importanceValue;
        private TextView urgencyValue;
        private ImageButton completeButton;
        private ImageButton deleteButton;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameEdit = itemView.findViewById(R.id.task_name_edit);
            importanceSeekBar = itemView.findViewById(R.id.importance_seekbar);
            urgencySeekBar = itemView.findViewById(R.id.urgency_seekbar);
            importanceValue = itemView.findViewById(R.id.importance_value);
            urgencyValue = itemView.findViewById(R.id.urgency_value);
            completeButton = itemView.findViewById(R.id.complete_task_button);
            deleteButton = itemView.findViewById(R.id.delete_task_button);
        }
        
        public void bind(TaskListFragment.TaskItem task, int position) {
            // 设置任务名称
            taskNameEdit.setText(task.getName());
            taskNameEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    task.setName(s.toString());
                    if (listener != null) {
                        listener.onTaskChanged();
                    }
                }
            });
            
            // 设置重要性滑块
            importanceSeekBar.setProgress(task.getImportance());
            importanceValue.setText(String.valueOf(task.getImportance()));
            importanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        task.setImportance(progress);
                        importanceValue.setText(String.valueOf(progress));
                        if (listener != null) {
                            listener.onTaskChanged();
                        }
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
            // 设置紧急性滑块
            urgencySeekBar.setProgress(task.getUrgency());
            urgencyValue.setText(String.valueOf(task.getUrgency()));
            urgencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        task.setUrgency(progress);
                        urgencyValue.setText(String.valueOf(progress));
                        if (listener != null) {
                            listener.onTaskChanged();
                        }
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
            // 设置完成按钮
            completeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            listener.onTaskCompleted(currentPosition);
                        }
                    }
                }
            });
            
            // 设置删除按钮 - 使用getAdapterPosition()确保位置正确
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            listener.onTaskDeleted(currentPosition);
                        }
                    }
                }
            });
        }
    }
} 