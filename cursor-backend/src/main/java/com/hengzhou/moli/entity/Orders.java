package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author 横州茉莉花
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商家ID */
    private Long shopId;

    /** 商品金额 */
    private BigDecimal totalAmount;

    /** 运费 */
    private BigDecimal freight;

    /** 优惠金额 */
    private BigDecimal discount;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 订单状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款 7-退款驳回 */
    private Integer status;

    /** 支付方式: 1-微信 2-支付宝 */
    private Integer payType;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 收货时间 */
    private LocalDateTime receiveTime;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货人地址 */
    private String receiverAddress;

    /** 订单备注 */
    private String remark;

    /** 管理员处理备注 */
    private String adminRemark;

    /** 删除标记: 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
