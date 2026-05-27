package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 茉莉花价格数据查询服务
 * 从price_monitor表查询最新价格数据，计算价格波动
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final PriceMonitorMapper priceMonitorMapper;

    /**
     * 获取最新价格数据
     * @return 最新价格列表
     */
    public List<PriceMonitor> getLatestPrices() {
        try {
            List<PriceMonitor> all = priceMonitorMapper.selectList(
                new LambdaQueryWrapper<PriceMonitor>()
                    .orderByDesc(PriceMonitor::getRecordDate)
                    .last("LIMIT 100")
            );
            
            // 按市场+分类分组取最新
            return all.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getMarket() + "-" + p.getCategory(),
                            Collectors.toList()
                    ))
                    .values()
                    .stream()
                    .map(list -> list.get(0))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取最新价格数据失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取茉莉鲜花（分类1）的最新价格
     * @return 最新价格数据
     */
    public PriceMonitor getLatestFreshFlowerPrice() {
        try {
            PriceMonitor price = priceMonitorMapper.selectOne(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, 1)
                    .orderByDesc(PriceMonitor::getRecordDate)
                    .last("LIMIT 1")
            );
            return price;
        } catch (Exception e) {
            log.error("获取茉莉鲜花最新价格失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定分类的价格概览
     * @param category 分类ID
     * @return 价格概览
     */
    public Map<String, Object> getPriceOverview(Integer category) {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate weekAgo = today.minusDays(7);
            
            // 获取今日价格
            List<PriceMonitor> todayPrices = priceMonitorMapper.selectList(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, category)
                    .eq(PriceMonitor::getRecordDate, today)
            );
            
            // 获取昨日价格
            List<PriceMonitor> yesterdayPrices = priceMonitorMapper.selectList(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, category)
                    .eq(PriceMonitor::getRecordDate, yesterday)
            );
            
            // 获取7天价格趋势
            List<PriceMonitor> weekPrices = priceMonitorMapper.selectList(
                new LambdaQueryWrapper<PriceMonitor>()
                    .eq(PriceMonitor::getCategory, category)
                    .ge(PriceMonitor::getRecordDate, weekAgo)
                    .orderByAsc(PriceMonitor::getRecordDate)
            );
            
            if (todayPrices == null || todayPrices.isEmpty()) {
                overview.put("hasData", false);
                overview.put("message", "今日暂无价格数据");
                return overview;
            }
            
            // 计算今日均价（使用最高价和最低价的平均值）
            BigDecimal todaySum = todayPrices.stream()
                    .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal todayAvg = todaySum.divide(
                    BigDecimal.valueOf(todayPrices.size()), 2, RoundingMode.HALF_UP);
            
            // 获取最新价格（取今日或最近的）
            PriceMonitor latest = todayPrices.stream()
                    .max(Comparator.comparing(PriceMonitor::getRecordDate))
                    .orElse(todayPrices.get(0));
            
            // 计算单日涨跌幅
            BigDecimal yesterdayAvg = BigDecimal.ZERO;
            double changePercent = 0;
            
            if (yesterdayPrices != null && !yesterdayPrices.isEmpty()) {
                BigDecimal yesterdaySum = yesterdayPrices.stream()
                        .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                yesterdayAvg = yesterdaySum.divide(BigDecimal.valueOf(yesterdayPrices.size()), 2, RoundingMode.HALF_UP);
                
                if (yesterdayAvg.compareTo(BigDecimal.ZERO) > 0) {
                    changePercent = todayAvg.subtract(yesterdayAvg)
                            .divide(yesterdayAvg, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                }
            }
            
            // 计算7天趋势
            List<Map<String, Object>> weekTrend = new ArrayList<>();
            if (weekPrices != null && !weekPrices.isEmpty()) {
                Map<LocalDate, BigDecimal> dailyAvg = weekPrices.stream()
                        .collect(Collectors.groupingBy(
                                PriceMonitor::getRecordDate,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            BigDecimal sum = list.stream()
                                                    .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
                                        }
                                )
                        ));
                
                dailyAvg.forEach((date, price) -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", date.toString());
                    dayData.put("price", price);
                    weekTrend.add(dayData);
                });
                
                // 按日期排序
                weekTrend.sort(Comparator.comparing(m -> (String) m.get("date")));
            }
            
            // 组装结果
            overview.put("hasData", true);
            overview.put("latestPrice", latest.getMaxPrice());
            overview.put("latestMinPrice", latest.getMinPrice());
            overview.put("latestMaxPrice", latest.getMaxPrice());
            overview.put("latestMarket", latest.getMarket());
            overview.put("latestDate", latest.getRecordDate());
            overview.put("todayAvgPrice", todayAvg);
            overview.put("yesterdayAvgPrice", yesterdayAvg);
            overview.put("changePercent", changePercent);
            overview.put("changeTrend", changePercent > 0 ? "上涨" : changePercent < 0 ? "下跌" : "持平");
            overview.put("weekTrend", weekTrend);
            overview.put("priceUnit", latest.getUnit() != null ? latest.getUnit() : "元/斤");
            overview.put("categoryName", getCategoryName(category));
            
        } catch (Exception e) {
            log.error("获取价格概览失败: {}", e.getMessage());
            overview.put("hasData", false);
            overview.put("message", "价格数据获取失败: " + e.getMessage());
        }
        
        return overview;
    }

    /**
     * 获取茉莉鲜花（分类1）的完整价格数据
     * @return 价格数据
     */
    public Map<String, Object> getFreshFlowerPriceData() {
        return getPriceOverview(1);
    }

    /**
     * 获取用于AI分析的格式化价格数据
     * @return 格式化的价格数据字符串
     */
    public String getFormattedPriceDataForAI() {
        Map<String, Object> priceData = getFreshFlowerPriceData();
        
        if (priceData == null || !Boolean.TRUE.equals(priceData.get("hasData"))) {
            return "茉莉花价格数据暂时无法获取，请稍后再试。";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("茉莉花市场价格数据：\n");
        sb.append("\n【最新价格】");
        sb.append(priceData.get("latestPrice")).append(priceData.get("priceUnit"));
        sb.append("（来源：").append(priceData.get("latestMarket")).append("）");
        sb.append("，日期：").append(priceData.get("latestDate")).append("\n");
        
        sb.append("\n【今日均价】").append(priceData.get("todayAvgPrice")).append(priceData.get("priceUnit")).append("\n");
        
        if (((Number) priceData.get("yesterdayAvgPrice")).doubleValue() > 0) {
            sb.append("【昨日均价】").append(priceData.get("yesterdayAvgPrice")).append(priceData.get("priceUnit")).append("\n");
        }
        
        sb.append("\n【单日涨跌幅】");
        double changePercent = ((Number) priceData.get("changePercent")).doubleValue();
        sb.append(String.format("%.2f", changePercent)).append("%");
        sb.append("（").append(priceData.get("changeTrend")).append("）\n");
        
        // 7天趋势
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> weekTrend = (List<Map<String, Object>>) priceData.get("weekTrend");
        if (weekTrend != null && !weekTrend.isEmpty()) {
            sb.append("\n【7天价格趋势】\n");
            for (Map<String, Object> day : weekTrend) {
                sb.append(day.get("date")).append("：")
                  .append(day.get("price")).append(priceData.get("priceUnit"))
                  .append("\n");
            }
            
            // 分析趋势
            if (weekTrend.size() >= 2) {
                BigDecimal firstPrice = new BigDecimal(weekTrend.get(0).get("price").toString());
                BigDecimal lastPrice = new BigDecimal(weekTrend.get(weekTrend.size() - 1).get("price").toString());
                
                if (lastPrice.compareTo(firstPrice) > 0) {
                    double weekChange = lastPrice.subtract(firstPrice)
                            .divide(firstPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    sb.append("\n【7日趋势分析】近7日价格上涨").append(String.format("%.2f", weekChange)).append("%\n");
                } else if (lastPrice.compareTo(firstPrice) < 0) {
                    double weekChange = firstPrice.subtract(lastPrice)
                            .divide(firstPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    sb.append("\n【7日趋势分析】近7日价格下跌").append(String.format("%.2f", weekChange)).append("%\n");
                } else {
                    sb.append("\n【7日趋势分析】近7日价格基本持平\n");
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 获取多分类价格对比数据
     * @return 多分类价格数据
     */
    public List<Map<String, Object>> getMultiCategoryPrices() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 茉莉鲜花、茉莉花茶、茉莉盆栽、茉莉花苗
        int[] categories = {1, 2, 3, 4};
        
        for (int category : categories) {
            Map<String, Object> priceData = getPriceOverview(category);
            if (Boolean.TRUE.equals(priceData.get("hasData"))) {
                result.add(priceData);
            }
        }
        
        return result;
    }

    /**
     * 判断价格波动风险等级
     * @param changePercent 涨跌幅百分比
     * @return 风险等级描述
     */
    public String getRiskLevel(double changePercent) {
        if (Math.abs(changePercent) > 20) {
            return "高风险";
        } else if (Math.abs(changePercent) > 10) {
            return "中等风险";
        } else if (Math.abs(changePercent) > 5) {
            return "低风险";
        } else {
            return "正常";
        }
    }

    /**
     * 获取分类名称
     * @param category 分类ID
     * @return 分类名称
     */
    private String getCategoryName(Integer category) {
        switch (category) {
            case 1: return "茉莉鲜花";
            case 2: return "茉莉花茶";
            case 3: return "茉莉盆栽";
            case 4: return "茉莉花苗";
            default: return "其他";
        }
    }
}
