#!/usr/bin/env python3
"""
四象限 MCP 服务器
基于四象限Android应用AI功能的MCP（Model Context Protocol）实现
提供番茄钟管理、任务管理、统计分析等AI功能的标准化接口
"""

import asyncio
import json
import logging
import sys
from typing import Any, Dict, List, Optional
from datetime import datetime, timedelta

import aiohttp
from mcp.server import Server
from mcp.server.models import InitializationOptions
from mcp.server.stdio import stdio_server
from mcp.types import (
    CallToolRequest,
    CallToolResult,
    ListToolsRequest,
    ListToolsResult,
    Tool,
    TextContent,
    ImageContent,
    EmbeddedResource,
)
from pydantic import BaseModel, Field

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# MCP服务器实例
server = Server("fourquadrant-mcp")

# Android设备配置
ANDROID_CONFIG = {
    "host": "192.168.1.100",  # Android设备IP地址
    "port": 8080,             # Android HTTP服务器端口
    "timeout": 10             # 超时时间（秒）
}

class AndroidBridge:
    """Android应用通信桥接器"""
    
    def __init__(self, host: str = None, port: int = None):
        self.host = host or ANDROID_CONFIG["host"]
        self.port = port or ANDROID_CONFIG["port"]
        self.base_url = f"http://{self.host}:{self.port}"
        self.timeout = ANDROID_CONFIG["timeout"]
        
    async def call_android_api(self, endpoint: str, method: str = "POST", data: Dict = None):
        """调用Android应用API"""
        url = f"{self.base_url}{endpoint}"
        
        async with aiohttp.ClientSession() as session:
            try:
                if method == "GET":
                    async with session.get(url, params=data, timeout=self.timeout) as response:
                        result = await response.json()
                else:
                    async with session.request(method, url, json=data, timeout=self.timeout) as response:
                        result = await response.json()
                        
                logger.info(f"Android API调用成功: {method} {endpoint}")
                return result
                
            except asyncio.TimeoutError:
                logger.error(f"Android API调用超时: {method} {endpoint}")
                raise Exception("Android应用响应超时")
            except Exception as e:
                logger.error(f"Android API调用失败: {method} {endpoint}, 错误: {str(e)}")
                raise Exception(f"Android应用通信失败: {str(e)}")

# 全局Android桥接器实例
android_bridge = AndroidBridge()

# 数据模型
class PomodoroArgs(BaseModel):
    task_name: str = Field(..., description="关联的任务名称")
    duration: Optional[int] = Field(25, description="持续时间（分钟）")
    task_id: Optional[str] = Field(None, description="任务ID")

class ControlArgs(BaseModel):
    action: str = Field(..., description="控制操作类型：pause|resume|stop|status")
    reason: Optional[str] = Field(None, description="操作原因")

class BreakArgs(BaseModel):
    action: str = Field(..., description="休息操作类型：start|skip")

