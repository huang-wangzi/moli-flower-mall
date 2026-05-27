package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 茉莉花收购申请记录表
 * 花农对收购需求的申请记录
 */
@Data
@TableName("acquisition_application")
public class AcquisitionApplication {

    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的收购需求ID
     */
    private Long demandId;

    /**
     * 申请花农用户ID
     */
    private Long userId;

    /**
     * 花农昵称
     */
    private String userNickname;

    /**
     * 花农姓名
     */
    private String farmerName;

    /**
     * 申请的茉莉花斤数
     */
    private BigDecimal quantity;

    /**
     * 茉莉花照片URLs(JSON数组)
     */
    @TableField("photo_urls")
    private String photoUrls;

    /**
     * 茉莉花照片URLs(兼容旧字段名 jasmine_photos)
     * 注意：此字段不直接映射数据库，只用于兼容处理
     */
    private String jasminePhotos;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请重量(斤)
     */
    private BigDecimal applyWeight;

    /**
     * 状态: 0-待审核 1-已同意/待交付 2-交付中 3-已完成 4-已拒绝 5-已取消
     */
    private Integer status;

    /**
     * 实际交付斤数
     */
    private BigDecimal actualQuantity;

    /**
     * 实际成交单价(元/斤)
     */
    private BigDecimal actualPrice;

    /**
     * 实际成交总金额
     */
    private BigDecimal totalAmount;

    /**
     * 同意时间
     */
    private LocalDateTime agreeTime;

    /**
     * 交付时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== 关联查询字段（不映射到数据库） ====================

    /**
     * 需求标题（从关联表查询）
     */
    private String demandTitle;

    /**
     * 收购商名称（从关联表查询）
     */
    private String shopName;

    /**
     * 市场地址（从关联表查询）
     */
    private String marketAddress;

    /**
     * 价格区间（从关联表查询，计算得出）
     */
    private String priceRange;

    /**
     * 需求价格最低价（从关联表查询）
     */
    private BigDecimal priceMin;

    /**
     * 需求价格最高价（从关联表查询）
     */
    private BigDecimal priceMax;

    /**
     * 花农用户名（从关联表查询）
     */
    private String username;

    /**
     * 花农手机号（从关联表查询）
     */
    private String phone;

    /**
     * 状态名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已同意/待交付";
            case 2: return "交付中";
            case 3: return "已完成";
            case 4: return "已拒绝";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 状态对应的颜色
     */
    public String getStatusColor() {
        if (status == null) return "#999";
        switch (status) {
            case 0: return "#ff9800";  // 橙色-待审核
            case 1: return "#2196f3";   // 蓝色-已同意
            case 2: return "#9c27b0";   // 紫色-交付中
            case 3: return "#4caf50";   // 绿色-已完成
            case 4: return "#f44336";  // 红色-已拒绝
            case 5: return "#9e9e9e";   // 灰色-已取消
            default: return "#999";
        }
    }
}
