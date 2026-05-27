package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.PriceMarket;
import com.hengzhou.moli.service.PriceMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 市场管理控制器
 * 功能：管理茉莉花交易市场信息
 */
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "市场管理", description = "管理茉莉花交易市场信息")
public class PriceMarketController {

    private final PriceMarketService priceMarketService;

    /**
     * 获取所有启用的市场列表
     * @return 市场列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取市场列表", description = "获取所有启用的市场列表")
    public Result<List<PriceMarket>> list() {
        List<PriceMarket> markets = priceMarketService.getEnabledMarkets();
        return Result.success(markets);
    }

    /**
     * 获取所有市场（包括禁用的）
     * @return 所有市场列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有市场", description = "获取所有市场（包括禁用的）")
    public Result<List<PriceMarket>> getAll() {
        List<PriceMarket> markets = priceMarketService.getAllMarkets();
        return Result.success(markets);
    }

    /**
     * 获取市场详情
     * @param id 市场ID
     * @return 市场详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取市场详情", description = "根据ID获取市场详细信息")
    public Result<PriceMarket> detail(@PathVariable Long id) {
        PriceMarket market = priceMarketService.getMarketById(id);
        if (market == null) {
            return Result.error("市场不存在");
        }
        return Result.success(market);
    }

    /**
     * 添加市场
     * @param market 市场信息
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加市场", description = "添加新的茉莉花交易市场")
    public Result<?> add(@RequestBody PriceMarket market) {
        // 检查市场名称是否已存在
        if (priceMarketService.isNameExists(market.getName(), null)) {
            return Result.error("市场名称已存在");
        }

        boolean success = priceMarketService.addMarket(market);
        if (success) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 更新市场信息
     * @param market 市场信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新市场", description = "更新市场信息")
    public Result<?> update(@RequestBody PriceMarket market) {
        if (market.getId() == null) {
            return Result.error("市场ID不能为空");
        }

        // 检查市场是否存在
        PriceMarket existing = priceMarketService.getMarketById(market.getId());
        if (existing == null) {
            return Result.error("市场不存在");
        }

        // 检查市场名称是否与其他市场冲突
        if (priceMarketService.isNameExists(market.getName(), market.getId())) {
            return Result.error("市场名称已存在");
        }

        boolean success = priceMarketService.updateMarket(market);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除市场（物理删除）
     * @param id 市场ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除市场", description = "删除市场（物理删除）")
    public Result<?> delete(@PathVariable Long id) {
        PriceMarket market = priceMarketService.getMarketById(id);
        if (market == null) {
            return Result.error("市场不存在");
        }

        boolean success = priceMarketService.deleteMarket(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除市场（物理删除）
     * @param ids 市场ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除市场", description = "批量删除市场（物理删除）")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的市场");
        }

        boolean success = priceMarketService.deleteMarkets(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 根据城市获取市场列表
     * @param city 城市名称
     * @return 市场列表
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "按城市获取市场", description = "根据城市名称获取市场列表")
    public Result<List<PriceMarket>> getByCity(@PathVariable String city) {
        List<PriceMarket> markets = priceMarketService.getMarketsByCity(city);
        return Result.success(markets);
    }

    /**
     * 根据省份获取市场列表
     * @param province 省份名称
     * @return 市场列表
     */
    @GetMapping("/province/{province}")
    @Operation(summary = "按省份获取市场", description = "根据省份名称获取市场列表")
    public Result<List<PriceMarket>> getByProvince(@PathVariable String province) {
        List<PriceMarket> markets = priceMarketService.getMarketsByProvince(province);
        return Result.success(markets);
    }
}
