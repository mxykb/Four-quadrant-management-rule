package com.example.fourquadrant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fourquadrant.R;
import com.example.fourquadrant.model.TaskFunction;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

/**
 * 任务AI功能权限适配器
 */
public class TaskFunctionAdapter extends RecyclerView.Adapter<TaskFunctionAdapter.ViewHolder> {
    
    private List<TaskFunction> functions;
    private OnFunctionPermissionChangeListener listener;

    public interface OnFunctionPermissionChangeListener {
        void onPermissionChanged(TaskFunction function, boolean enabled);
    }

    public TaskFunctionAdapter(List<TaskFunction> functions) {
        this.functions = functions;
    }

    public void setOnFunctionPermissionChangeListener(OnFunctionPermissionChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_function, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskFunction function = functions.get(position);
        
        holder.ivIcon.setImageResource(function.getIconResId());
        holder.tvName.setText(function.getName());
        holder.tvDescription.setText(function.getDescription());
        
        // 设置开关状态，先移除监听器避免触发回调
        holder.switchPermission.setOnCheckedChangeListener(null);
        holder.switchPermission.setChecked(function.isEnabled());
        
        // 设置开关监听器
        holder.switchPermission.setOnCheckedChangeListener((buttonView, isChecked) -> {
            function.setEnabled(isChecked);
            if (listener != null) {
                listener.onPermissionChanged(function, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return functions != null ? functions.size() : 0;
    }

    public void updateFunctions(List<TaskFunction> newFunctions) {
        this.functions = newFunctions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvDescription;
        SwitchMaterial switchPermission;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_function_icon);
            tvName = itemView.findViewById(R.id.tv_function_name);
            tvDescription = itemView.findViewById(R.id.tv_function_description);
            switchPermission = itemView.findViewById(R.id.switch_function_permission);
        }
    }
}