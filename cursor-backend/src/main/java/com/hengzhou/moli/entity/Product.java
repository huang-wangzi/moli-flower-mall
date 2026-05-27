package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 状态说明（status）：
 *   0 - 待审核（商家发布后默认值，管理员审核后方可展示）
 *   1 - 已审核 / 上架（管理员审核通过，用户端可见）
 *   2 - 审核拒绝（管理员审核未通过，商家可修改后重新提交）
 *
 * @author 横州茉莉花
 */
@Data
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商家ID */
    private Long shopId;

    /** 分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 商品图片(JSON数组) */
    @TableField("images")
    private String images;

    /** 商品价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 商品规格(JSON) */
    private String specs;

    /** 商品描述 */
    private String description;

    /**
     * 商品状态：
     *   0 - 待审核（默认，商家提交后须管理员审核）
     *   1 - 已审核 / 上架
     *   2 - 审核拒绝
     */
    private Integer status;

    /** 删除标记: 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
