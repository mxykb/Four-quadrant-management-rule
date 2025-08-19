# AI功能实现指南

基于TaskAiActivity和PomodoroAiActivity的成功实现经验，本文档提供了为其他功能创建AI助手的完整指南和提示词模板。

## 一、架构概述

每个AI功能模块包含以下核心组件：

1. **Control类** - 核心业务逻辑控制器
2. **PermissionManager** - 功能权限管理器
3. **AiActivity** - AI助手界面
4. **Function模型** - 功能数据模型
5. **FunctionAdapter** - RecyclerView适配器
6. **布局文件** - XML界面布局
7. **路由注册** - CommandRouter集成
8. **界面跳转** - AiToolsFragment配置

## 二、通用提示词模板

### 基础模板
```
请为[功能名称]创建AI助手功能，完全参考TaskAiActivity和PomodoroAiActivity的设计模式和代码结构，实现以下完整组件：

1. **核心控制类**：
   - 创建[功能名称]Control类，继承BaseControl
   - 实现以下功能：[具体功能列表]
   - 添加@Override注解和适当的参数验证

2. **权限管理器**：
   - 创建[功能名称]FunctionPermissionManager
   - 参考PomodoroFunctionPermissionManager的实现模式
   - 定义功能常量和权限控制逻辑

3. **AI界面**：
   - 创建[功能名称]AiActivity，完全参考PomodoroAiActivity的布局和逻辑
   - 包含：返回按钮、功能列表RecyclerView、操作选择Spinner、执行按钮、结果显示
   - 确保所有控件类型与布局文件匹配

4. **数据模型和适配器**：
   - 创建[功能名称]Function模型类
   - 创建[功能名称]FunctionAdapter适配器
   - 创建item_[功能名称]_function.xml布局

5. **路由和集成**：
   - 在CommandRouter中注册所有功能
   - 在AiToolsFragment中配置正确的targetActivity
   - 在AndroidManifest.xml中注册Activity

请确保：
- 所有类名、包名、资源ID保持一致性
- 控件类型与布局文件完全匹配
- 权限管理功能完整可用
- 界面跳转配置正确
```

## 三、具体功能示例

### 3.1 统计AI功能

**完整提示词：**
```
请为统计功能创建AI助手，完全参考TaskAiActivity和PomodoroAiActivity的设计，实现：

1. **StatisticsControl类**：
   - 继承BaseControl
   - 实现功能：数据查询、报表生成、趋势分析、数据导出、统计概览
   - 方法签名参考PomodoroControl的模式

2. **StatisticsFunctionPermissionManager**：
   - 定义常量：QUERY_DATA_STATISTICS、GENERATE_REPORT_STATISTICS、ANALYZE_TREND_STATISTICS、EXPORT_DATA_STATISTICS、OVERVIEW_STATISTICS
   - 实现权限控制和状态管理

3. **StatisticsAiActivity**：
   - 布局文件：activity_statistics_ai.xml
   - 包含统计功能列表和操作选择器
   - 操作类型：查询数据、生成报表、趋势分析、导出数据、统计概览

4. **数据模型**：
   - StatisticsFunction类
   - StatisticsFunctionAdapter适配器
   - item_statistics_function.xml布局

5. **集成配置**：
   - CommandRouter注册
   - AiToolsFragment中添加统计AI按钮配置
   - AndroidManifest.xml注册

确保所有组件命名一致，控件类型匹配，功能完整可用。
```

### 3.2 设置AI功能

**完整提示词：**
```
请为设置功能创建AI助手，参考现有AI模式，实现：

1. **SettingsControl类**：
   - 继承BaseControl
   - 实现功能：主题切换、通知管理、数据备份、偏好设置、系统配置
   - 添加适当的参数验证和错误处理

2. **SettingsFunctionPermissionManager**：
   - 定义常量：THEME_SWITCH_SETTINGS、NOTIFICATION_MANAGE_SETTINGS、DATA_BACKUP_SETTINGS、PREFERENCE_SETTINGS、SYSTEM_CONFIG_SETTINGS
   - 实现完整的权限控制机制

3. **SettingsAiActivity**：
   - 布局文件：activity_settings_ai.xml
   - 操作类型：切换主题、管理通知、备份数据、设置偏好、系统配置
   - 确保ImageView类型的返回按钮

4. **完整组件**：
   - SettingsFunction模型
   - SettingsFunctionAdapter适配器
   - item_settings_function.xml布局

5. **系统集成**：
   - CommandRouter功能注册
   - AiToolsFragment跳转配置
   - 清单文件Activity注册

重点确保控件类型匹配，避免ClassCastException错误。
```

