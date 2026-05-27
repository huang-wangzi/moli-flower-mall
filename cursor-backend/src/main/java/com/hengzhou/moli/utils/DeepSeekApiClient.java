package com.hengzhou.moli.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DeepSeek大模型通用调用工具类
 * 提供统一的AI接口调用能力，支持prompt模板
 */
@Component
@Slf4j
public class DeepSeekApiClient {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.timeout:30}")
    private int timeout;

    @Value("${deepseek.max-tokens:2048}")
    private int maxTokens;

    private final org.springframework.web.client.RestTemplate restTemplate;

    public DeepSeekApiClient() {
        this.restTemplate = new org.springframework.web.client.RestTemplate();
    }

    /**
     * 调用DeepSeek生成内容
     * @param prompt 提示词
     * @return AI生成的内容，失败时返回友好提示
     */
    public String generate(String prompt) {
        try {
            log.info("开始调用DeepSeek API...");
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            // 构建消息
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", Collections.singletonList(message));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", false);
            
            // 设置请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // 设置超时
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout * 1000);
            factory.setReadTimeout(timeout * 1000);
            restTemplate.setRequestFactory(factory);
            
            // 发送请求
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            log.info("DeepSeek API响应: {}", response);
            
            // 解析响应
            return parseResponse(response);
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("DeepSeek API调用失败 - 密钥错误或权限不足: {}", e.getMessage());
            return "【系统提示】AI服务暂时不可用，请稍后再试。(密钥验证失败)";
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("DeepSeek API调用超时: {}", e.getMessage());
            return "【系统提示】AI服务响应超时，请稍后再试。";
        } catch (Exception e) {
            log.error("DeepSeek API调用异常: {}", e.getMessage(), e);
            return "【系统提示】AI服务暂时繁忙，请稍后再试。";
        }
    }

    /**
     * 解析DeepSeek API响应
     * @param response API响应字符串
     * @return 生成的内容
     */
    private String parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "【系统提示】AI服务未返回有效内容，请稍后再试。";
        }
        
        try {
            JSONObject json = JSON.parseObject(response);
            
            // 检查错误码
            if (json.containsKey("error")) {
                JSONObject error = json.getJSONObject("error");
                String errorMessage = error.getString("message");
                log.error("DeepSeek API返回错误: {}", errorMessage);
                return "【系统提示】AI服务处理失败: " + errorMessage;
            }
            
            // 解析正常响应
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "【系统提示】AI服务未生成有效内容，请稍后再试。";
            }
            
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            
            if (message != null) {
                String content = message.getString("content");
                if (content != null && !content.isEmpty()) {
                    return content.trim();
                }
            }
            
            return "【系统提示】AI服务未返回有效内容，请稍后再试。";
            
        } catch (Exception e) {
            log.error("解析DeepSeek响应失败: {}", e.getMessage());
            return "【系统提示】AI服务响应解析失败，请稍后再试。";
        }
    }

    /**
     * 使用自定义模板生成内容
     * @param template 模板名称
     * @param params 参数Map
     * @return 生成的内容
     */
    public String generateWithTemplate(String template, Map<String, Object> params) {
        String prompt = buildPromptFromTemplate(template, params);
        return generate(prompt);
    }

    /**
     * 根据模板名称构建prompt
     * @param template 模板名称
     * @param params 参数
     * @return 格式化后的prompt
     */
    private String buildPromptFromTemplate(String template, Map<String, Object> params) {
        switch (template) {
            case "market_warning":
                return buildMarketWarningPrompt(params);
            default:
                // 如果没有匹配的模板，直接使用参数中的content
                return (String) params.getOrDefault("content", "");
        }
    }

    /**
     * 构建市场预警prompt
     * @param params 包含天气和价格数据的参数
     * @return 格式化后的prompt
     */
    private String buildMarketWarningPrompt(Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();

        // 从天气数据中提取日期信息告知AI
        prompt.append("【数据时间参考】以下是横州市天气预报数据，请根据预报中的日期（第1天为今天）来分析：\n\n");

        prompt.append("你是横州茉莉花商城的专业市场分析师，专门为花农和商家提供市场行情分析与风险预警。\n\n");

        // 天气数据
        if (params.containsKey("weather")) {
            prompt.append("【天气预报数据】\n");
            prompt.append(params.get("weather")).append("\n\n");
        }

        // 价格数据
        if (params.containsKey("price")) {
            prompt.append("【茉莉花价格数据】\n");
            prompt.append(params.get("price")).append("\n\n");
        }

        prompt.append("请按以下结构生成分析报告：\n");
        prompt.append("① 当前茉莉花价格行情分析（基于最新价格、均价、涨跌幅）\n");
        prompt.append("② 未来5天天气对茉莉花花期、采摘、供货的影响\n");
        prompt.append("③ 天气-价格关联分析（重点！）：\n");
        prompt.append("   - 晴天/多云：茉莉花香浓郁、品质优良，价格普遍较高\n");
        prompt.append("   - 雨天：采摘受阻、市场供应减少，价格可能上涨\n");
        prompt.append("   - 大风/暴雨：可能造成花枝折断损毁，供应锐减，价格大幅上涨\n");
        prompt.append("   - 高温/低温：影响茉莉花生长和存储，价格可能波动\n");
        prompt.append("④ 市场风险提示（价格暴涨暴跌风险、天气灾害影响预警）\n");
        prompt.append("⑤ 给花农/商家的经营决策建议\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 内容专业、客观、数据支撑\n");
        prompt.append("2. 风险提示要具体、有可操作性\n");
        prompt.append("3. 建议要贴合横州茉莉花产业实际情况\n");
        prompt.append("4. 结尾必须添加：本内容由AI生成，仅供参考，不构成投资或交易建议");

        return prompt.toString();
    }

    /**
     * 测试API连接
     * @return 测试结果
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = generate("你好，请回复'连接成功'");
            if (response.contains("连接成功")) {
                result.put("success", true);
                result.put("message", "DeepSeek API连接正常");
            } else {
                result.put("success", false);
                result.put("message", "DeepSeek API响应异常: " + response);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "DeepSeek API连接失败: " + e.getMessage());
        }
        return result;
    }
}
