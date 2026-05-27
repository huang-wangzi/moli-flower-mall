package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息Service层
 */
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    private final UserService userService;

    /**
     * 获取用户消息列表
     */
    public List<Message> getMessagesByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getDeleted, 0)
                .orderByDesc(Message::getCreateTime));
    }

    /**
     * 获取未读消息数量
     */
    public long getUnreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .eq(Message::getDeleted, 0));
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(Message message) {
        message.setIsRead(0);
        return this.save(message);
    }

    /**
     * 批量发送系统消息（all=全部用户 / users=普通用户 / shops=商家）
     * @param scope all / users / shops
     * @param title 消息标题
     * @param content 消息内容
     * @return 发送数量
     */
    @Transactional
    public int sendSystemMessage(String scope, String title, String content) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if ("users".equals(scope)) {
            wrapper.eq(User::getRole, 1);
        } else if ("shops".equals(scope)) {
            wrapper.eq(User::getRole, 2);
        }
        // all = 不过滤，全部用户
        List<User> targets = userService.list(wrapper);
        if (targets.isEmpty()) return 0;
        List<Message> messages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User u : targets) {
            messages.add(Message.builder()
                    .userId(u.getId())
                    .senderId(null)
                    .type(1)
                    .title(title)
                    .content(content)
                    .relatedId(null)
                    .isRead(0)
                    .createTime(now)
                    .build());
        }
        this.saveBatch(messages);
        return messages.size();
    }

    /**
     * 发送评价提醒给商家
     */
    public boolean sendReviewNotification(Long shopUserId, Long productId, String productName, String reviewerName) {
        Message message = Message.builder()
                .userId(shopUserId)
                .type(3)
                .title("您有新评价")
                .content("用户「" + reviewerName + "」对商品「" + productName + "」发表了评价，请及时查看并回复。")
                .relatedId(productId)
                .isRead(0)
                .build();
        return sendMessage(message);
    }

    /**
     * 发送审核通知给商家
     */
    public boolean sendAuditNotification(Long shopUserId, boolean approved, String reason) {
        Message message;
        if (approved) {
            message = Message.builder()
                    .userId(shopUserId)
                    .type(4)
                    .title("商家入驻审核通过")
                    .content("恭喜！您的商家入驻申请已通过审核，现在可以开始上架商品了。")
                    .isRead(0)
                    .build();
        } else {
            message = Message.builder()
                    .userId(shopUserId)
                    .type(4)
                    .title("商家入驻审核未通过")
                    .content("很抱歉，您的商家入驻申请未通过审核。原因：" + (reason != null ? reason : "请重新提交申请。"))
                    .isRead(0)
                    .build();
        }
        return sendMessage(message);
    }

    /**
     * 标记消息已读（通过 messageId）
     */
    public boolean markAsRead(Long messageId) {
        return this.updateById(Message.builder().id(messageId).isRead(1).build());
    }

    /**
     * 标记所有消息已读
     */
    public boolean markAllAsRead(Long userId) {
        Message message = Message.builder().isRead(1).build();
        return this.update(message, new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    /**
     * 删除消息
     */
    public boolean deleteMessage(Long messageId) {
        return this.removeById(messageId);
    }

    /**
     * 获取系统消息列表（管理员查看，type=1 且 is_broadcast=1）
     */
    public List<Map<String, Object>> listForAdmin() {
        List<Message> msgs = this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getType, 1)
                .eq(Message::getIsBroadcast, 1)
                .eq(Message::getDeleted, 0)
                .orderByDesc(Message::getCreateTime));
        return msgs.stream().map(msg -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", msg.getId());
            map.put("title", msg.getTitle());
            map.put("content", msg.getContent());
            map.put("scope", "all");
            map.put("isBroadcast", 1);
            map.put("createTime", msg.getCreateTime());
            return map;
        }).toList();
    }

    /**
     * 发送广播系统消息（只存一条记录，供指定范围的商家在聊天框里看到并回复）
     * @param scope all-全部用户 users-普通用户 shops-商家
     */
    @Transactional
    public Message sendBroadcastSystemMessage(String scope, String title, String content, Long senderId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("系统消息标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("系统消息内容不能为空");
        }
        Message msg = Message.builder()
                .userId(0L)                    // broadcast 消息无单一接收人，用 0 占位
                .senderId(senderId)
                .type(1)
                .isBroadcast(1)
                .scope(scope)
                .title(title.trim())
                .content(content.trim())
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        this.save(msg);
        return msg;
    }
}
