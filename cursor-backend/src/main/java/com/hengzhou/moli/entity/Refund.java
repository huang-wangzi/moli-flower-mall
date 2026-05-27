package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后/退款申请实体
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 售后ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 商家ID */
    private Long shopId;

    /** 售后类型: refund-仅退款, return-退货退款 */
    @TableField("refund_type")
    private String refundType;

    /** 退款原因 */
    private String reason;

    /** 详细说明 */
    private String description;

    /** 退款金额 */
    private BigDecimal amount;

    /** 状态: 0-待处理, 1-处理中, 2-已同意, 3-已拒绝, 4-已完成 */
    private Integer status;

    /** 是否需要管理员介入: 0-否, 1-是 */
    @TableField("admin_intervention")
    private Integer adminIntervention;

    /** 管理员处理备注 */
    @TableField("admin_remark")
    private String adminRemark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 删除标记: 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}
