package com.hengzhou.moli.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 和风天气预报服务 - 真实调用和风天气API
 */
@Service
@Slf4j
public class WeatherForecastService {

    private static final String FORECAST_URL_3D = "https://devapi.qweather.com/v7/weather/3d";
    private static final String FORECAST_URL_7D = "https://devapi.qweather.com/v7/weather/7d";

    private final RestTemplate restTemplate;

    @Value("${weather.api-key:364b89af300649a99d4a19d792c6578a}")
    private String weatherApiKey;

    @Value("${weather.location-id:101300301}")
    private String locationId;

    public WeatherForecastService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 获取3天天气预报
     */
    @Cacheable(value = "weather", key = "'forecast_3d'")
    public List<Map<String, Object>> get3DayForecast() {
        try {
            String url = String.format("%s?location=%s&key=%s&lang=zh",
                FORECAST_URL_3D, locationId, weatherApiKey);
            log.info("调用和风天气3天预报API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            log.info("3天预报响应: {}", response);

            if (response != null) {
                JSONObject json = JSON.parseObject(response);
                if ("200".equals(json.getString("code"))) {
                    JSONArray daily = json.getJSONArray("daily");
                    return parseDailyForecast(daily);
                }
            }
        } catch (Exception e) {
            log.error("获取3天预报异常: {}", e.getMessage());
        }
        return getDefaultForecast();
    }

    /**
     * 获取5天天气预报
     */
    @Cacheable(value = "weather", key = "'forecast_5d'")
    public List<Map<String, Object>> get5DayForecast() {
        try {
            String url = String.format("%s?location=%s&key=%s&lang=zh",
                FORECAST_URL_7D, locationId, weatherApiKey);
            log.info("调用和风天气7天预报API获取5天数据");

            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JSONObject json = JSON.parseObject(response);
                if ("200".equals(json.getString("code"))) {
                    JSONArray daily = json.getJSONArray("daily");
                    List<Map<String, Object>> result = new ArrayList<>();
                    int days = Math.min(5, daily.size());
                    for (int i = 0; i < days; i++) {
                        JSONObject day = daily.getJSONObject(i);
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("date", day.getString("fxDate"));
                        item.put("weatherDay", day.getString("textDay"));
                        item.put("weatherNight", day.getString("textNight"));
                        item.put("tempMin", day.getDouble("tempMin"));
                        item.put("tempMax", day.getDouble("tempMax"));
                        item.put("windDirDay", day.getString("windDirDay"));
                        item.put("windSpeedDay", day.getString("windSpeedDay"));
                        item.put("precipProb", day.getInteger("precipProb"));
                        item.put("humidity", day.getInteger("humidity"));
                        result.add(item);
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("获取5天预报异常: {}", e.getMessage());
        }
        return getDefaultForecast5Day();
    }

    private List<Map<String, Object>> parseDailyForecast(JSONArray daily) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (daily == null || daily.isEmpty()) {
            return getDefaultForecast();
        }
        for (int i = 0; i < daily.size(); i++) {
            JSONObject day = daily.getJSONObject(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day.getString("fxDate"));
            item.put("weatherDay", day.getString("textDay"));
            item.put("weatherNight", day.getString("textNight"));
            item.put("tempMin", day.getDouble("tempMin"));
            item.put("tempMax", day.getDouble("tempMax"));
            item.put("windDirDay", day.getString("windDirDay"));
            item.put("windSpeedDay", day.getString("windSpeedDay"));
            item.put("precipProb", day.getInteger("precipProb"));
            item.put("humidity", day.getInteger("humidity"));
            item.put("sunrise", day.getString("sunrise"));
            item.put("sunset", day.getString("sunset"));
            result.add(item);
        }
        return result;
    }

    public String getFormattedWeatherForAI() {
        List<Map<String, Object>> forecast = get3DayForecast();
        if (forecast == null || forecast.isEmpty()) {
            return "天气数据暂时无法获取，请稍后再试。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("横州市未来3天天气预报：\n");
        for (int i = 0; i < forecast.size(); i++) {
            Map<String, Object> day = forecast.get(i);
            sb.append("\n第").append(i + 1).append("天（").append(day.get("date")).append("）：");
            sb.append(day.get("weatherDay")).append("转").append(day.get("weatherNight"));
            sb.append("，气温").append(day.get("tempMin")).append("℃~").append(day.get("tempMax")).append("℃");
            sb.append("，").append(day.get("windDirDay")).append(day.get("windSpeedDay"));
            sb.append("，降水概率").append(day.get("precipProb")).append("%");
            sb.append("，湿度").append(day.get("humidity")).append("%");
        }
        return sb.toString();
    }

    public String getFormatted5DayWeatherForAI() {
        List<Map<String, Object>> forecast = get5DayForecast();
        if (forecast == null || forecast.isEmpty()) {
            return "天气数据暂时无法获取，请稍后再试。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("横州市未来5天天气预报：\n");
        for (int i = 0; i < forecast.size(); i++) {
            Map<String, Object> day = forecast.get(i);
            sb.append("\n第").append(i + 1).append("天（").append(day.get("date")).append("）：");
            sb.append(day.get("weatherDay")).append("转").append(day.get("weatherNight"));
            sb.append("，气温").append(day.get("tempMin")).append("℃~").append(day.get("tempMax")).append("℃");
            sb.append("，").append(day.get("windDirDay")).append(day.get("windSpeedDay"));
            sb.append("，降水概率").append(day.get("precipProb")).append("%");
            sb.append("，湿度").append(day.get("humidity")).append("%");
        }
        Map<String, Object> analysis = getWeatherAnalysis();
        sb.append("\n\n").append(analysis.get("impactAnalysis"));
        return sb.toString();
    }

    public Map<String, Object> getWeatherAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> forecast = get5DayForecast();
        analysis.put("forecast", forecast);
        StringBuilder impactAnalysis = new StringBuilder();
        int rainDays = 0, hotDays = 0, coldDays = 0;
        for (Map<String, Object> day : forecast) {
            int precipProb = day.get("precipProb") != null ? (Integer) day.get("precipProb") : 0;
            if (precipProb > 50) rainDays++;
            double tempMax = day.get("tempMax") != null ? (Double) day.get("tempMax") : 28;
            double tempMin = day.get("tempMin") != null ? (Double) day.get("tempMin") : 20;
            if (tempMax > 35) hotDays++;
            if (tempMin < 15) coldDays++;
        }
        if (rainDays >= 2) {
            impactAnalysis.append("【降雨预警】未来5天有").append(rainDays).append("天降水概率较高，可能影响茉莉花采摘和运输。\n");
        }
        if (hotDays >= 2) {
            impactAnalysis.append("【高温预警】未来5天有").append(hotDays).append("天高温天气（>35℃），注意遮阳降温。\n");
        }
        if (coldDays >= 2) {
            impactAnalysis.append("【低温预警】未来5天有").append(coldDays).append("天低温天气（<15℃），注意防寒。\n");
        }
        if (impactAnalysis.length() == 0) {
            impactAnalysis.append("【天气良好】未来5天天气总体适宜，无极端天气预警，适合茉莉花采摘和交易。\n");
        }
        analysis.put("impactAnalysis", impactAnalysis.toString());
        analysis.put("updateTime", LocalDateTime.now().toString());
        return analysis;
    }

    private List<Map<String, Object>> getDefaultForecast() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", today.plusDays(i).toString());
            day.put("weatherDay", "多云");
            day.put("weatherNight", "多云");
            day.put("tempMin", 18 + i);
            day.put("tempMax", 26 + i);
            day.put("windDirDay", "东南风");
            day.put("windSpeedDay", "2级");
            day.put("precipProb", 20);
            day.put("humidity", 65);
            day.put("sunrise", "06:30");
            day.put("sunset", "18:45");
            result.add(day);
        }
        return result;
    }

    private List<Map<String, Object>> getDefaultForecast5Day() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String[] weatherTexts = {"多云", "晴", "多云", "小雨", "晴"};
        double[] tempMins = {18, 19, 20, 21, 20};
        double[] tempMaxs = {28, 29, 27, 26, 28};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", today.plusDays(i).toString());
            day.put("weatherDay", weatherTexts[i]);
            day.put("weatherNight", weatherTexts[(i + 1) % weatherTexts.length]);
            day.put("tempMin", tempMins[i]);
            day.put("tempMax", tempMaxs[i]);
            day.put("windDirDay", "东南风");
            day.put("windSpeedDay", "2级");
            day.put("precipProb", i == 3 ? 60 : 20);
            day.put("humidity", 65 + i * 2);
            result.add(day);
        }
        return result;
    }
}
