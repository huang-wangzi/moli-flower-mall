package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 茉莉花收购需求表
 * 收购商发布的茉莉花收购需求
 */
@Data
@TableName("acquisition_demand")
public class AcquisitionDemand {

    /**
     * 需求ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收购商商家ID
     */
    private Long shopId;

    /**
     * 收购商名称
     */
    private String shopName;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 最低收购价(元/斤)
     */
    private BigDecimal priceMin;

    /**
     * 最高收购价(元/斤)
     */
    private BigDecimal priceMax;

    /**
     * 单位
     */
    private String unit;

    /**
     * 计划收购量(斤)
     */
    private BigDecimal quantityNeeded;

    /**
     * 剩余收购量(斤)
     */
    private BigDecimal quantityRemaining;

    /**
     * 市场地址
     */
    private String marketAddress;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 备注说明
     */
    private String description;

    /**
     * 状态: 0-已下架 1-收购中 2-已结束
     */
    private Integer status;

    /**
     * 收购日期
     */
    private LocalDate demandDate;

    /**
     * 需求总申请量
     */
    private Integer totalDemand;

    /**
     * 剩余需求数
     */
    private Integer remainingDemand;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 获取价格区间字符串
     */
    public String getPriceRange() {
        return priceMin + " - " + priceMax + " 元/" + unit;
    }

    /**
     * 获取进度百分比
     */
    public Integer getProgressPercent() {
        if (quantityNeeded == null || quantityNeeded.compareTo(BigDecimal.ZERO) == 0) {
            return 100;
        }
        BigDecimal progress = quantityNeeded.subtract(quantityRemaining)
                .divide(quantityNeeded, 2, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return progress.intValue();
    }
}
