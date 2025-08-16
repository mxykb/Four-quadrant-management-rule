package com.example.fourquadrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.database.entity.TaskEntity;
import com.example.fourquadrant.database.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 新的任务列表Fragment，使用数据库存储
 */
public class TaskListFragmentNew extends Fragment {
    
    private RecyclerView taskRecyclerView;
    private TaskAdapterNew taskAdapter;
    private List<TaskEntity> taskList;
    private Button addTaskButton;
    private Button clearAllButton;
    private TaskRepository taskRepository;
    
    public interface TaskListListener {
        void onTasksUpdated(List<QuadrantView.Task> tasks);
    }
    
    private List<TaskListListener> listeners = new ArrayList<>();
    
    public void addTaskListListener(TaskListListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeTaskListListener(TaskListListener listener) {
        listeners.remove(listener);
    }
    
    // 保持向后兼容
    public void setTaskListListener(TaskListListener listener) {
        addTaskListListener(listener);
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskList = new ArrayList<>();
        taskRepository = new TaskRepository(getActivity().getApplication());
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置监听器
        if (getActivity() instanceof TaskListListener) {
            setTaskListListener((TaskListListener) getActivity());
        }
        
        // 观察数据库中的任务变化
        observeActiveTasks();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        addTaskButton = view.findViewById(R.id.add_task_button);
        clearAllButton = view.findViewById(R.id.clear_all_button);
        
        setupRecyclerView();
        setupAddButton();
        setupClearAllButton();
        
        return view;
    }
    
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapterNew(taskList, new TaskAdapterNew.TaskAdapterListener() {
            @Override
            public void onTaskChanged() {
                notifyTasksUpdated();
            }
            
            @Override
            public void onTaskCompleted(TaskEntity task) {
                // 完成任务
                new Thread(() -> {
                    taskRepository.completeTask(task);
                }).start();
                Toast.makeText(getContext(), "任务已完成", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onTaskDeleted(TaskEntity task) {
                // 删除任务
                new Thread(() -> {
                    taskRepository.deleteTask(task);
                }).start();
                Toast.makeText(getContext(), "任务已删除", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onTaskUpdated(TaskEntity task) {
                // 更新任务
                new Thread(() -> {
                    taskRepository.updateTask(task);
                }).start();
            }
        });
        
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }
    
    private void setupAddButton() {
        addTaskButton.setOnClickListener(v -> {
            // 创建新任务
            String taskName = "新任务";
            int importance = 5;
            int urgency = 5;
            
            new Thread(() -> {
                taskRepository.createTask(taskName, importance, urgency);
            }).start();
            Toast.makeText(getContext(), "已添加新任务", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupClearAllButton() {
        clearAllButton.setOnClickListener(v -> {
            // 软删除所有活跃任务
            new Thread(() -> {
                taskRepository.softDeleteActiveTasks();
            }).start();
            Toast.makeText(getContext(), "已清空所有进行中的任务", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void observeActiveTasks() {
        try {
            androidx.lifecycle.LiveData<java.util.List<TaskEntity>> liveData = taskRepository.getActiveTasks();
            if (liveData != null) {
                liveData.observe(getViewLifecycleOwner(), tasks -> {
                    if (tasks != null) {
                        taskList.clear();
                        taskList.addAll(tasks);
                        taskAdapter.notifyDataSetChanged();
                        notifyTasksUpdated();
                    }
                });
            } else {
                android.util.Log.w("TaskListFragmentNew", "TaskRepository returned null LiveData for getActiveTasks");
                // 显示空列表，避免崩溃
                taskList.clear();
                taskAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            android.util.Log.e("TaskListFragmentNew", "Error observing active tasks", e);
            // 显示空列表，避免崩溃
            taskList.clear();
            taskAdapter.notifyDataSetChanged();
        }
    }
    
    private void notifyTasksUpdated() {
        // 转换TaskEntity为QuadrantView.Task
        List<QuadrantView.Task> quadrantTasks = new ArrayList<>();
        for (TaskEntity task : taskList) {
            QuadrantView.Task quadrantTask = new QuadrantView.Task(
                task.getName(),
                task.getImportance(),
                task.getUrgency()
            );
            quadrantTasks.add(quadrantTask);
        }
        
        // 通知所有监听器
        for (TaskListListener listener : listeners) {
            if (listener != null) {
                listener.onTasksUpdated(quadrantTasks);
            }
        }
    }
    
    // 获取所有活跃任务
    public List<TaskEntity> getActiveTasks() {
        return new ArrayList<>(taskList);
    }
    
    // 获取任务名称列表（用于番茄钟等功能）
    public List<String> getTaskNames() {
        List<String> taskNames = new ArrayList<>();
        for (TaskEntity task : taskList) {
            if (!task.isCompleted()) {
                taskNames.add(task.getName());
            }
        }
        return taskNames;
    }
    
    // 根据名称查找任务
    public TaskEntity getTaskByName(String taskName) {
        for (TaskEntity task : taskList) {
            if (task.getName().equals(taskName) && !task.isCompleted()) {
                return task;
            }
        }
        return null;
    }
    
    // 添加任务
    public void addTask(String name, int importance, int urgency) {
        new Thread(() -> {
            taskRepository.createTask(name, importance, urgency);
        }).start();
    }
    
    // 内部任务适配器
    public static class TaskAdapterNew extends RecyclerView.Adapter<TaskAdapterNew.TaskViewHolder> {
        
        private List<TaskEntity> tasks;
        private TaskAdapterListener listener;
        
        public interface TaskAdapterListener {
            void onTaskChanged();
            void onTaskCompleted(TaskEntity task);
            void onTaskDeleted(TaskEntity task);
            void onTaskUpdated(TaskEntity task);
        }
        
        public TaskAdapterNew(List<TaskEntity> tasks, TaskAdapterListener listener) {
            this.tasks = tasks;
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
            TaskEntity task = tasks.get(position);
            holder.bind(task, listener);
        }
        
        @Override
        public int getItemCount() {
            return tasks.size();
        }
        
        public static class TaskViewHolder extends RecyclerView.ViewHolder {
            // 这里应该包含具体的UI绑定逻辑
            // 由于原有的TaskAdapter.TaskViewHolder比较复杂，这里简化处理
            
            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
            }
            
            public void bind(TaskEntity task, TaskAdapterListener listener) {
                // 绑定任务数据到UI
                // 这里需要根据原有的TaskAdapter.TaskViewHolder实现
                // 暂时留空，具体实现需要参考原有代码
            }
        }
    }
}
