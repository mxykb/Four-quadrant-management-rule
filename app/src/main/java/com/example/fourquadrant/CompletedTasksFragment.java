package com.example.fourquadrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletedTasksFragment extends Fragment {
    
    private RecyclerView completedTasksRecyclerView;
    private CompletedTasksAdapter completedTasksAdapter;
    private List<TaskListFragment.TaskItem> completedTasks;
    private TextView emptyStateText;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        completedTasks = new ArrayList<>();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_tasks, container, false);
        
        completedTasksRecyclerView = view.findViewById(R.id.completed_tasks_recycler_view);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        
        setupRecyclerView();
        loadCompletedTasks();
        
        return view;
    }
    
    private void setupRecyclerView() {
        completedTasksAdapter = new CompletedTasksAdapter(completedTasks);
        completedTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        completedTasksRecyclerView.setAdapter(completedTasksAdapter);
    }
    
    public void loadCompletedTasks() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            TaskListFragment taskListFragment = (TaskListFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag("f0");
            if (taskListFragment != null) {
                completedTasks.clear();
                completedTasks.addAll(taskListFragment.getCompletedTasks());
                completedTasksAdapter.notifyDataSetChanged();
                updateEmptyState();
            }
        }
    }
    
    private void updateEmptyState() {
        if (completedTasks.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            completedTasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            completedTasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    public static class CompletedTasksAdapter extends RecyclerView.Adapter<CompletedTasksAdapter.CompletedTaskViewHolder> {
        
        private List<TaskListFragment.TaskItem> completedTasks;
        private SimpleDateFormat dateFormat;
        
        public CompletedTasksAdapter(List<TaskListFragment.TaskItem> completedTasks) {
            this.completedTasks = completedTasks;
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
        
        @NonNull
        @Override
        public CompletedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_completed_task, parent, false);
            return new CompletedTaskViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CompletedTaskViewHolder holder, int position) {
            TaskListFragment.TaskItem task = completedTasks.get(position);
            holder.bind(task);
        }
        
        @Override
        public int getItemCount() {
            return completedTasks.size();
        }
        
        class CompletedTaskViewHolder extends RecyclerView.ViewHolder {
            private TextView taskNameText;
            private TextView importanceText;
            private TextView urgencyText;
            private TextView completedTimeText;
            
            public CompletedTaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskNameText = itemView.findViewById(R.id.completed_task_name);
                importanceText = itemView.findViewById(R.id.completed_importance);
                urgencyText = itemView.findViewById(R.id.completed_urgency);
                completedTimeText = itemView.findViewById(R.id.completed_time);
            }
            
            public void bind(TaskListFragment.TaskItem task) {
                taskNameText.setText(task.getName());
                importanceText.setText("重要性: " + task.getImportance());
                urgencyText.setText("紧急性: " + task.getUrgency());
                
                if (task.getCompletedTime() > 0) {
                    String completedTime = dateFormat.format(new Date(task.getCompletedTime()));
                    completedTimeText.setText("完成时间: " + completedTime);
                } else {
                    completedTimeText.setText("完成时间: 未知");
                }
            }
        }
    }
} 