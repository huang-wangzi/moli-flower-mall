package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.service.AiMarketWarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 市场AI预警控制器
 * 提供市场行情简报与风险预警的对外接口
 */
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
@Tag(name = "市场AI预警", description = "横州茉莉花市场行情简报与AI风险预警接口")
public class MarketAiWarningController {

    private final AiMarketWarningService aiMarketWarningService;

    /**
     * 获取AI市场预警报告
     * 整合天气数据+价格数据，调用DeepSeek生成智能分析
     * @return AI生成的预警报告
     */
    @GetMapping("/ai-warning")
    @Operation(summary = "获取AI市场预警", description = "整合天气预报和茉莉花价格数据，AI智能生成市场行情分析与风险预警报告")
    public Result<Map<String, Object>> getAiMarketWarning() {
        try {
            log.info("收到AI市场预警请求");
            Map<String, Object> result = aiMarketWarningService.generateMarketWarning();
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.success(result);
            } else {
                return Result.error(result.get("message").toString());
            }
        } catch (Exception e) {
            log.error("获取AI市场预警失败: {}", e.getMessage(), e);
            return Result.error("获取预警失败: " + e.getMessage());
        }
    }

    /**
     * 获取快速预警摘要
     * 不调用AI，直接整合天气和价格核心数据
     * @return 快速预警摘要
     */
    @GetMapping("/quick-warning")
    @Operation(summary = "获取快速预警摘要", description = "快速获取天气和价格核心数据，不调用AI生成")
    public Result<Map<String, Object>> getQuickWarning() {
        try {
            log.info("收到快速预警摘要请求");
            Map<String, Object> result = aiMarketWarningService.getQuickWarning();
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.success(result);
            } else {
                return Result.error(result.get("message").toString());
            }
        } catch (Exception e) {
            log.error("获取快速预警摘要失败: {}", e.getMessage(), e);
            return Result.error("获取预警摘要失败: " + e.getMessage());
        }
    }
}
