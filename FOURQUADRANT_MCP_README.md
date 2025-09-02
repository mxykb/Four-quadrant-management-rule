# 🍅 四象限 MCP 服务器

基于四象限Android应用AI功能的MCP（Model Context Protocol）实现，提供番茄钟管理、任务管理、统计分析等AI功能的标准化接口。

## 📋 功能概览

### 🍅 番茄钟管理
- **启动番茄钟**: 开始专注工作时间，支持自定义时长和任务关联
- **控制番茄钟**: 暂停、恢复、停止、查询状态
- **休息管理**: 开始休息或跳过休息时间

### 📋 任务管理  
- **CRUD操作**: 创建、查看、更新、删除任务
- **四象限分类**: 基于重要性和紧急性的智能分类
- **任务状态**: 支持待办、已完成、已删除等状态管理

### 📊 统计分析
- **多维度统计**: 总体、日、周、月统计
- **番茄钟统计**: 专注时间分析和趋势
- **任务统计**: 四象限任务分布分析

### ⚙️ 设置管理
- **界面设置**: 深色模式切换
- **番茄钟设置**: 时长、休息时间配置
- **通知设置**: 提醒开关和声音设置

## 🚀 快速开始

### 1. 环境准备

**Python环境**:
```bash
# 确保Python 3.8+已安装
python --version

# 安装依赖
pip install -r requirements.txt
```

**Android端配置**:
1. 在Android应用中集成`AndroidHttpServer`类
2. 在MainActivity中启动HTTP服务器:
```java
AndroidHttpServer httpServer = new AndroidHttpServer(this);
new Thread(() -> {
    try {
        httpServer.startServer(8080);
        Log.i("MainActivity", "MCP HTTP服务器启动成功");
    } catch (IOException e) {
        Log.e("MainActivity", "MCP HTTP服务器启动失败", e);
    }
}).start();
```

### 2. 配置设置

**修改Android设备IP地址**:
编辑`config.json`或`fourquadrant-mcp-server.py`中的`ANDROID_CONFIG`:
```python
ANDROID_CONFIG = {
    "host": "你的Android设备IP",  # 例如: "192.168.1.100"
    "port": 8080,
    "timeout": 10
}
```

**网络权限**:
确保Android应用具有网络权限，在`AndroidManifest.xml`中添加:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 启动服务器

**直接运行**:
```bash
python fourquadrant-mcp-server.py
```

**通过MCP客户端运行**:
服务器已配置在您的MCP配置文件中，会自动启动。

## 🛠️ 工具使用指南

### 🍅 番茄钟功能

#### 启动番茄钟
```json
{
  "tool": "start_pomodoro",
  "arguments": {
    "task_name": "学习MCP协议",
    "duration": 25,
    "task_id": "task_001"
  }
}
```

#### 控制番茄钟
```json
{
  "tool": "control_pomodoro", 
  "arguments": {
    "action": "pause",
    "reason": "临时休息"
  }
}
```
支持的操作: `pause`, `resume`, `stop`, `status`

#### 管理休息
```json
{
  "tool": "manage_break",
  "arguments": {
    "action": "start"
  }
}
```
支持的操作: `start`, `skip`

### 📋 任务管理

#### 创建任务
```json
{
  "tool": "manage_tasks",
  "arguments": {
    "action": "create",
    "task_data": {
      "name": "完成项目文档",
      "description": "编写技术文档和用户手册",
      "importance": 4,
      "urgency": 3,
      "due_date": "2024-02-01"
    }
  }
}
```

#### 查看任务列表
```json
{
  "tool": "manage_tasks",
  "arguments": {
    "action": "list",
    "task_data": {
      "status": "pending",
      "quadrant": 1
    }
  }
}
```

#### 更新任务
```json
{
  "tool": "manage_tasks", 
  "arguments": {
    "action": "update",
    "task_id": "task_001",
    "task_data": {
      "status": "completed"
    }
  }
}
```

### 📊 统计功能

#### 获取周统计
```json
{
  "tool": "get_statistics",
  "arguments": {
    "type": "weekly",
    "period": "2024-01-15",
    "filters": {
      "quadrant": 1
    }
  }
}
```

