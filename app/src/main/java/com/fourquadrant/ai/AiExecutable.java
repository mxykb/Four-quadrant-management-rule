package com.fourquadrant.ai;

import java.util.Map;

/**
 * AI可执行功能接口
 * 定义统一接口，用于封装所有可被AI调用的功能
 */
public interface AiExecutable {
    /**
     * 执行功能
     * @param args 参数映射，包含执行所需的所有参数
     */
    void execute(Map<String, Object> args);
    
    /**
     * 获取功能描述
     * @return 功能描述信息
     */
    default String getDescription() {
        return "AI可执行功能";
    }
    
    /**
     * 验证参数
     * @param args 参数映射
     * @return 参数是否有效
     */
    default boolean validateArgs(Map<String, Object> args) {
        return true;
    }
}