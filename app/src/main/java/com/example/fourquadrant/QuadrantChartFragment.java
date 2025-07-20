package com.example.fourquadrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuadrantChartFragment extends Fragment {
    
    private QuadrantView quadrantView;
    private Button generateButton;
    private Button saveButton;
    private Button settingsButton;
    private List<QuadrantView.Task> tasks;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasks = new ArrayList<>();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 尝试从MainActivity获取任务数据
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // 延迟一下，确保TaskListFragment已经初始化
            view.post(() -> {
                try {
                    TaskListFragment taskListFragment = (TaskListFragment) getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag("f0");
                    if (taskListFragment != null) {
                        List<QuadrantView.Task> currentTasks = taskListFragment.getCurrentTasks();
                        updateTasks(currentTasks);
                    }
                } catch (Exception e) {
                    // 忽略异常，任务数据会在页面切换时同步
                }
            });
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quadrant_chart, container, false);
        
        quadrantView = view.findViewById(R.id.quadrant_view);
        generateButton = view.findViewById(R.id.generate_button);
        saveButton = view.findViewById(R.id.save_button);
        settingsButton = view.findViewById(R.id.settings_button);
        
        setupButtons();
        
        return view;
    }
    
    private void setupButtons() {
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQuadrant();
            }
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveQuadrantImage();
            }
        });
        
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    
    public void updateTasks(List<QuadrantView.Task> newTasks) {
        this.tasks = newTasks;
        if (quadrantView != null) {
            quadrantView.setTasks(tasks);
            // 如果任务列表不为空，自动生成图表
            if (tasks != null && !tasks.isEmpty()) {
                quadrantView.invalidate(); // 强制重绘
            }
        }
    }
    
    private void generateQuadrant() {
        if (tasks == null || tasks.isEmpty()) {
            Toast.makeText(getContext(), "请先在任务列表中添加任务", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 过滤掉空名称的任务
        List<QuadrantView.Task> validTasks = new ArrayList<>();
        for (QuadrantView.Task task : tasks) {
            if (task != null && !task.getName().trim().isEmpty()) {
                validTasks.add(task);
            }
        }
        
        if (validTasks.isEmpty()) {
            Toast.makeText(getContext(), "请先添加有效的任务", Toast.LENGTH_SHORT).show();
            return;
        }
        
        quadrantView.setTasks(validTasks);
        quadrantView.invalidate(); // 强制重绘
        Toast.makeText(getContext(), "四象限图表已生成", Toast.LENGTH_SHORT).show();
    }
    
    private void saveQuadrantImage() {
        if (tasks == null || tasks.isEmpty()) {
            Toast.makeText(getContext(), "请先生成四象限图表", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // 创建位图
            Bitmap bitmap = Bitmap.createBitmap(
                quadrantView.getWidth(),
                quadrantView.getHeight(),
                Bitmap.Config.ARGB_8888
            );
            
            // 绘制视图到位图
            Canvas canvas = new Canvas(bitmap);
            quadrantView.draw(canvas);
            
            // 保存到文件
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "quadrant_" + timeStamp + ".png";
            
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(picturesDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            // 通知媒体扫描器
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            getActivity().sendBroadcast(mediaScanIntent);
            
            Toast.makeText(getContext(), "图片已保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(getContext(), "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 