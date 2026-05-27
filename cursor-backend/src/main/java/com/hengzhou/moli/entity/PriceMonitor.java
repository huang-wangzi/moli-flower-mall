package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格监测实体类
 */
@Data
@TableName("price_monitor")
public class PriceMonitor implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 分类: 1-茉莉鲜花 2-茉莉花茶 3-茉莉文创 */
    private Integer category;
    /** 市场名称 */
    private String market;
    /** 单位 */
    private String unit;
    /** 最低价 */
    private BigDecimal minPrice;
    /** 最高价 */
    private BigDecimal maxPrice;
    /** 记录日期 */
    private java.time.LocalDate recordDate;
    /** 录入人ID */
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