class TaskData(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    importance: Optional[int] = Field(None, ge=1, le=4)
    urgency: Optional[int] = Field(None, ge=1, le=4)
    due_date: Optional[str] = None
    status: Optional[str] = None

class TaskArgs(BaseModel):
    action: str = Field(..., description="任务操作类型：create|update|delete|list|complete")
    task_data: Optional[TaskData] = None
    task_id: Optional[str] = None

class StatisticsArgs(BaseModel):
    type: str = Field(..., description="统计类型：general|daily|weekly|monthly|pomodoro|tasks")
    period: Optional[str] = None
    filters: Optional[Dict] = None

class SettingsArgs(BaseModel):
    dark_mode: Optional[bool] = None
    tomato_duration: Optional[int] = Field(None, ge=1, le=120)
    break_duration: Optional[int] = Field(None, ge=1, le=60)
    notification_enabled: Optional[bool] = None
    auto_start_break: Optional[bool] = None
    sound_enabled: Optional[bool] = None

@server.list_tools()
async def handle_list_tools() -> ListToolsResult:
    """返回所有可用的工具列表"""
    return ListToolsResult(
        tools=[
            Tool(
                name="start_pomodoro",
                description="启动番茄钟计时器，开始专注工作时间",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "task_name": {
                            "type": "string",
                            "description": "关联的任务名称，用于记录和统计"
                        },
                        "duration": {
                            "type": "number",
                            "description": "持续时间（分钟），默认25分钟",
                            "minimum": 1,
                            "maximum": 120,
                            "default": 25
                        },
                        "task_id": {
                            "type": "string",
                            "description": "任务ID（可选），用于关联具体任务"
                        }
                    },
                    "required": ["task_name"]
                }
            ),
            Tool(
                name="control_pomodoro",
                description="控制番茄钟状态（暂停/恢复/停止/查询状态）",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "action": {
                            "type": "string",
                            "enum": ["pause", "resume", "stop", "status"],
                            "description": "控制操作类型"
                        },
                        "reason": {
                            "type": "string",
                            "description": "操作原因（可选），用于日志记录"
                        }
                    },
                    "required": ["action"]
                }
            ),
            Tool(
                name="manage_break",
                description="管理番茄钟休息时间",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "action": {
                            "type": "string",
                            "enum": ["start", "skip"],
                            "description": "休息操作：开始休息或跳过休息"
                        }
                    },
                    "required": ["action"]
                }
            ),
            Tool(
                name="manage_tasks",
                description="管理四象限任务，支持CRUD操作",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "action": {
                            "type": "string",
                            "enum": ["create", "update", "delete", "list", "complete"],
                            "description": "任务操作类型"
                        },
                        "task_data": {
                            "type": "object",
                            "description": "任务数据，根据action不同而变化",
                            "properties": {
                                "name": {"type": "string", "description": "任务名称"},
                                "description": {"type": "string", "description": "任务描述"},
                                "importance": {"type": "number", "minimum": 1, "maximum": 4, "description": "重要性级别（1-4）"},
                                "urgency": {"type": "number", "minimum": 1, "maximum": 4, "description": "紧急性级别（1-4）"},
                                "due_date": {"type": "string", "format": "date", "description": "截止日期"},
                                "status": {"type": "string", "enum": ["pending", "completed", "deleted"], "description": "任务状态"}
                            }
                        },
                        "task_id": {
                            "type": "string",
                            "description": "任务ID（update/delete操作必需）"
                        }
                    },
                    "required": ["action"]
                }
            ),
            Tool(
                name="get_statistics",
                description="获取统计数据和分析报告",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "type": {
                            "type": "string",
                            "enum": ["general", "daily", "weekly", "monthly", "pomodoro", "tasks"],
                            "description": "统计类型"
                        },
                        "period": {
                            "type": "string",
                            "description": "时间段，如'2024-01-01'或'2024-01'"
                        },
                        "filters": {
                            "type": "object",
                            "description": "过滤条件",
                            "properties": {
                                "quadrant": {"type": "number", "minimum": 1, "maximum": 4, "description": "象限过滤（1-4）"},
                                "status": {"type": "string", "description": "状态过滤"}
                            }
                        }
                    },
                    "required": ["type"]
                }
            ),
            Tool(
                name="update_settings",
                description="更新系统设置",
                inputSchema={
                    "type": "object",
                    "properties": {
                        "dark_mode": {
                            "type": "boolean",
                            "description": "是否启用深色模式"
                        },
                        "tomato_duration": {
                            "type": "number",
                            "description": "番茄钟时长（分钟）",
                            "minimum": 1,
                            "maximum": 120
                        },
                        "break_duration": {
                            "type": "number",
                            "description": "休息时长（分钟）",
                            "minimum": 1,
                            "maximum": 60
                        },
                        "notification_enabled": {
                            "type": "boolean",
                            "description": "是否启用通知"
                        },
                        "auto_start_break": {
                            "type": "boolean",
                            "description": "是否自动开始休息"
                        },
                        "sound_enabled": {
                            "type": "boolean",
                            "description": "是否启用声音提醒"
                        }
                    }
                }
            ),
            Tool(
                name="check_android_status",
                description="检查Android应用连接状态",
                inputSchema={
                    "type": "object",
                    "properties": {}
                }
            )
        ]
    )

