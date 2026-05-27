package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.MarketBriefing;
import com.hengzhou.moli.service.MarketBriefingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 市场简报控制器
 */
@RestController
@RequestMapping("/briefing")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "市场简报", description = "市场简报相关接口")
public class MarketBriefingController {

    private final MarketBriefingService marketBriefingService;

    /**
     * 生成日报
     */
    @PostMapping("/generate/daily")
    @Operation(summary = "生成日报", description = "自动生成今日市场简报")
    public Result<MarketBriefing> generateDailyBriefing() {
        MarketBriefing briefing = marketBriefingService.generateDailyBriefing();
        return Result.success(briefing);
    }

    /**
     * 获取简报列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取简报列表", description = "分页查询简报列表")
    public Result<Page<MarketBriefing>> getBriefingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer briefingType,
            @RequestParam(required = false) Integer status) {
        Page<MarketBriefing> page = marketBriefingService.getBriefingPage(pageNum, pageSize, briefingType, status);
        return Result.success(page);
    }

    /**
     * 获取简报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取简报详情", description = "根据ID获取简报详情")
    public Result<MarketBriefing> getBriefingDetail(@PathVariable Long id) {
        MarketBriefing briefing = marketBriefingService.getBriefingDetail(id);
        return Result.success(briefing);
    }

    /**
     * 获取最近简报
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近简报", description = "获取最近N条简报")
    public Result<List<MarketBriefing>> getRecentBriefings(@RequestParam(defaultValue = "7") Integer limit) {
        List<MarketBriefing> briefings = marketBriefingService.getRecentBriefings(limit);
        return Result.success(briefings);
    }

    /**
     * 删除简报
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除简报", description = "删除单条简报")
    public Result<?> deleteBriefing(@PathVariable Long id) {
        boolean success = marketBriefingService.deleteBriefing(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 发布简报
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布简报", description = "发布简报")
    public Result<?> publishBriefing(@PathVariable Long id) {
        boolean success = marketBriefingService.publishBriefing(id);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }
}