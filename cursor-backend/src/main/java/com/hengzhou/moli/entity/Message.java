package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 对应数据库表 message
 *
 * @author 横州茉莉花
 */
@Data
@Builder                // 开启Builder构造模式，修复builder()报错
@NoArgsConstructor      // 无参构造器
@AllArgsConstructor     // 全参构造器
@TableName("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收消息的用户ID
     */
    private Long userId;

    /**
     * 消息发送者ID（系统消息为null）
     */
    private Long senderId;

    /**
     * 消息类型
     * 1-系统通知  2-订单通知  3-评价提醒  4-审核通知  5-聊天消息
     */
    private Integer type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联业务ID（订单ID/商品ID/评价ID）
     */
    private Long relatedId;

    /**
     * 是否已读
     * 0-未读
     * 1-已读
     */
    private Integer isRead;

    /**
     * 逻辑删除标记
     * 0-未删除
     * 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 是否为广播消息
     * 0-普通消息
     * 1-管理员发送的广播消息（只存一条，与商家回复在 chat_record 里互通）
     */
    private Integer isBroadcast;

    /**
     * 广播消息的接收范围（仅 is_broadcast=1 时有意义）
     * all-全部用户  users-普通用户  shops-商家
     */
    private String scope;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}