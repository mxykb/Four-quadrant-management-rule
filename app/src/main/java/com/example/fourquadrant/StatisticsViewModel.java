package com.example.fourquadrant;

import android.content.Context;
import android.graphics.Color;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StatisticsViewModel extends ViewModel {
    
    // 页面状态数据
    private MutableLiveData<StatisticsData> kpiData;
    private MutableLiveData<List<ChartData.CompletionTrend>> taskTrendData;
    private MutableLiveData<List<ChartData.QuadrantDistribution>> quadrantData;
    private MutableLiveData<List<ChartData.PomodoroDistribution>> pomodoroData;
    private MutableLiveData<List<TaskAnalysisData.HighPriorityTask>> highPriorityTasks;
    private MutableLiveData<List<TaskAnalysisData.LongestDurationTask>> longestTasks;
    private MutableLiveData<List<TaskAnalysisData.Suggestion>> suggestions;
    
    // 加载状态
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    
    // 原有的组合数据（为了向后兼容）
    private MutableLiveData<StatisticsData> statisticsData;
    private MutableLiveData<ChartData.ChartDataSet> chartData;
    private MutableLiveData<TaskAnalysisData.TaskAnalysisDataSet> taskAnalysisData;
    
    private String currentTimeRange = "today";
    private StatisticsDataManager dataManager;
    private boolean useRealData = true; // 是否使用真实数据
    
    public StatisticsViewModel() {
        // 初始化所有LiveData
        initializeLiveData();
        
        // 注意：dataManager 需要在 setContext 后才能初始化
    }
    
    /**
     * 设置Context并初始化数据管理器
     */
    public void setContext(Context context) {
        if (dataManager == null) {
            dataManager = new StatisticsDataManager(context);
            // 加载初始数据
            loadAllData("today");
        }
    }
    
    private void initializeLiveData() {
        kpiData = new MutableLiveData<>();
        taskTrendData = new MutableLiveData<>();
        quadrantData = new MutableLiveData<>();
        pomodoroData = new MutableLiveData<>();
        highPriorityTasks = new MutableLiveData<>();
        longestTasks = new MutableLiveData<>();
        suggestions = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        
        // 向后兼容的组合数据
        statisticsData = new MutableLiveData<>();
        chartData = new MutableLiveData<>();
        taskAnalysisData = new MutableLiveData<>();
    }
    
    public LiveData<StatisticsData> getStatisticsData() {
        return statisticsData;
    }
    
    public LiveData<ChartData.ChartDataSet> getChartData() {
        return chartData;
    }
    
    public LiveData<TaskAnalysisData.TaskAnalysisDataSet> getTaskAnalysisData() {
        return taskAnalysisData;
    }
    
    // 新的细分状态数据访问方法
    public LiveData<StatisticsData> getKpiData() {
        return kpiData;
    }
    
    public LiveData<List<ChartData.CompletionTrend>> getTaskTrendData() {
        return taskTrendData;
    }
    
    public LiveData<List<ChartData.QuadrantDistribution>> getQuadrantData() {
        return quadrantData;
    }
    
    public LiveData<List<ChartData.PomodoroDistribution>> getPomodoroData() {
        return pomodoroData;
    }
    
    public LiveData<List<TaskAnalysisData.HighPriorityTask>> getHighPriorityTasks() {
        return highPriorityTasks;
    }
    
    public LiveData<List<TaskAnalysisData.LongestDurationTask>> getLongestTasks() {
        return longestTasks;
    }
    
    public LiveData<List<TaskAnalysisData.Suggestion>> getSuggestions() {
        return suggestions;
    }
    
    // 状态管理
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 设置时间范围并刷新数据
     */
    public void setTimeRange(String timeRange) {
        if (!timeRange.equals(currentTimeRange)) {
            this.currentTimeRange = timeRange;
            loadAllData(timeRange);
        }
    }
    
    /**
     * 刷新所有数据
     */
    public void refreshAllData() {
        loadAllData(currentTimeRange);
    }
    
    /**
     * 加载所有数据的统一入口
     */
    private void loadAllData(String timeRange) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        try {
            // 模拟网络延迟
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    loadKpiData(timeRange);
                    loadChartDataSeparately(timeRange);
                    loadTaskAnalysisDataSeparately(timeRange);
                    
                    // 向后兼容：更新组合数据
                    loadStatisticsData(timeRange);
                    loadChartData(timeRange);
                    loadTaskAnalysisData(timeRange);
                    
                    isLoading.setValue(false);
                } catch (Exception e) {
                    errorMessage.setValue("数据加载失败: " + e.getMessage());
                    isLoading.setValue(false);
                }
            }, 500); // 模拟500ms加载时间
            
        } catch (Exception e) {
            errorMessage.setValue("数据加载失败: " + e.getMessage());
            isLoading.setValue(false);
        }
    }
    
    public String getCurrentTimeRange() {
        return currentTimeRange;
    }
    
    /**
     * 根据时间范围加载统计数据（模拟数据）
     */
    private void loadStatisticsData(String timeRange) {
        StatisticsData data;
        Random random = new Random();
        
        switch (timeRange) {
            case "today":
                data = new StatisticsData(
                    5 + random.nextInt(15),  // 今日完成任务数 5-19
                    2 + random.nextInt(8),   // 今日番茄钟数 2-9
                    60 + random.nextFloat() * 40,  // 今日完成率 60%-100%
                    3.0f + random.nextFloat() * 2.0f  // 平均重要性 3.0-5.0
                );
                break;
                
            case "week":
                data = new StatisticsData(
                    35 + random.nextInt(50),  // 本周完成任务数 35-84
                    15 + random.nextInt(25),  // 本周番茄钟数 15-39
                    70 + random.nextFloat() * 30,  // 本周完成率 70%-100%
                    3.5f + random.nextFloat() * 1.5f  // 平均重要性 3.5-5.0
                );
                break;
                
            case "month":
                data = new StatisticsData(
                    120 + random.nextInt(180), // 本月完成任务数 120-299
                    60 + random.nextInt(100),  // 本月番茄钟数 60-159
                    75 + random.nextFloat() * 25,  // 本月完成率 75%-100%
                    3.8f + random.nextFloat() * 1.2f  // 平均重要性 3.8-5.0
                );
                break;
                
            case "custom":
                data = new StatisticsData(
                    80 + random.nextInt(120),  // 自定义范围完成任务数 80-199
                    40 + random.nextInt(80),   // 自定义范围番茄钟数 40-119
                    65 + random.nextFloat() * 35,  // 自定义范围完成率 65%-100%
                    3.2f + random.nextFloat() * 1.8f  // 平均重要性 3.2-5.0
                );
                break;
                
            default:
                data = new StatisticsData(12, 25, 85.0f, 4.2f);
                break;
        }
        
        statisticsData.setValue(data);
    }
    
    /**
     * 加载图表数据
     */
    private void loadChartData(String timeRange) {
        List<ChartData.CompletionTrend> completionTrends = generateCompletionTrendData(timeRange);
        List<ChartData.QuadrantDistribution> quadrantDistributions = generateQuadrantDistributionData();
        List<ChartData.PomodoroDistribution> pomodoroDistributions = generatePomodoroDistributionData();
        
        ChartData.ChartDataSet dataSet = new ChartData.ChartDataSet(
            completionTrends, quadrantDistributions, pomodoroDistributions);
        chartData.setValue(dataSet);
    }
    
    /**
     * 生成任务完成趋势数据
     */
    private List<ChartData.CompletionTrend> generateCompletionTrendData(String timeRange) {
        List<ChartData.CompletionTrend> trends = new ArrayList<>();
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // 生成最近7天的数据
        for (int i = 6; i >= 0; i--) {
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String date = dateFormat.format(calendar.getTime());
            int completedTasks = 3 + random.nextInt(12); // 3-14个任务
            trends.add(new ChartData.CompletionTrend(date, completedTasks));
            calendar = Calendar.getInstance(); // 重置日历
        }
        
        return trends;
    }
    
    /**
     * 生成四象限分布数据
     */
    private List<ChartData.QuadrantDistribution> generateQuadrantDistributionData() {
        List<ChartData.QuadrantDistribution> distributions = new ArrayList<>();
        Random random = new Random();
        
        distributions.add(new ChartData.QuadrantDistribution(
            "重要且紧急", 20 + random.nextInt(20), Color.parseColor("#F44336")));
        distributions.add(new ChartData.QuadrantDistribution(
            "重要不紧急", 25 + random.nextInt(25), Color.parseColor("#FF9800")));
        distributions.add(new ChartData.QuadrantDistribution(
            "紧急不重要", 15 + random.nextInt(20), Color.parseColor("#2196F3")));
        distributions.add(new ChartData.QuadrantDistribution(
            "不重要不紧急", 10 + random.nextInt(15), Color.parseColor("#9E9E9E")));
        
        return distributions;
    }
    
    /**
     * 生成番茄钟时间分布数据
     */
    private List<ChartData.PomodoroDistribution> generatePomodoroDistributionData() {
        List<ChartData.PomodoroDistribution> distributions = new ArrayList<>();
        Random random = new Random();
        
        distributions.add(new ChartData.PomodoroDistribution(
            "上午", 8 + random.nextInt(8), Color.parseColor("#4CAF50")));
        distributions.add(new ChartData.PomodoroDistribution(
            "下午", 10 + random.nextInt(10), Color.parseColor("#FF9800")));
        distributions.add(new ChartData.PomodoroDistribution(
            "晚上", 5 + random.nextInt(8), Color.parseColor("#9C27B0")));
        
        return distributions;
    }
    
    /**
     * 加载任务分析数据
     */
    private void loadTaskAnalysisData(String timeRange) {
        List<TaskAnalysisData.HighPriorityTask> highPriorityTasks = generateHighPriorityTasks();
        List<TaskAnalysisData.LongestDurationTask> longestDurationTasks = generateLongestDurationTasks();
        List<TaskAnalysisData.Suggestion> suggestions = generateSuggestions();
        
        TaskAnalysisData.TaskAnalysisDataSet dataSet = new TaskAnalysisData.TaskAnalysisDataSet(
            highPriorityTasks, longestDurationTasks, suggestions);
        taskAnalysisData.setValue(dataSet);
    }
    
    /**
     * 生成高优先级任务数据
     */
    private List<TaskAnalysisData.HighPriorityTask> generateHighPriorityTasks() {
        List<TaskAnalysisData.HighPriorityTask> tasks = new ArrayList<>();
        Random random = new Random();
        
        String[] taskNames = {
            "完成项目季度报告", "处理客户紧急问题", "准备明天的重要会议",
            "修复系统安全漏洞", "完成产品功能设计", "处理团队冲突问题"
        };
        
        for (int i = 0; i < Math.min(5, taskNames.length); i++) {
            int importance = 8 + random.nextInt(3); // 8-10
            int urgency = 8 + random.nextInt(3);    // 8-10
            boolean isCompleted = random.nextBoolean();
            
            tasks.add(new TaskAnalysisData.HighPriorityTask(
                taskNames[i], importance, urgency, isCompleted, "task_" + i));
        }
        
        return tasks;
    }
    
    /**
     * 生成耗时最长任务数据
     */
    private List<TaskAnalysisData.LongestDurationTask> generateLongestDurationTasks() {
        List<TaskAnalysisData.LongestDurationTask> tasks = new ArrayList<>();
        Random random = new Random();
        
        String[] taskNames = {
            "重构核心代码架构", "学习新技术栈", "撰写技术文档",
            "市场调研分析", "产品原型设计", "团队培训计划"
        };
        
        for (int i = 0; i < Math.min(4, taskNames.length); i++) {
            int duration = 3 + random.nextInt(12); // 3-14天
            boolean isCompleted = i < 2; // 前两个已完成
            
            tasks.add(new TaskAnalysisData.LongestDurationTask(
                taskNames[i], duration, isCompleted, "duration_task_" + i));
        }
        
        return tasks;
    }
    
    /**
     * 生成智能建议
     */
    private List<TaskAnalysisData.Suggestion> generateSuggestions() {
        List<TaskAnalysisData.Suggestion> suggestions = new ArrayList<>();
        
        String[] suggestionTexts = {
            "第二象限任务比例偏低，建议提前安排重要但不紧急的任务",
            "番茄钟主要集中在晚上，可尝试调整作息提高白天效率",
            "高优先级任务较多，建议合理分配时间避免过度压力",
            "任务完成周期较长，可考虑将大任务拆分为小任务",
            "本周任务完成率很高，继续保持良好的执行力"
        };
        
        String[] types = {"priority", "timing", "balance"};
        Random random = new Random();
        
        // 随机选择2-3条建议
        int suggestionsCount = 2 + random.nextInt(2);
        for (int i = 0; i < suggestionsCount; i++) {
            int textIndex = random.nextInt(suggestionTexts.length);
            int typeIndex = random.nextInt(types.length);
            
            suggestions.add(new TaskAnalysisData.Suggestion(
                suggestionTexts[textIndex], types[typeIndex]));
        }
        
        return suggestions;
    }
    
    /**
     * 加载KPI数据
     */
    private void loadKpiData(String timeRange) {
        StatisticsData data;
        if (useRealData && dataManager != null) {
            data = dataManager.getRealKpiData(timeRange);
        } else {
            data = generateKpiData(timeRange);
        }
        kpiData.setValue(data);
    }
    
    /**
     * 分别加载图表数据
     */
    private void loadChartDataSeparately(String timeRange) {
        if (useRealData && dataManager != null) {
            taskTrendData.setValue(dataManager.getRealTaskTrendData(timeRange));
            quadrantData.setValue(dataManager.getRealQuadrantData(timeRange));
            pomodoroData.setValue(dataManager.getRealPomodoroData(timeRange));
        } else {
            taskTrendData.setValue(generateCompletionTrendData(timeRange));
            quadrantData.setValue(generateQuadrantDistributionData());
            pomodoroData.setValue(generatePomodoroDistributionData());
        }
    }
    
    /**
     * 分别加载任务分析数据
     */
    private void loadTaskAnalysisDataSeparately(String timeRange) {
        if (useRealData && dataManager != null) {
            highPriorityTasks.setValue(dataManager.getRealHighPriorityTasks(timeRange));
            longestTasks.setValue(dataManager.getRealLongestTasks(timeRange));
            suggestions.setValue(dataManager.getRealSuggestions(timeRange));
        } else {
            highPriorityTasks.setValue(generateHighPriorityTasks());
            longestTasks.setValue(generateLongestDurationTasks());
            suggestions.setValue(generateSuggestions());
        }
    }
    
    /**
     * 切换数据模式
     */
    public void setUseRealData(boolean useRealData) {
        this.useRealData = useRealData;
        // 重新加载数据
        if (dataManager != null) {
            loadAllData(currentTimeRange);
        }
    }
    
    /**
     * 获取数据管理器（用于外部记录数据）
     */
    public StatisticsDataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * 生成KPI数据（增强版，支持不同时间范围）
     */
    private StatisticsData generateKpiData(String timeRange) {
        Random random = new Random();
        
        if (timeRange.startsWith("custom_")) {
            // 处理自定义时间范围
            return generateCustomRangeKpiData(timeRange, random);
        }
        
        switch (timeRange) {
            case "today":
                return new StatisticsData(
                    5 + random.nextInt(15),      // 今日完成任务数 5-19
                    2 + random.nextInt(8),       // 今日番茄钟数 2-9
                    60 + random.nextFloat() * 40, // 今日完成率 60%-100%
                    3.0f + random.nextFloat() * 2.0f  // 平均重要性 3.0-5.0
                );
                
            case "week":
                return new StatisticsData(
                    35 + random.nextInt(50),     // 本周完成任务数 35-84
                    15 + random.nextInt(25),     // 本周番茄钟数 15-39
                    70 + random.nextFloat() * 30, // 本周完成率 70%-100%
                    3.5f + random.nextFloat() * 1.5f  // 平均重要性 3.5-5.0
                );
                
            case "month":
                return new StatisticsData(
                    120 + random.nextInt(180),   // 本月完成任务数 120-299
                    60 + random.nextInt(100),    // 本月番茄钟数 60-159
                    75 + random.nextFloat() * 25, // 本月完成率 75%-100%
                    3.8f + random.nextFloat() * 1.2f  // 平均重要性 3.8-5.0
                );
                
            case "custom":
                return new StatisticsData(
                    80 + random.nextInt(120),    // 自定义范围完成任务数 80-199
                    40 + random.nextInt(80),     // 自定义范围番茄钟数 40-119
                    65 + random.nextFloat() * 35, // 自定义范围完成率 65%-100%
                    3.2f + random.nextFloat() * 1.8f  // 平均重要性 3.2-5.0
                );
                
            default:
                return new StatisticsData(12, 25, 85.0f, 4.2f);
        }
    }
    
    /**
     * 生成自定义时间范围的KPI数据
     */
    private StatisticsData generateCustomRangeKpiData(String timeRange, Random random) {
        try {
            // 解析时间范围：custom_2024-01-01_2024-01-07
            String[] parts = timeRange.split("_");
            if (parts.length >= 3) {
                String startDateStr = parts[1];
                String endDateStr = parts[2];
                
                // 计算天数差异，用于调整数据规模
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date startDate = sdf.parse(startDateStr);
                Date endDate = sdf.parse(endDateStr);
                
                if (startDate != null && endDate != null) {
                    long diffInMillis = endDate.getTime() - startDate.getTime();
                    int dayCount = (int) (diffInMillis / (24 * 60 * 60 * 1000)) + 1;
                    
                    // 根据天数调整数据规模
                    float dayFactor = dayCount / 7.0f; // 以一周为基准
                    
                    int baseTasks = Math.round(35 * dayFactor);
                    int basePomodoro = Math.round(15 * dayFactor);
                    
                    return new StatisticsData(
                        Math.max(1, baseTasks + random.nextInt(Math.max(1, (int)(20 * dayFactor)))),
                        Math.max(1, basePomodoro + random.nextInt(Math.max(1, (int)(10 * dayFactor)))),
                        65 + random.nextFloat() * 30, // 完成率 65%-95%
                        3.3f + random.nextFloat() * 1.7f  // 平均重要性 3.3-5.0
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("解析自定义时间范围失败: " + timeRange);
        }
        
        // 解析失败时返回默认值
        return new StatisticsData(
            20 + random.nextInt(60),
            10 + random.nextInt(30),
            70 + random.nextFloat() * 25,
            3.5f + random.nextFloat() * 1.5f
        );
    }
    
    /**
     * 刷新数据（向后兼容）
     */
    public void refreshData() {
        refreshAllData();
    }
}
