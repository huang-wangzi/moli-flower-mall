package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 天气数据实体类
 */
@Data
@TableName("weather_data")
public class WeatherData implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 城市名称 */
    private String cityName;
    
    /** 城市代码 */
    private String cityCode;
    
    /** 天气现象 */
    private String weather;
    
    /** 气温（摄氏度） */
    private Double temp;
    
    /** 体感温度 */
    private Double feelsLike;
    
    /** 最高气温 */
    private Double tempMax;
    
    /** 最低气温 */
    private Double tempMin;
    
    /** 湿度（百分比） */
    private Integer humidity;
    
    /** 风力等级 */
    private String windSpeed;
    
    /** 风向 */
    private String windDir;
    
    /** 降雨量（mm） */
    private Double precipitation;
    
    /** 气压（hPa） */
    private Integer pressure;
    
    /** 能见度（km） */
    private Double visibility;
    
    /** 紫外线指数 */
    private Integer uvIndex;
    
    /** 空气质量指数 */
    private Integer aqi;
    
    /** 空气质量等级 */
    private String airLevel;
    
    /** 日出时间 */
    private String sunrise;
    
    /** 日落时间 */
    private String sunset;
    
    /** 预报日期 */
    private String reportTime;
    
    /** 数据来源 */
    private String source;
    
    /** 记录时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}