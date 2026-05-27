package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 天气控制器
 * 负责处理天气相关的所有HTTP请求，调用和风天气API获取横州市的实时天气、预报、空气质量等数据
 * 专属HOST: https://nh5ctvwh4a.re.qweatherapi.com
 * 提供给前端茉莉花种植区的精准天气预报服务
 */
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "天气监测", description = "横州茉莉花种植区天气监测相关接口")
public class WeatherController {

    /**
     * 天气服务：调用和风天气API获取天气数据
     */
    private final WeatherService weatherService;

    /**
     * 获取横州实时天气
     * 返回当前时刻的温度、湿度、风力、天气状况等实时数据
     * 接口: GET /weather/now
     * @return 实时天气数据Map，包含温度、湿度、风速、天气描述等字段
     */
    @GetMapping("/now")
    @Operation(summary = "获取实时天气", description = "获取横州市当前实时天气数据")
    public Result<Map<String, Object>> getNowWeather() {
        try {
            log.info("[天气接口] 请求实时天气");
            Map<String, Object> weather = weatherService.getHengzhouWeather();
            log.info("[天气接口] 实时天气获取成功: temp={}", weather.get("temp"));
            return Result.success(weather);
        } catch (Exception e) {
            log.error("[天气接口] 获取实时天气失败: {}", e.getMessage(), e);
            // 返回兜底数据，永远不报错，确保前端不会因天气接口异常而崩溃
            return Result.success(weatherService.getHengzhouWeather());
        }
    }

    /**
     * 获取横州天气预报
     * 返回未来3天的天气预报数据
     * 接口: GET /weather/forecast 或 /weather/3d
     * @return 天气预报Map，包含daily每日预报数组
     */
    @GetMapping({"/forecast", "/3d"})
    @Operation(summary = "获取天气预报", description = "获取横州市未来3天天气预报")
    public Result<Map<String, Object>> getForecast() {
        try {
            log.info("[天气接口] 请求天气预报");
            Map<String, Object> forecast = weatherService.getHengzhouForecast();
            log.info("[天气接口] 天气预报获取成功");
            return Result.success(forecast);
        } catch (Exception e) {
            log.error("[天气接口] 获取天气预报失败: {}", e.getMessage(), e);
            return Result.success(weatherService.getHengzhouForecast());
        }
    }

    /**
     * 获取空气质量
     * 返回当前空气质量的AQI、PM2.5、PM10等指标数据
     * 接口: GET /weather/air
     * @return 空气质量数据Map，包含aqi空气质量指数、category空气质量等级等
     */
    @GetMapping("/air")
    @Operation(summary = "获取空气质量", description = "获取横州市当前空气质量数据")
    public Result<Map<String, Object>> getAirQuality() {
        try {
            log.info("[天气接口] 请求空气质量");
            Map<String, Object> air = weatherService.getAirQuality();
            log.info("[天气接口] 空气质量获取成功: aqi={}", air.get("aqi"));
            return Result.success(air);
        } catch (Exception e) {
            log.error("[天气接口] 获取空气质量失败: {}", e.getMessage(), e);
            return Result.success(weatherService.getAirQuality());
        }
    }

    /**
     * 获取综合天气信息
     * 整合实时天气、天气预报、空气质量、种植建议等多项数据
     * 接口: GET /weather/comprehensive
     * @return 综合天气信息Map，包含now当前天气、forecast预报、air空气质量、impact种植影响分析
     */
    @GetMapping("/comprehensive")
    @Operation(summary = "获取综合天气信息", description = "获取横州市综合天气信息（含种植建议）")
    public Result<Map<String, Object>> getComprehensiveWeather() {
        try {
            log.info("[天气接口] 请求综合天气");
            Map<String, Object> comprehensive = weatherService.getComprehensiveWeather();
            log.info("[天气接口] 综合天气获取成功");
            return Result.success(comprehensive);
        } catch (Exception e) {
            log.error("[天气接口] 获取综合天气失败: {}", e.getMessage(), e);
            return Result.success(weatherService.getComprehensiveWeather());
        }
    }

    /**
     * 获取历史天气数据
     * 按日期范围查询横州市的历史天气记录
     * 接口: GET /weather/history
     * @param startDate 查询开始日期，格式：yyyy-MM-dd
     * @param endDate 查询结束日期，格式：yyyy-MM-dd
     * @return 历史天气数据列表
     */
    @GetMapping("/history")
    @Operation(summary = "获取历史天气", description = "获取横州市历史天气数据")
    public Result<Map<String, Object>> getHistory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            log.info("[天气接口] 请求历史天气: {} ~ {}", startDate, endDate);
            Map<String, Object> history = weatherService.getHistoricalWeather(startDate.toString(), endDate.toString());
            return Result.success(history);
        } catch (Exception e) {
            log.error("[天气接口] 获取历史天气失败: {}", e.getMessage(), e);
            return Result.success(java.util.Map.of("list", java.util.Collections.emptyList(), "total", 0));
        }
    }
}