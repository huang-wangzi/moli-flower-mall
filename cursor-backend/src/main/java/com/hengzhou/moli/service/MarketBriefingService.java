package com.hengzhou.moli.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.MarketBriefing;
import com.hengzhou.moli.entity.PriceMonitor;
import com.hengzhou.moli.entity.WeatherData;
import com.hengzhou.moli.mapper.MarketBriefingMapper;
import com.hengzhou.moli.mapper.PriceMonitorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 市场简报服务类
 * 自动生成市场简报
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class MarketBriefingService extends ServiceImpl<MarketBriefingMapper, MarketBriefing> {

    private final MarketBriefingMapper marketBriefingMapper;
    private final PriceMonitorMapper priceMonitorMapper;
    private final WeatherService weatherService;

    /**
     * 自动生成日报
     */
    public MarketBriefing generateDailyBriefing() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        // 检查是否已存在今日简报
        LambdaQueryWrapper<MarketBriefing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketBriefing::getReportDate, today);
        wrapper.eq(MarketBriefing::getBriefingType, 1);
        MarketBriefing existing = marketBriefingMapper.selectOne(wrapper);
        
        if (existing != null) {
            return existing;
        }
        
        // 收集数据
        Map<String, Object> priceData = collectPriceData();
        Map<String, Object> weatherData = weatherService.getHengzhouWeather();
        Map<String, Object> forecastData = weatherService.getHengzhouForecast();
        
        // 生成简报
        MarketBriefing briefing = new MarketBriefing();
        briefing.setTitle("横州茉莉花市场日报 " + today);
        briefing.setBriefingType(1); // 日报
        briefing.setReportDate(today);
        briefing.setWeatherInfo(JSON.toJSONString(weatherData));
        briefing.setPriceOverview(JSON.toJSONString(priceData));
        briefing.setAnalysis(generatePriceAnalysis(priceData, weatherData));
        briefing.setForecast(generateForecast(priceData, weatherData, forecastData));
        briefing.setSuggestions(generateSuggestions(priceData, weatherData));
        briefing.setSummary(generateSummary(priceData, weatherData));
        briefing.setAiGenerated(1);
        briefing.setStatus(1);
        briefing.setPublishTime(LocalDateTime.now());
        briefing.setCreateTime(LocalDateTime.now());
        briefing.setUpdateTime(LocalDateTime.now());
        
        marketBriefingMapper.insert(briefing);
        
        return briefing;
    }

    /**
     * 获取简报列表（分页）
     */
    public Page<MarketBriefing> getBriefingPage(Integer pageNum, Integer pageSize, Integer briefingType, Integer status) {
        Page<MarketBriefing> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<MarketBriefing> wrapper = new LambdaQueryWrapper<>();
        
        if (briefingType != null) {
            wrapper.eq(MarketBriefing::getBriefingType, briefingType);
        }
        if (status != null) {
            wrapper.eq(MarketBriefing::getStatus, status);
        }
        
        wrapper.orderByDesc(MarketBriefing::getReportDate);
        
        return page(page, wrapper);
    }

    /**
     * 获取简报详情
     */
    public MarketBriefing getBriefingDetail(Long id) {
        return marketBriefingMapper.selectById(id);
    }

    /**
     * 获取最近简报
     */
    public List<MarketBriefing> getRecentBriefings(int limit) {
        LambdaQueryWrapper<MarketBriefing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketBriefing::getStatus, 1);
        wrapper.orderByDesc(MarketBriefing::getReportDate);
        wrapper.last("LIMIT " + limit);
        
        return marketBriefingMapper.selectList(wrapper);
    }

    /**
     * 删除简报
     */
    public boolean deleteBriefing(Long id) {
        return marketBriefingMapper.deleteById(id) > 0;
    }

    /**
     * 发布简报
     */
    public boolean publishBriefing(Long id) {
        MarketBriefing briefing = marketBriefingMapper.selectById(id);
        if (briefing != null) {
            briefing.setStatus(1);
            briefing.setPublishTime(LocalDateTime.now());
            return marketBriefingMapper.updateById(briefing) > 0;
        }
        return false;
    }

    /**
     * 收集价格数据
     */
    private Map<String, Object> collectPriceData() {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 获取今日和昨日价格
        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PriceMonitor::getRecordDate, yesterday);
        List<PriceMonitor> prices = priceMonitorMapper.selectList(wrapper);
        
        List<PriceMonitor> todayPrices = new ArrayList<>();
        List<PriceMonitor> yesterdayPrices = new ArrayList<>();
        
        for (PriceMonitor p : prices) {
            if (p.getRecordDate().equals(today)) {
                todayPrices.add(p);
            } else if (p.getRecordDate().equals(yesterday)) {
                yesterdayPrices.add(p);
            }
        }
        
        // 按分类汇总
        Map<Integer, List<PriceMonitor>> byCategory = todayPrices.stream()
                .collect(java.util.stream.Collectors.groupingBy(PriceMonitor::getCategory));
        
        List<Map<String, Object>> categorySummary = new ArrayList<>();
        Map<Integer, String> categoryNames = Map.of(
                1, "茉莉鲜花",
                2, "茉莉花茶",
                3, "茉莉盆栽",
                4, "茉莉花苗"
        );
        
        for (Map.Entry<Integer, List<PriceMonitor>> entry : byCategory.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("category", entry.getKey());
            cat.put("name", categoryNames.getOrDefault(entry.getKey(), "其他"));
            
            // 计算均价（使用最高价和最低价的平均值）
            BigDecimal sum = entry.getValue().stream()
                    .map(p -> p.getMaxPrice().add(p.getMinPrice()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            cat.put("avgPrice", avg);
            cat.put("count", entry.getValue().size());
            
            // 查找昨日价格对比
            BigDecimal yesterdayAvg = BigDecimal.ZERO;
            for (PriceMonitor yp : yesterdayPrices) {
                if (yp.getCategory().equals(entry.getKey())) {
                    yesterdayAvg = yp.getMaxPrice();
                    break;
                }
            }
            
            if (yesterdayAvg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = avg.subtract(yesterdayAvg)
                        .divide(yesterdayAvg, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                cat.put("change", change.setScale(2, RoundingMode.HALF_UP));
            }
            
            categorySummary.add(cat);
        }
        
        result.put("categories", categorySummary);
        result.put("totalMarkets", todayPrices.stream().map(PriceMonitor::getMarket).distinct().count());
        result.put("totalRecords", todayPrices.size());
        
        return result;
    }

    /**
     * 生成价格分析
     */
    private String generatePriceAnalysis(Map<String, Object> priceData, Map<String, Object> weatherData) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("【今日市场概况】\n");
        
        List<Map<String, Object>> categories = (List<Map<String, Object>>) priceData.get("categories");
        if (categories != null && !categories.isEmpty()) {
            analysis.append("今日各品类价格稳定，");
            
            for (Map<String, Object> cat : categories) {
                String name = (String) cat.get("name");
                BigDecimal avgPrice = (BigDecimal) cat.get("avgPrice");
                BigDecimal change = (BigDecimal) cat.get("change");
                
                if (change != null && change.compareTo(BigDecimal.ZERO) != 0) {
                    String direction = change.compareTo(BigDecimal.ZERO) >= 0 ? "上涨" : "下跌";
                    analysis.append(name).append("均价").append(avgPrice).append("元，较昨日").append(direction)
                            .append(Math.abs(change.doubleValue())).append("%；");
                }
            }
        }
        
        // 天气影响
        if (weatherData != null && !weatherData.isEmpty()) {
            String weather = (String) weatherData.get("weather");
            Double temp = (Double) weatherData.get("temp");
            Integer humidity = (Integer) weatherData.get("humidity");
            
            analysis.append("\n\n【天气影响】\n");
            analysis.append("今日天气").append(weather).append("，气温").append(temp).append("℃，湿度").append(humidity).append("%。");
            
            if (temp != null && (temp > 30 || temp < 15)) {
                analysis.append("气温").append(temp > 30 ? "偏高" : "偏低").append("，对茉莉花采摘有一定影响。");
            }
        }
        
        return analysis.toString();
    }

    /**
     * 生成趋势预测
     */
    private String generateForecast(Map<String, Object> priceData, Map<String, Object> weatherData, Map<String, Object> forecastData) {
        StringBuilder forecast = new StringBuilder();
        
        forecast.append("【短期趋势预测】\n");
        
        // 分析近期走势
        List<Map<String, Object>> categories = (List<Map<String, Object>>) priceData.get("categories");
        if (categories != null && !categories.isEmpty()) {
            for (Map<String, Object> cat : categories) {
                BigDecimal change = (BigDecimal) cat.get("change");
                if (change != null) {
                    String name = (String) cat.get("name");
                    if (change.compareTo(new BigDecimal("5")) > 0) {
                        forecast.append(name).append("价格持续上涨，预计短期内仍有上升空间；");
                    } else if (change.compareTo(new BigDecimal("-5")) < 0) {
                        forecast.append(name).append("价格有所回落，建议关注市场供需变化；");
                    }
                }
            }
        }
        
        // 天气预报影响
        if (forecastData != null && forecastData.containsKey("daily")) {
            forecast.append("\n\n【天气预报】\n");
            forecast.append("未来几天天气情况可能影响采摘进度，建议关注天气预报合理安排交易。");
        }
        
        return forecast.toString();
    }

    /**
     * 生成建议
     */
    private String generateSuggestions(Map<String, Object> priceData, Map<String, Object> weatherData) {
        StringBuilder suggestions = new StringBuilder();
        
        suggestions.append("【交易建议】\n");
        suggestions.append("1. 密切关注天气变化，合理安排采摘和交易时间；\n");
        suggestions.append("2. 价格波动期间，建议分批交易，降低风险；\n");
        suggestions.append("3. 注意储存条件，特别是高温天气下的保鲜措施；\n");
        suggestions.append("4. 建议与长期合作商户保持沟通，获取更稳定的销售渠道。\n");
        
        return suggestions.toString();
    }

    /**
     * 生成简报摘要
     */
    private String generateSummary(Map<String, Object> priceData, Map<String, Object> weatherData) {
        String summary = "今日横州茉莉花市场整体平稳，";
        
        List<Map<String, Object>> categories = (List<Map<String, Object>>) priceData.get("categories");
        if (categories != null && !categories.isEmpty()) {
            summary += "主要交易品种包括";
            for (int i = 0; i < categories.size(); i++) {
                Map<String, Object> cat = categories.get(i);
                summary += cat.get("name");
                if (i < categories.size() - 1) {
                    summary += "、";
                }
            }
            summary += "。";
        }
        
        if (weatherData != null) {
            String weather = (String) weatherData.get("weather");
            if (weather != null) {
                summary += "当前天气" + weather + "，";
            }
        }
        
        summary += "建议各位商户根据实际情况合理安排交易。";
        
        return summary;
    }
}