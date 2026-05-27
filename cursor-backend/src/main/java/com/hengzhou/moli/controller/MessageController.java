package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 消息控制器
 * 负责处理系统消息和通知相关的所有HTTP请求
 * 支持消息列表查询、未读数统计、已读标记、删除、回复等功能
 * 用于向用户推送系统通知、订单状态变更、评价提醒等消息
 *
 * @author 横州茉莉花
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "消息管理")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    /**
     * 获取用户消息列表（附发送者信息）
     * 返回指定用户的所有消息，并关联查询发送者的用户名和头像
     * 接口: GET /message/list
     * @param userId 用户ID
     * @return 消息列表，每条消息包含发送者详细信息
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户消息列表（附发送者信息）")
    public Result<List<Map<String, Object>>> getMessageList(@RequestParam Long userId) {
        List<Message> messages = messageService.getMessagesByUserId(userId);
        List<Map<String, Object>> result = messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("type", msg.getType());
            map.put("title", msg.getTitle());
            map.put("content", msg.getContent());
            map.put("relatedId", msg.getRelatedId());
            map.put("isRead", msg.getIsRead());
            map.put("createTime", msg.getCreateTime());
            // 附发送者信息（senderId 直接取，senderName/senderAvatar 查用户表）
            map.put("senderId", msg.getSenderId());
            if (msg.getSenderId() != null) {
                User sender = userService.getById(msg.getSenderId());
                if (sender != null) {
                    map.put("senderName", sender.getUsername());
                    map.put("senderAvatar", sender.getAvatar() != null ? sender.getAvatar() : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
                } else {
                    map.put("senderName", "用户" + msg.getSenderId());
                    map.put("senderAvatar", "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
                }
            } else {
                map.put("senderName", "系统");
                map.put("senderAvatar", "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            }
            return map;
        }).toList();
        return Result.success(result);
    }

    /**
     * 获取用户未读消息数量
     * 用于消息中心显示红点提示
     * 接口: GET /message/unread
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读消息数量")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条消息为已读
     * 用户查看消息后，将该消息标记为已读状态
     * 接口: PUT /message/{id}/read
     * @param id 消息ID
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记消息已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        boolean success = messageService.markAsRead(id);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }

    /**
     * 标记用户所有消息为已读
     * 一键已读功能，将指定用户的所有未读消息标记为已读
     * 接口: PUT /message/readAll
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/readAll")
    @Operation(summary = "标记所有消息已读")
    public Result<String> markAllAsRead(@RequestParam Long userId) {
        boolean success = messageService.markAllAsRead(userId);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }

    /**
     * 删除消息
     * 用户删除指定的系统消息
     * 接口: DELETE /message/{id}
     * @param id 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息")
    public Result<String> deleteMessage(@PathVariable Long id) {
        boolean success = messageService.deleteMessage(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 回复消息
     * 用户对收到的消息进行回复，创建新的消息通知发送给原消息发送者
     * 接口: POST /message/reply
     * @param params 包含 receiverId（接收者ID）、content（回复内容）、relatedId（关联ID）
     * @param userId 当前登录用户ID（从请求头获取）
     * @param request HTTP请求对象
     * @return 操作结果
     */
    @PostMapping("/reply")
    @Operation(summary = "回复消息")
    public Result<String> replyMessage(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            jakarta.servlet.http.HttpServletRequest request) {
        // 如果没有传 userId，尝试从请求属性中获取
        if (userId == null) {
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                userId = Long.valueOf(userIdAttr.toString());
            }
        }
        if (userId == null) {
            return Result.error("请登录");
        }

        Long receiverId = params.get("receiverId") != null ? Long.valueOf(params.get("receiverId").toString()) : null;
        String content = params.get("content") != null ? params.get("content").toString() : null;
        Long relatedId = params.get("relatedId") != null ? Long.valueOf(params.get("relatedId").toString()) : null;

        if (receiverId == null || content == null || content.isBlank()) {
            return Result.error("缺少必要参数");
        }

        // 创建回复消息
        Message replyMsg = new Message();
        replyMsg.setUserId(receiverId);  // 接收者是原消息的发送者
        replyMsg.setSenderId(userId);    // 发送者是当前用户
        replyMsg.setType(5);  // 聊天消息类型
        replyMsg.setTitle("收到新回复");
        replyMsg.setContent(content);
        replyMsg.setRelatedId(relatedId);
        replyMsg.setIsRead(0);

        messageService.sendMessage(replyMsg);
        return Result.success("回复已发送");
    }

    /**
     * 获取我的消息（通过请求头用户ID）
     * 与 /message/list 接口功能相同，但用户ID从请求头 X-User-Id 获取
     * 接口: GET /message/my
     * @param userId 当前登录用户ID（从请求头获取）
     * @param request HTTP请求对象
     * @return 消息列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的消息（通过header用户ID）")
    public Result<List<Map<String, Object>>> getMyMessages(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            jakarta.servlet.http.HttpServletRequest request) {
        // 如果没有传 userId，尝试从请求属性中获取
        if (userId == null) {
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                userId = Long.valueOf(userIdAttr.toString());
            }
        }
        if (userId == null) {
            return Result.error("请登录");
        }
        return getMessageList(userId);
    }
}
