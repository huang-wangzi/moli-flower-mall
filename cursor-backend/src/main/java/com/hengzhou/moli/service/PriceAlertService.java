package com.hengzhou.moli.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.PriceAlert;
import com.hengzhou.moli.entity.PriceMonitor;
import com.hengzhou.moli.mapper.PriceAlertMapper;
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
 * 价格预警服务类
 * 根据天气和价格数据智能分析，生成价格风险预警
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertService extends ServiceImpl<PriceAlertMapper, PriceAlert> {

    private final PriceAlertMapper priceAlertMapper;
    private final PriceMonitorMapper priceMonitorMapper;
    private final WeatherService weatherService;

    /**
     * 智能分析并生成价格预警
     * 根据天气数据和价格波动自动生成预警
     * @return 生成的预警列表
     */
    public List<PriceAlert> analyzeAndGenerateAlerts() {
        List<PriceAlert> alerts = new ArrayList<>();
        
        try {
            // 获取天气数据
            Map<String, Object> weather = weatherService.getHengzhouWeather();
            Map<String, Object> forecast = weatherService.getHengzhouForecast();
            
            // 获取价格数据
            List<PriceMonitor> latestPrices = getLatestPrices();
            List<PriceMonitor> yesterdayPrices = getYesterdayPrices();
            
            // 天气影响分析
            Map<String, Object> weatherAnalysis = analyzeWeatherImpact(weather, forecast);
            
            // 价格波动检测
            for (PriceMonitor latest : latestPrices) {
                PriceMonitor yesterday = findYesterdayPrice(yesterdayPrices, latest.getMarket(), latest.getCategory());
                
                if (yesterday != null) {
                    // 计算涨跌幅（使用最高价）
                    BigDecimal changeRate = calculateChangeRate(latest.getMaxPrice(), yesterday.getMaxPrice());
                    
                    // 价格波动预警
                    if (changeRate.abs().compareTo(new BigDecimal("10")) >= 0) {
                        PriceAlert alert = createPriceAlert(latest, yesterday, changeRate, weather, weatherAnalysis);
                        alerts.add(alert);
                        save(alert);
                    }
                }
            }
            
            // 天气影响预警
            if (!Boolean.TRUE.equals(weatherAnalysis.get("suitable"))) {
                Integer score = (Integer) weatherAnalysis.get("score");
                if (score != null && score < 60) {
                    PriceAlert weatherAlert = createWeatherAlert(weather, weatherAnalysis);
                    alerts.add(weatherAlert);
                    save(weatherAlert);
                }
            }
            
        } catch (Exception e) {
            log.error("分析生成预警失败", e);
        }
        
        return alerts;
    }

    /**
     * 获取所有预警列表（分页）
     */
    public Page<PriceAlert> getAlertPage(Integer pageNum, Integer pageSize, Integer alertType, Integer alertLevel, Integer status) {
        Page<PriceAlert> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
        
        if (alertType != null) {
            wrapper.eq(PriceAlert::getAlertType, alertType);
        }
        if (alertLevel != null) {
            wrapper.eq(PriceAlert::getAlertLevel, alertLevel);
        }
        if (status != null) {
            wrapper.eq(PriceAlert::getStatus, status);
        }
        
        wrapper.orderByDesc(PriceAlert::getAlertTime);
        
        return page(page, wrapper);
    }

    /**
     * 获取未处理的预警数量
     */
    public long getUnreadCount() {
        LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceAlert::getStatus, 0);
        return count(wrapper);
    }

    /**
     * 获取预警统计
     */
    public Map<String, Object> getAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总预警数
        stats.put("total", count());
        
        // 未读数
        stats.put("unread", getUnreadCount());
        
        // 按类型统计
        Map<Integer, Long> byType = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PriceAlert::getAlertType, i);
            byType.put(i, count(wrapper));
        }
        stats.put("byType", byType);
        
        // 按级别统计
        Map<Integer, Long> byLevel = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PriceAlert::getAlertLevel, i);
            byLevel.put(i, count(wrapper));
        }
        stats.put("byLevel", byLevel);
        
        return stats;
    }

    /**
     * 标记预警已读
     */
    public boolean markAsRead(Long id) {
        PriceAlert alert = getById(id);
        if (alert != null) {
            alert.setStatus(1);
            alert.setHandleTime(LocalDateTime.now());
            return updateById(alert);
        }
        return false;
    }

    /**
     * 标记预警已处理
     */
    public boolean markAsHandled(Long id, Long handleBy, String remark) {
        PriceAlert alert = getById(id);
        if (alert != null) {
            alert.setStatus(2);
            alert.setHandleBy(handleBy);
            alert.setHandleRemark(remark);
            alert.setHandleTime(LocalDateTime.now());
            return updateById(alert);
        }
        return false;
    }

    /**
     * 删除预警
     */
    public boolean deleteAlert(Long id) {
        return removeById(id);
    }

    /**
     * 获取最新价格数据
     */
    private List<PriceMonitor> getLatestPrices() {
        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PriceMonitor::getRecordDate);
        
        List<PriceMonitor> all = priceMonitorMapper.selectList(wrapper);
        
        // 按市场和分类分组，取每组最新
        return all.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getMarket() + "-" + p.getCategory(),
                        java.util.stream.Collectors.toList()))
                .values()
                .stream()
                .map(list -> list.get(0))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取昨天的价格数据
     */
    private List<PriceMonitor> getYesterdayPrices() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        LambdaQueryWrapper<PriceMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceMonitor::getRecordDate, yesterday);
        
        return priceMonitorMapper.selectList(wrapper);
    }

    /**
     * 查找对应市场分类的昨天价格
     */
    private PriceMonitor findYesterdayPrice(List<PriceMonitor> yesterdayPrices, String market, Integer category) {
        return yesterdayPrices.stream()
                .filter(p -> p.getMarket().equals(market) && p.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    /**
     * 计算涨跌幅
     */
    private BigDecimal calculateChangeRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 分析天气对茉莉花价格的影响
     */
    private Map<String, Object> analyzeWeatherImpact(Map<String, Object> weather, Map<String, Object> forecast) {
        Map<String, Object> analysis = new HashMap<>();
        List<String> impacts = new ArrayList<>();
        String level = "normal";
        
        if (weather == null || weather.isEmpty()) {
            analysis.put("suitable", true);
            analysis.put("impact", "无天气数据");
            return analysis;
        }
        
        double temp = (Double) weather.getOrDefault("temp", 25.0);
        int humidity = (Integer) weather.getOrDefault("humidity", 60);
        double precipitation = (Double) weather.getOrDefault("precipitation", 0.0);
        
        // 温度影响
        if (temp > 35) {
            impacts.add("高温（" + temp + "℃）将导致茉莉花产量下降，价格可能上涨");
            level = "high";
        } else if (temp < 10) {
            impacts.add("低温（" + temp + "℃）影响茉莉花生长，价格可能波动");
            level = "high";
        } else if (temp > 30 || temp < 15) {
            impacts.add("温度（" + temp + "℃）偏差，可能影响产量");
            if (level.equals("normal")) level = "medium";
        }
        
        // 降雨影响
        if (precipitation > 10) {
            impacts.add("大雨/暴雨可能导致采摘困难，供应减少，价格上涨");
            level = "high";
        } else if (precipitation > 5) {
            impacts.add("中雨可能影响采摘进度");
            if (level.equals("normal")) level = "medium";
        }
        
        // 湿度影响
        if (humidity > 85) {
            impacts.add("高湿度（" + humidity + "%）易引发病虫害，影响品质");
            if (level.equals("normal")) level = "medium";
        } else if (humidity < 40) {
            impacts.add("干燥天气（湿度" + humidity + "%）可能影响花苞形成");
            if (level.equals("normal")) level = "medium";
        }
        
        // 天气预报影响
        if (forecast != null && forecast.containsKey("daily")) {
            JSONArray daily = (JSONArray) forecast.get("daily");
            if (daily != null && daily.size() > 0) {
                JSONObject tomorrow = (JSONObject) daily.get(1);
                if (tomorrow != null) {
                    double tomorrowPrecip = tomorrow.getDoubleValue("precipitude");
                    if (tomorrowPrecip > 10) {
                        impacts.add("明日预报有强降雨，建议提前采摘");
                        level = "high";
                    }
                }
            }
        }
        
        boolean suitable = level.equals("normal");
        analysis.put("suitable", suitable);
        analysis.put("level", level);
        analysis.put("impact", String.join("；", impacts));
        
        return analysis;
    }

    /**
     * 创建价格预警
     */
    private PriceAlert createPriceAlert(PriceMonitor latest, PriceMonitor yesterday, 
            BigDecimal changeRate, Map<String, Object> weather, Map<String, Object> weatherAnalysis) {
        PriceAlert alert = new PriceAlert();
        
        // 判断是涨还是跌
        boolean isUp = changeRate.compareTo(BigDecimal.ZERO) >= 0;
        
        alert.setAlertType(isUp ? 1 : 2); // 1-上涨 2-下跌
        alert.setAlertLevel(determineAlertLevel(changeRate.abs()));
        alert.setTitle((isUp ? "价格上涨" : "价格下跌") + "预警：" + getCategoryName(latest.getCategory()));
        alert.setCategory(latest.getCategory());
        alert.setMarket(latest.getMarket());
        alert.setCurrentPrice(latest.getMaxPrice());
        alert.setTriggerPrice(yesterday.getMaxPrice());
        alert.setChangeRate(changeRate);
        
        // 生成内容
        StringBuilder content = new StringBuilder();
        content.append(getCategoryName(latest.getCategory())).append("在")
                .append(latest.getMarket()).append("的价格区间为")
                .append(yesterday.getMinPrice()).append("-").append(latest.getMaxPrice()).append(latest.getUnit())
                .append("，较昨日");
        
        if (isUp) {
            content.append("上涨");
        } else {
            content.append("下跌");
        }
        content.append(changeRate.abs().setScale(2, RoundingMode.HALF_UP)).append("%");
        
        // 添加天气影响
        if (weatherAnalysis != null && weatherAnalysis.containsKey("impact")) {
            String impact = (String) weatherAnalysis.get("impact");
            if (impact != null && !impact.isEmpty()) {
                content.append("。当前天气：").append(impact);
            }
        }
        
        alert.setContent(content.toString());
        alert.setWeatherImpact((String) weatherAnalysis.get("impact"));
        alert.setAnalysis("价格波动可能受天气、市场供需等因素影响。");
        alert.setSuggestion(generateSuggestion(changeRate, weather));
        
        alert.setStatus(0);
        alert.setAlertTime(LocalDateTime.now());
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        
        return alert;
    }

    /**
     * 创建天气影响预警
     */
    private PriceAlert createWeatherAlert(Map<String, Object> weather, Map<String, Object> analysis) {
        PriceAlert alert = new PriceAlert();
        
        alert.setAlertType(3); // 天气影响
        alert.setAlertLevel(2); // 警告级别
        alert.setTitle("天气影响预警：茉莉花种植条件较差");
        alert.setWeatherImpact((String) analysis.get("impact"));
        alert.setAnalysis("当前天气条件不适合茉莉花生长，可能导致产量下降。");
        alert.setSuggestion((String) analysis.get("suggestions"));
        alert.setStatus(0);
        alert.setAlertTime(LocalDateTime.now());
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        
        return alert;
    }

    /**
     * 确定预警级别
     */
    private Integer determineAlertLevel(BigDecimal changeRate) {
        if (changeRate.compareTo(new BigDecimal("20")) >= 0) {
            return 3; // 紧急
        } else if (changeRate.compareTo(new BigDecimal("10")) >= 0) {
            return 2; // 警告
        } else {
            return 1; // 提示
        }
    }

    /**
     * 生成建议
     */
    private String generateSuggestion(BigDecimal changeRate, Map<String, Object> weather) {
        StringBuilder suggestion = new StringBuilder();
        
        if (changeRate.compareTo(BigDecimal.ZERO) >= 0) {
            // 价格上涨
            suggestion.append("1. 价格上涨，建议适时出售手中的存货；");
            suggestion.append("2. 可适当增加采购，但需关注价格回调风险；");
        } else {
            // 价格下跌
            suggestion.append("1. 价格下跌，建议观望市场走势；");
            suggestion.append("2. 如有库存，可适当持有等待价格回升；");
        }
        
        // 添加天气相关建议
        if (weather != null) {
            double temp = (Double) weather.getOrDefault("temp", 25.0);
            if (temp > 30) {
                suggestion.append("3. 高温天气，请做好遮阳降温措施；");
            } else if (temp < 15) {
                suggestion.append("3. 低温天气，请做好防寒保温措施；");
            }
        }
        
        suggestion.append("4. 请持续关注天气预报和市场动态。");
        
        return suggestion.toString();
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(Integer category) {
        Map<Integer, String> names = Map.of(
                1, "茉莉鲜花",
                2, "茉莉花茶",
                3, "茉莉盆栽",
                4, "茉莉花苗"
        );
        return names.getOrDefault(category, "未知");
    }

    /**
     * 获取最近N条预警
     */
    public List<PriceAlert> getRecentAlerts(int limit) {
        LambdaQueryWrapper<PriceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PriceAlert::getAlertTime);
        wrapper.last("LIMIT " + limit);
        
        return list(wrapper);
    }
}