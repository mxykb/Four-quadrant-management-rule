package com.example.fourquadrant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment {
    
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskItem> taskList; // 活跃任务列表
    private List<TaskItem> allTasks; // 所有任务列表（包括已完成的）
    private Button addTaskButton;
    private Button clearAllButton;
    private SharedPreferences preferences;
    private Gson gson;
    
    private static final String PREF_NAME = "TaskListPrefs";
    private static final String KEY_TASKS = "saved_tasks";
    
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
        allTasks = new ArrayList<>();
        gson = new Gson();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化SharedPreferences
        preferences = getActivity().getSharedPreferences(PREF_NAME, 0);
        // 加载保存的任务
        loadSavedTasks();
        // 设置监听器
        if (getActivity() instanceof TaskListListener) {
            setTaskListListener((TaskListListener) getActivity());
        }
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
        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.TaskAdapterListener() {
            @Override
            public void onTaskChanged() {
                notifyTasksUpdated();
                saveTasks(); // 自动保存任务
            }
            
            @Override
            public void onTaskDeleted(int position) {
                try {
                    if (position >= 0 && position < taskList.size()) {
                        TaskItem taskToDelete = taskList.get(position);
                        taskList.remove(position);
                        allTasks.remove(taskToDelete); // 从allTasks中也删除
                        taskAdapter.notifyItemRemoved(position);
                        notifyTasksUpdated();
                        saveTasks(); // 自动保存任务
                        Toast.makeText(getContext(), "任务已删除", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "删除任务失败：位置无效", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "删除任务时发生错误", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onTaskCompleted(int position) {
                try {
                    if (position >= 0 && position < taskList.size()) {
                        TaskItem task = taskList.get(position);
                        task.setCompleted(true);
                        System.out.println("Task completion time set: " + task.getCompletedTime());
                        
                        // 从活跃任务列表中移除已完成的任务
                        taskList.remove(position);
                        taskAdapter.notifyItemRemoved(position);
                        
                        // 任务已经在allTasks中，所以不需要重复添加
                        // 只需要确保它在allTasks中存在即可
                        if (!allTasks.contains(task)) {
                            allTasks.add(task);
                            System.out.println("Added completed task to allTasks: " + task.getName() + " (id=" + task.getId() + ")");
                        } else {
                            System.out.println("Task already in allTasks: " + task.getName() + " (id=" + task.getId() + ")");
                        }
                        
                        // 添加调试信息
                        System.out.println("Task completed: " + task.getName() + ", completed=" + task.isCompleted() + ", id=" + task.getId());
                        System.out.println("After completion: allTasks size=" + allTasks.size() + ", taskList size=" + taskList.size());
                        System.out.println("allTasks contents:");
                        for (TaskItem t : allTasks) {
                            System.out.println("  - " + t.getName() + " (completed=" + t.isCompleted() + ", id=" + t.getId() + ")");
                        }
                        
                        notifyTasksUpdated();
                        saveTasks(); // 自动保存任务
                        Toast.makeText(getContext(), "任务已完成", Toast.LENGTH_SHORT).show();
                        
                        // 通知其他Fragment更新
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.notifyFragmentsUpdate();
                            System.out.println("notifyFragmentsUpdate called");
                        }
                    } else {
                        Toast.makeText(getContext(), "完成任务失败：位置无效", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "完成任务时发生错误", Toast.LENGTH_SHORT).show();
                    System.out.println("onTaskCompleted error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }
    
    private void setupAddButton() {
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TaskItem newTask = new TaskItem("新任务", 5, 5);
                    taskList.add(newTask);
                    allTasks.add(newTask); // 同时添加到allTasks
                    taskAdapter.notifyItemInserted(taskList.size() - 1);
                    notifyTasksUpdated();
                    saveTasks(); // 自动保存任务
                    Toast.makeText(getContext(), "任务已添加", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "添加任务时发生错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void setupClearAllButton() {
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!taskList.isEmpty()) {
                        // 只清空活跃任务，保留已完成任务
                        taskList.clear();
                        taskAdapter.notifyDataSetChanged();
                        notifyTasksUpdated();
                        saveTasks(); // 自动保存任务
                        Toast.makeText(getContext(), "所有活跃任务已清空", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "任务列表已为空", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "清空任务时发生错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void loadSavedTasks() {
        try {
            if (preferences == null) {
                System.out.println("loadSavedTasks: preferences is null");
                return;
            }
            
            String tasksJson = preferences.getString(KEY_TASKS, "[]");
            Type type = new TypeToken<ArrayList<TaskItem>>(){}.getType();
            List<TaskItem> savedTasks = gson.fromJson(tasksJson, type);
            if (savedTasks != null) {
                allTasks.clear();
                
                // 为每个任务验证和补充ID
                for (TaskItem task : savedTasks) {
                    if (task != null) {
                        // 如果任务没有ID，生成一个新的
                        if (task.getId() == null || task.getId().isEmpty()) {
                            task.setId(generateUniqueId());
                            System.out.println("Generated new ID for task: " + task.getName() + " -> " + task.getId());
                        }
                        allTasks.add(task);
                    }
                }
                
                // 分离活跃任务和已完成任务
                taskList.clear();
                for (TaskItem task : allTasks) {
                    if (task != null && !task.isCompleted()) {
                        taskList.add(task);
                    }
                }
                
                // 添加调试信息
                System.out.println("loadSavedTasks: loaded " + savedTasks.size() + " tasks, allTasks=" + allTasks.size() + ", taskList=" + taskList.size());
                for (TaskItem task : allTasks) {
                    if (task != null) {
                        System.out.println("Task: " + task.getName() + ", completed=" + task.isCompleted() + ", id=" + task.getId());
                    }
                }
                
                // 立即保存一次，确保所有任务都有有效的ID
                saveTasks();
            }
        } catch (Exception e) {
            // 如果加载失败，使用空列表
            taskList.clear();
            allTasks.clear();
            System.out.println("loadSavedTasks error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveTasks() {
        try {
            if (preferences == null) {
                System.out.println("saveTasks: preferences is null");
                return;
            }
            
            // 更新allTasks列表，确保包含所有任务
            updateAllTasksList();
            String tasksJson = gson.toJson(allTasks);
            preferences.edit().putString(KEY_TASKS, tasksJson).apply();
            
            // 添加调试信息
            System.out.println("saveTasks: saved " + allTasks.size() + " tasks");
            System.out.println("JSON: " + tasksJson);
        } catch (Exception e) {
            Toast.makeText(getContext(), "保存任务失败", Toast.LENGTH_SHORT).show();
            System.out.println("saveTasks error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateAllTasksList() {
        // 更安全的方式：保留已完成的任务，确保活跃任务也正确同步
        int beforeSize = allTasks.size();
        
        // 获取当前已完成的任务列表（保存原有的已完成任务）
        List<TaskItem> completedTasks = new ArrayList<>();
        for (TaskItem task : allTasks) {
            if (task != null && task.isCompleted()) {
                completedTasks.add(task);
            }
        }
        
        // 重建allTasks列表：先添加所有活跃任务，再添加已完成任务
        allTasks.clear();
        
        // 添加当前活跃任务
        for (TaskItem task : taskList) {
            if (task != null) {
                allTasks.add(task);
            }
        }
        
        // 添加已完成任务（避免重复）
        for (TaskItem completedTask : completedTasks) {
            if (!allTasks.contains(completedTask)) {
                allTasks.add(completedTask);
            }
        }
        
        // 添加调试信息
        System.out.println("updateAllTasksList: before=" + beforeSize + ", after=" + allTasks.size() + ", taskList=" + taskList.size() + ", completedTasks=" + completedTasks.size());
        System.out.println("allTasks contents after update:");
        for (TaskItem task : allTasks) {
            System.out.println("  - " + task.getName() + " (completed=" + task.isCompleted() + ", id=" + task.getId() + ")");
        }
    }
    
    private void notifyTasksUpdated() {
        try {
            List<QuadrantView.Task> tasks = new ArrayList<>();
            for (TaskItem item : taskList) {
                if (item != null && !item.getName().trim().isEmpty()) {
                    tasks.add(new QuadrantView.Task(item.getName(), item.getImportance(), item.getUrgency()));
                }
            }
            
            // 通知所有监听器
            for (TaskListListener listener : listeners) {
                if (listener != null) {
                    listener.onTasksUpdated(tasks);
                }
            }
        } catch (Exception e) {
            // 静默处理异常，避免影响UI操作
        }
    }
    
    public List<QuadrantView.Task> getCurrentTasks() {
        List<QuadrantView.Task> tasks = new ArrayList<>();
        for (TaskItem item : taskList) {
            if (item != null && !item.getName().trim().isEmpty() && !item.isCompleted()) {
                tasks.add(new QuadrantView.Task(item.getName(), item.getImportance(), item.getUrgency()));
            }
        }
        return tasks;
    }
    
    public List<TaskItem> getCompletedTasks() {
        List<TaskItem> completedTasks = new ArrayList<>();
        for (TaskItem item : allTasks) {
            if (item != null && item.isCompleted()) {
                completedTasks.add(item);
            }
        }
        // 添加调试信息
        System.out.println("getCompletedTasks: allTasks size=" + allTasks.size() + ", completedTasks size=" + completedTasks.size());
        return completedTasks;
    }
    
    public List<TaskItem> getActiveTasks() {
        List<TaskItem> activeTasks = new ArrayList<>();
        for (TaskItem item : allTasks) {
            if (item != null && !item.isCompleted()) {
                activeTasks.add(item);
            }
        }
        // 添加调试信息
        System.out.println("getActiveTasks: allTasks size=" + allTasks.size() + ", activeTasks size=" + activeTasks.size());
        return activeTasks;
    }
    
    // 从任务列表中移除已完成的任务
    public void removeCompletedTasks() {
        List<TaskItem> tasksToRemove = new ArrayList<>();
        for (TaskItem item : taskList) {
            if (item != null && item.isCompleted()) {
                tasksToRemove.add(item);
            }
        }
        taskList.removeAll(tasksToRemove);
        taskAdapter.notifyDataSetChanged();
    }
    
    public static class TaskItem {
        private String id;
        private String name;
        private int importance;
        private int urgency;
        private boolean completed;
        private long completedTime;
        
        public TaskItem(String name, int importance, int urgency) {
            this.id = TaskListFragment.generateUniqueId();
            this.name = name;
            this.importance = importance;
            this.urgency = urgency;
            this.completed = false;
            this.completedTime = 0;
        }
        
        // 默认构造函数，用于Gson序列化
        public TaskItem() {
            this.id = null; // 不自动生成ID，由loadSavedTasks方法处理
            this.name = "";
            this.importance = 5;
            this.urgency = 5;
            this.completed = false;
            this.completedTime = 0;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getImportance() {
            return importance;
        }
        
        public void setImportance(int importance) {
            this.importance = importance;
        }
        
        public int getUrgency() {
            return urgency;
        }
        
        public void setUrgency(int urgency) {
            this.urgency = urgency;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.completedTime = System.currentTimeMillis();
            } else {
                this.completedTime = 0;
            }
        }
        
        public long getCompletedTime() {
            return completedTime;
        }
        
        public void setCompletedTime(long completedTime) {
            this.completedTime = completedTime;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
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
    
    // 生成唯一ID的方法
    private static String generateUniqueId() {
        return "task_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 保存数据
        if (preferences != null) {
            saveTasks();
        }
    }
} 