@server.call_tool()
async def handle_call_tool(name: str, arguments: dict) -> CallToolResult:
    """处理工具调用请求"""
    try:
        logger.info(f"处理工具调用: {name}, 参数: {arguments}")
        
        if name == "start_pomodoro":
            return await start_pomodoro_tool(arguments)
        elif name == "control_pomodoro":
            return await control_pomodoro_tool(arguments)
        elif name == "manage_break":
            return await manage_break_tool(arguments)
        elif name == "manage_tasks":
            return await manage_tasks_tool(arguments)
        elif name == "get_statistics":
            return await get_statistics_tool(arguments)
        elif name == "update_settings":
            return await update_settings_tool(arguments)
        elif name == "check_android_status":
            return await check_android_status_tool(arguments)
        else:
            raise ValueError(f"未知的工具: {name}")
            
    except Exception as e:
        logger.error(f"工具调用失败 {name}: {str(e)}")
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 工具调用失败: {str(e)}"
                )
            ]
        )

async def start_pomodoro_tool(arguments: dict) -> CallToolResult:
    """启动番茄钟工具"""
    try:
        args = PomodoroArgs(**arguments)
        
        android_data = {
            "command": "start_pomodoro",
            "args": {
                "task_name": args.task_name,
                "duration": args.duration,
                "task_id": args.task_id
            }
        }
        
        result = await android_bridge.call_android_api("/api/command/execute", "POST", android_data)
        
        response_text = f"""🍅 番茄钟启动成功！

📝 任务名称: {args.task_name}
⏰ 时长: {args.duration} 分钟
🆔 任务ID: {args.task_id or '无'}
📱 Android响应: {result.get('message', '执行成功')}
🕐 开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

专注工作，保持高效！"""

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 启动番茄钟失败: {str(e)}"
                )
            ]
        )

async def control_pomodoro_tool(arguments: dict) -> CallToolResult:
    """控制番茄钟工具"""
    try:
        args = ControlArgs(**arguments)
        
        android_data = {
            "command": f"{args.action}_pomodoro",
            "args": {"reason": args.reason} if args.reason else {}
        }
        
        result = await android_bridge.call_android_api("/api/command/execute", "POST", android_data)
        
        action_map = {
            "pause": "⏸️ 暂停",
            "resume": "▶️ 恢复",
            "stop": "⏹️ 停止",
            "status": "📊 状态查询"
        }
        
        response_text = f"""🍅 番茄钟{action_map.get(args.action, args.action)}

🎯 操作: {args.action}
💭 原因: {args.reason or '无'}
📱 Android响应: {result.get('message', '执行成功')}
🕐 操作时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"""

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 番茄钟控制失败: {str(e)}"
                )
            ]
        )

async def manage_break_tool(arguments: dict) -> CallToolResult:
    """管理休息时间工具"""
    try:
        args = BreakArgs(**arguments)
        
        android_data = {
            "command": f"{args.action}_break",
            "args": {}
        }
        
        result = await android_bridge.call_android_api("/api/command/execute", "POST", android_data)
        
        action_map = {
            "start": "🛌 开始休息",
            "skip": "⏭️ 跳过休息"
        }
        
        response_text = f"""☕ 休息管理

🎯 操作: {action_map.get(args.action, args.action)}
📱 Android响应: {result.get('message', '执行成功')}
🕐 操作时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

{'好好休息，为下一个番茄钟做准备！' if args.action == 'start' else '继续加油，保持专注！'}"""

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 休息管理失败: {str(e)}"
                )
            ]
        )

