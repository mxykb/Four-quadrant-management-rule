package com.example.fourquadrant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class QuadrantView extends View {
    
    private Paint gridPaint;
    private Paint textPaint;
    private Paint taskPaint;
    private Paint backgroundPaint;
    
    private List<Task> tasks;
    private int maxScore = 10;
    
    public QuadrantView(Context context) {
        super(context);
        init();
    }
    
    public QuadrantView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public QuadrantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        tasks = new ArrayList<>();
        
        // 网格线画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);
        
        // 文本画笔
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        
        // 任务画笔
        taskPaint = new Paint();
        taskPaint.setAntiAlias(true);
        taskPaint.setStyle(Paint.Style.FILL);
        
        // 背景画笔
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
    }
    
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;
        
        // 绘制背景
        canvas.drawColor(Color.WHITE);
        
        // 计算边距
        int margin = 60;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        
        // 绘制四象限背景
        drawQuadrantBackgrounds(canvas, margin, chartWidth, chartHeight);
        
        // 绘制网格线
        drawGrid(canvas, margin, chartWidth, chartHeight);
        
        // 绘制坐标轴标签
        drawAxisLabels(canvas, margin, chartWidth, chartHeight);
        
        // 绘制任务点
        drawTasks(canvas, margin, chartWidth, chartHeight);
    }
    
    private void drawQuadrantBackgrounds(Canvas canvas, int margin, int chartWidth, int chartHeight) {
        int centerX = margin + chartWidth / 2;
        int centerY = margin + chartHeight / 2;
        
        // 第一象限：紧急且重要 (右上)
        backgroundPaint.setColor(Color.parseColor("#FFEBEE"));
        canvas.drawRect(centerX, margin, margin + chartWidth, centerY, backgroundPaint);
        
        // 第二象限：重要不紧急 (左上)
        backgroundPaint.setColor(Color.parseColor("#FFF3E0"));
        canvas.drawRect(margin, margin, centerX, centerY, backgroundPaint);
        
        // 第三象限：紧急不重要 (右下)
        backgroundPaint.setColor(Color.parseColor("#E3F2FD"));
        canvas.drawRect(centerX, centerY, margin + chartWidth, margin + chartHeight, backgroundPaint);
        
        // 第四象限：不紧急不重要 (左下)
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(margin, centerY, centerX, margin + chartHeight, backgroundPaint);
    }
    
    private void drawGrid(Canvas canvas, int margin, int chartWidth, int chartHeight) {
        int centerX = margin + chartWidth / 2;
        int centerY = margin + chartHeight / 2;
        
        // 垂直线
        canvas.drawLine(centerX, margin, centerX, margin + chartHeight, gridPaint);
        
        // 水平线
        canvas.drawLine(margin, centerY, margin + chartWidth, centerY, gridPaint);
    }
    
    private void drawAxisLabels(Canvas canvas, int margin, int chartWidth, int chartHeight) {
        int centerX = margin + chartWidth / 2;
        int centerY = margin + chartHeight / 2;
        
        // Y轴标签 (重要性)
        textPaint.setTextSize(14f);
        textPaint.setColor(Color.BLACK);
        canvas.save();
        canvas.rotate(-90, margin - 20, centerY);
        canvas.drawText("重要性", margin - 20, centerY, textPaint);
        canvas.restore();
        
        // X轴标签 (紧急性)
        canvas.drawText("紧急性", centerX, margin + chartHeight + 30, textPaint);
        
        // 象限标签
        textPaint.setTextSize(12f);
        textPaint.setColor(Color.GRAY);
        
        // 第一象限
        canvas.drawText("紧急且重要", centerX + chartWidth / 4, centerY - chartHeight / 4, textPaint);
        
        // 第二象限
        canvas.drawText("重要不紧急", centerX - chartWidth / 4, centerY - chartHeight / 4, textPaint);
        
        // 第三象限
        canvas.drawText("紧急不重要", centerX + chartWidth / 4, centerY + chartHeight / 4, textPaint);
        
        // 第四象限
        canvas.drawText("不紧急不重要", centerX - chartWidth / 4, centerY + chartHeight / 4, textPaint);
    }
    
    private void drawTasks(Canvas canvas, int margin, int chartWidth, int chartHeight) {
        if (tasks == null || tasks.isEmpty()) return;
        
        int centerX = margin + chartWidth / 2;
        int centerY = margin + chartHeight / 2;
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getName() == null || task.getName().isEmpty()) continue;
            
            // 计算任务位置
            float x = margin + (task.getUrgency() / (float) maxScore) * chartWidth;
            float y = margin + chartHeight - (task.getImportance() / (float) maxScore) * chartHeight;
            
            // 设置任务点颜色
            int quadrant = getQuadrant(task.getImportance(), task.getUrgency());
            switch (quadrant) {
                case 1: // 紧急且重要
                    taskPaint.setColor(getResources().getColor(R.color.quadrant_1_color, null));
                    break;
                case 2: // 重要不紧急
                    taskPaint.setColor(getResources().getColor(R.color.quadrant_2_color, null));
                    break;
                case 3: // 紧急不重要
                    taskPaint.setColor(getResources().getColor(R.color.quadrant_3_color, null));
                    break;
                case 4: // 不紧急不重要
                    taskPaint.setColor(getResources().getColor(R.color.quadrant_4_color, null));
                    break;
            }
            
            // 绘制任务点
            canvas.drawCircle(x, y, 15, taskPaint);
            
            // 绘制任务名称
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(10f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            
            // 如果任务名称太长，截断显示
            String displayName = task.getName();
            if (displayName.length() > 8) {
                displayName = displayName.substring(0, 8) + "...";
            }
            
            canvas.drawText(displayName, x, y + 3, textPaint);
            
            // 绘制任务编号
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(8f);
            canvas.drawText(String.valueOf(i + 1), x, y - 8, textPaint);
        }
    }
    
    private int getQuadrant(int importance, int urgency) {
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
    
    public static class Task {
        private String name;
        private int importance;
        private int urgency;
        
        public Task(String name, int importance, int urgency) {
            this.name = name;
            this.importance = importance;
            this.urgency = urgency;
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
    }
} 