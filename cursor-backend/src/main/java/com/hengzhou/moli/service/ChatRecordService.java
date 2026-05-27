package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.ChatRecord;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.ChatRecordMapper;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 聊天记录Service层
 */
@Service
@RequiredArgsConstructor
public class ChatRecordService extends ServiceImpl<ChatRecordMapper, ChatRecord> {

    private final MessageService messageService;
    private final UserService userService;

    /**
     * 获取两个用户之间的聊天记录
     */
    public List<ChatRecord> getChatHistory(Long userId1, Long userId2) {
        return this.list(new LambdaQueryWrapper<ChatRecord>()
                .and(w -> w.eq(ChatRecord::getFromUserId, userId1).eq(ChatRecord::getToUserId, userId2)
                        .or()
                        .eq(ChatRecord::getFromUserId, userId2).eq(ChatRecord::getToUserId, userId1))
                .eq(ChatRecord::getDeleted, 0)
                .orderByAsc(ChatRecord::getCreateTime));
    }

    /**
     * 获取与某商品商家的聊天记录
     */
    public List<ChatRecord> getChatHistoryByProduct(Long userId, Long productId) {
        return this.list(new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getProductId, productId)
                .and(w -> w.eq(ChatRecord::getFromUserId, userId).or().eq(ChatRecord::getToUserId, userId))
                .eq(ChatRecord::getDeleted, 0)
                .orderByAsc(ChatRecord::getCreateTime));
    }

    /**
     * 发送消息并创建聊天记录
     */
    @Transactional
    public ChatRecord sendMessage(Long fromUserId, Long toUserId, String content, Long productId) {
        ChatRecord record = ChatRecord.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .content(content)
                .productId(productId)
                .type("chat")
                .isRead(0)
                .deleted(0)
                .createTime(java.time.LocalDateTime.now())
                .build();
        this.save(record);

        // 给接收者发站内信通知（聊天消息类型 = 5）
        try {
            messageService.sendMessage(Message.builder()
                    .userId(toUserId)
                    .senderId(fromUserId)
                    .type(5)
                    .title("新消息")
                    .content(content)
                    .relatedId(productId)
                    .build());
        } catch (Exception e) {
            // 站内信通知失败不影响聊天主流程
        }

        return record;
    }

    /**
     * 发送广播回复（商家对管理员系统消息的回复），存入 chat_record
     */
    @Transactional
    public ChatRecord sendBroadcastReply(Long fromUserId, Long relatedMsgId, String content) {
        ChatRecord record = ChatRecord.builder()
                .fromUserId(fromUserId)
                .toUserId(null)
                .content(content)
                .productId(null)
                .type("broadcast_reply")
                .relatedMsgId(relatedMsgId)
                .isRead(0)
                .deleted(0)
                .createTime(java.time.LocalDateTime.now())
                .build();
        this.save(record);
        return record;
    }

    /**
     * 获取用户所有聊天会话（含广播会话）
     * 返回 Map: key=会话标识（普通聊天=对方用户ID, 广播=msgId)
     */
    public List<ChatRecord> getUserConversations(Long userId) {
        return this.list(new LambdaQueryWrapper<ChatRecord>()
                .and(w -> w.eq(ChatRecord::getFromUserId, userId)
                        .or()
                        .eq(ChatRecord::getToUserId, userId))
                .eq(ChatRecord::getDeleted, 0)
                .orderByDesc(ChatRecord::getCreateTime));
    }

    /**
     * 获取用户所有会话列表（含广播），供 ChatController.conversations 使用
     */
    public List<Map<String, Object>> getConversationsWithBroadcast(Long userId, Long adminId) {
        List<ChatRecord> chatRecords = getUserConversations(userId);
        Map<String, Map<String, Object>> convMap = new LinkedHashMap<>();

        // 普通聊天会话（排除与管理员的普通聊天，管理员会话走广播消息渠道）
        for (ChatRecord record : chatRecords) {
            Long otherId = record.getFromUserId().equals(userId) ? record.getToUserId() : record.getFromUserId();
            if (otherId == null) continue;
            // 跳过与管理员的普通聊天（管理员会话统一走广播消息 /broadcast 接口）
            if (adminId != null && otherId.equals(adminId)) continue;
            String key = "chat_" + otherId;
            if (!convMap.containsKey(key)) {
                User otherUser = userService.getById(otherId);
                Map<String, Object> conv = new HashMap<>();
                conv.put("type", "chat");
                conv.put("userId", otherId);
                conv.put("username", otherUser != null ? otherUser.getUsername() : "用户" + otherId);
                conv.put("avatar", otherUser != null && otherUser.getAvatar() != null ? otherUser.getAvatar() : "");
                conv.put("lastMessage", record.getContent());
                conv.put("lastTime", record.getCreateTime());
                conv.put("productId", record.getProductId());
                conv.put("unread", 0L);
                convMap.put(key, conv);
            }
            // 更新最新消息和时间
            Map<String, Object> conv = convMap.get(key);
            Object prevTime = (Object) conv.get("lastTime");
            if (prevTime == null || record.getCreateTime() != null && record.getCreateTime().isAfter((java.time.LocalDateTime) prevTime)) {
                conv.put("lastMessage", record.getContent());
                conv.put("lastTime", record.getCreateTime());
            }
            // 未读计数（发给userId且未读，排除与管理员的普通聊天）
            if (record.getToUserId() != null && record.getToUserId().equals(userId) && record.getIsRead() == 0
                    && (adminId == null || !record.getFromUserId().equals(adminId))) {
                conv.put("unread", ((Number) conv.get("unread")).longValue() + 1);
            }
        }

        // 广播会话：查 message 表 is_broadcast=1 的记录
        List<Message> broadcasts = messageService.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getIsBroadcast, 1)
                .eq(Message::getDeleted, 0));
        User adminUser = adminId != null ? userService.getById(adminId) : null;
        for (Message msg : broadcasts) {
            String key = "broadcast_" + msg.getId();
            Map<String, Object> conv = new HashMap<>();
            conv.put("type", "broadcast");
            conv.put("msgId", msg.getId());
            conv.put("userId", adminId);
            conv.put("username", adminUser != null ? adminUser.getUsername() : "平台客服");
            conv.put("avatar", adminUser != null && adminUser.getAvatar() != null ? adminUser.getAvatar() : "");
            conv.put("title", msg.getTitle());
            conv.put("lastMessage", msg.getContent());
            conv.put("lastTime", msg.getCreateTime());
            conv.put("unread", msg.getIsRead() == 0 ? 1L : 0L);
            convMap.put(key, conv);
        }

        // 按 lastTime 倒序
        List<Map<String, Object>> result = new ArrayList<>(convMap.values());
        result.sort((a, b) -> {
            Object ta = a.get("lastTime");
            Object tb = b.get("lastTime");
            if (ta == null) return 1;
            if (tb == null) return -1;
            return ((java.time.LocalDateTime) tb).compareTo((java.time.LocalDateTime) ta);
        });
        return result;
    }

    /**
     * 标记消息已读
     */
    public boolean markAsRead(Long fromUserId, Long toUserId) {
        ChatRecord update = ChatRecord.builder().isRead(1).build();
        return this.update(update,
                new LambdaQueryWrapper<ChatRecord>()
                        .eq(ChatRecord::getFromUserId, fromUserId)
                        .eq(ChatRecord::getToUserId, toUserId)
                        .eq(ChatRecord::getIsRead, 0));
    }

    /**
     * 获取未读消息数量
     */
    public long getUnreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getToUserId, userId)
                .eq(ChatRecord::getIsRead, 0)
                .eq(ChatRecord::getDeleted, 0));
    }

    // ==================== 管理员客服功能 ====================

    /**
     * 获取管理员收到的所有用户聊天会话列表
     */
    public List<Map<String, Object>> getAdminConversations(Long adminId) {
        // 查询所有发给管理员的消息（toUserId=adminId）及其回复
        List<ChatRecord> allRecords = this.list(new LambdaQueryWrapper<ChatRecord>()
                .and(w -> w.eq(ChatRecord::getToUserId, adminId)
                        .or()
                        .eq(ChatRecord::getFromUserId, adminId))
                .eq(ChatRecord::getDeleted, 0)
                .orderByDesc(ChatRecord::getCreateTime));

        Map<Long, Map<String, Object>> convMap = new LinkedHashMap<>();

        for (ChatRecord record : allRecords) {
            // 确定对方用户ID
            Long otherId = record.getFromUserId().equals(adminId) ? record.getToUserId() : record.getFromUserId();
            if (otherId == null || otherId.equals(adminId)) continue;

            if (!convMap.containsKey(otherId)) {
                User otherUser = userService.getById(otherId);
                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", otherId);
                conv.put("username", otherUser != null ? (otherUser.getUsername() != null ? otherUser.getUsername() : "用户" + otherId) : "用户" + otherId);
                conv.put("nickname", otherUser != null ? otherUser.getNickname() : null);
                conv.put("avatar", otherUser != null && otherUser.getAvatar() != null ? otherUser.getAvatar() : "");
                conv.put("lastMessage", record.getContent());
                conv.put("lastTime", record.getCreateTime());
                conv.put("productId", record.getProductId());
                conv.put("unread", 0L);
                convMap.put(otherId, conv);
            }

            // 更新最新消息
            Map<String, Object> conv = convMap.get(otherId);
            Object prevTime = conv.get("lastTime");
            if (prevTime == null || (record.getCreateTime() != null && ((java.time.LocalDateTime) prevTime).isBefore(record.getCreateTime()))) {
                conv.put("lastMessage", record.getContent());
                conv.put("lastTime", record.getCreateTime());
            }

            // 未读计数：发给adminId且未读
            if (record.getToUserId() != null && record.getToUserId().equals(adminId) && record.getIsRead() == 0) {
                conv.put("unread", ((Number) conv.get("unread")).longValue() + 1);
            }
        }

        // 按 lastTime 倒序
        List<Map<String, Object>> result = new ArrayList<>(convMap.values());
        result.sort((a, b) -> {
            Object ta = a.get("lastTime");
            Object tb = b.get("lastTime");
            if (ta == null) return 1;
            if (tb == null) return -1;
            return ((java.time.LocalDateTime) tb).compareTo((java.time.LocalDateTime) ta);
        });
        return result;
    }

    /**
     * 获取管理员与指定用户的完整聊天记录
     */
    public List<ChatRecord> getAdminChatHistory(Long adminId, Long userId) {
        return this.list(new LambdaQueryWrapper<ChatRecord>()
                .and(w -> w.eq(ChatRecord::getFromUserId, adminId).eq(ChatRecord::getToUserId, userId)
                        .or()
                        .eq(ChatRecord::getFromUserId, userId).eq(ChatRecord::getToUserId, adminId))
                .eq(ChatRecord::getDeleted, 0)
                .orderByAsc(ChatRecord::getCreateTime));
    }
}