async def manage_tasks_tool(arguments: dict) -> CallToolResult:
    """管理任务工具"""
    try:
        args = TaskArgs(**arguments)
        
        android_data = {
            "command": "task_management",
            "args": {
                "action": args.action,
                "task_id": args.task_id,
                **(args.task_data.dict(exclude_unset=True) if args.task_data else {})
            }
        }
        
        result = await android_bridge.call_android_api("/api/command/execute", "POST", android_data)
        
        action_map = {
            "create": "➕ 创建任务",
            "update": "✏️ 更新任务",
            "delete": "🗑️ 删除任务",
            "list": "📋 查看任务",
            "complete": "✅ 完成任务"
        }
        
        # 判断任务属于哪个象限
        quadrant_text = ""
        if args.task_data and args.task_data.importance and args.task_data.urgency:
            importance = args.task_data.importance
            urgency = args.task_data.urgency
            
            if importance >= 3 and urgency >= 3:
                quadrant_text = "\n📍 象限: 第一象限（重要且紧急）"
            elif importance >= 3 and urgency < 3:
                quadrant_text = "\n📍 象限: 第二象限（重要不紧急）"
            elif importance < 3 and urgency >= 3:
                quadrant_text = "\n📍 象限: 第三象限（不重要但紧急）"
            else:
                quadrant_text = "\n📍 象限: 第四象限（不重要不紧急）"
        
        response_text = f"""📋 任务管理

🎯 操作: {action_map.get(args.action, args.action)}
🆔 任务ID: {args.task_id or '无'}"""

        if args.task_data:
            if args.task_data.name:
                response_text += f"\n📝 任务名称: {args.task_data.name}"
            if args.task_data.description:
                response_text += f"\n📄 描述: {args.task_data.description}"
            if args.task_data.importance:
                response_text += f"\n⭐ 重要性: {args.task_data.importance}/4"
            if args.task_data.urgency:
                response_text += f"\n🔥 紧急性: {args.task_data.urgency}/4"
            if args.task_data.due_date:
                response_text += f"\n📅 截止日期: {args.task_data.due_date}"
            if args.task_data.status:
                response_text += f"\n📊 状态: {args.task_data.status}"
        
        response_text += quadrant_text
        response_text += f"\n📱 Android响应: {result.get('message', '执行成功')}"
        response_text += f"\n🕐 操作时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 任务管理失败: {str(e)}"
                )
            ]
        )

async def get_statistics_tool(arguments: dict) -> CallToolResult:
    """获取统计数据工具"""
    try:
        args = StatisticsArgs(**arguments)
        
        android_data = {
            "command": "open_statistics",
            "args": {
                "type": args.type,
                "period": args.period,
                "filters": args.filters
            }
        }
        
        result = await android_bridge.call_android_api("/api/command/execute", "POST", android_data)
        
        type_map = {
            "general": "📊 总体统计",
            "daily": "📅 日统计",
            "weekly": "📆 周统计",
            "monthly": "🗓️ 月统计",
            "pomodoro": "🍅 番茄钟统计",
            "tasks": "📋 任务统计"
        }
        
        response_text = f"""📊 统计数据

📈 类型: {type_map.get(args.type, args.type)}
📅 时间段: {args.period or '默认'}"""

        if args.filters:
            response_text += f"\n🔍 过滤条件: {json.dumps(args.filters, ensure_ascii=False, indent=2)}"
        
        response_text += f"\n📱 Android响应: {result.get('message', '执行成功')}"
        response_text += f"\n🕐 查询时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
        
        # 如果Android返回了具体的统计数据，显示它们
        if isinstance(result, dict) and 'data' in result:
            response_text += f"\n\n📊 统计结果:\n{json.dumps(result['data'], ensure_ascii=False, indent=2)}"

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 获取统计数据失败: {str(e)}"
                )
            ]
        )

