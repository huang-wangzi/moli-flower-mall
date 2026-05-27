package com.hengzhou.moli.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.WeatherData;
import com.hengzhou.moli.mapper.WeatherDataMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 天气服务 - 调用和风天气API
 * 专属HOST: https://nh5ctvwh4a.re.qweatherapi.com
 */
@Service
@Slf4j
public class WeatherService extends ServiceImpl<WeatherDataMapper, WeatherData> {

    private final WeatherDataMapper weatherDataMapper;

    // 和风天气专属HOST（用户提供的）
    private static final String HEFENG_HOST = "https://nh5ctvwh4a.re.qweatherapi.com";

    // 和风天气API配置
    @Value("${weather.api-key:***WEATHER_API_KEY_REMOVED***}")
    private String apiKey;

    @Value("${weather.location-id:101300301}")
    private String locationId;

    @Value("${weather.location-name:横州}")
    private String locationName;

    // 请求超时（毫秒）
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    public WeatherService(WeatherDataMapper weatherDataMapper) {
        this.weatherDataMapper = weatherDataMapper;
    }

    @PostConstruct
    public void init() {
        log.info("========== 天气服务初始化 ==========");
        log.info("[天气服务] 专属HOST: {}", HEFENG_HOST);
        log.info("[天气服务] Location ID: {}", locationId);
        log.info("[天气服务] 地区: {}", locationName);
        log.info("[天气服务] API KEY: {}...", apiKey.substring(0, Math.min(8, apiKey.length())));
        log.info("====================================");
    }

    /**
     * 获取横州实时天气
     * 接口: /v7/weather/now
     */
    @Cacheable(value = "weather", key = "'now'")
    public Map<String, Object> getHengzhouWeather() {
        try {
            String url = HEFENG_HOST + "/v7/weather/now?location=" + locationId + "&key=" + apiKey + "&lang=zh";
            log.info("[天气API] 请求URL: {}", url);
            String response = httpGet(url);
            if (response == null || response.isBlank()) {
                log.warn("[天气API] 返回空响应，使用兜底数据");
                return getFallbackWeather();}
            log.debug("[天气API] 原始响应: {}", response);
            JSONObject json = JSON.parseObject(response);
            String code = json.getString("code");
            if (!"200".equals(code)) {
                log.warn("[天气API] 返回错误码: {}, message: {}", code, json.getString("message"));
                return getFallbackWeather();}
            JSONObject now = json.getJSONObject("now");
            if (now == null) {
                log.warn("[天气API] 天气数据为空，使用兜底数据");
                return getFallbackWeather();}
            Map<String, Object> weather = new HashMap<>();
            weather.put("temp", now.getDoubleValue("temp"));
            weather.put("feelsLike", now.getDoubleValue("feelsLike"));
            weather.put("text", now.getString("text"));
            weather.put("windDir", now.getString("windDir"));
            weather.put("windSpeed", now.getDoubleValue("windSpeed"));
            weather.put("humidity", now.getInteger("humidity"));
            weather.put("precip", now.getDoubleValue("precip"));
            weather.put("pressure", now.getInteger("pressure"));
            weather.put("vis", now.getDoubleValue("vis"));
            weather.put("cloud", now.getInteger("cloud"));
            weather.put("dew", now.getDoubleValue("dew"));
            weather.put("updateTime", json.getString("updateTime"));
            weather.put("source", "和风天气");
            weather.put("location", locationName);
            saveWeatherData(weather); // 保存到数据库
            return weather;
        } catch (Exception e) {
            log.error("[天气API] 获取天气异常: {}", e.getMessage(), e);
            return getFallbackWeather();}}

    /**
     * 获取横州3天天气预报
     * 接口: /v7/weather/3d
     */
    @Cacheable(value = "weather", key = "'forecast'")
    public Map<String, Object> getHengzhouForecast() {
        try {
            String url = HEFENG_HOST + "/v7/weather/3d?location=" + locationId + "&key=" + apiKey + "&lang=zh";
            log.info("[预报API] 请求URL: {}", url);

            String response = httpGet(url);
            if (response == null || response.isBlank()) {
                log.warn("[预报API] 返回空响应，使用兜底数据");
                return getFallbackForecast();
            }

            log.debug("[预报API] 原始响应: {}", response);
            JSONObject json = JSON.parseObject(response);

            if (!"200".equals(json.getString("code"))) {
                log.warn("[预报API] 返回错误码: {}", json.getString("code"));
                return getFallbackForecast();
            }

            JSONArray daily = json.getJSONArray("daily");
            Map<String, Object> result = new HashMap<>();
            result.put("daily", daily);
            result.put("updateTime", json.getString("updateTime"));
            result.put("source", "和风天气");
            result.put("location", locationName);

            return result;

        } catch (Exception e) {
            log.error("[预报API] 获取预报异常: {}", e.getMessage(), e);
            return getFallbackForecast();
        }
    }

