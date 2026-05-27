package com.hengzhou.moli.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天记录实体类
 * 对应数据库表 chat_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_record")
public class ChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读: 0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 关联商品ID(可选)
     */
    private Long productId;

    /**
     * 消息类型：chat-普通聊天 broadcast_reply-广播回复
     */
    private String type;

    /**
     * 关联的广播消息ID（用于广播回复场景）
     */
    private Long relatedMsgId;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
