package com.example.fourquadrant;

/**
 * 统计页面数据绑定辅助类
 * 用于演示ViewModel与UI组件的数据绑定关系
 */
public class StatisticsDataBinding {
    
    /**
     * 数据绑定映射说明
     */
    public static class DataBindingMap {
        // KPI数据绑定
        public static final String KPI_COMPLETED_TASKS = "kpiData.completedTasksToday -> tvCompletedTasksCount";
        public static final String KPI_POMODORO_COUNT = "kpiData.pomodoroCountWeek -> tvPomodoroCount";
        public static final String KPI_COMPLETION_RATE = "kpiData.completionRateWeek -> tvCompletionRate, progressCompletionRate";
        public static final String KPI_AVG_IMPORTANCE = "kpiData.avgImportanceScore -> tvAvgImportance";
        
        // 图表数据绑定
        public static final String TASK_TREND_CHART = "taskTrendData -> lineChartCompletionTrend";
        public static final String QUADRANT_CHART = "quadrantData -> pieChartQuadrantDistribution";
        public static final String POMODORO_CHART = "pomodoroData -> barChartPomodoroDistribution";
        
        // 列表数据绑定
        public static final String HIGH_PRIORITY_TASKS = "highPriorityTasks -> rvHighPriorityTasks";
        public static final String LONGEST_TASKS = "longestTasks -> rvLongestDurationTasks";
        public static final String SUGGESTIONS = "suggestions -> llSuggestionsContainer";
        
        // 状态数据绑定
        public static final String LOADING_STATE = "isLoading -> swipeRefreshLayout.refreshing";
        public static final String ERROR_MESSAGE = "errorMessage -> Toast显示";
    }
    
    /**
     * 时间范围对应的数据差异
     */
    public static class TimeRangeDataDifference {
        public static final String TODAY_RANGE = "今日: 5-19任务, 2-9番茄钟, 60-100%完成率";
        public static final String WEEK_RANGE = "本周: 35-84任务, 15-39番茄钟, 70-100%完成率";
        public static final String MONTH_RANGE = "本月: 120-299任务, 60-159番茄钟, 75-100%完成率";
        public static final String CUSTOM_RANGE = "自定义: 80-199任务, 40-119番茄钟, 65-100%完成率";
    }
    
    /**
     * 数据刷新流程
     */
    public static class RefreshFlow {
        public static final String STEP_1 = "用户触发刷新 -> setTimeRange() 或 refreshAllData()";
        public static final String STEP_2 = "ViewModel设置loading状态 -> UI显示加载动画";
        public static final String STEP_3 = "模拟网络延迟500ms -> 生成新的模拟数据";
        public static final String STEP_4 = "更新所有LiveData -> 触发UI自动更新";
        public static final String STEP_5 = "loading状态结束 -> UI隐藏加载动画";
    }
    
    /**
     * UI更新方法映射
     */
    public static class UIUpdateMethods {
        public static final String KPI_UPDATE = "updateKpiUI(StatisticsData)";
        public static final String LINE_CHART_UPDATE = "updateLineChart(List<CompletionTrend>)";
        public static final String PIE_CHART_UPDATE = "updatePieChart(List<QuadrantDistribution>)";
        public static final String BAR_CHART_UPDATE = "updateBarChart(List<PomodoroDistribution>)";
        public static final String HIGH_PRIORITY_UPDATE = "updateHighPriorityTasks(List<HighPriorityTask>)";
        public static final String LONGEST_TASKS_UPDATE = "updateLongestDurationTasks(List<LongestDurationTask>)";
        public static final String SUGGESTIONS_UPDATE = "updateSuggestions(List<Suggestion>)";
    }
}
