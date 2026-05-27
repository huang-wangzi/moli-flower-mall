package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.PriceAlert;
import com.hengzhou.moli.service.PriceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 价格预警控制器
 */
@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "价格预警", description = "价格预警相关接口")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    /**
     * 智能分析并生成价格预警
     * @return 生成的预警列表
     */

    @PostMapping("/analyze")
    @Operation(summary = "智能分析预警", description = "根据天气和价格数据智能分析，生成价格风险预警")
    public Result<List<PriceAlert>> analyzeAlerts() {
        List<PriceAlert> alerts = priceAlertService.analyzeAndGenerateAlerts();
        return Result.success(alerts);
    }

    /**
     * 获取预警列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取预警列表", description = "分页查询预警列表")
    public Result<Page<PriceAlert>> getAlertList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer alertType,
            @RequestParam(required = false) Integer alertLevel,
            @RequestParam(required = false) Integer status) {
        Page<PriceAlert> page = priceAlertService.getAlertPage(pageNum, pageSize, alertType, alertLevel, status);
        return Result.success(page);
    }

    /**
     * 获取预警统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取预警统计", description = "获取预警统计数据")
    public Result<Map<String, Object>> getAlertStats() {
        Map<String, Object> stats = priceAlertService.getAlertStats();
        return Result.success(stats);
    }

    /**
     * 获取未读预警数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读数量", description = "获取未处理的预警数量")
    public Result<Long> getUnreadCount() {
        long count = priceAlertService.getUnreadCount();
        return Result.success(count);
    }

    /**
     * 获取最近预警
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近预警", description = "获取最近N条预警")
    public Result<List<PriceAlert>> getRecentAlerts(@RequestParam(defaultValue = "5") Integer limit) {
        List<PriceAlert> alerts = priceAlertService.getRecentAlerts(limit);
        return Result.success(alerts);
    }

    /**
     * 获取预警详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预警详情", description = "根据ID获取预警详情")
    public Result<PriceAlert> getAlertDetail(@PathVariable Long id) {
        PriceAlert alert = priceAlertService.getById(id);
        return Result.success(alert);
    }

    /**
     * 标记预警已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记预警为已读状态")
    public Result<?> markAsRead(@PathVariable Long id) {
        boolean success = priceAlertService.markAsRead(id);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }

    /**
     * 标记预警已处理
     */
    @PutMapping("/{id}/handle")
    @Operation(summary = "标记已处理", description = "标记预警为已处理状态")
    public Result<?> markAsHandled(
            @PathVariable Long id,
            @RequestParam(required = false) Long handleBy,
            @RequestParam(required = false) String remark) {
        boolean success = priceAlertService.markAsHandled(id, handleBy, remark);
        return success ? Result.success("处理成功") : Result.error("处理失败");
    }

    /**
     * 删除预警
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警", description = "删除单条预警")
    public Result<?> deleteAlert(@PathVariable Long id) {
        boolean success = priceAlertService.deleteAlert(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}