    /**
     * 获取横州空气质量
     * 接口: /v7/air/now
     */
    @Cacheable(value = "weather", key = "'air'")
    public Map<String, Object> getAirQuality() {
        try {
            String url = HEFENG_HOST + "/v7/air/now?location=" + locationId + "&key=" + apiKey + "&lang=zh";
            log.info("[空气API] 请求URL: {}", url);

            String response = httpGet(url);
            if (response == null || response.isBlank()) {
                log.warn("[空气API] 返回空响应，使用兜底数据");
                return getFallbackAir();
            }

            log.debug("[空气API] 原始响应: {}", response);
            JSONObject json = JSON.parseObject(response);

            if (!"200".equals(json.getString("code"))) {
                log.warn("[空气API] 返回错误码: {}", json.getString("code"));
                return getFallbackAir();
            }

            JSONObject now = json.getJSONObject("now");
            Map<String, Object> air = new HashMap<>();
            air.put("aqi", now.getInteger("aqi"));
            air.put("level", now.getString("level"));
            air.put("category", now.getString("category"));
            air.put("pm2p5", now.getInteger("pm2p5"));
            air.put("pm10", now.getInteger("pm10"));
            air.put("so2", now.getInteger("so2"));
            air.put("no2", now.getInteger("no2"));
            air.put("co", now.getDouble("co"));
            air.put("o3", now.getInteger("o3"));
            air.put("source", "和风天气");

            return air;

        } catch (Exception e) {
            log.error("[空气API] 获取空气质量异常: {}", e.getMessage(), e);
            return getFallbackAir();
        }
    }

    /**
     * 获取综合天气信息（含种植建议）
     */
    @Cacheable(value = "weather", key = "'comprehensive'")
    public Map<String, Object> getComprehensiveWeather() {
        Map<String, Object> result = new HashMap<>();
        result.put("now", getHengzhouWeather());
        result.put("forecast", getHengzhouForecast());
        result.put("air", getAirQuality());
        result.put("impact", analyzeWeatherImpact(result));
        result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    /**
     * HTTP GET请求（支持Gzip解压）
     */
    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            log.warn("[HTTP] 响应码: {}", responseCode);
            return null;
        }

        String encoding = conn.getContentEncoding();
        StringBuilder response = new StringBuilder();

