package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.PriceMonitor;
import com.hengzhou.moli.service.PriceMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 价格监测控制器
 * 功能：提供价格监测的完整API接口
 */
@RestController
@RequestMapping("/price")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "价格监测", description = "茉莉花价格监测相关接口")
public class PriceController {

    private final PriceMonitorService priceMonitorService;

    /**
     * 获取今日最新价格（仅茉莉鲜花，分类1）
     * @return 今日各市场茉莉鲜花价格列表
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日最新价格", description = "获取今日各市场茉莉鲜花最新价格")
    public Result<List<Map<String, Object>>> today() {
        List<Map<String, Object>> todayPrices = priceMonitorService.getTodayFreshFlowerPrices();
        return Result.success(todayPrices);
    }

    /**
     * 获取多市场茉莉花价格趋势
     * @param days 天数
     * @return 多市场价格趋势数据（包含每个市场每天的最高价和最低价）
     */
    @GetMapping("/multi-market-trend")
    @Operation(summary = "获取多市场价格趋势", description = "获取所有市场茉莉鲜花的每日价格区间趋势")
    public Result<List<Map<String, Object>>> multiMarketTrend(
            @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(priceMonitorService.getMultiMarketTrend(days));
    }

    /**
     * 获取所有市场列表
     * @return 市场名称列表
     */
    @GetMapping("/markets")
    @Operation(summary = "获取所有市场", description = "获取所有已录入的市场名称")
    public Result<List<String>> allMarkets() {
        return Result.success(priceMonitorService.getAllMarkets());
    }

    /**
     * 获取单市场茉莉花价格走势
     * @param market 市场名称
     * @param days 天数
     * @return 单市场价格走势数据
     */
    @GetMapping("/single-market-trend")
    @Operation(summary = "获取单市场走势", description = "获取指定市场茉莉鲜花的价格走势")
    public Result<List<PriceMonitor>> singleMarketTrend(
            @RequestParam String market,
            @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(priceMonitorService.getSingleMarketTrend(market, days));
    }

    /**
     * 获取价格概览
     * @return 按分类汇总的价格概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取价格概览", description = "获取各分类的价格概览和涨跌幅")
    public Result<List<Map<String, Object>>> overview() {
        List<Map<String, Object>> overview = priceMonitorService.getPriceOverview();
        return Result.success(overview);
    }

    /**
     * 获取最新价格数据
     * @return 最新价格列表
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新价格", description = "获取各市场各分类的最新价格")
    public Result<List<PriceMonitor>> latest() {
        return Result.success(priceMonitorService.getLatestPrices());
    }

    /**
     * 获取指定市场的最新价格
     * @param market 市场名称
     * @return 市场的最新价格列表
     */
    @GetMapping("/latest/{market}")
    @Operation(summary = "获取指定市场价格", description = "获取指定市场的最新价格")
    public Result<List<PriceMonitor>> latestByMarket(@PathVariable String market) {
        return Result.success(priceMonitorService.getLatestPricesByMarket(market));
    }

    /**
     * 获取价格趋势
     * @param category 分类ID
     * @param days 天数（默认30天）
     * @return 价格趋势列表
     */
    @GetMapping("/trend")
    @Operation(summary = "获取价格趋势", description = "获取指定分类的价格趋势")
    public Result<List<PriceMonitor>> trend(
            @RequestParam Integer category,
            @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(priceMonitorService.getPriceTrend(category, days));
    }

    /**
     * 获取指定市场的价格趋势
     * @param market 市场名称
     * @param category 分类ID
     * @param days 天数（默认30天）
     * @return 价格趋势列表
     */
    @GetMapping("/trend/{market}")
    @Operation(summary = "获取指定市场价格趋势", description = "获取指定市场的价格趋势")
    public Result<List<PriceMonitor>> marketTrend(
            @PathVariable String market,
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(priceMonitorService.getMarketTrend(market, category, days));
    }

    /**
     * 获取市场价格对比
     * @param category 分类ID
     * @return 各市场价格列表
     */
    @GetMapping("/market")
    @Operation(summary = "获取市场价格对比", description = "获取各市场同一分类的价格对比")
    public Result<List<PriceMonitor>> market(@RequestParam Integer category) {
        return Result.success(priceMonitorService.getMarketPrices(category));
    }

    /**
     * 获取价格统计数据
     * @param market 市场名称
     * @param category 分类ID
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取价格统计", description = "获取价格统计数据（均价、最高价、最低价等）")
    public Result<Map<String, Object>> stats(
            @RequestParam(required = false) String market,
            @RequestParam(required = false) Integer category) {
        return Result.success(priceMonitorService.getPriceStats(market, category));
    }

    /**
     * 价格记录列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "价格记录列表", description = "分页查询价格记录")
    public Result<Page<PriceMonitor>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(priceMonitorService.getPricePage(pageNum, pageSize, category, market, startDate, endDate));
    }

    /**
     * 添加价格记录
     */
    @PostMapping
    @Operation(summary = "添加价格记录", description = "录入新的价格数据")
    public Result<?> add(@RequestBody PriceMonitor priceMonitor) {
        boolean success = priceMonitorService.addPriceRecord(priceMonitor);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新价格记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新价格记录", description = "修改价格记录")
    public Result<?> update(@PathVariable Long id, @RequestBody PriceMonitor priceMonitor) {
        priceMonitor.setId(id);
        boolean success = priceMonitorService.updatePriceRecord(priceMonitor);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除价格记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除价格记录", description = "删除单条价格记录")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = priceMonitorService.deletePriceRecord(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除价格记录
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除价格记录", description = "批量删除价格记录")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的记录");
        }
        boolean success = priceMonitorService.deletePriceRecords(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 按日期获取价格（最高价和最低价）
     * @param date 日期
     * @param market 市场名称（可选）
     * @return 该日期的最高价和最低价
     */
    @GetMapping("/by-date")
    @Operation(summary = "按日期获取价格", description = "获取指定日期的最高价和最低价")
    public Result<Map<String, Object>> byDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String market) {
        return Result.success(priceMonitorService.getPriceByDate(date, market));
    }
}
