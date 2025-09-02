package com.example.fourquadrant.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import android.content.Context;
import android.util.Log;
import com.fourquadrant.ai.CommandRouter;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Android内置HTTP服务器
 * 用于接收MCP服务器的命令调用
 */
public class AndroidHttpServer {
    private static final String TAG = "AndroidHttpServer";
    private HttpServer server;
    private Context context;
    private boolean isRunning = false;
    private long startTime = System.currentTimeMillis();
    
    public AndroidHttpServer(Context context) {
        this.context = context;
        // 初始化CommandRouter
        CommandRouter.initialize(context);
    }
    
    /**
     * 启动HTTP服务器
     */
    public void startServer(int port) throws IOException {
        if (isRunning) {
            Log.w(TAG, "服务器已在运行中");
            return;
        }
        
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // 注册API端点
        server.createContext("/api/command/execute", new CommandExecuteHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/health", new HealthHandler());
        
        // 设置线程池
        server.setExecutor(null);
        server.start();
        
        isRunning = true;
        Log.i(TAG, "HTTP服务器已启动，端口：" + port);
    }
    
    /**
     * 停止HTTP服务器
     */
    public void stopServer() {
        if (server != null && isRunning) {
            server.stop(0);
            isRunning = false;
            Log.i(TAG, "HTTP服务器已停止");
        }
    }
    
    /**
     * 命令执行处理器
     */
    class CommandExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            // 设置CORS头
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            if ("OPTIONS".equals(method)) {
                // 处理预检请求
                sendResponse(exchange, 200, "");
                return;
            }
            
            if (!"POST".equals(method)) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                // 读取请求体
                String requestBody = readRequestBody(exchange);
                Log.d(TAG, "收到命令请求: " + requestBody);
                
                JSONObject request = new JSONObject(requestBody);
                String command = request.getString("command");
                JSONObject args = request.optJSONObject("args");
                
                // 转换参数
                Map<String, Object> argsMap = new HashMap<>();
                if (args != null) {
                    argsMap = jsonToMap(args);
                }
                
                // 执行命令
                CommandRouter.ExecutionResult result = CommandRouter.executeCommand(command, argsMap);
                
                // 构建响应
                JSONObject response = new JSONObject();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("timestamp", System.currentTimeMillis());
                response.put("command", command);
                
                if (result.getData() != null) {
                    response.put("data", result.getData());
                }
                
                Log.d(TAG, "命令执行结果: " + response.toString());
                sendJsonResponse(exchange, 200, response.toString());
                
            } catch (JSONException e) {
                Log.e(TAG, "JSON解析错误", e);
                sendErrorResponse(exchange, 400, "Invalid JSON format");
            } catch (Exception e) {
                Log.e(TAG, "命令执行异常", e);
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
    }
    
    /**
     * 状态查询处理器
     */
    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 设置CORS头
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                JSONObject status = new JSONObject();
                status.put("server_status", "running");
                status.put("registered_functions", CommandRouter.getRegisteredFunctions().size());
                status.put("android_version", android.os.Build.VERSION.RELEASE);
                status.put("app_version", getAppVersion());
                status.put("timestamp", System.currentTimeMillis());
                status.put("uptime", System.currentTimeMillis() - startTime);
                
                // 获取CommandRouter状态
                JSONObject routerStatus = new JSONObject();
                for (String functionName : CommandRouter.getRegisteredFunctions()) {
                    routerStatus.put(functionName, CommandRouter.isToolEnabled(functionName));
                }
                status.put("function_status", routerStatus);
                
                // 获取当前番茄钟状态
                try {
                    Map<String, Object> pomodoroStatus = new HashMap<>();
                    // 这里需要调用PomodoroService获取状态
                    // pomodoroStatus.put("is_running", pomodoroService.isTimerRunning());
                    // pomodoroStatus.put("is_paused", pomodoroService.isTimerPaused());
                    // pomodoroStatus.put("remaining_time", pomodoroService.getRemainingTime());
                    status.put("pomodoro_status", new JSONObject(pomodoroStatus));
                } catch (Exception e) {
                    Log.w(TAG, "获取番茄钟状态失败: " + e.getMessage());
                }
                
                sendJsonResponse(exchange, 200, status.toString());
                
            } catch (JSONException e) {
                Log.e(TAG, "状态查询异常", e);
                sendErrorResponse(exchange, 500, "Status query failed");
            }
        }
    }
    
    /**
     * 健康检查处理器
     */
    class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            JSONObject health = new JSONObject();
            try {
                health.put("status", "healthy");
                health.put("timestamp", System.currentTimeMillis());
                health.put("uptime", System.currentTimeMillis() - startTime);
                health.put("server_version", "1.0.0");
                health.put("mcp_compatible", true);
                sendJsonResponse(exchange, 200, health.toString());
            } catch (JSONException e) {
                sendErrorResponse(exchange, 500, "Health check failed");
            }
        }
    }
    
    // 工具方法
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String body = scanner.useDelimiter("\\A").next();
        scanner.close();
        return body;
    }
    
    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        java.util.Iterator<String> keys = json.keys();
        
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            
            if (value instanceof JSONObject) {
                map.put(key, jsonToMap((JSONObject) value));
            } else {
                map.put(key, value);
            }
        }
        
        return map;
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendResponse(exchange, statusCode, response);
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject error = new JSONObject();
        try {
            error.put("success", false);
            error.put("error", message);
            error.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            // 如果JSON构建失败，发送简单的错误消息
            sendResponse(exchange, statusCode, "{\"success\":false,\"error\":\"" + message + "\"}");
            return;
        }
        
        sendJsonResponse(exchange, statusCode, error.toString());
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }
    
    private String getAppVersion() {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
}
