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
    private List<TaskItem> taskList;
    private Button addTaskButton;
    private Button clearAllButton;
    private SharedPreferences preferences;
    private Gson gson;
    
    private static final String PREF_NAME = "TaskListPrefs";
    private static final String KEY_TASKS = "saved_tasks";
    
    public interface TaskListListener {
        void onTasksUpdated(List<QuadrantView.Task> tasks);
    }
    
    private TaskListListener listener;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskList = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(PREF_NAME, 0);
        gson = new Gson();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                        taskList.remove(position);
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
                        taskAdapter.notifyItemChanged(position);
                        notifyTasksUpdated();
                        saveTasks(); // 自动保存任务
                        Toast.makeText(getContext(), "任务已完成", Toast.LENGTH_SHORT).show();
                        
                        // 通知其他Fragment更新
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.notifyFragmentsUpdate();
                        }
                    } else {
                        Toast.makeText(getContext(), "完成任务失败：位置无效", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "完成任务时发生错误", Toast.LENGTH_SHORT).show();
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
                        taskList.clear();
                        taskAdapter.notifyDataSetChanged();
                        notifyTasksUpdated();
                        saveTasks(); // 自动保存任务
                        Toast.makeText(getContext(), "所有任务已清空", Toast.LENGTH_SHORT).show();
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
            String tasksJson = preferences.getString(KEY_TASKS, "[]");
            Type type = new TypeToken<ArrayList<TaskItem>>(){}.getType();
            List<TaskItem> savedTasks = gson.fromJson(tasksJson, type);
            if (savedTasks != null) {
                taskList.clear();
                taskList.addAll(savedTasks);
            }
        } catch (Exception e) {
            // 如果加载失败，使用空列表
            taskList.clear();
        }
    }
    
    private void saveTasks() {
        try {
            String tasksJson = gson.toJson(taskList);
            preferences.edit().putString(KEY_TASKS, tasksJson).apply();
        } catch (Exception e) {
            Toast.makeText(getContext(), "保存任务失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void notifyTasksUpdated() {
        try {
            if (listener != null) {
                List<QuadrantView.Task> tasks = new ArrayList<>();
                for (TaskItem item : taskList) {
                    if (item != null && !item.getName().trim().isEmpty()) {
                        tasks.add(new QuadrantView.Task(item.getName(), item.getImportance(), item.getUrgency()));
                    }
                }
                listener.onTasksUpdated(tasks);
            }
        } catch (Exception e) {
            // 静默处理异常，避免影响UI操作
        }
    }
    
    public void setTaskListListener(TaskListListener listener) {
        this.listener = listener;
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
        for (TaskItem item : taskList) {
            if (item != null && item.isCompleted()) {
                completedTasks.add(item);
            }
        }
        return completedTasks;
    }
    
    public List<TaskItem> getActiveTasks() {
        List<TaskItem> activeTasks = new ArrayList<>();
        for (TaskItem item : taskList) {
            if (item != null && !item.isCompleted()) {
                activeTasks.add(item);
            }
        }
        return activeTasks;
    }
    
    public static class TaskItem {
        private String name;
        private int importance;
        private int urgency;
        private boolean completed;
        private long completedTime;
        
        public TaskItem(String name, int importance, int urgency) {
            this.name = name;
            this.importance = importance;
            this.urgency = urgency;
            this.completed = false;
            this.completedTime = 0;
        }
        
        // 默认构造函数，用于Gson序列化
        public TaskItem() {
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
    }
} 