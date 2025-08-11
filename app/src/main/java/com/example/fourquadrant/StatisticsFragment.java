package com.example.fourquadrant;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    
    private StatisticsViewModel viewModel;
    private ChipGroup chipGroupTime;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // KPI卡片
    private MaterialCardView cardCompletedTasks;
    private MaterialCardView cardPomodoroCount;
    private MaterialCardView cardCompletionRate;
    private MaterialCardView cardAvgImportance;
    
    // KPI数据显示
    private TextView tvCompletedTasksCount;
    private TextView tvPomodoroCount;
    private TextView tvCompletionRate;
    private TextView tvAvgImportance;
    private ProgressBar progressCompletionRate;
    
    // KPI标签显示
    private TextView tvCompletedTasksLabel;
    private TextView tvPomodoroCountLabel;
    private TextView tvCompletionRateLabel;
    private TextView tvAvgImportanceLabel;
    
    // 图表标题
    private TextView tvChartSubtitle;
    
    // 图表组件
    private LineChart lineChartCompletionTrend;
    private PieChart pieChartQuadrantDistribution;
    private BarChart barChartPomodoroDistribution;
    
    // 列表和建议组件
    private RecyclerView rvHighPriorityTasks;
    private RecyclerView rvLongestDurationTasks;
    private TextView tvNoHighPriorityTasks;
    private TextView tvNoDurationTasks;
    private LinearLayout llSuggestionsContainer;
    
    // 适配器
    private HighPriorityTaskAdapter highPriorityTaskAdapter;
    private LongestDurationTaskAdapter longestDurationTaskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics_new, container, false);
        
        initViews(view);
        initViewModel();
        // 设置ViewModel的Context
        viewModel.setContext(requireContext());
        // 初始化KPI标签（默认为"today"）
        updateKpiLabels("today");
        setupTimeRangeChips();
        setupKpiCardClickListeners();
        setupCharts();
        setupRecyclerViews();
        setupSwipeRefresh();
        observeData();
        
        return view;
    }
    
    private void initViews(View view) {
        chipGroupTime = view.findViewById(R.id.chip_group_time);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        
        // KPI卡片
        cardCompletedTasks = view.findViewById(R.id.card_completed_tasks);
        cardPomodoroCount = view.findViewById(R.id.card_pomodoro_count);
        cardCompletionRate = view.findViewById(R.id.card_completion_rate);
        cardAvgImportance = view.findViewById(R.id.card_avg_importance);
        
        // KPI数据显示
        tvCompletedTasksCount = view.findViewById(R.id.tv_completed_tasks_count);
        tvPomodoroCount = view.findViewById(R.id.tv_pomodoro_count);
        tvCompletionRate = view.findViewById(R.id.tv_completion_rate);
        tvAvgImportance = view.findViewById(R.id.tv_avg_importance);
        progressCompletionRate = view.findViewById(R.id.progress_completion_rate);
        
        // KPI标签显示
        tvCompletedTasksLabel = view.findViewById(R.id.tv_completed_tasks_label);
        tvPomodoroCountLabel = view.findViewById(R.id.tv_pomodoro_count_label);
        tvCompletionRateLabel = view.findViewById(R.id.tv_completion_rate_label);
        tvAvgImportanceLabel = view.findViewById(R.id.tv_avg_importance_label);
        
        // 图表标题
        tvChartSubtitle = view.findViewById(R.id.tv_chart_subtitle);
        
        // 初始化图表组件
        lineChartCompletionTrend = view.findViewById(R.id.line_chart_completion_trend);
        pieChartQuadrantDistribution = view.findViewById(R.id.pie_chart_quadrant_distribution);
        barChartPomodoroDistribution = view.findViewById(R.id.bar_chart_pomodoro_distribution);
        
        // 初始化列表和建议组件
        rvHighPriorityTasks = view.findViewById(R.id.rv_high_priority_tasks);
        rvLongestDurationTasks = view.findViewById(R.id.rv_longest_duration_tasks);
        tvNoHighPriorityTasks = view.findViewById(R.id.tv_no_high_priority_tasks);
        tvNoDurationTasks = view.findViewById(R.id.tv_no_duration_tasks);
        llSuggestionsContainer = view.findViewById(R.id.ll_suggestions_container);
        
        // 移除了toolbar设置，使用主Activity的汉堡菜单导航
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }
    
    private void setupTimeRangeChips() {
        chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String timeRange = getTimeRangeFromChipId(checkedId);
                
                if ("custom".equals(timeRange)) {
                    showCustomDateRangePicker();
                } else {
                    updateKpiLabels(timeRange);
                    viewModel.setTimeRange(timeRange);
                }
            }
        });
    }
    
    private String getTimeRangeFromChipId(int chipId) {
        if (chipId == R.id.chip_today) {
            return "today";
        } else if (chipId == R.id.chip_week) {
            return "week";
        } else if (chipId == R.id.chip_month) {
            return "month";
        } else if (chipId == R.id.chip_custom) {
            return "custom";
        }
        return "today";
    }
    
    private void setupKpiCardClickListeners() {
        cardCompletedTasks.setOnClickListener(v -> {
            Toast.makeText(getContext(), "查看已完成任务详情", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到详细页面
        });
        
        cardPomodoroCount.setOnClickListener(v -> {
            Toast.makeText(getContext(), "查看番茄钟使用详情", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到详细页面
        });
        
        cardCompletionRate.setOnClickListener(v -> {
            Toast.makeText(getContext(), "查看任务完成率趋势", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到详细页面
        });
        
        cardAvgImportance.setOnClickListener(v -> {
            Toast.makeText(getContext(), "查看重要性评分分布", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到详细页面
        });
    }
    
    private void setupCharts() {
        setupLineChart();
        setupPieChart();
        setupBarChart();
    }
    
    private void setupLineChart() {
        lineChartCompletionTrend.setDrawGridBackground(false);
        lineChartCompletionTrend.setDrawBorders(false);
        lineChartCompletionTrend.setTouchEnabled(true);
        lineChartCompletionTrend.setDragEnabled(true);
        lineChartCompletionTrend.setScaleEnabled(false);
        lineChartCompletionTrend.setPinchZoom(false);
        
        Description desc = new Description();
        desc.setText("");
        lineChartCompletionTrend.setDescription(desc);
        
        lineChartCompletionTrend.getLegend().setEnabled(false);
        lineChartCompletionTrend.getAxisRight().setEnabled(false);
        
        // 配置X轴
        XAxis xAxis = lineChartCompletionTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); // 旋转标签避免拥挤
        xAxis.setTextSize(10f);
        
        // 配置Y轴
        lineChartCompletionTrend.getAxisLeft().setAxisMinimum(0f); // 设置最小值为0
        lineChartCompletionTrend.getAxisLeft().setGranularity(1f); // 设置间隔为1
        lineChartCompletionTrend.getAxisLeft().setDrawGridLines(true);
        lineChartCompletionTrend.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));
        lineChartCompletionTrend.getAxisLeft().setTextSize(10f);
        
        lineChartCompletionTrend.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getContext(), "完成 " + (int)e.getY() + " 个任务", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNothingSelected() {}
        });
    }
    
    private void setupPieChart() {
        pieChartQuadrantDistribution.setUsePercentValues(false);
        pieChartQuadrantDistribution.setDrawHoleEnabled(true);
        pieChartQuadrantDistribution.setHoleRadius(40f);
        pieChartQuadrantDistribution.setTransparentCircleRadius(45f);
        
        Description desc = new Description();
        desc.setText("");
        pieChartQuadrantDistribution.setDescription(desc);
        
        pieChartQuadrantDistribution.getLegend().setEnabled(true);
        pieChartQuadrantDistribution.setEntryLabelTextSize(12f);
        
        pieChartQuadrantDistribution.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry pieEntry = (PieEntry) e;
                    Toast.makeText(getContext(), pieEntry.getLabel() + ": " + (int)pieEntry.getValue() + " 个任务", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onNothingSelected() {}
        });
    }
    
    private void setupBarChart() {
        barChartPomodoroDistribution.setDrawGridBackground(false);
        barChartPomodoroDistribution.setDrawBorders(false);
        barChartPomodoroDistribution.setTouchEnabled(true);
        barChartPomodoroDistribution.setDragEnabled(true);
        barChartPomodoroDistribution.setScaleEnabled(false);
        barChartPomodoroDistribution.setPinchZoom(false);
        
        Description desc = new Description();
        desc.setText("");
        barChartPomodoroDistribution.setDescription(desc);
        
        barChartPomodoroDistribution.getLegend().setEnabled(false);
        barChartPomodoroDistribution.getAxisRight().setEnabled(false);
        
        // 配置X轴
        XAxis xAxis = barChartPomodoroDistribution.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        
        // 配置Y轴
        barChartPomodoroDistribution.getAxisLeft().setAxisMinimum(0f); // 设置最小值为0
        barChartPomodoroDistribution.getAxisLeft().setGranularity(1f); // 设置间隔为1
        barChartPomodoroDistribution.getAxisLeft().setDrawGridLines(true);
        barChartPomodoroDistribution.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));
        barChartPomodoroDistribution.getAxisLeft().setTextSize(10f);
    }
    
    private void setupRecyclerViews() {
        // 设置高优先级任务列表
        highPriorityTaskAdapter = new HighPriorityTaskAdapter(new ArrayList<>(), task -> {
            Toast.makeText(getContext(), "点击查看任务: " + task.getTaskName(), Toast.LENGTH_SHORT).show();
            // TODO: 跳转到任务详情页面
        });
        rvHighPriorityTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHighPriorityTasks.setAdapter(highPriorityTaskAdapter);
        
        // 设置耗时最长任务列表
        longestDurationTaskAdapter = new LongestDurationTaskAdapter(new ArrayList<>(), task -> {
            Toast.makeText(getContext(), "点击查看任务: " + task.getTaskName(), Toast.LENGTH_SHORT).show();
            // TODO: 跳转到任务详情页面
        });
        rvLongestDurationTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLongestDurationTasks.setAdapter(longestDurationTaskAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.accent_color,
            R.color.secondary_color
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshAllData();
        });
    }
    
    private void observeData() {
        // 加载状态观察
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });
        
        // 错误信息观察
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        // 新的细分数据观察
        viewModel.getKpiData().observe(getViewLifecycleOwner(), this::updateKpiUI);
        viewModel.getTaskTrendData().observe(getViewLifecycleOwner(), this::updateLineChart);
        viewModel.getQuadrantData().observe(getViewLifecycleOwner(), this::updatePieChart);
        viewModel.getPomodoroData().observe(getViewLifecycleOwner(), this::updateBarChart);
        viewModel.getHighPriorityTasks().observe(getViewLifecycleOwner(), this::updateHighPriorityTasks);
        viewModel.getLongestTasks().observe(getViewLifecycleOwner(), this::updateLongestDurationTasks);
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), this::updateSuggestions);
        
        // 向后兼容的组合数据观察
        viewModel.getStatisticsData().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getChartData().observe(getViewLifecycleOwner(), this::updateCharts);
        viewModel.getTaskAnalysisData().observe(getViewLifecycleOwner(), this::updateTaskAnalysis);
    }
    
    private void updateUI(StatisticsData data) {
        if (data == null) return;
        
        // 更新数据显示
        tvCompletedTasksCount.setText(String.valueOf(data.getCompletedTasksToday()));
        tvPomodoroCount.setText(String.valueOf(data.getPomodoroCountWeek()));
        
        // 格式化完成率
        DecimalFormat df = new DecimalFormat("#.#");
        String completionRateText = df.format(data.getCompletionRateWeek()) + "%";
        tvCompletionRate.setText(completionRateText);
        progressCompletionRate.setProgress((int) data.getCompletionRateWeek());
        
        // 格式化重要性评分
        String avgImportanceText = df.format(data.getAvgImportanceScore());
        tvAvgImportance.setText(avgImportanceText);
        
        // 更新标题文本根据时间范围
        updateKpiTitles();
    }
    
    private void updateCharts(ChartData.ChartDataSet chartDataSet) {
        if (chartDataSet == null) return;
        
        updateLineChart(chartDataSet.getCompletionTrends());
        updatePieChart(chartDataSet.getQuadrantDistributions());
        updateBarChart(chartDataSet.getPomodoroDistributions());
    }
    
    private void updateLineChart(List<ChartData.CompletionTrend> trends) {
        android.util.Log.d("StatisticsFragment", "开始更新折线图，趋势数据: " + (trends != null ? trends.size() : "null") + " 个数据点");
        if (trends == null || trends.isEmpty()) {
            android.util.Log.w("StatisticsFragment", "趋势数据为空，跳过图表更新");
            return;
        }
        
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < trends.size(); i++) {
            ChartData.CompletionTrend trend = trends.get(i);
            entries.add(new Entry(i, trend.getCompletedTasks()));
            labels.add(trend.getDate());
            android.util.Log.d("StatisticsFragment", "数据点 " + i + ": 时间=" + trend.getDate() + ", 完成数=" + trend.getCompletedTasks());
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "任务完成数");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E3F2FD"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false); // 禁用数据点显示
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // 格式化为整数
            }
        });
        
        LineData lineData = new LineData(dataSet);
        lineChartCompletionTrend.setData(lineData);
        
        XAxis xAxis = lineChartCompletionTrend.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        // 根据数据点数量调整标签显示
        if (labels.size() > 7) {
            xAxis.setLabelCount(Math.min(7, labels.size()), false); // 最多显示7个标签
        } else {
            xAxis.setLabelCount(labels.size(), true);
        }
        
        lineChartCompletionTrend.invalidate();
        android.util.Log.d("StatisticsFragment", "折线图更新完成");
    }
    
    private void updatePieChart(List<ChartData.QuadrantDistribution> distributions) {
        if (distributions == null || distributions.isEmpty()) return;
        
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        for (ChartData.QuadrantDistribution distribution : distributions) {
            entries.add(new PieEntry(distribution.getTaskCount(), distribution.getQuadrantName()));
            colors.add(distribution.getColor());
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "四象限分布");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // 格式化为整数
            }
        });
        
        PieData pieData = new PieData(dataSet);
        pieChartQuadrantDistribution.setData(pieData);
        pieChartQuadrantDistribution.invalidate();
    }
    
    private void updateBarChart(List<ChartData.PomodoroDistribution> distributions) {
        if (distributions == null || distributions.isEmpty()) return;
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        for (int i = 0; i < distributions.size(); i++) {
            ChartData.PomodoroDistribution distribution = distributions.get(i);
            entries.add(new BarEntry(i, distribution.getPomodoroCount()));
            labels.add(distribution.getTimePeriod());
            colors.add(distribution.getColor());
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "番茄钟分布");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false); // 禁用数据点显示
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // 格式化为整数
            }
        });
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChartPomodoroDistribution.setData(barData);
        
        XAxis xAxis = barChartPomodoroDistribution.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        // 优化X轴标签显示，避免过于拥挤
        int maxLabels = Math.min(labels.size(), 7);
        xAxis.setLabelCount(maxLabels);
        
        barChartPomodoroDistribution.invalidate();
    }
    
    private void updateTaskAnalysis(TaskAnalysisData.TaskAnalysisDataSet dataSet) {
        if (dataSet == null) return;
        
        updateHighPriorityTasks(dataSet.getHighPriorityTasks());
        updateLongestDurationTasks(dataSet.getLongestDurationTasks());
        updateSuggestions(dataSet.getSuggestions());
    }
    
    private void updateHighPriorityTasks(List<TaskAnalysisData.HighPriorityTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            rvHighPriorityTasks.setVisibility(View.GONE);
            tvNoHighPriorityTasks.setVisibility(View.VISIBLE);
        } else {
            rvHighPriorityTasks.setVisibility(View.VISIBLE);
            tvNoHighPriorityTasks.setVisibility(View.GONE);
            highPriorityTaskAdapter.updateTasks(tasks);
        }
    }
    
    private void updateLongestDurationTasks(List<TaskAnalysisData.LongestDurationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            rvLongestDurationTasks.setVisibility(View.GONE);
            tvNoDurationTasks.setVisibility(View.VISIBLE);
        } else {
            rvLongestDurationTasks.setVisibility(View.VISIBLE);
            tvNoDurationTasks.setVisibility(View.GONE);
            longestDurationTaskAdapter.updateTasks(tasks);
        }
    }
    
    private void updateSuggestions(List<TaskAnalysisData.Suggestion> suggestions) {
        llSuggestionsContainer.removeAllViews();
        
        if (suggestions != null && !suggestions.isEmpty()) {
            for (TaskAnalysisData.Suggestion suggestion : suggestions) {
                View suggestionView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_suggestion, llSuggestionsContainer, false);
                
                TextView tvSuggestionText = suggestionView.findViewById(R.id.tv_suggestion_text);
                tvSuggestionText.setText(suggestion.getText());
                
                llSuggestionsContainer.addView(suggestionView);
            }
        }
    }
    
    /**
     * 更新KPI UI（新方法）
     */
    private void updateKpiUI(StatisticsData data) {
        if (data == null) return;
        
        // 更新数据显示
        tvCompletedTasksCount.setText(String.valueOf(data.getCompletedTasksToday()));
        tvPomodoroCount.setText(String.valueOf(data.getPomodoroCountWeek()));
        
        // 格式化完成率
        DecimalFormat df = new DecimalFormat("#.#");
        String completionRateText = df.format(data.getCompletionRateWeek()) + "%";
        tvCompletionRate.setText(completionRateText);
        progressCompletionRate.setProgress((int) data.getCompletionRateWeek());
        
        // 格式化重要性评分
        String avgImportanceText = df.format(data.getAvgImportanceScore());
        tvAvgImportance.setText(avgImportanceText);
        
        // 更新卡片标题（根据时间范围）
        updateKpiTitles();
    }
    
    private void updateKpiTitles() {
        String timeRange = viewModel.getCurrentTimeRange();
        // 这里可以根据不同的时间范围更新卡片标题
        // 例如："今日完成任务" vs "本周完成任务" vs "本月完成任务"
        // 暂时保持静态文本，后续可以动态更新
    }
    
    /**
     * 显示自定义日期范围选择弹窗
     */
    private void showCustomDateRangePicker() {
        if (getContext() == null) return;
        
        CustomDateRangePickerDialog dialog = new CustomDateRangePickerDialog(
            getContext(),
            (startDate, endDate) -> {
                // 验证时间跨度不超过90天
                long diffInMillis = endDate.getTime() - startDate.getTime();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                
                if (diffInDays > 90) {
                    Toast.makeText(getContext(), "时间跨度不能超过90天，请重新选择", Toast.LENGTH_LONG).show();
                    // 重新取消自定义选择，回到之前的选择
                    resetToLastTimeRange();
                    return;
                }
                
                if (diffInDays < 0) {
                    Toast.makeText(getContext(), "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                    resetToLastTimeRange();
                    return;
                }
                
                // 设置自定义时间范围
                String customRange = formatCustomRange(startDate, endDate);
                updateKpiLabels(customRange);
                viewModel.setTimeRange(customRange);
                
                Toast.makeText(getContext(), 
                    "已设置时间范围：" + formatDateRange(startDate, endDate), 
                    Toast.LENGTH_SHORT).show();
            },
            () -> {
                // 用户取消选择，重置到之前的选择
                resetToLastTimeRange();
            }
        );
        
        dialog.show();
    }
    
    /**
     * 重置到上一次的时间范围选择
     */
    private void resetToLastTimeRange() {
        String currentRange = viewModel.getCurrentTimeRange();
        if (currentRange.startsWith("custom")) {
            // 如果当前是自定义范围，回到"今日"
            chipGroupTime.check(R.id.chip_today);
            viewModel.setTimeRange("today");
        } else {
            // 回到对应的Chip
            int chipId = getChipIdFromTimeRange(currentRange);
            chipGroupTime.check(chipId);
        }
    }
    
    /**
     * 根据时间范围获取对应的Chip ID
     */
    private int getChipIdFromTimeRange(String timeRange) {
        switch (timeRange) {
            case "today": return R.id.chip_today;
            case "week": return R.id.chip_week;
            case "month": return R.id.chip_month;
            default: return R.id.chip_today;
        }
    }
    
    /**
     * 格式化自定义时间范围为ViewModel使用的格式
     */
    private String formatCustomRange(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return "custom_" + sdf.format(startDate) + "_" + sdf.format(endDate);
    }
    
    /**
     * 格式化日期范围为显示文本
     */
    private String formatDateRange(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日", Locale.getDefault());
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }
    
    /**
     * 根据时间范围更新KPI标签
     */
    private void updateKpiLabels(String timeRange) {
        String completedTasksLabel;
        String pomodoroCountLabel;
        String completionRateLabel;
        String avgImportanceLabel;
        String chartSubtitle;
        
        switch (timeRange) {
            case "today":
                completedTasksLabel = "今日完成任务";
                pomodoroCountLabel = "今日番茄钟";
                completionRateLabel = "今日完成率";
                avgImportanceLabel = "平均重要性";
                chartSubtitle = "今日任务完成情况（按小时统计）";
                break;
                
            case "week":
                completedTasksLabel = "本周完成任务";
                pomodoroCountLabel = "本周番茄钟";
                completionRateLabel = "本周完成率";
                avgImportanceLabel = "平均重要性";
                chartSubtitle = "最近一周的任务完成情况";
                break;
                
            case "month":
                completedTasksLabel = "本月完成任务";
                pomodoroCountLabel = "本月番茄钟";
                completionRateLabel = "本月完成率";
                avgImportanceLabel = "平均重要性";
                chartSubtitle = "最近一月的任务完成情况";
                break;
                
            default:
                // 自定义时间范围
                if (timeRange.startsWith("custom_")) {
                    completedTasksLabel = "所选时间完成任务";
                    pomodoroCountLabel = "所选时间番茄钟";
                    completionRateLabel = "所选时间完成率";
                    avgImportanceLabel = "平均重要性";
                    chartSubtitle = "自定义时间范围的任务完成情况";
                } else {
                    // 默认显示
                    completedTasksLabel = "完成任务";
                    pomodoroCountLabel = "番茄钟数";
                    completionRateLabel = "完成率";
                    avgImportanceLabel = "平均重要性";
                    chartSubtitle = "任务完成情况";
                }
                break;
        }
        
        // 更新UI
        if (tvCompletedTasksLabel != null) {
            tvCompletedTasksLabel.setText(completedTasksLabel);
        }
        if (tvPomodoroCountLabel != null) {
            tvPomodoroCountLabel.setText(pomodoroCountLabel);
        }
        if (tvCompletionRateLabel != null) {
            tvCompletionRateLabel.setText(completionRateLabel);
        }
        if (tvAvgImportanceLabel != null) {
            tvAvgImportanceLabel.setText(avgImportanceLabel);
        }
        if (tvChartSubtitle != null) {
            tvChartSubtitle.setText(chartSubtitle);
        }
    }
}