package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收购商常用地址实体
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("acquirer_address")
public class AcquirerAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收购商用户ID */
    private Long userId;

    /** 市场地址 */
    private String marketAddress;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 是否默认地址 */
    private Integer isDefault;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