#### 获取番茄钟统计
```json
{
  "tool": "get_statistics",
  "arguments": {
    "type": "pomodoro",
    "period": "2024-01"
  }
}
```

### ⚙️ 设置管理

#### 更新设置
```json
{
  "tool": "update_settings",
  "arguments": {
    "dark_mode": true,
    "tomato_duration": 30,
    "break_duration": 5,
    "notification_enabled": true,
    "auto_start_break": false,
    "sound_enabled": true
  }
}
```

### 🔍 状态检查

#### 检查Android连接
```json
{
  "tool": "check_android_status",
  "arguments": {}
}
```

## 🏗️ 系统架构

```
┌─────────────────┐    HTTP/JSON    ┌─────────────────┐
│   MCP客户端      │ ◄─────────────► │  MCP服务器       │
│ (Cursor/Claude) │                 │ (Python)        │
└─────────────────┘                 └─────────────────┘
                                            │
                                            │ HTTP API
                                            ▼
                                    ┌─────────────────┐
                                    │ Android HTTP     │
                                    │ 服务器           │
                                    └─────────────────┘
                                            │
                                            │ 内部调用
                                            ▼
                                    ┌─────────────────┐
                                    │ CommandRouter   │
                                    │ + AI功能模块     │
                                    └─────────────────┘
```

## 📱 Android集成指南

### 1. 添加HTTP服务器类

将`android-http-server.java`文件中的`AndroidHttpServer`类添加到您的Android项目中:

```java
// 建议路径: app/src/main/java/com/example/fourquadrant/server/
```

### 2. 在MainActivity中启动服务器

```java
public class MainActivity extends AppCompatActivity {
    private AndroidHttpServer httpServer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 启动MCP HTTP服务器
        startMcpServer();
    }
    
    private void startMcpServer() {
        httpServer = new AndroidHttpServer(this);
        
        new Thread(() -> {
            try {
                httpServer.startServer(8080);
                runOnUiThread(() -> {
                    // 可以显示服务器启动成功的提示
                    Toast.makeText(this, "MCP服务器已启动", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                Log.e("MainActivity", "MCP服务器启动失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "MCP服务器启动失败: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpServer != null) {
            httpServer.stopServer();
        }
    }
}
```

### 3. 添加网络权限

在`AndroidManifest.xml`中添加必要权限:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- MCP服务器所需权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <application>
        <!-- 允许明文HTTP流量 (开发环境) -->
        <meta-data
            android:name="android.app.meta-data.network_security_config"
            android:resource="@xml/network_security_config" />
        
        <!-- 您的其他配置 -->
    </application>
</manifest>
```

### 4. 网络安全配置

创建`app/src/main/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">192.168.1.0/24</domain>
        <domain includeSubdomains="true">10.0.0.0/8</domain>
    </domain-config>
</network-security-config>
```

## 🔧 故障排除

### 常见问题

1. **连接失败**: 
   - 检查Android设备IP地址是否正确
   - 确认Android和电脑在同一网络
   - 检查防火墙设置

2. **权限错误**:
   - 确认Android应用已授予网络权限
   - 检查CommandRouter权限设置

3. **功能不可用**:
   - 确认相关AI功能模块已启用
   - 检查Android端CommandRouter注册状态

### 调试技巧

**Python端调试**:
```bash
# 启用详细日志
export PYTHONPATH="."
python -u fourquadrant-mcp-server.py
```

**Android端调试**:
```bash
# 查看日志
adb logcat | grep -E "(AndroidHttpServer|CommandRouter|MCP)"
```

**网络连通性测试**:
```bash
# 测试Android HTTP服务器
curl http://ANDROID_IP:8080/api/health

# 检查端口占用
netstat -an | grep 8080
```

## 📋 API参考

详细的API文档请参考项目中的`四象限AI功能分析与MCP实现方案.md`文档的"API参考文档"部分。

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 支持

- **技术支持**: support@fourquadrant.com
- **开发者社区**: https://github.com/fourquadrant/mcp-server
- **文档更新**: docs@fourquadrant.com

---

**项目信息**:
- **版本**: v1.0.0
- **最后更新**: 2024年
- **维护者**: 四象限开发团队