        java.io.InputStream inputStream = conn.getInputStream();
        BufferedReader reader;
        if ("gzip".equalsIgnoreCase(encoding)) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        try {
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                response.append(buffer, 0, len);
            }
        } finally {
            reader.close();
        }

        return response.toString();
    }

    // ==================== 存储到数据库 ====================

    private Map<String, Object> analyzeWeatherImpact(Map<String, Object> weatherData) {
        Map<String, Object> impact = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> now = (Map<String, Object>) weatherData.get("now");
            if (now == null) {
                return getDefaultImpact();
            }

            double temp = getDoubleValue(now.get("temp"));
            int humidity = getIntValue(now.get("humidity"));
            double precip = getDoubleValue(now.get("precip"));
            double windSpeed = getDoubleValue(now.get("windSpeed"));
            String weatherText = now.get("text") != null ? now.get("text").toString() : "";

            StringBuilder analysis = new StringBuilder();
            StringBuilder suggestions = new StringBuilder();
            StringBuilder pricePrediction = new StringBuilder();
            int score = 100;
            boolean suitable = true;

            // ========== 温度分析 ==========
            if (temp < 10) {
                analysis.append("气温过低，需做好防寒措施。");
                score -= 30;
                suitable = false;
                pricePrediction.append("低温天气，茉莉花生长缓慢，采摘量减少，价格预计偏高。");
            } else if (temp < 15) {
                analysis.append("气温偏低，茉莉花生长较慢。");
                score -= 15;
                pricePrediction.append("气温偏低，花苞发育较慢，市场供应一般。");
            } else if (temp > 35) {
                analysis.append("气温过高，需注意遮阳降温。");
                score -= 25;
                suitable = false;
                pricePrediction.append("高温天气易导致花朵早衰，采摘后存储困难，价格可能波动。");
            } else if (temp > 30) {
                analysis.append("气温偏高，需适当浇水降温。");
                score -= 10;
                pricePrediction.append("气温偏高，茉莉花香浓，但需注意及时采摘。");
            } else {
                analysis.append("气温适宜，适合茉莉花生长。");
                pricePrediction.append("温度适宜，茉莉花品质优良，采摘活跃。");
            }

            // ========== 湿度分析 ==========
            if (humidity < 40) {
                analysis.append("空气干燥，需增加浇水频率。");
                score -= 15;
                pricePrediction.append("空气干燥，花朵易失水，影响品质，价格可能受影响。");
            } else if (humidity > 85) {
                analysis.append("空气潮湿，易引发病虫害，需加强通风。");
                score -= 20;
                pricePrediction.append("高湿环境易引发病虫害，茉莉花品质下降，价格可能偏低。");
            } else if (humidity < 60) {
                analysis.append("空气湿度偏低，需适当补水。");
                score -= 5;
            } else {
                analysis.append("湿度适宜。");
            }

            // ========== 降雨分析 ==========
            if (precip > 10) {
                analysis.append("降雨量大，需及时排水。");
                score -= 25;
                suitable = false;
                pricePrediction.append("强降雨严重阻碍采摘，供应骤降，但需求仍在，价格可能大幅上涨！");
            } else if (precip > 5) {
                analysis.append("有中雨，需注意防涝。");
                score -= 10;
                pricePrediction.append("中雨天气，采摘受阻，市场供应减少，价格预计上涨。");
            } else if (precip > 0) {
                analysis.append("有小雨，对茉莉花生长有利。");
                pricePrediction.append("小雨天气对茉莉花生长有利，但采摘略有影响。");
            }

            // ========== 风速分析（新增） ==========
            if (windSpeed > 40) {
                analysis.append("风速过大，可能折断花枝，请做好防护！");
                score -= 25;
                suitable = false;
                pricePrediction.append("大风天气可能造成花枝折断损毁，产量受损严重，价格可能大幅上涨！");
            } else if (windSpeed > 25) {
                analysis.append("风速较大，需注意花枝防护。");
                score -= 15;
                pricePrediction.append("较大风速增加采摘难度，供应可能减少，价格有上涨压力。");
            } else if (windSpeed > 15) {
                analysis.append("有微风，有利于通风。");
                score -= 5;
            }

            // ========== 天气现象分析 ==========
            if (weatherText.contains("暴雨") || weatherText.contains("雷暴")) {
                analysis.append("极端天气，请做好防护！");
                score -= 30;
                suitable = false;
                pricePrediction.append("暴雨/雷暴天气造成严重损毁，供应锐减，价格预计大幅上涨！");
            } else if (weatherText.contains("阴")) {
                analysis.append("阴天光照不足。");
                score -= 5;
                pricePrediction.append("阴天光照不足，茉莉花香可能略淡，但整体影响不大。");
            } else if (weatherText.contains("晴")) {
                analysis.append("天气晴朗，利于采摘和晾晒。");
                pricePrediction.append("晴天阳光充足，茉莉花香浓郁，品质优良，价格普遍较高！");
            }

            // ========== 特殊天气影响 ==========
            if (weatherText.contains("雾") || weatherText.contains("霾")) {
                analysis.append("有雾/霾天气，能见度低，注意出行安全。");
                score -= 10;
                pricePrediction.append("雾霾天气影响采摘效率，但茉莉花品质稳定。");
            }
            if (weatherText.contains("冰") || weatherText.contains("霜")) {
                analysis.append("有冰冻/霜冻风险，可能造成严重冻害！");
                score -= 35;
                suitable = false;
                pricePrediction.append("霜冻天气可能造成茉莉花严重冻伤，供应锐减，价格预计暴涨！");
            }

            // ========== 生成种植建议 ==========
            if (temp < 15) {
                suggestions.append("1.覆盖保温膜防寒；2.减少浇水避免冻害；");
            } else if (temp > 30) {
                suggestions.append("1.搭设遮阳网；2.早晚浇水；3.叶面喷水降温；");
            }
            if (humidity < 50) {
                suggestions.append("4.增加浇水频率；5.叶面喷雾增湿；");
            } else if (humidity > 80) {
                suggestions.append("4.加强通风排水；5.喷洒杀菌剂预防病害；");
            }
            if (precip > 5) {
                suggestions.append("6.及时排除田间积水；");
            }
            if (windSpeed > 25) {
                suggestions.append("7.加固花架支架，防止倒伏；");
            }
            if (suggestions.length() == 0) {
                suggestions.append("1.正常养护管理；2.适时采摘；3.注意病虫害防治；");
            }

            // ========== 综合价格走势预测 ==========
            int priceScore = calculatePricePrediction(temp, humidity, precip, windSpeed, weatherText);
            String priceTrend = getPriceTrendDescription(priceScore);

            impact.put("suitable", suitable);
            impact.put("analysis", analysis.toString());
            impact.put("suggestions", suggestions.toString());
            impact.put("pricePrediction", pricePrediction.toString());
            impact.put("priceTrend", priceTrend);
            impact.put("priceScore", priceScore);
            impact.put("score", Math.max(0, score));
            impact.put("level", score >= 80 ? "优" : score >= 60 ? "良" : score >= 40 ? "中" : "差");

        } catch (Exception e) {
            log.warn("[天气影响] 分析异常: {}", e.getMessage());
            return getDefaultImpact();
        }

        return impact;
    }

    /**
     * 计算价格预测指数
     * @return 0-100的分数，>50表示价格偏高，<50表示价格偏低
     */
    private int calculatePricePrediction(double temp, int humidity, double precip, double windSpeed, String weatherText) {
        int score = 50; // 基准分数

        // 温度影响（适宜温度20-30度最优）
        if (temp >= 20 && temp <= 30) {
            score += 10; // 适宜温度，价格偏高
        } else if (temp < 10 || temp > 35) {
            score -= 15; // 极端温度，价格波动大
        } else if (temp < 15 || temp > 30) {
            score -= 5; // 偏冷或偏热
        }

        // 降雨影响（雨天采摘困难，价格通常上涨）
        if (precip > 10) {
            score += 25; // 暴雨严重影响供应
        } else if (precip > 5) {
            score += 15; // 中雨影响较大
        } else if (precip > 0) {
            score += 5; // 小雨略有影响
        }

        // 风速影响
        if (windSpeed > 40) {
            score += 20; // 大风损毁
        } else if (windSpeed > 25) {
            score += 10; // 较大风
        } else if (windSpeed > 15) {
            score += 3; // 微风
        }

        // 天气现象影响
        String text = weatherText.toLowerCase();
        if (text.contains("晴")) {
            score += 10; // 晴天品质好，价格高
        } else if (text.contains("阴")) {
            score -= 3; // 阴天略低
        } else if (text.contains("暴雨") || text.contains("雷暴")) {
            score += 30; // 极端天气
        } else if (text.contains("雾") || text.contains("霾")) {
            score -= 5;
        } else if (text.contains("冰") || text.contains("霜")) {
            score += 35; // 霜冻最严重
        }

        // 湿度影响
        if (humidity > 85) {
            score -= 10; // 高湿易发病害
        } else if (humidity < 40) {
            score -= 5; // 太干燥
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * 获取价格走势描述
     */
    private String getPriceTrendDescription(int priceScore) {
        if (priceScore >= 75) {
            return "↑↑ 大幅上涨";
        } else if (priceScore >= 60) {
            return "↑ 小幅上涨";
        } else if (priceScore >= 40) {
            return "→ 基本平稳";
        } else if (priceScore >= 25) {
            return "↓ 小幅下跌";
        } else {
            return "↓↓ 大幅下跌";
        }
    }

    private double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int getIntValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> getDefaultImpact() {
        Map<String, Object> impact = new HashMap<>();
        impact.put("suitable", true);
        impact.put("analysis", "天气数据获取中...");
        impact.put("suggestions", "请稍后刷新获取最新分析");
        impact.put("pricePrediction", "正在分析天气对价格的影响...");
        impact.put("priceTrend", "分析中");
        impact.put("priceScore", 50);
        impact.put("score", 100);
        impact.put("level", "优");
        return impact;
    }

    // ==================== 兜底数据（API异常时返回真实日期） ====================

    private Map<String, Object> getFallbackWeather() {
        Map<String, Object> weather = new HashMap<>();
        weather.put("temp", 25.0);
        weather.put("feelsLike", 26.0);
        weather.put("text", "多云");
        weather.put("windDir", "东南风");
        weather.put("windSpeed", 2.0);
        weather.put("humidity", 65);
        weather.put("precip", 0.0);
        weather.put("pressure", 1010);
        weather.put("vis", 15.0);
        weather.put("cloud", 30);
        weather.put("dew", 18.0);
        weather.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        weather.put("source", locationName + "（数据获取失败，显示参考值）");
        weather.put("location", locationName);
        weather.put("fallback", true);
        return weather;
    }

    private Map<String, Object> getFallbackForecast() {
        Map<String, Object> result = new HashMap<>();
        JSONArray daily = new JSONArray();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            JSONObject day = new JSONObject();
            day.put("fxDate", today.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            day.put("textDay", "多云");
            day.put("textNight", "多云");
            day.put("tempMin", 20 + i);
            day.put("tempMax", 28 + i);
            day.put("humidity", 65);
            day.put("precip", 0.0);
            day.put("windDirDay", "东南风");
            day.put("windSpeedDay", "2级");
            daily.add(day);
        }

        result.put("daily", daily);
        result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("source", locationName + "（数据获取失败，显示参考值）");
        result.put("location", locationName);
        result.put("fallback", true);
        return result;
    }

    private Map<String, Object> getFallbackAir() {
        Map<String, Object> air = new HashMap<>();
        air.put("aqi", 50);
        air.put("level", "良");
        air.put("category", "良好");
        air.put("pm2p5", 30);
        air.put("pm10", 50);
        air.put("so2", 10);
        air.put("no2", 30);
        air.put("co", 0.5);
        air.put("o3", 80);
        air.put("source", locationName + "（数据获取失败，显示参考值）");
        air.put("fallback", true);
        return air;
    }

    // ==================== 数据库操作 ====================

    private void saveWeatherData(Map<String, Object> weather) {
        try {
            LambdaQueryWrapper<WeatherData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WeatherData::getCityName, locationName);
            WeatherData existing = weatherDataMapper.selectOne(wrapper);

            WeatherData data = new WeatherData();
            data.setCityName(locationName);
            data.setCityCode(locationId);
            data.setTemp(getDoubleValue(weather.get("temp")));
            data.setHumidity(getIntValue(weather.get("humidity")));
            data.setPrecipitation(getDoubleValue(weather.get("precip")));
            data.setPressure(getIntValue(weather.get("pressure")));
            data.setVisibility(getDoubleValue(weather.get("vis")));
            data.setWindSpeed(weather.get("windSpeed") != null ? weather.get("windSpeed").toString() : "0");
            data.setWeather(weather.get("text") != null ? weather.get("text").toString() : "");
            data.setWindDir(weather.get("windDir") != null ? weather.get("windDir").toString() : "");
            data.setSource(weather.get("source") != null ? weather.get("source").toString() : "和风天气");
            data.setUpdateTime(LocalDateTime.now());
            if (existing != null) {
                data.setId(existing.getId());
                data.setCreateTime(existing.getCreateTime());
                weatherDataMapper.updateById(data);
            } else {
                data.setCreateTime(LocalDateTime.now());
                weatherDataMapper.insert(data);
            }
        } catch (Exception e) {
            log.warn("[数据库] 保存天气数据失败: {}", e.getMessage());
        }
    }

    /**
     * 获取历史天气数据
     */
    public Map<String, Object> getHistoricalWeather(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<WeatherData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WeatherData::getCityName, locationName);
            wrapper.orderByDesc(WeatherData::getUpdateTime);
            List<WeatherData> list = weatherDataMapper.selectList(wrapper);
            result.put("list", list);
            result.put("total", list.size());
        } catch (Exception e) {
            log.warn("[历史天气] 获取失败: {}", e.getMessage());
            result.put("list", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }
}
