package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.ChatRecord;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.ChatRecordService;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 聊天控制器
 * 负责处理用户与用户、用户与管理员之间的即时聊天消息
 * 支持普通私聊、联系客服、与商品商家聊天、广播消息等功能
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    /**
     * 聊天记录服务：处理聊天消息的存储和查询
     */
    private final ChatRecordService chatRecordService;

    /**
     * 消息服务：处理系统消息和通知
     */
    private final MessageService messageService;

    /**
     * 用户服务：获取用户信息
     */
    private final UserService userService;

    /**
     * 获取平台客服（管理员）用户ID
     * 用于「联系客服」功能，建立用户与管理员的会话
     * @return 管理员用户ID
     */
    @GetMapping("/support-admin-id")
    public Result<Long> getSupportAdminId() {
        // 查询角色为管理员的用户（role=4）
        User admin = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 4)
                .last("LIMIT 1"));
        if (admin == null) {
            return Result.error("未配置管理员账号");
        }
        return Result.success(admin.getId());
    }

    /**
     * 获取与指定用户的聊天历史记录
     * 用于私聊界面，展示与某个用户的完整聊天记录
     * @param toUserId 聊天对象的用户ID
     * @param fromUserId 当前登录用户ID（从请求头获取）
     * @return 聊天记录列表
     */
    @GetMapping("/history/{toUserId}")
    public Result<List<ChatRecord>> getChatHistory(
            @PathVariable Long toUserId,
            @RequestHeader(value = "X-User-Id", required = false) Long fromUserId) {
        if (fromUserId == null) {
            return Result.error("请先登录");
        }
        // 获取两个用户之间的双向聊天记录
        List<ChatRecord> records = chatRecordService.getChatHistory(fromUserId, toUserId);
        return Result.success(records);
    }

    /**
     * 获取与商品商家的聊天历史
     * 用户在商品详情页点击「联系商家」时，获取与该商品卖家的历史聊天记录
     * @param productId 商品ID
     * @param userId 当前登录用户ID
     * @return 聊天记录列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<ChatRecord>> getChatHistoryByProduct(
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 获取通过商品建立关联的聊天记录
        List<ChatRecord> records = chatRecordService.getChatHistoryByProduct(userId, productId);
        return Result.success(records);
    }

    /**
     * 发送消息
     * 用户向另一个用户发送私聊消息
     * @param toUserId 接收消息的用户ID
     * @param content 消息内容
     * @param productId 关联的商品ID（可选，用于追踪是从哪个商品页发起的聊天）
     * @param fromUserId 发送消息的用户ID
     * @return 发送成功后的聊天记录
     */
    @PostMapping("/send")
    public Result<ChatRecord> sendMessage(
            @RequestParam Long toUserId,
            @RequestParam String content,
            @RequestParam(required = false) Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long fromUserId) {
        if (fromUserId == null) {
            return Result.error("请先登录");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        // 调用服务层发送消息并保存记录
        ChatRecord record = chatRecordService.sendMessage(fromUserId, toUserId, content.trim(), productId);
        return Result.success(record);
    }

    /**
     * 获取当前用户的所有会话列表
     * 包括普通私聊会话和广播会话（与管理员的系统通知会话）
     * @param userId 当前登录用户ID
     * @return 会话列表，每个会话包含最新一条消息
     */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("请先登录");
            }
            // 获取管理员ID，用于广播会话
            User admin = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getRole, 4).last("LIMIT 1"));
            Long adminId = admin != null ? admin.getId() : null;
            // 获取会话列表（包含广播会话）
            List<Map<String, Object>> conversations = chatRecordService.getConversationsWithBroadcast(userId, adminId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取广播消息的完整对话记录
     * 展示系统广播消息及其所有用户回复
     * @param msgId 系统消息ID
     * @param userId 当前登录用户ID
     * @return 消息及回复列表
     */
    @GetMapping("/broadcast/{msgId}")
    public Result<List<Map<String, Object>>> getBroadcastHistory(
            @PathVariable Long msgId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (msgId == null || msgId <= 0) {
            return Result.error("无效的消息ID");
        }

        List<Map<String, Object>> history = new ArrayList<>();

        // 1. 获取原始系统消息
        Message msg = messageService.getById(msgId);
        if (msg == null) {
            return Result.error("消息不存在");
        }

        // 获取管理员信息用于展示发送者
        User adminUser = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 4).last("LIMIT 1"));

        // 构建原消息数据
        Map<String, Object> orig = new HashMap<>();
        orig.put("id", "sys_" + msg.getId());
        orig.put("fromUserId", adminUser != null ? adminUser.getId() : null);
        orig.put("toUserId", (Object) null);
        orig.put("content", msg.getContent());
        orig.put("title", msg.getTitle());
        orig.put("createTime", msg.getCreateTime());
        orig.put("isSystem", true);
        orig.put("senderId", adminUser != null ? adminUser.getId() : null);
        orig.put("senderName", adminUser != null ? adminUser.getUsername() : "平台客服");
        orig.put("avatar", adminUser != null && adminUser.getAvatar() != null ? adminUser.getAvatar() : "");
        history.add(orig);

        // 2. 获取用户的所有回复
        List<ChatRecord> replies = chatRecordService.list(new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getRelatedMsgId, msgId)
                .eq(ChatRecord::getDeleted, 0)
                .orderByAsc(ChatRecord::getCreateTime));

        // 遍历回复，构建回复数据
        for (ChatRecord r : replies) {
            User sender = userService.getById(r.getFromUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("fromUserId", r.getFromUserId());
            item.put("toUserId", r.getToUserId());
            item.put("content", r.getContent());
            item.put("createTime", r.getCreateTime());
            item.put("isSystem", false);
            item.put("senderId", r.getFromUserId());
            item.put("senderName", sender != null ? sender.getUsername() : "用户" + r.getFromUserId());
            item.put("avatar", sender != null && sender.getAvatar() != null ? sender.getAvatar() : "");
            history.add(item);
        }

        // 3. 标记消息为已读
        if (msg.getIsRead() == 0) {
            messageService.markAsRead(msgId);
        }

        return Result.success(history);
    }

    /**
     * 商家回复管理员广播消息
     * 商家对收到的系统广播消息进行回复
     * @param msgId 被回复的系统消息ID
     * @param content 回复内容
     * @param fromUserId 发送回复的用户ID
     * @return 回复的聊天记录
     */
    @PostMapping("/broadcast-reply")
    public Result<ChatRecord> sendBroadcastReply(
            @RequestParam Long msgId,
            @RequestParam String content,
            @RequestHeader(value = "X-User-Id", required = false) Long fromUserId) {
        if (fromUserId == null) {
            return Result.error("请先登录");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        ChatRecord record = chatRecordService.sendBroadcastReply(fromUserId, msgId, content.trim());
        return Result.success(record);
    }

    /**
     * 获取当前用户的未读消息数量
     * 用于消息中心显示红点提示
     * @param userId 当前登录用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread")
    public Result<Long> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chatRecordService.getUnreadCount(userId));
    }

    /**
     * 标记与指定用户的聊天消息为已读
     * 用户查看聊天后，将对方发来的未读消息标记为已读
     * @param fromUserId 发送消息的用户ID
     * @param toUserId 接收消息的用户ID（当前用户）
     */
    @PutMapping("/mark-read")
    public Result<String> markConversationAsRead(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId) {
        if (fromUserId == null || toUserId == null) {
            return Result.error("参数错误");
        }
        chatRecordService.markAsRead(fromUserId, toUserId);
        return Result.success("标记成功");
    }

    // ==================== 管理员客服功能 ====================

    /**
     * 获取管理员收到的所有用户聊天会话列表
     * 管理员客服界面显示所有与用户进行过聊天的会话
     * @param adminId 管理员用户ID
     * @return 所有用户会话列表
     */
    @GetMapping("/admin/conversations")
    public Result<List<Map<String, Object>>> getAdminConversations(
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (adminId == null) {
            return Result.error("请先登录");
        }
        try {
            List<Map<String, Object>> conversations = chatRecordService.getAdminConversations(adminId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取与指定用户的完整聊天记录（管理员视角）
     * 管理员查看与某个用户的全部聊天历史
     * @param userId 目标用户ID
     * @param adminId 管理员ID
     * @return 聊天记录列表
     */
    @GetMapping("/admin/history/{userId}")
    public Result<List<ChatRecord>> getAdminChatHistory(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (adminId == null) {
            return Result.error("请先登录");
        }
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        List<ChatRecord> records = chatRecordService.getAdminChatHistory(adminId, userId);
        return Result.success(records);
    }

    /**
     * 管理员发送消息给用户
     * 客服回复用户的消息
     * @param toUserId 接收消息的用户ID
     * @param content 消息内容
     * @param productId 关联商品ID（可选）
     * @param adminId 管理员ID
     * @return 发送成功的聊天记录
     */
    @PostMapping("/admin/send")
    public Result<ChatRecord> adminSendMessage(
            @RequestParam Long toUserId,
            @RequestParam String content,
            @RequestParam(required = false) Long productId,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (adminId == null) {
            return Result.error("请先登录");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        ChatRecord record = chatRecordService.sendMessage(adminId, toUserId, content.trim(), productId);
        return Result.success(record);
    }

    /**
     * 管理员标记与用户的所有消息为已读
     * @param userId 目标用户ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @PutMapping("/admin/mark-read/{userId}")
    public Result<String> adminMarkAsRead(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (adminId == null) {
            return Result.error("请先登录");
        }
        chatRecordService.markAsRead(userId, adminId);
        return Result.success("标记成功");
    }
}
