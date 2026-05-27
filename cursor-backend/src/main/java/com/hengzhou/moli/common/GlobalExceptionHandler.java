package com.hengzhou.moli.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各类异常，返回友好的错误提示
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理HTTP客户端错误（密钥错误、权限不足等）
     */
    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleHttpClientError(HttpClientErrorException e) {
        log.error("HTTP客户端错误: {}", e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorType", "API调用错误");
        data.put("suggestion", "请检查API密钥是否正确，或联系技术支持");
        
        return Result.error(400, "服务暂时不可用，请稍后再试");
    }

    /**
     * 处理网络超时异常
     */
    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Result<Map<String, Object>> handleResourceAccessError(ResourceAccessException e) {
        log.error("网络连接超时: {}", e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        data.put("errorType", "连接超时");
        data.put("suggestion", "网络连接超时，请检查网络后重试");
        
        return Result.error(504, "服务响应超时，请稍后再试");
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("参数类型错误: {}", e.getMessage());
        
        String message = String.format("参数'%s'类型错误，请检查输入", e.getName());
        
        return Result.error(400, message);
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleDataAccessException(DataAccessException e) {
        String message = e.getMessage();
        log.error("数据库访问异常: {}", message, e);
        
        // 检查是否是表不存在的问题
        if (message != null && message.contains("Table") && message.contains("doesn't exist")) {
            return Result.error("系统数据表缺失，请联系管理员初始化数据库");
        }
        
        // 检查是否是列不存在的问题
        if (message != null && message.contains("Unknown column")) {
            return Result.error("数据库表结构不完整，请联系管理员执行数据库修复");
        }
        
        // 检查是否是外键约束问题
        if (message != null && message.contains("foreign key")) {
            return Result.error("数据关联异常，请检查数据完整性");
        }
        
        return Result.error("数据访问异常，请稍后再试");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleNullPointer(NullPointerException e) {
        log.error("空指针异常: {}", e.getMessage(), e);
        return Result.error("系统数据异常，请稍后再试");
    }

    /**
     * 处理数字格式异常
     */
    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleNumberFormat(NumberFormatException e) {
        log.error("数字格式错误: {}", e.getMessage());
        return Result.error(400, "数字格式错误，请检查输入");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("非法参数: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        // 返回具体的错误消息，而不是通用消息
        String userMessage = e.getMessage();
        if (userMessage == null || userMessage.isBlank()) {
            userMessage = "操作失败，请稍后再试";
        }
        // 避免返回可能包含敏感信息的错误
        if (userMessage.contains("password") || userMessage.contains("token") || 
            userMessage.contains("secret") || userMessage.contains("key")) {
            return Result.error(400, "操作失败，请稍后再试");
        }
        return Result.error(400, userMessage);
    }

    /**
     * 处理所有未捕获的异常 - 确保永远不会返回500 HTML页面
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        // 返回JSON而不是HTML，确保前端能正确处理
        return Result.error("系统繁忙，请稍后再试");
    }
}
