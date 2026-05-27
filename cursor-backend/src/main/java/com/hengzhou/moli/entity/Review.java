package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价实体类
 */
@Data
@TableName("product_review")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户头像 */
    @TableField("avatar")
    private String avatar;

    /** 订单ID */
    private Long orderId;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片(JSON数组) */
    @TableField("images")
    private String images;

    /** 商家回复 */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 状态: 0-隐藏 1-显示 */
    private Integer status;

    /** 删除标记: 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