### 3.3 提醒AI功能

**完整提示词：**
```
请为提醒功能创建AI助手，完全按照现有AI架构实现：

1. **ReminderControl类**：
   - 继承BaseControl
   - 实现功能：创建提醒、修改提醒、删除提醒、查询提醒、批量管理
   - 参考PomodoroControl的方法签名模式

2. **ReminderFunctionPermissionManager**：
   - 定义常量：CREATE_REMINDER、MODIFY_REMINDER、DELETE_REMINDER、QUERY_REMINDER、BATCH_MANAGE_REMINDER
   - 实现权限状态管理

3. **ReminderAiActivity**：
   - 布局文件：activity_reminder_ai.xml
   - 操作选择：创建提醒、修改提醒、删除提醒、查询提醒、批量管理
   - 包含任务名称和时间输入框

4. **数据组件**：
   - ReminderFunction模型类
   - ReminderFunctionAdapter适配器
   - item_reminder_function.xml布局文件

5. **系统配置**：
   - CommandRouter中注册所有提醒功能
   - AiToolsFragment中配置提醒AI按钮
   - AndroidManifest.xml中注册Activity

确保遵循现有的命名规范和代码结构。
```

## 四、实现检查清单

### 4.1 代码结构检查
- [ ] Control类继承BaseControl并实现所有必要方法
- [ ] PermissionManager定义了所有功能常量
- [ ] AiActivity包含完整的UI组件和事件处理
- [ ] Function模型类包含必要的属性
- [ ] FunctionAdapter正确处理ViewHolder和数据绑定

### 4.2 布局文件检查
- [ ] activity_[功能名称]_ai.xml布局完整
- [ ] item_[功能名称]_function.xml适配器布局
- [ ] 所有控件ID命名一致
- [ ] 控件类型与Java代码声明匹配

### 4.3 集成配置检查
- [ ] CommandRouter中注册所有功能方法
- [ ] AiToolsFragment中配置targetActivity
- [ ] AndroidManifest.xml中注册新Activity
- [ ] 包名和类名引用正确

### 4.4 常见错误预防
- [ ] 避免ImageButton/ImageView类型不匹配
- [ ] 确保所有资源文件存在
- [ ] 检查权限常量命名规范
- [ ] 验证方法签名和注解正确性

## 五、扩展建议

### 5.1 功能扩展
- 可以根据具体需求添加更多操作类型
- 支持复杂参数输入（日期选择、文件选择等）
- 添加结果展示的图表或列表

### 5.2 UI优化
- 参考Material Design规范
- 添加加载状态和错误提示
- 支持深色模式适配

### 5.3 性能优化
- 实现懒加载和数据缓存
- 优化RecyclerView性能
- 添加异步处理机制

## 六、故障排除

### 6.1 常见编译错误
1. **ClassCastException**: 检查控件类型声明与布局文件是否匹配
2. **资源未找到**: 确认所有drawable和layout文件存在
3. **方法签名错误**: 检查@Override注解和参数类型
4. **权限常量错误**: 确认常量命名符合规范

### 6.2 运行时错误
1. **Activity启动失败**: 检查AndroidManifest.xml注册
2. **界面跳转失败**: 确认AiToolsFragment中targetActivity配置
3. **功能执行失败**: 检查CommandRouter注册和方法实现

### 6.3 调试建议
- 使用Logcat查看详细错误信息
- 逐步测试每个组件的功能
- 参考已有的TaskAi和PomodoroAi实现

---

**注意**: 本指南基于FourQuadrant项目的实际实现经验，建议在使用时根据具体项目需求进行适当调整。