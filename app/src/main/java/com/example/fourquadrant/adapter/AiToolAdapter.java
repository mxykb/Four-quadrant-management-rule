package com.example.fourquadrant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fourquadrant.R;
import com.example.fourquadrant.model.AiTool;

import java.util.List;

/**
 * AI工具列表适配器
 */
public class AiToolAdapter extends RecyclerView.Adapter<AiToolAdapter.AiToolViewHolder> {
    
    private List<AiTool> toolList;
    private OnToolToggleListener toggleListener;
    
    public interface OnToolToggleListener {
        void onToolToggle(AiTool tool, boolean enabled);
    }
    
    public AiToolAdapter(List<AiTool> toolList) {
        this.toolList = toolList;
    }
    
    public void setOnToolToggleListener(OnToolToggleListener listener) {
        this.toggleListener = listener;
    }
    
    @NonNull
    @Override
    public AiToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_tool, parent, false);
        return new AiToolViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AiToolViewHolder holder, int position) {
        AiTool tool = toolList.get(position);
        holder.bind(tool);
    }
    
    @Override
    public int getItemCount() {
        return toolList != null ? toolList.size() : 0;
    }
    
    public void updateToolList(List<AiTool> newToolList) {
        this.toolList = newToolList;
        notifyDataSetChanged();
    }
    
    class AiToolViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivToolIcon;
        private TextView tvToolName;
        private TextView tvToolDescription;
        private SwitchCompat switchToolEnabled;
        
        public AiToolViewHolder(@NonNull View itemView) {
            super(itemView);
            ivToolIcon = itemView.findViewById(R.id.iv_tool_icon);
            tvToolName = itemView.findViewById(R.id.tv_tool_name);
            tvToolDescription = itemView.findViewById(R.id.tv_tool_description);
            switchToolEnabled = itemView.findViewById(R.id.switch_tool_enabled);
        }
        
        public void bind(AiTool tool) {
            // 设置工具图标
            if (tool.getIconResId() != 0) {
                ivToolIcon.setImageResource(tool.getIconResId());
            } else {
                ivToolIcon.setImageResource(R.drawable.ic_tool);
            }
            
            // 设置工具名称和描述
            tvToolName.setText(tool.getDisplayName());
            tvToolDescription.setText(tool.getDescription());
            
            // 设置开关状态
            switchToolEnabled.setOnCheckedChangeListener(null); // 清除之前的监听器
            switchToolEnabled.setChecked(tool.isEnabled());
            
            // 设置开关监听器
            switchToolEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tool.setEnabled(isChecked);
                if (toggleListener != null) {
                    toggleListener.onToolToggle(tool, isChecked);
                }
            });
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                switchToolEnabled.setChecked(!switchToolEnabled.isChecked());
            });
        }
    }
}