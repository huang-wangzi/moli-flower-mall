package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格预警实体类
 */
@Data
@TableName("price_alert")
public class PriceAlert implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 预警类型：1-价格上涨预警 2-价格下跌预警 3-天气影响预警 4-市场异常预警 */
    private Integer alertType;
    
    /** 预警级别：1-提示 2-警告 3-紧急 */
    private Integer alertLevel;
    
    /** 预警标题 */
    private String title;
    
    /** 预警内容 */
    private String content;
    
    /** 关联分类：1-茉莉鲜花 2-茉莉花茶 3-茉莉盆栽 4-茉莉花苗 */
    private Integer category;
    
    /** 关联市场 */
    private String market;
    
    /** 当前价格 */
    private BigDecimal currentPrice;
    
    /** 预警触发价格 */
    private BigDecimal triggerPrice;
    
    /** 涨跌幅（百分比） */
    private BigDecimal changeRate;
    
    /** 关联天气数据ID */
    private Long weatherId;
    
    /** 天气影响描述 */
    private String weatherImpact;
    
    /** 影响分析 */
    private String analysis;
    
    /** 建议措施 */
    private String suggestion;
    
    /** 预警状态：0-未读 1-已读 2-已处理 */
    private Integer status;
    
    /** 预警时间 */
    private LocalDateTime alertTime;
    
    /** 处理时间 */
    private LocalDateTime handleTime;
    
    /** 处理人 */
    private Long handleBy;
    
    /** 处理备注 */
    private String handleRemark;
    
    /** 是否已发送通知：0-否 1-是 */
    private Integer notified;
    
    /** 通知方式：1-系统通知 2-短信 3-邮件 */
    private String notifyWay;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}