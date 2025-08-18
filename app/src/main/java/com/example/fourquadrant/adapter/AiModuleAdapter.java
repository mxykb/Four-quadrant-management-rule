package com.example.fourquadrant.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.R;
import com.example.fourquadrant.model.AiModule;
import com.example.fourquadrant.utils.ModulePermissionManager;

import java.util.List;

/**
 * AI模块适配器
 * 用于显示智能工具主页的AI模块列表
 */
public class AiModuleAdapter extends RecyclerView.Adapter<AiModuleAdapter.ModuleViewHolder> {

    private Context context;
    private List<AiModule> moduleList;
    private ModulePermissionManager permissionManager;

    public AiModuleAdapter(Context context, List<AiModule> moduleList) {
        this.context = context;
        this.moduleList = moduleList;
        this.permissionManager = ModulePermissionManager.getInstance(context);
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ai_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        AiModule module = moduleList.get(position);
        
        // 设置模块信息
        holder.tvModuleIcon.setText(module.getIcon());
        holder.tvModuleName.setText(module.getName());
        holder.tvModuleDescription.setText(module.getDescription());
        
        // 设置权限开关状态
        boolean isEnabled = permissionManager.isModuleEnabled(module.getId());
        holder.switchModuleEnabled.setChecked(isEnabled);
        
        // 设置开关监听器
        holder.switchModuleEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            permissionManager.setModuleEnabled(module.getId(), isChecked);
            module.setEnabled(isChecked);
            
            String message = isChecked ? 
                module.getName() + " 已启用" : 
                module.getName() + " 已禁用";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (permissionManager.isModuleEnabled(module.getId())) {
                // 模块已启用，跳转到对应页面
                if (module.getTargetActivity() != null) {
                    Intent intent = new Intent(context, module.getTargetActivity());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "功能即将上线", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 模块未启用，提示用户
                Toast.makeText(context, "请先启用 " + module.getName() + " 权限", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvModuleIcon;
        TextView tvModuleName;
        TextView tvModuleDescription;
        SwitchCompat switchModuleEnabled;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleIcon = itemView.findViewById(R.id.tv_module_icon);
            tvModuleName = itemView.findViewById(R.id.tv_module_name);
            tvModuleDescription = itemView.findViewById(R.id.tv_module_description);
            switchModuleEnabled = itemView.findViewById(R.id.switch_module_enabled);
        }
    }
}