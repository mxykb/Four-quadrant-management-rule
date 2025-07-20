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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskSortFragment extends Fragment {
    
    private RecyclerView sortedTasksRecyclerView;
    private SortedTasksAdapter sortedTasksAdapter;
    private List<TaskListFragment.TaskItem> sortedTasks;
    private TextView emptyStateText;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sortedTasks = new ArrayList<>();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_sort, container, false);
        
        sortedTasksRecyclerView = view.findViewById(R.id.sorted_tasks_recycler_view);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        
        setupRecyclerView();
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAndSortTasks();
    }
    
    private void setupRecyclerView() {
        sortedTasksAdapter = new SortedTasksAdapter(sortedTasks);
        sortedTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sortedTasksRecyclerView.setAdapter(sortedTasksAdapter);
    }
    
    public void loadAndSortTasks() {
        if (getActivity() instanceof MainActivity) {
            // 遍历所有Fragment找到TaskListFragment
            for (androidx.fragment.app.Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof TaskListFragment) {
                    TaskListFragment taskListFragment = (TaskListFragment) fragment;
                    sortedTasks.clear();
                    sortedTasks.addAll(taskListFragment.getActiveTasks());
                    sortTasksByPriority();
                    sortedTasksAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    // 添加调试信息
                    System.out.println("TaskSortFragment loaded: " + sortedTasks.size() + " active tasks");
                    break;
                }
            }
        }
    }
    
    private void sortTasksByPriority() {
        Collections.sort(sortedTasks, new Comparator<TaskListFragment.TaskItem>() {
            @Override
            public int compare(TaskListFragment.TaskItem task1, TaskListFragment.TaskItem task2) {
                int quadrant1 = getQuadrant(task1);
                int quadrant2 = getQuadrant(task2);
                
                // 首先按象限排序
                if (quadrant1 != quadrant2) {
                    return Integer.compare(quadrant1, quadrant2);
                }
                
                // 同一象限内按具体规则排序
                switch (quadrant1) {
                    case 1: // 第一象限：紧急评分降序，重要评分降序
                        int urgencyCompare = Integer.compare(task2.getUrgency(), task1.getUrgency());
                        if (urgencyCompare != 0) return urgencyCompare;
                        return Integer.compare(task2.getImportance(), task1.getImportance());
                        
                    case 2: // 第二象限：重要评分降序，紧急评分降序
                        int importanceCompare = Integer.compare(task2.getImportance(), task1.getImportance());
                        if (importanceCompare != 0) return importanceCompare;
                        return Integer.compare(task2.getUrgency(), task1.getUrgency());
                        
                    case 3: // 第三象限：紧急评分降序，重要评分降序
                        int urgencyCompare3 = Integer.compare(task2.getUrgency(), task1.getUrgency());
                        if (urgencyCompare3 != 0) return urgencyCompare3;
                        return Integer.compare(task2.getImportance(), task1.getImportance());
                        
                    case 4: // 第四象限：紧急评分降序，重要评分降序
                        int urgencyCompare4 = Integer.compare(task2.getUrgency(), task1.getUrgency());
                        if (urgencyCompare4 != 0) return urgencyCompare4;
                        return Integer.compare(task2.getImportance(), task1.getImportance());
                        
                    default:
                        return 0;
                }
            }
        });
    }
    
    private int getQuadrant(TaskListFragment.TaskItem task) {
        int importance = task.getImportance();
        int urgency = task.getUrgency();
        int maxScore = 10; // 假设最大分数为10
        
        if (importance >= maxScore / 2 && urgency >= maxScore / 2) {
            return 1; // 紧急且重要
        } else if (importance >= maxScore / 2 && urgency < maxScore / 2) {
            return 2; // 重要不紧急
        } else if (importance < maxScore / 2 && urgency >= maxScore / 2) {
            return 3; // 紧急不重要
        } else {
            return 4; // 不紧急不重要
        }
    }
    
    private void updateEmptyState() {
        if (sortedTasks.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            sortedTasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            sortedTasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    public static class SortedTasksAdapter extends RecyclerView.Adapter<SortedTasksAdapter.SortedTaskViewHolder> {
        
        private List<TaskListFragment.TaskItem> sortedTasks;
        
        public SortedTasksAdapter(List<TaskListFragment.TaskItem> sortedTasks) {
            this.sortedTasks = sortedTasks;
        }
        
        @NonNull
        @Override
        public SortedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sorted_task, parent, false);
            return new SortedTaskViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull SortedTaskViewHolder holder, int position) {
            TaskListFragment.TaskItem task = sortedTasks.get(position);
            holder.bind(task, position + 1);
        }
        
        @Override
        public int getItemCount() {
            return sortedTasks.size();
        }
        
        class SortedTaskViewHolder extends RecyclerView.ViewHolder {
            private TextView rankText;
            private TextView taskNameText;
            private TextView importanceText;
            private TextView urgencyText;
            private TextView quadrantText;
            
            public SortedTaskViewHolder(@NonNull View itemView) {
                super(itemView);
                rankText = itemView.findViewById(R.id.rank_text);
                taskNameText = itemView.findViewById(R.id.sorted_task_name);
                importanceText = itemView.findViewById(R.id.sorted_importance);
                urgencyText = itemView.findViewById(R.id.sorted_urgency);
                quadrantText = itemView.findViewById(R.id.quadrant_text);
            }
            
            public void bind(TaskListFragment.TaskItem task, int rank) {
                rankText.setText("#" + rank);
                taskNameText.setText(task.getName());
                importanceText.setText("重要性: " + task.getImportance());
                urgencyText.setText("紧急性: " + task.getUrgency());
                
                int quadrant = getQuadrant(task);
                String quadrantName = getQuadrantName(quadrant);
                quadrantText.setText(quadrantName);
            }
            
            private int getQuadrant(TaskListFragment.TaskItem task) {
                int importance = task.getImportance();
                int urgency = task.getUrgency();
                int maxScore = 10;
                
                if (importance >= maxScore / 2 && urgency >= maxScore / 2) {
                    return 1;
                } else if (importance >= maxScore / 2 && urgency < maxScore / 2) {
                    return 2;
                } else if (importance < maxScore / 2 && urgency >= maxScore / 2) {
                    return 3;
                } else {
                    return 4;
                }
            }
            
            private String getQuadrantName(int quadrant) {
                switch (quadrant) {
                    case 1: return "第一象限：紧急且重要";
                    case 2: return "第二象限：重要不紧急";
                    case 3: return "第三象限：紧急不重要";
                    case 4: return "第四象限：不紧急不重要";
                    default: return "未知象限";
                }
            }
        }
    }
} 