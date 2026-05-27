package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场实体类
 * 功能：管理茉莉花交易市场信息
 * 说明：每个市场有独立的地理位置、价格数据
 */
@Data
@TableName("price_market")
public class PriceMarket implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 市场ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 市场名称
     * 例如：横州茉莉花市场、南宁花卉市场
     */
    private String name;

    /**
     * 市场地址
     * 例如：广西横州市横州镇
     */
    private String address;

    /**
     * 省份
     * 用于区域分类显示
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 纬度
     * 用于地图展示
     */
    private BigDecimal latitude;

    /**
     * 经度
     * 用于地图展示
     */
    private BigDecimal longitude;

    /**
     * 市场描述
     */
    private String description;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 排序权重
     * 数值越大越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
