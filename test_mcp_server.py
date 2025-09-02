#!/usr/bin/env python3
"""
四象限MCP服务器测试脚本
用于验证MCP服务器功能是否正常工作
"""

import asyncio
import json
import aiohttp
from datetime import datetime

# 测试配置
ANDROID_HOST = "192.168.1.100"  # 修改为您的Android设备IP
ANDROID_PORT = 8080

class MCPServerTester:
    def __init__(self, host=ANDROID_HOST, port=ANDROID_PORT):
        self.host = host
        self.port = port
        self.base_url = f"http://{host}:{port}"
        
    async def test_android_connection(self):
        """测试Android连接"""
        print("🔍 测试Android连接...")
        
        async with aiohttp.ClientSession() as session:
            try:
                # 测试健康检查端点
                async with session.get(f"{self.base_url}/api/health", timeout=5) as response:
                    if response.status == 200:
                        data = await response.json()
                        print(f"✅ Android连接正常")
                        print(f"   状态: {data.get('status', 'unknown')}")
                        print(f"   运行时间: {data.get('uptime', 0)}ms")
                        return True
                    else:
                        print(f"❌ Android连接异常: HTTP {response.status}")
                        return False
                        
            except Exception as e:
                print(f"❌ Android连接失败: {str(e)}")
                return False
                
    async def test_android_status(self):
        """测试Android状态查询"""
        print("📊 测试Android状态查询...")
        
        async with aiohttp.ClientSession() as session:
            try:
                async with session.get(f"{self.base_url}/api/status", timeout=5) as response:
                    if response.status == 200:
                        data = await response.json()
                        print(f"✅ 状态查询成功")
                        print(f"   服务器状态: {data.get('server_status', 'unknown')}")
                        print(f"   注册功能数: {data.get('registered_functions', 0)}")
                        print(f"   Android版本: {data.get('android_version', 'unknown')}")
                        print(f"   应用版本: {data.get('app_version', 'unknown')}")
                        
                        # 显示功能状态
                        if 'function_status' in data:
                            print("   功能状态:")
                            for func_name, enabled in data['function_status'].items():
                                status = "启用" if enabled else "禁用"
                                icon = "✅" if enabled else "❌"
                                print(f"     {icon} {func_name}: {status}")
                        
                        return True
                    else:
                        print(f"❌ 状态查询失败: HTTP {response.status}")
                        return False
                        
            except Exception as e:
                print(f"❌ 状态查询异常: {str(e)}")
                return False
                
    async def test_command_execution(self):
        """测试命令执行"""
        print("🎯 测试命令执行...")
        
        # 测试命令列表
        test_commands = [
            {
                "name": "查询番茄钟状态",
                "command": "get_pomodoro_status",
                "args": {}
            },
            {
                "name": "启动番茄钟",
                "command": "start_pomodoro", 
                "args": {
                    "task_name": "MCP服务器测试",
                    "duration": 1  # 1分钟测试
                }
            },
            {
                "name": "查询任务列表",
                "command": "task_management",
                "args": {
                    "action": "list"
                }
            }
        ]
        
        async with aiohttp.ClientSession() as session:
            for test_cmd in test_commands:
                try:
                    print(f"   测试: {test_cmd['name']}")
                    
                    payload = {
                        "command": test_cmd["command"],
                        "args": test_cmd["args"]
                    }
                    
                    async with session.post(
                        f"{self.base_url}/api/command/execute",
                        json=payload,
                        timeout=10
                    ) as response:
                        
                        if response.status == 200:
                            data = await response.json()
                            if data.get('success'):
                                print(f"     ✅ 成功: {data.get('message', '执行成功')}")
                            else:
                                print(f"     ⚠️ 命令失败: {data.get('message', '未知错误')}")
                        else:
                            print(f"     ❌ HTTP错误: {response.status}")
                            
                except Exception as e:
                    print(f"     ❌ 异常: {str(e)}")
                    
                # 短暂延迟
                await asyncio.sleep(1)
                
    async def run_all_tests(self):
        """运行所有测试"""
        print("🚀 开始MCP服务器测试")
        print("=" * 50)
        
        # 测试Android连接
        if not await self.test_android_connection():
            print("\n❌ Android连接测试失败，请检查:")
            print("   1. Android设备IP地址是否正确")
            print("   2. Android应用是否已启动HTTP服务器")
            print("   3. 网络连接是否正常")
            print("   4. 防火墙设置")
            return False
            
        print()
        
        # 测试状态查询
        if not await self.test_android_status():
            print("\n❌ Android状态查询失败")
            return False
            
        print()
        
        # 测试命令执行
        await self.test_command_execution()
        
        print()
        print("✅ MCP服务器测试完成")
        return True

def print_usage():
    """打印使用说明"""
    print("📋 使用说明:")
    print("1. 确保Android设备已连接到网络")
    print("2. 确保Android应用已启动HTTP服务器")
    print("3. 修改脚本中的ANDROID_HOST为您的Android设备IP")
    print("4. 运行测试: python test_mcp_server.py")
    print()

async def main():
    """主函数"""
    print("🍅 四象限MCP服务器测试工具")
    print("=" * 50)
    
    print_usage()
    
    # 创建测试器实例
    tester = MCPServerTester()
    
    try:
        # 运行测试
        success = await tester.run_all_tests()
        
        if success:
            print("\n🎉 所有测试通过！MCP服务器可以正常使用。")
        else:
            print("\n⚠️ 部分测试失败，请检查配置和网络连接。")
            
    except KeyboardInterrupt:
        print("\n🛑 测试被用户中断")
    except Exception as e:
        print(f"\n❌ 测试过程出现异常: {str(e)}")

if __name__ == "__main__":
    # 运行测试
    asyncio.run(main())
