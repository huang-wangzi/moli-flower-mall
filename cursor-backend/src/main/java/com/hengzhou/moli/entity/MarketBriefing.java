package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场简报实体类
 */
@Data
@TableName("market_briefing")
public class MarketBriefing implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 简报标题 */
    private String title;
    
    /** 简报类型：1-日报 2-周报 3-月报 4-专题报告 */
    private Integer briefingType;
    
    /** 简报日期 */
    private String reportDate;
    
    /** 简报摘要 */
    private String summary;
    
    /** 简报内容（JSON格式） */
    private String content;
    
    /** 价格概览JSON */
    private String priceOverview;
    
    /** 天气情况JSON */
    private String weatherInfo;
    
    /** 市场分析 */
    private String analysis;
    
    /** 趋势预测 */
    private String forecast;
    
    /** 建议措施 */
    private String suggestions;
    
    /** 包含的市场 */
    private String markets;
    
    /** 包含的分类 */
    private String categories;
    
    /** 简报来源 */
    private String source;
    
    /** 关联的价格预警IDs */
    private String relatedAlerts;
    
    /** 作者/生成人 */
    private String author;
    
    /** AI生成标记：0-手动生成 1-AI生成 */
    private Integer aiGenerated;
    
    /** 附件路径 */
    private String attachment;
    
    /** 发布时间 */
    private LocalDateTime publishTime;
    
    /** 状态：0-草稿 1-已发布 2-已归档 */
    private Integer status;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}