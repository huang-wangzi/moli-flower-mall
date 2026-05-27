package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.PriceMonitor;
import com.hengzhou.moli.mapper.PriceMonitorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 价格监测Service层
 * 功能：提供价格监测的完整业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceMonitorService extends ServiceImpl<PriceMonitorMapper, PriceMonitor> {

    /**
     * 获取最新价格数据（按分类汇总）
     * @return 最新价格列表
     */
    public List<PriceMonitor> getLatestPrices() {
        // 获取所有价格记录，按日期倒序
        List<PriceMonitor> all = this.list(new LambdaQueryWrapper<PriceMonitor>()
                .orderByDesc(PriceMonitor::getRecordDate));

        if (all.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 找到每个分类的最新日期
        Map<Integer, LocalDate> latestDateByCategory = all.stream()
                .collect(Collectors.groupingBy(
                        PriceMonitor::getCategory,
                        Collectors.mapping(PriceMonitor::getRecordDate, 
                                Collectors.maxBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        // 2. 只保留每个分类在对应最新日期的各市场记录
        // 先按分类和市场分组，确保每个市场每个分类只取一条（理论上同一天同一个市场同一个分类只有一条，但这里做个保险）
        return all.stream()
                .filter(p -> {
                    LocalDate latest = latestDateByCategory.get(p.getCategory());
                    return latest != null && p.getRecordDate().equals(latest);
                })
                .collect(Collectors.groupingBy(
                        p -> p.getMarket() + "-" + p.getCategory(),
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(list -> list.get(0))
                .collect(Collectors.toList());
    }

    /**
     * 获取按分类汇总的价格概览
     * @return 价格概览列表
     */
    public List<Map<String, Object>> getPriceOverview() {
        List<PriceMonitor> latest = getLatestPrices();
        
        // 按分类分组计算平均价格（使用最高价和最低价的平均值）
        Map<Integer, List<PriceMonitor>> byCategory = latest.stream()
                .collect(Collectors.groupingBy(PriceMonitor::getCategory));

        List<Map<String, Object>> overview = new ArrayList<>();
        
        // 定义分类信息
        Map<Integer, String> categoryNames = Map.of(
                1, "茉莉鲜花",
                2, "茉莉花茶",
                3, "茉莉盆栽",
                4, "茉莉花苗"
        );
        
        Map<Integer, String> categoryIcons = Map.of(
                1, "flower",
                2, "tea",
                3, "potted",
                4, "seedling"
        );
        
        Map<Integer, String> categoryUnits = Map.of(
                1, "元/斤",
                2, "元/斤",
                3, "元/盆",
                4, "元/株"
        );

        byCategory.forEach((category, items) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", category);
            item.put("name", categoryNames.getOrDefault(category, "其他"));
            item.put("icon", categoryIcons.getOrDefault(category, "box"));
            item.put("unit", categoryUnits.getOrDefault(category, "元"));

            // 计算平均价格（使用最高价和最低价的平均值）
            BigDecimal avgPrice = items.stream()
                    .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

            // 获取最新价格（按日期）- 使用最高价
            BigDecimal latestPrice = items.stream()
                    .max(Comparator.comparing(PriceMonitor::getRecordDate))
                    .map(PriceMonitor::getMaxPrice)
                    .orElse(BigDecimal.ZERO);

            item.put("avgPrice", avgPrice);
            item.put("currentPrice", latestPrice);

            // 计算涨跌幅（与昨天对比）
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            BigDecimal yesterdayPrice = items.stream()
                    .filter(p -> p.getRecordDate().equals(yesterday))
                    .map(PriceMonitor::getMaxPrice)
                    .findFirst()
                    .orElse(latestPrice);

            if (yesterdayPrice.compareTo(BigDecimal.ZERO) > 0) {
                double change = (latestPrice.subtract(yesterdayPrice))
                        .divide(yesterdayPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                item.put("change", change);
            } else {
                item.put("change", 0);
            }

            overview.add(item);
        });

        return overview;
    }

    /**
     * 获取指定分类的价格趋势
     * @param category 分类ID
     * @param days 天数
     * @return 价格趋势列表
     */
    public List<PriceMonitor> getPriceTrend(Integer category, Integer days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(PriceMonitor::getCategory, category);
        }
        wrapper.ge(PriceMonitor::getRecordDate, startDate);
        wrapper.orderByAsc(PriceMonitor::getRecordDate);

        return this.list(wrapper);
    }

    /**
     * 获取指定市场的价格趋势
     * @param market 市场名称
     * @param category 分类ID
     * @param days 天数
     * @return 价格趋势列表
     */
    public List<PriceMonitor> getMarketTrend(String market, Integer category, Integer days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceMonitor::getMarket, market);
        if (category != null) {
            wrapper.eq(PriceMonitor::getCategory, category);
        }
        wrapper.ge(PriceMonitor::getRecordDate, startDate);
        wrapper.orderByAsc(PriceMonitor::getRecordDate);

        return this.list(wrapper);
    }

    /**
     * 获取各市场价格对比
     * @param category 分类ID
     * @return 各市场价格列表
     */
    public List<PriceMonitor> getMarketPrices(Integer category) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceMonitor::getCategory, category);
        wrapper.eq(PriceMonitor::getRecordDate, today);
        wrapper.orderByDesc(PriceMonitor::getMaxPrice);

        return this.list(wrapper);
    }

    /**
     * 获取指定市场的最新价格
     * @param market 市场名称
     * @return 最新价格列表
     */
    public List<PriceMonitor> getLatestPricesByMarket(String market) {
        List<PriceMonitor> all = this.list(new LambdaQueryWrapper<PriceMonitor>()
                .eq(PriceMonitor::getMarket, market)
                .orderByDesc(PriceMonitor::getRecordDate));

        // 按分类分组取最新
        return all.stream()
                .collect(Collectors.groupingBy(PriceMonitor::getCategory))
                .values()
                .stream()
                .map(list -> list.get(0))
                .collect(Collectors.toList());
    }

    /**
     * 添加价格记录
     * @param priceMonitor 价格记录
     * @return 是否成功
     */
    public boolean addPriceRecord(PriceMonitor priceMonitor) {
        return this.save(priceMonitor);
    }

    /**
     * 更新价格记录
     * @param priceMonitor 价格记录
     * @return 是否成功
     */
    public boolean updatePriceRecord(PriceMonitor priceMonitor) {
        return this.updateById(priceMonitor);
    }

    /**
     * 删除价格记录
     * @param id 价格记录ID
     * @return 是否成功
     */
    public boolean deletePriceRecord(Long id) {
        return this.removeById(id);
    }

    /**
     * 批量删除价格记录
     * @param ids 价格记录ID列表
     * @return 是否成功
     */
    public boolean deletePriceRecords(List<Long> ids) {
        return this.removeByIds(ids);
    }

    /**
     * 分页查询价格记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param category 分类
     * @param market 市场名称
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    public Page<PriceMonitor> getPricePage(Integer pageNum, Integer pageSize,
                                          Integer category, String market, 
                                          LocalDate startDate, LocalDate endDate) {
        Page<PriceMonitor> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null) {
            wrapper.eq(PriceMonitor::getCategory, category);
        }
        if (market != null && !market.isEmpty()) {
            wrapper.eq(PriceMonitor::getMarket, market);
        }
        if (startDate != null) {
            wrapper.ge(PriceMonitor::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(PriceMonitor::getRecordDate, endDate);
        }

        wrapper.orderByDesc(PriceMonitor::getRecordDate);

        return this.page(page, wrapper);
    }

    /**
     * 获取价格统计数据
     * @param market 市场名称
     * @param category 分类ID
     * @return 统计数据
     */
    public Map<String, Object> getPriceStats(String market, Integer category) {
        Map<String, Object> stats = new HashMap<>();

        // 获取最近30天的数据
        LocalDate startDate = LocalDate.now().minusDays(30);

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        if (market != null && !market.isEmpty()) {
            wrapper.eq(PriceMonitor::getMarket, market);
        }
        if (category != null) {
            wrapper.eq(PriceMonitor::getCategory, category);
        }
        wrapper.ge(PriceMonitor::getRecordDate, startDate);

        List<PriceMonitor> records = this.list(wrapper);

        if (records.isEmpty()) {
            stats.put("count", 0);
            stats.put("avgPrice", BigDecimal.ZERO);
            stats.put("maxPrice", BigDecimal.ZERO);
            stats.put("minPrice", BigDecimal.ZERO);
            return stats;
        }

        // 计算统计数据（使用最高价）
        BigDecimal total = records.stream()
                .map(PriceMonitor::getMaxPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal max = records.stream()
                .map(PriceMonitor::getMaxPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal min = records.stream()
                .map(PriceMonitor::getMinPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        stats.put("count", records.size());
        stats.put("avgPrice", total.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP));
        stats.put("maxPrice", max);
        stats.put("minPrice", min);

        return stats;
    }

    /**
     * 获取今日各市场茉莉鲜花最新价格（仅分类1）
     * @return 今日各市场价格列表
     */
    public List<Map<String, Object>> getTodayFreshFlowerPrices() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 1. 先找到最近的有数据的日期（茉莉鲜花分类1）
            PriceMonitor latestOne = this.getOne(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, 1)
                    .orderByDesc(PriceMonitor::getRecordDate)
                    .last("LIMIT 1")
            );

            if (latestOne == null) {
                return result;
            }

            LocalDate targetDate = latestOne.getRecordDate();
            LocalDate dayBefore = targetDate.minusDays(1);

            // 2. 查询该日期的所有市场价格
            List<PriceMonitor> targetPrices = this.list(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, 1)
                    .eq(PriceMonitor::getRecordDate, targetDate)
            );

            // 3. 查询前一天的价格用于计算涨跌幅
            List<PriceMonitor> yesterdayPrices = this.list(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, 1)
                    .eq(PriceMonitor::getRecordDate, dayBefore)
            );

            // 按市场分组前一天价格
            Map<String, PriceMonitor> yesterdayMap = yesterdayPrices.stream()
                    .collect(Collectors.toMap(PriceMonitor::getMarket, p -> p, (a, b) -> b));

            // 4. 计算该日期所有市场的平均值
            BigDecimal targetAvg = targetPrices.stream()
                    .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(targetPrices.size()), 2, RoundingMode.HALF_UP);

            // 5. 组装结果
            for (PriceMonitor price : targetPrices) {
                Map<String, Object> item = new HashMap<>();
                item.put("market", price.getMarket());
                item.put("minPrice", price.getMinPrice());
                item.put("maxPrice", price.getMaxPrice());
                BigDecimal currentAvg = price.getMaxPrice().add(price.getMinPrice())
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                item.put("avgPrice", currentAvg);
                item.put("todayMarketAvg", targetAvg); // 今日市场总均价（或最新日期的总均价）
                item.put("recordDate", price.getRecordDate().toString());

                // 计算涨跌幅
                PriceMonitor yesterdayPrice = yesterdayMap.get(price.getMarket());
                if (yesterdayPrice != null && yesterdayPrice.getMaxPrice().compareTo(BigDecimal.ZERO) > 0) {
                    double changePercent = price.getMaxPrice().subtract(yesterdayPrice.getMaxPrice())
                            .divide(yesterdayPrice.getMaxPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    item.put("changePercent", changePercent);
                } else {
                    item.put("changePercent", 0);
                }

                result.add(item);
            }
        } catch (Exception e) {
            log.error("获取今日价格失败", e);
        }

        return result;
    }

    /**
     * 获取多市场茉莉花价格趋势（仅分类1）
     * 返回格式：包含每个市场每天的最高价和最低价
     * @param days 天数
     * @return 多市场价格趋势数据列表
     */
    public List<Map<String, Object>> getMultiMarketTrend(Integer days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        List<PriceMonitor> records = this.list(
            new LambdaQueryWrapper<PriceMonitor>()
                .eq(PriceMonitor::getCategory, 1)
                .ge(PriceMonitor::getRecordDate, startDate)
                .orderByAsc(PriceMonitor::getRecordDate)
        );

        // 按市场和日期分组，每天每个市场只保留一条记录
        Map<String, PriceMonitor> grouped = new LinkedHashMap<>();
        for (PriceMonitor p : records) {
            String key = p.getMarket() + "_" + p.getRecordDate();
            grouped.put(key, p);
        }

        // 转换为结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (PriceMonitor p : grouped.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("market", p.getMarket());
            item.put("recordDate", p.getRecordDate().toString());
            item.put("minPrice", p.getMinPrice());
            item.put("maxPrice", p.getMaxPrice());
            item.put("avgPrice", p.getMaxPrice().add(p.getMinPrice())
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            result.add(item);
        }

        return result;
    }

    /**
     * 获取单市场茉莉花价格走势（仅分类1）
     * @param market 市场名称
     * @param days 天数
     * @return 单市场价格走势数据
     */
    public List<PriceMonitor> getSingleMarketTrend(String market, Integer days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        return this.list(
            new LambdaQueryWrapper<PriceMonitor>()
                .eq(PriceMonitor::getCategory, 1)
                .eq(PriceMonitor::getMarket, market)
                .ge(PriceMonitor::getRecordDate, startDate)
                .orderByAsc(PriceMonitor::getRecordDate)
        );
    }

    /**
     * 获取所有市场列表（从市场管理表）
     * @return 市场名称列表
     */
    public List<String> getAllMarkets() {
        // 方式1：从price_monitor表获取所有市场名称
        List<PriceMonitor> records = this.list(
            new LambdaQueryWrapper<PriceMonitor>()
                .select(PriceMonitor::getMarket)
                .eq(PriceMonitor::getCategory, 1)
        );

        return records.stream()
                .map(PriceMonitor::getMarket)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 按日期获取价格（最高价和最低价）
     * @param date 日期
     * @param market 市场名称（可选）
     * @return 最高价和最低价
     */
    public Map<String, Object> getPriceByDate(LocalDate date, String market) {
        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toString());

        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceMonitor::getRecordDate, date);
        if (market != null && !market.isEmpty()) {
            wrapper.eq(PriceMonitor::getMarket, market);
        }

        List<PriceMonitor> records = this.list(wrapper);

        if (records.isEmpty()) {
            result.put("min", 0);
            result.put("max", 0);
            result.put("hasData", false);
            return result;
        }

        BigDecimal minPrice = records.stream()
                .map(PriceMonitor::getMinPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = records.stream()
                .map(PriceMonitor::getMaxPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        result.put("min", minPrice);
        result.put("max", maxPrice);
        result.put("hasData", true);

        return result;
    }
}
