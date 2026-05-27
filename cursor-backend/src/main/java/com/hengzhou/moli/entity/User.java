package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author 横州茉莉花
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    @TableField("avatar")
    private String avatar;

    /** 性别: 0-保密 1-男 2-女 */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色: 1-普通用户 2-商家 3-园艺师 4-管理员 */
    private Integer role;

    /**
     * 商家类型: 1-普通商家(商品销售) 2-收购商(茉莉花收购)
     * 仅当role=2时有效
     */
    private Integer shopType;

    /**
     * 角色字符串（用于接收前端传入的注册类型，不存入数据库）
     * 可选值: user, shop, acquirer, admin
     */
    @TableField(exist = false)
    private String roleStr;

    /**
     * 登录端类型（仅登录请求使用，不存库）：user / shop / acquirer / admin
     */
    @TableField(exist = false)
    private String loginType;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;

    /** 会员等级 */
    private String memberLevel;

    /** 会员积分 */
    private Integer memberPoints;

    /** 账户余额 */
    private BigDecimal balance;

    /** 店铺名称（商家专属） */
    private String shopName;

    /** 店铺类型（商家专属） */
    private String shopCategory;

    /**
     * 收购商名称/店铺名称（收购商专属）
     */
    private String merchantName;

    /**
     * 商家资质审核状态：0-未提交资质 1-待审核 2-通过 3-已拒绝
     * 商家通过注册审核后自动设为0，只有为2时商家端才开放全部功能
     * 收购商不需要资质审核
     */
    private Integer shopQualificationStatus;

    /** 专业领域（园艺师专属） */
    private String specialty;

    /** 从业年限（园艺师专属） */
    private Integer experience;

    /** 个人简介（园艺师专属） */
    private String bio;

    /** 删除标记: 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
