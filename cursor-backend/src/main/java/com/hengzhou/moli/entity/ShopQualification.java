package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家资质实体类
 */
@Data
@TableName("shop_qualification")
public class ShopQualification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopName;
    /** 营业执照（Base64或URL） */
    @TableField("business_license")
    private String businessLicense;
    /** 身份证正面（Base64或URL） */
    @TableField("id_card_front")
    private String idCardFront;
    /** 身份证背面（Base64或URL） */
    @TableField("id_card_back")
    private String idCardBack;
    /** 品质认证（Base64或URL） */
    private String qualityCert;
    /** 联系电话 */
    private String contact;
    /** 状态: 0-待审核 1-通过 2-拒绝 */
    private Integer status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private Long auditBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
