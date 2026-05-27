package com.hengzhou.moli.service;

import com.hengzhou.moli.utils.DeepSeekApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI市场预警生成服务
 * 整合天气数据、价格数据，调用DeepSeek生成市场预警报告
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiMarketWarningService {

    private final DeepSeekApiClient deepSeekApiClient;
    private final WeatherForecastService weatherForecastService;
    private final MarketPriceService marketPriceService;

    /**
     * 生成完整的市场AI预警报告
     * @return 预警报告结果
     */
    public Map<String, Object> generateMarketWarning() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始生成市场AI预警报告...");
            Map<String, Object> weatherData = getWeatherData();// 1. 获取天气数据
            result.put("weather", weatherData);
            Map<String, Object> priceData = getPriceData();// 2. 获取价格数据
            result.put("price", priceData);
            String prompt = buildWarningPrompt(weatherData, priceData); // 3. 构建prompt并调用AI
            log.info("AI预警Prompt构建完成，开始调用DeepSeek...");
            String aiContent = deepSeekApiClient.generate(prompt);
            result.put("success", true);            // 4. 组装最终结果
            result.put("aiContent", aiContent);
            result.put("generateTime", java.time.LocalDateTime.now().toString());
            result.put("message", "预警报告生成成功");
            log.info("市场AI预警报告生成完成");
        } catch (Exception e) {
            log.error("生成市场预警失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "预警生成失败: " + e.getMessage());
            result.put("aiContent", getFallbackWarning());}
        return result;}
    /*** 获取天气数据* @return 天气数据*/
    private Map<String, Object> getWeatherData() {
        Map<String, Object> weather = new HashMap<>();
        try {// 获取天预报
            weather.put("forecast", weatherForecastService.get5DayForecast());
            weather.put("formatted", weatherForecastService.getFormatted5DayWeatherForAI());
            Map<String, Object> analysis = weatherForecastService.getWeatherAnalysis();// 获取影响分析
            weather.put("impactAnalysis", analysis.get("impactAnalysis"));
        } catch (Exception e) {
            log.error("获取天气数据失败: {}", e.getMessage());
            weather.put("error", "天气数据获取失败");
        }return weather;}
    /*** 获取价格数* @return 价格数据*/
    private Map<String, Object> getPriceData() {
        Map<String, Object> price = new HashMap<>();
        try {// 获取茉莉鲜花价格概览
            Map<String, Object> overview = marketPriceService.getFreshFlowerPriceData();
            price.put("freshFlower", overview);
            price.put("formatted", marketPriceService.getFormattedPriceDataForAI());// 获取多分类价格对比
            price.put("multiCategory", marketPriceService.getMultiCategoryPrices());
        } catch (Exception e) {
            log.error("获取价格数据失败: {}", e.getMessage());
            price.put("error", "价格数据获取失败");
        }return price;}

    /**
     * 构建预警prompt
     * @param weatherData 天气数据
     * @param priceData 价格数据
     * @return 完整的prompt
     */
    private String buildWarningPrompt(Map<String, Object> weatherData, Map<String, Object> priceData) {
        StringBuilder prompt = new StringBuilder();
        // 从天气数据中提取日期信息告知AI（weatherData.formatted中包含天气预报日期）
        prompt.append("【数据时间参考】以下是横州市未来5天的天气预报数据，请根据预报中的日期（第1天为今天）来分析：\n\n");
        prompt.append("你是横州茉莉花商城的专业市场分析师，专门为花农和商家提供市场行情分析与风险预警。\n\n");
        // 天气数据部分
        prompt.append("【天气预报数据】\n");
        Object weatherFormatted = weatherData.get("formatted");
        if (weatherFormatted != null) {
            prompt.append(weatherFormatted.toString());
        } else {prompt.append("天气数据暂时无法获取。\n");}
        prompt.append("\n\n");
        // 价格数据部分
        prompt.append("【茉莉花价格数据】\n");
        Object priceFormatted = priceData.get("formatted");
        if (priceFormatted != null) {prompt.append(priceFormatted.toString());
        } else {prompt.append("价格数据暂时无法获取。\n");}
        prompt.append("\n\n");
        // 分析要求
        prompt.append("请根据以上数据，按以下结构生成一份茉莉花市场行情简报与风险预警：\n\n");
        
        prompt.append("一、当前茉莉花价格行情分析\n");
        prompt.append("- 基于最新价格、均价、涨跌幅进行客观分析\n");
        prompt.append("- 分析近期价格走势及可能原因\n\n");
        
        prompt.append("二、未来5天天气对茉莉花花期、采摘、供货的影响\n");
        prompt.append("- 分析降雨对采摘的影响\n");
        prompt.append("- 分析温度对花期的影响\n");
        prompt.append("- 给出供货量预判\n\n");
        
        prompt.append("三、市场风险提示\n");
        prompt.append("- 价格暴涨暴跌风险预警\n");
        prompt.append("- 天气灾害影响预警\n");
        prompt.append("- 其他需要关注的风险点\n\n");
        
        prompt.append("四、价格走势预测\n");
        prompt.append("- 根据天气预报预测未来5天茉莉花价格走势\n");
        prompt.append("- 晴天/多云：价格通常较高（品质好，采摘活跃）\n");
        prompt.append("- 雨天：价格可能上涨（采摘受阻，供应减少）\n");
        prompt.append("- 大风/暴雨：价格可能大幅上涨（损毁严重，供应锐减）\n\n");

        prompt.append("五、经营决策建议\n");
        prompt.append("- 给花农的建议（采摘时机、存储建议）\n");
        prompt.append("- 给商家的建议（采购策略、库存建议）\n");
        prompt.append("- 短期和中期的操作建议\n\n");
        
        prompt.append("【重要提示】\n");
        prompt.append("1. 内容要专业、客观、数据支撑，避免空洞的套话\n");
        prompt.append("2. 风险提示要具体，明确指出可能的风险点和影响程度\n");
        prompt.append("3. 建议要贴合横州茉莉花产业实际情况，具有可操作性\n");
        prompt.append("4. 语气要温和但严谨，让花农和商家能看懂、能执行\n");
        prompt.append("5. 结尾必须添加免责声明：\n");
        prompt.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        prompt.append("本内容由AI生成，仅供参考，不构成投资或交易建议。花农和商家应结合实际情况自行判断，决策风险自负。\n");
        prompt.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        return prompt.toString();
    }

    /**
     * 获取备用预警内容（当AI服务不可用时）
     * @return 备用预警内容
     */
    private String getFallbackWarning() {
        StringBuilder warning = new StringBuilder();
        warning.append("【横州茉莉花市场行情提示】\n\n");
        warning.append("尊敬的用户，您好！\n\n");
        warning.append("目前AI预警服务正在维护中，暂时无法生成完整的智能分析报告。\n\n");
        warning.append("请您暂时参考以下基础提示：\n\n");
        
        // 添加天气基础提示
        try {
            String weather = weatherForecastService.getFormatted5DayWeatherForAI();
            warning.append("【近期天气预报】\n").append(weather).append("\n\n");
        } catch (Exception e) {
            warning.append("【近期天气预报】暂时无法获取，请关注当地天气预报。\n\n");
        }
        
        // 添加价格基础提示
        try {
            String price = marketPriceService.getFormattedPriceDataForAI();
            warning.append("【近期价格参考】\n").append(price).append("\n\n");
        } catch (Exception e) {
            warning.append("【近期价格参考】暂时无法获取，请关注市场实时报价。\n\n");
        }
        
        warning.append("【温馨提示】\n");
        warning.append("1. 关注天气变化，合理安排采摘时间\n");
        warning.append("2. 根据价格走势，理性决策买卖时机\n");
        warning.append("3. 如需帮助，请联系平台客服\n\n");
        warning.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        warning.append("本内容由AI生成，仅供参考，不构成投资或交易建议。\n");
        warning.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        return warning.toString();
    }

    /**
     * 获取快速预警摘要（不调用AI）
     * @return 快速预警摘要
     */
    public Map<String, Object> getQuickWarning() {
        Map<String, Object> result = new HashMap<>();
        try {// 天气影响分析
            Map<String, Object> weatherAnalysis = weatherForecastService.getWeatherAnalysis();
            result.put("weatherImpact", weatherAnalysis.get("impactAnalysis"));
            result.put("weatherForecast", weatherAnalysis.get("forecast"));
            // 价格分析
            Map<String, Object> priceData = marketPriceService.getFreshFlowerPriceData();
            if (priceData != null && Boolean.TRUE.equals(priceData.get("hasData"))) {
                result.put("latestPrice", priceData.get("latestPrice"));
                result.put("priceChange", priceData.get("changePercent"));
                result.put("priceTrend", priceData.get("changeTrend"));
                result.put("riskLevel", marketPriceService.getRiskLevel(
                        ((Number) priceData.get("changePercent")).doubleValue()));}
            result.put("success", true);
            result.put("message", "快速预警获取成功");
        } catch (Exception e) {
            log.error("获取快速预警失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "快速预警获取失败");
        }return result;}
}
