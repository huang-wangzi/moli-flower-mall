package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易客诉（含售后「客服介入」）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("complaint")
public class Complaint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 用户名（冗余字段，避免联表查询） */
    private String username;

    private Long shopId;

    /** 被投诉方ID（冗余字段，等同于 shopId） */
    private Long accusedId;

    private Long orderId;

    private Long refundId;

    /** 0-普通投诉 1-售后客服介入 */
    private Integer sourceType;

    /** 投诉类型: 0-商品问题 1-服务问题 2-物流问题 3-其他 */
    private Integer type;

    /** 投诉标题 (数据库中可能不存在，标记为非数据库字段) */
    @TableField(exist = false)
    private String title;

    /** 投诉内容 (冗余字段，旧表结构中为 NOT NULL) */
    private String content;

    private String reason;

    private String description;

    /** 0-待处理 1-已处理 2-已驳回 */
    private Integer status;

    private String adminRemark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
