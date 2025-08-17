# AI功能路由系统说明

## 概述

本系统实现了一个基于注册表/工厂模式的本地功能路由架构，用于封装和管理所有可被AI调用的功能。通过统一的接口和动态路由机制，实现了功能的模块化管理和扩展。

## 系统架构

### 1. 核心组件

#### AiExecutable 接口
- **位置**: `com.fourquadrant.ai.AiExecutable`
- **作用**: 定义统一的功能执行接口
- **方法**:
  - `execute(Map<String, Object> args)`: 执行功能的核心方法
  - `getDescription()`: 获取功能描述
  - `validateArgs(Map<String, Object> args)`: 验证参数有效性

#### CommandRouter 路由器
- **位置**: `com.fourquadrant.ai.CommandRouter`
- **作用**: 功能注册表和执行器
- **特性**:
  - 静态注册表管理
  - 线程安全的初始化
  - 完整的错误处理
  - 参数验证机制
  - 执行结果封装

### 2. 功能实现类

#### StartPomodoro - 启动番茄钟
- **功能**: 启动番茄钟计时器，支持指定时长和关联任务
- **参数**: 
  - `task_name` (必需): 关联的任务名称，字符串类型，不能为空
  - `duration` (可选): 持续时间，默认25分钟，范围1-120分钟
  - `task_id` (可选): 关联的任务ID，字符串类型，可为null
- **验证**: 检查时长范围有效性、任务名称非空、任务ID格式正确

#### OpenStatistics - 打开统计页面
- **功能**: 打开应用统计页面
- **参数**:
  - `type` (可选): 统计类型，支持 general/daily/weekly/monthly
- **验证**: 检查统计类型有效性

#### ToggleDarkMode - 切换深色模式
- **功能**: 切换或设置深色模式
- **参数**:
  - `enable` (可选): 布尔值或字符串，指定开启/关闭状态
- **验证**: 检查参数格式有效性

## 使用方法

### 1. 初始化系统

```java
// 在Application或MainActivity中初始化
CommandRouter.initialize(context);
```

### 2. 执行功能

```java
// 创建参数映射（必须包含task_name）
Map<String, Object> args = new HashMap<>();
args.put("task_name", "完成项目报告");
args.put("duration", 30);
args.put("task_id", "task_001");

// 执行功能
CommandRouter.ExecutionResult result = CommandRouter.executeCommand("start_pomodoro", args);

// 检查执行结果
if (result.isSuccess()) {
    Log.i(TAG, "功能执行成功: " + result.getMessage());
} else {
    Log.e(TAG, "功能执行失败: " + result.getMessage());
}
```

### 3. 查询可用功能

```java
// 获取所有功能名称
Set<String> functions = CommandRouter.getRegisteredFunctions();

// 获取功能描述
String description = CommandRouter.getFunctionDescription("start_pomodoro");

// 检查功能是否存在
boolean exists = CommandRouter.hasFunction("start_pomodoro");

// 获取所有功能信息
Map<String, String> allInfo = CommandRouter.getAllFunctionInfo();
```

## 扩展指南

### 1. 添加新功能

#### 步骤1: 创建功能实现类

```java
package com.fourquadrant.ai.commands;

import com.fourquadrant.ai.AiExecutable;
import java.util.Map;

public class NewFeature implements AiExecutable {
    private Context context;
    
    public NewFeature(Context context) {
        this.context = context;
    }
    
    @Override
    public void execute(Map<String, Object> args) {
        try {
            // 实现具体功能逻辑
            // 1. 参数提取和验证
            // 2. 业务逻辑执行
            // 3. 结果处理
        } catch (Exception e) {
            Log.e(TAG, "NewFeature 执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getDescription() {
        return "新功能描述";
    }
    
    @Override
    public boolean validateArgs(Map<String, Object> args) {
        // 实现参数验证逻辑
        return true;
    }
}
```

#### 步骤2: 在CommandRouter中注册

```java
// 在CommandRouter.initialize()方法中添加
toolRegistry.put("new_feature", new NewFeature(context));
```

### 2. 动态注册功能

```java
// 运行时注册新功能
CommandRouter.registerFunction("dynamic_feature", new DynamicFeature(context));

// 注销功能
CommandRouter.unregisterFunction("dynamic_feature");
```

## 最佳实践

### 1. 参数处理

- 使用类型安全的参数提取
- 提供合理的默认值
- 进行参数范围验证
- 处理类型转换异常

```java
// 安全的参数提取示例
int duration = 25; // 默认值
if (args.containsKey("duration")) {
    Object durationObj = args.get("duration");
    if (durationObj instanceof Integer) {
        duration = (Integer) durationObj;
    } else if (durationObj instanceof String) {
        try {
            duration = Integer.parseInt((String) durationObj);
        } catch (NumberFormatException e) {
            Log.w(TAG, "无效的duration参数，使用默认值");
        }
    }
}
```

### 2. 错误处理

- 捕获所有可能的异常
- 提供有意义的错误信息
- 记录详细的日志
- 优雅降级处理

### 3. 日志记录

- 使用统一的TAG
- 记录关键操作
- 区分不同日志级别
- 避免敏感信息泄露

### 4. 性能考虑

- 避免在主线程执行耗时操作
- 合理使用缓存机制
- 及时释放资源
- 考虑内存使用优化

## 示例场景

### AI语音助手集成

```java
// 模拟AI语音命令处理
public void handleVoiceCommand(String voiceInput) {
    Map<String, Object> args = new HashMap<>();
    
    if (voiceInput.contains("开始25分钟番茄钟")) {
        args.put("duration", 25);
        CommandRouter.executeCommand("start_pomodoro", args);
    } else if (voiceInput.contains("打开周统计")) {
        args.put("type", "weekly");
        CommandRouter.executeCommand("open_statistics", args);
    } else if (voiceInput.contains("开启深色模式")) {
        args.put("enable", true);
        CommandRouter.executeCommand("toggle_dark_mode", args);
    }
}
```

### 快捷方式集成

```java
// 处理应用快捷方式
public void handleShortcut(String shortcutId, Bundle extras) {
    Map<String, Object> args = new HashMap<>();
    
    // 从Bundle转换参数
    for (String key : extras.keySet()) {
        args.put(key, extras.get(key));
    }
    
    CommandRouter.executeCommand(shortcutId, args);
}
```

## 注意事项

1. **线程安全**: CommandRouter的初始化是线程安全的，但功能实现类需要自行处理线程安全问题

2. **内存管理**: 功能实现类持有Context引用，注意避免内存泄露

3. **权限检查**: 功能实现时需要检查必要的Android权限

4. **版本兼容**: 考虑不同Android版本的API兼容性

5. **测试覆盖**: 为每个功能实现类编写单元测试

## 未来扩展方向

1. **异步执行支持**: 添加异步功能执行机制
2. **权限管理**: 集成功能级别的权限控制
3. **配置管理**: 支持功能的动态配置
4. **监控统计**: 添加功能使用统计和监控
5. **插件系统**: 支持第三方功能插件

---

通过这个AI功能路由系统，可以轻松地管理和扩展应用的AI功能，实现模块化的架构设计，提高代码的可维护性和扩展性。