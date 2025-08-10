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
    private MaterialToolbar toolbar;
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
        setupTimeRangeChips();
        setupKpiCardClickListeners();
        setupCharts();
        setupRecyclerViews();
        setupSwipeRefresh();
        observeData();
        
        return view;
    }
    
    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
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
        
        // 设置返回按钮
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showTabs();
            }
        });
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }
    
    private void setupTimeRangeChips() {
        chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                String timeRange = getTimeRangeFromChipId(checkedId);
                viewModel.setTimeRange(timeRange);
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
        
        Description desc = new Description();
        desc.setText("");
        lineChartCompletionTrend.setDescription(desc);
        
        lineChartCompletionTrend.getLegend().setEnabled(false);
        lineChartCompletionTrend.getAxisRight().setEnabled(false);
        
        XAxis xAxis = lineChartCompletionTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        lineChartCompletionTrend.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getContext(), "该天完成 " + (int)e.getY() + " 个任务", Toast.LENGTH_SHORT).show();
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
        
        Description desc = new Description();
        desc.setText("");
        barChartPomodoroDistribution.setDescription(desc);
        
        barChartPomodoroDistribution.getLegend().setEnabled(false);
        barChartPomodoroDistribution.getAxisRight().setEnabled(false);
        
        XAxis xAxis = barChartPomodoroDistribution.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
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
        if (trends == null || trends.isEmpty()) return;
        
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < trends.size(); i++) {
            ChartData.CompletionTrend trend = trends.get(i);
            entries.add(new Entry(i, trend.getCompletedTasks()));
            labels.add(trend.getDate());
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "任务完成数");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E3F2FD"));
        dataSet.setValueTextSize(10f);
        
        LineData lineData = new LineData(dataSet);
        lineChartCompletionTrend.setData(lineData);
        
        XAxis xAxis = lineChartCompletionTrend.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        
        lineChartCompletionTrend.invalidate();
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
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChartPomodoroDistribution.setData(barData);
        
        XAxis xAxis = barChartPomodoroDistribution.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        
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
}