async def update_settings_tool(arguments: dict) -> CallToolResult:
    """更新设置工具"""
    try:
        args = SettingsArgs(**arguments)
        
        # 处理深色模式切换
        results = []
        if args.dark_mode is not None:
            dark_mode_data = {
                "command": "toggle_dark_mode",
                "args": {"enable": args.dark_mode}
            }
            dark_result = await android_bridge.call_android_api("/api/command/execute", "POST", dark_mode_data)
            results.append(f"深色模式: {'启用' if args.dark_mode else '禁用'}")
        
        # 处理其他设置
        other_settings = args.dict(exclude_unset=True, exclude={"dark_mode"})
        if other_settings:
            settings_data = {
                "command": "set_pomodoro_settings",
                "args": other_settings
            }
            settings_result = await android_bridge.call_android_api("/api/command/execute", "POST", settings_data)
            results.append("其他设置已更新")
        
        response_text = f"""⚙️ 设置更新

🎯 更新内容:"""

        if args.dark_mode is not None:
            response_text += f"\n🌙 深色模式: {'启用' if args.dark_mode else '禁用'}"
        if args.tomato_duration:
            response_text += f"\n🍅 番茄钟时长: {args.tomato_duration} 分钟"
        if args.break_duration:
            response_text += f"\n☕ 休息时长: {args.break_duration} 分钟"
        if args.notification_enabled is not None:
            response_text += f"\n🔔 通知: {'启用' if args.notification_enabled else '禁用'}"
        if args.auto_start_break is not None:
            response_text += f"\n🔄 自动开始休息: {'启用' if args.auto_start_break else '禁用'}"
        if args.sound_enabled is not None:
            response_text += f"\n🔊 声音提醒: {'启用' if args.sound_enabled else '禁用'}"
        
        response_text += f"\n📱 Android响应: {', '.join(results)}"
        response_text += f"\n🕐 更新时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"❌ 设置更新失败: {str(e)}"
                )
            ]
        )

async def check_android_status_tool(arguments: dict) -> CallToolResult:
    """检查Android状态工具"""
    try:
        result = await android_bridge.call_android_api("/api/status", "GET")
        
        response_text = f"""📱 Android 应用状态

🟢 连接状态: 正常
🏠 设备地址: {android_bridge.host}:{android_bridge.port}
📊 服务器状态: {result.get('server_status', '未知')}
🔧 注册功能数: {result.get('registered_functions', 0)}
📱 Android版本: {result.get('android_version', '未知')}
📦 应用版本: {result.get('app_version', '未知')}
🕐 检查时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"""

        # 显示功能状态
        if 'function_status' in result:
            response_text += "\n\n🛠️ 功能状态:"
            for func_name, enabled in result['function_status'].items():
                status_icon = "✅" if enabled else "❌"
                response_text += f"\n{status_icon} {func_name}: {'启用' if enabled else '禁用'}"

        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=response_text
                )
            ]
        )
        
    except Exception as e:
        return CallToolResult(
            content=[
                TextContent(
                    type="text",
                    text=f"""📱 Android 应用状态

🔴 连接状态: 异常
🏠 设备地址: {android_bridge.host}:{android_bridge.port}
❌ 错误信息: {str(e)}
🕐 检查时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

💡 解决建议:
1. 确认Android设备IP地址是否正确
2. 确认Android应用HTTP服务器是否启动
3. 检查网络连接是否正常
4. 确认防火墙设置"""
                )
            ]
        )

async def main():
    """主函数"""
    # 服务器启动时的欢迎信息
    logger.info("🍅 四象限 MCP 服务器启动中...")
    logger.info(f"📱 Android设备配置: {ANDROID_CONFIG['host']}:{ANDROID_CONFIG['port']}")
    
    # 运行MCP服务器
    async with stdio_server() as (read_stream, write_stream):
        await server.run(
            read_stream,
            write_stream,
            InitializationOptions(
                server_name="fourquadrant-mcp",
                server_version="1.0.0",
                capabilities=server.get_capabilities(
                    notification_options=None,
                    experimental_capabilities=None,
                ),
            ),
        )

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("🛑 服务器已停止")
    except Exception as e:
        logger.error(f"❌ 服务器启动失败: {str(e)}")
        sys.exit(1)
