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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.database.repository.TaskRepository;
import com.example.fourquadrant.database.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskListFragment extends Fragment {
    
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskItem> taskList; // 活跃任务列表
    private List<TaskItem> allTasks; // 所有任务列表（包括已完成的）
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
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        
        initViews(view);
        initDatabase();
        setupRecyclerView();
        setupButtons();
        loadTasksFromDatabase();
        
        return view;
    }
    
    private void initViews(View view) {
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        addTaskButton = view.findViewById(R.id.add_task_button);
        clearAllButton = view.findViewById(R.id.clear_all_button);
    }
    
    private void initDatabase() {
        taskRepository = new TaskRepository(requireActivity().getApplication());
    }
    
    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        allTasks = new ArrayList<>();
        
        // 创建TaskAdapterListener
        TaskAdapter.TaskAdapterListener adapterListener = new TaskAdapter.TaskAdapterListener() {
            @Override
            public void onTaskChanged() {
                // 任务改变时保存到数据库
                // TODO: 实现保存逻辑
            }
            
            @Override
            public void onTaskDeleted(int position) {
                TaskListFragment.this.onTaskDeleted(position);
            }
            
            @Override
            public void onTaskCompleted(int position) {
                TaskListFragment.this.onTaskCompleted(position);
            }
        };
        
        taskAdapter = new TaskAdapter(taskList, adapterListener);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }
    
    private void setupButtons() {
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());
        clearAllButton.setOnClickListener(v -> clearAllTasks());
    }
    
    private void loadTasksFromDatabase() {
        // 使用数据库线程池执行查询，避免并发访问问题
        com.example.fourquadrant.database.AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 等待数据库完全初始化
                    Thread.sleep(200);
                    
                    // 同步查询活跃任务
                    List<TaskEntity> activeTaskEntities = taskRepository.getActiveTasksSync();
                    // 同步查询所有任务  
                    List<TaskEntity> allTaskEntities = taskRepository.getAllTasksSync();
                    
                    // 在主线程更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateActiveTaskList(activeTaskEntities);
                                updateAllTasksList(allTaskEntities);
                                android.util.Log.d("TaskListFragment", "Loaded " + 
                                    activeTaskEntities.size() + " active tasks and " + 
                                    allTaskEntities.size() + " total tasks from database");
                            }
                        });
                    }
                } catch (Exception e) {
                    android.util.Log.e("TaskListFragment", "Error loading tasks from database", e);
                }
            }
        });
    }
    
    // 刷新数据库数据的同步方法
    private void refreshTasksFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 同步查询最新数据
                    List<TaskEntity> activeTaskEntities = taskRepository.getActiveTasksSync();
                    List<TaskEntity> allTaskEntities = taskRepository.getAllTasksSync();
                    
                    // 在主线程更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 只更新数据，不重复日志
                                updateActiveTaskListQuiet(activeTaskEntities);
                                updateAllTasksListQuiet(allTaskEntities);
                            }
                        });
                    }
                } catch (Exception e) {
                    android.util.Log.e("TaskListFragment", "Error refreshing tasks from database", e);
                }
            }
        }).start();
    }
    
    private void updateActiveTaskList(List<TaskEntity> taskEntities) {
        // 调试信息
        android.util.Log.d("TaskListFragment", "updateActiveTaskList: received " + 
                           (taskEntities != null ? taskEntities.size() : 0) + " tasks from database");
        
        taskList.clear();
        if (taskEntities != null) {
            for (TaskEntity entity : taskEntities) {
                TaskItem item = convertEntityToTaskItem(entity);
                taskList.add(item);
                android.util.Log.d("TaskListFragment", "Added task: " + item.getName() + 
                                  " (ID: " + item.getId() + ")");
            }
        }
        taskAdapter.notifyDataSetChanged();
        notifyTasksUpdated();
    }
    
    private void updateAllTasksList(List<TaskEntity> taskEntities) {
        allTasks.clear();
        for (TaskEntity entity : taskEntities) {
            allTasks.add(convertEntityToTaskItem(entity));
        }
        notifyTasksUpdated();
    }
    
    // 静默更新方法（不打印日志，用于刷新）
    private void updateActiveTaskListQuiet(List<TaskEntity> taskEntities) {
        taskList.clear();
        if (taskEntities != null) {
            for (TaskEntity entity : taskEntities) {
                TaskItem item = convertEntityToTaskItem(entity);
                taskList.add(item);
            }
        }
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
        notifyTasksUpdated();
    }
    
    private void updateAllTasksListQuiet(List<TaskEntity> taskEntities) {
        allTasks.clear();
        if (taskEntities != null) {
            for (TaskEntity entity : taskEntities) {
                allTasks.add(convertEntityToTaskItem(entity));
            }
        }
    }
    
    private TaskItem convertEntityToTaskItem(TaskEntity entity) {
        TaskItem item = new TaskItem();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setImportance(entity.getImportance());
        item.setUrgency(entity.getUrgency());
        item.setCompleted(entity.isCompleted());
        if (entity.getCompletedAt() != null) {
            item.setCompletedTime(entity.getCompletedAt());
        }
        return item;
    }
    
    private TaskEntity convertTaskItemToEntity(TaskItem item) {
        if (item == null) {
            android.util.Log.e("TaskListFragment", "TaskItem is null in convertTaskItemToEntity");
            return null;
        }
        
        try {
            // 如果是已存在的任务，从数据库获取原始实体以保持时间戳
            TaskEntity entity = null;
            if (item.getId() != null) {
                try {
                    entity = taskRepository.getTaskByIdSync(item.getId());
                } catch (Exception e) {
                    android.util.Log.w("TaskListFragment", "Could not load existing task from DB: " + e.getMessage());
                }
            }
            
            // 如果没有找到现有实体，创建新的
            if (entity == null) {
                entity = new TaskEntity();
                entity.setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString());
                entity.setCreatedAt(System.currentTimeMillis());
            }
            
            // 更新所有可变字段
            entity.setName(item.getName() != null ? item.getName() : "");
            entity.setImportance(item.getImportance());
            entity.setUrgency(item.getUrgency());
            
            // 计算象限
            int quadrant = calculateQuadrant(item.getImportance(), item.getUrgency());
            entity.setQuadrant(quadrant);
            
            entity.setCompleted(item.isCompleted());
            if (item.isCompleted() && item.getCompletedTime() > 0) {
                entity.setCompletedAt(item.getCompletedTime());
            } else if (!item.isCompleted()) {
                // 如果任务被标记为未完成，清除完成时间
                entity.setCompletedAt(null);
            }
            
            entity.setUpdatedAt(System.currentTimeMillis());
            
            return entity;
        } catch (Exception e) {
            android.util.Log.e("TaskListFragment", "Error converting TaskItem to TaskEntity", e);
            return null;
        }
    }
    
    private int calculateQuadrant(int importance, int urgency) {
        // 四象限分类：
        // 1: 重要且紧急 (importance >= 6 && urgency >= 6)
        // 2: 重要不紧急 (importance >= 6 && urgency < 6)  
        // 3: 紧急不重要 (importance < 6 && urgency >= 6)
        // 4: 不重要不紧急 (importance < 6 && urgency < 6)
        if (importance >= 6 && urgency >= 6) {
            return 1;
        } else if (importance >= 6 && urgency < 6) {
            return 2;
        } else if (importance < 6 && urgency >= 6) {
            return 3;
        } else {
            return 4;
        }
    }
    
    private void onTaskCompleted(int position) {
        android.util.Log.d("TaskListFragment", "onTaskCompleted called with position: " + position);
        
        if (position >= 0 && position < taskList.size()) {
            try {
                TaskItem task = taskList.get(position);
                long completedTime = System.currentTimeMillis();
                android.util.Log.d("TaskListFragment", "Processing task: " + task.getName() + 
                    " (ID: " + task.getId() + ") at " + new java.util.Date(completedTime));
                
                task.setCompleted(true);
                task.setCompletedTime(completedTime);
                
                // 立即从活跃列表移除
                taskList.remove(position);
                if (taskAdapter != null) {
                    taskAdapter.notifyItemRemoved(position);
                }
                
                // 更新数据库
                if (taskRepository != null) {
                    TaskEntity entity = convertTaskItemToEntity(task);
                    if (entity != null) {
                        new Thread(() -> {
                            taskRepository.updateTask(entity);
                            // 在主线程刷新UI
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> refreshTasksFromDatabase());
                            }
                        }).start();
                    }
                }
                
                // 通知其他组件任务已更新
                try {
                    notifyTasksUpdated();
                } catch (Exception e) {
                    android.util.Log.e("TaskListFragment", "Error in notifyTasksUpdated", e);
                }
                
                if (getContext() != null) {
                    Toast.makeText(getContext(), "任务已完成", Toast.LENGTH_SHORT).show();
                }
                

                
            } catch (Exception e) {
                android.util.Log.e("TaskListFragment", "Error in onTaskCompleted", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "完成任务时出错", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            android.util.Log.w("TaskListFragment", "Invalid position: " + position + ", list size: " + taskList.size());
        }
    }
    
    private void onTaskDeleted(int position) {
        if (position >= 0 && position < taskList.size()) {
            TaskItem task = taskList.get(position);
            
            // 立即从UI列表移除
            taskList.remove(position);
            allTasks.remove(task);
            taskAdapter.notifyItemRemoved(position);
            
            // 从数据库删除
            if (task.getId() != null) {
                new Thread(() -> {
                    taskRepository.deleteTaskById(task.getId());
                    // 在主线程刷新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> refreshTasksFromDatabase());
                    }
                }).start();
            }
            
            // 通知其他组件任务已更新
            notifyTasksUpdated();
            
            Toast.makeText(getContext(), "任务已删除", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAddTaskDialog() {
        AddTaskDialogFragment dialog = new AddTaskDialogFragment();
        dialog.setTaskAddListener(this::addTask);
        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }
    
    public void addTask(String name, int importance, int urgency) {
        android.util.Log.d("TaskListFragment", "addTask called: " + name + " (" + importance + "," + urgency + ")");
        
        TaskItem newTask = new TaskItem(name, importance, urgency);
        newTask.setId(UUID.randomUUID().toString());
        
        android.util.Log.d("TaskListFragment", "Created task with ID: " + newTask.getId());
        
        // 临时解决方案：立即添加到UI列表
        taskList.add(newTask);
        allTasks.add(newTask);
        taskAdapter.notifyItemInserted(taskList.size() - 1);
        
        android.util.Log.d("TaskListFragment", "Added to UI lists, now have " + taskList.size() + " active tasks");
        
        // 保存到数据库
        TaskEntity entity = convertTaskItemToEntity(newTask);
        if (entity != null) {
            android.util.Log.d("TaskListFragment", "Saving to database: quadrant=" + entity.getQuadrant());
            new Thread(() -> {
                taskRepository.insertTask(entity);
                // 在主线程刷新UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> refreshTasksFromDatabase());
                }
            }).start();
        } else {
            android.util.Log.e("TaskListFragment", "Failed to convert TaskItem to TaskEntity");
        }
        
        // 通知其他组件任务已更新
        notifyTasksUpdated();
        
        Toast.makeText(getContext(), "任务已添加", Toast.LENGTH_SHORT).show();
    }
    
    private void clearAllTasks() {
        // 立即清空UI中的活跃任务列表
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
        
        // 软删除数据库中的活跃任务
        new Thread(() -> {
            taskRepository.softDeleteActiveTasks();
            // 在主线程刷新UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> refreshTasksFromDatabase());
            }
        }).start();
        
        // 通知其他组件任务已更新
        notifyTasksUpdated();
        
        Toast.makeText(getContext(), "所有进行中的任务已清除", Toast.LENGTH_SHORT).show();
        
        // 立即刷新数据库数据
        refreshTasksFromDatabase();
    }
    
    public List<TaskItem> getAllTasks() {
        return new ArrayList<>(allTasks);
    }
    
    public List<TaskItem> getActiveTasks() {
        return new ArrayList<>(taskList);
    }
    
    // 为向后兼容添加的方法
    public List<QuadrantView.Task> getCurrentTasks() {
        List<QuadrantView.Task> quadrantTasks = new ArrayList<>();
        for (TaskItem item : taskList) {
            QuadrantView.Task quadrantTask = new QuadrantView.Task(
                item.getName(), 
                item.getImportance(), 
                item.getUrgency()
            );
            quadrantTasks.add(quadrantTask);
        }
        return quadrantTasks;
    }
    
    public List<TaskItem> getCompletedTasks() {
        List<TaskItem> completedTasks = new ArrayList<>();
        for (TaskItem task : allTasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            }
        }
        return completedTasks;
    }
    
    private void notifyTasksUpdated() {
        try {
            List<QuadrantView.Task> quadrantTasks = new ArrayList<>();
            
            if (taskList != null) {
                for (TaskItem item : taskList) {
                    if (item != null && item.getName() != null) {
                        try {
                            QuadrantView.Task quadrantTask = new QuadrantView.Task(
                                item.getName(), 
                                item.getImportance(), 
                                item.getUrgency()
                            );
                            quadrantTasks.add(quadrantTask);
                        } catch (Exception e) {
                            android.util.Log.e("TaskListFragment", "Error creating QuadrantView.Task for item: " + item.getName(), e);
                        }
                    }
                }
            }
            
            if (listeners != null) {
                for (TaskListListener listener : listeners) {
                    if (listener != null) {
                        try {
                            listener.onTasksUpdated(quadrantTasks);
                        } catch (Exception e) {
                            android.util.Log.e("TaskListFragment", "Error notifying listener", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("TaskListFragment", "Error in notifyTasksUpdated", e);
        }
    }
    
    /**
     * TaskItem类 - 保持与原有代码的兼容性
     */
    public static class TaskItem {
        private String id;
        private String name;
        private int importance;
        private int urgency;
        private boolean completed;
        private long completedTime;
        
        public TaskItem(String name, int importance, int urgency) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.importance = importance;
            this.urgency = urgency;
            this.completed = false;
            this.completedTime = 0;
        }
        
        public TaskItem() {
            // Room需要无参构造函数
            this.id = null; // 不自动生成ID，由外部设置
            this.name = "";
            this.importance = 5;
            this.urgency = 5;
            this.completed = false;
            this.completedTime = 0;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getImportance() { return importance; }
        public void setImportance(int importance) { this.importance = importance; }
        
        public int getUrgency() { return urgency; }
        public void setUrgency(int urgency) { this.urgency = urgency; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.completedTime = System.currentTimeMillis();
            } else {
                this.completedTime = 0;
            }
        }
        
        public long getCompletedTime() { return completedTime; }
        public void setCompletedTime(long completedTime) { this.completedTime = completedTime; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TaskItem taskItem = (TaskItem) obj;
            return id != null && id.equals(taskItem.id);
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
}