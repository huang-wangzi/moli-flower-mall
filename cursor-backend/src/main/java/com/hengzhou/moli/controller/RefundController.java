package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.Orders;
import com.hengzhou.moli.entity.Refund;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.OrderService;
import com.hengzhou.moli.service.RefundService;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 售后管理控制器
 */
@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@Tag(name = "售后管理")
@CrossOrigin(origins = "*")
public class RefundController {

    private final RefundService refundService;
    private final MessageService messageService;
    private final OrderService orderService;
    private final UserService userService;

    // ============================================================
    // 用户端
    // ============================================================

    @PostMapping("/create")
    @Operation(summary = "创建售后申请")
    public Result<Map<String, Object>> createRefund(@RequestBody Refund refund) {
        if (refund.getShopId() == null || refund.getOrderId() == null || refund.getUserId() == null) {
            return Result.error("订单或商家信息不完整");
        }
        boolean success = refundService.createRefund(refund);
        if (success) {
            orderService.setRefunding(refund.getOrderId());
            try {
                messageService.sendMessage(Message.builder()
                        .userId(refund.getShopId())
                        .type(2)
                        .title("您有新售后申请")
                        .content("用户提交了售后申请，请及时处理。")
                        .relatedId(refund.getOrderId())
                        .build());
            } catch (Exception ignored) {}
            Map<String, Object> data = new HashMap<>();
            data.put("id", refund.getId());
            return Result.success(data);
        }
        return Result.error("提交失败");
    }

    @GetMapping("/user/list")
    @Operation(summary = "获取用户的售后列表")
    public Result<List<Map<String, Object>>> getUserRefunds(@RequestParam Long userId) {
        List<Refund> refunds = refundService.getRefundsByUserId(userId);
        List<Map<String, Object>> result = buildRefundListWithDetail(refunds);
        return Result.success(result);
    }

    // ============================================================
    // 商家端
    // ============================================================

    @GetMapping("/shop/list")
    @Operation(summary = "获取商家的售后列表")
    public Result<List<Map<String, Object>>> getShopRefunds(@RequestParam Long shopId) {
        List<Refund> refunds = refundService.getAllRefundsByShopId(shopId);
        List<Map<String, Object>> result = buildRefundListWithDetail(refunds);
        return Result.success(result);
    }

    /**
     * 商家处理售后申请
     * 前提：售后未申请管理员介入（adminIntervention != 1）
     * 处理方式：同意退款(status=2) / 拒绝(status=3)
     */
    @PutMapping("/shop/{id}/process")
    @Operation(summary = "商家处理售后申请（仅限非介入状态）")
    public Result<String> shopProcessRefund(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark,
            @RequestParam Long userId,
            @RequestParam Long shopId) {
        Refund refund = refundService.getById(id);
        if (refund == null) return Result.error("售后记录不存在");
        if (refund.getAdminIntervention() != null && refund.getAdminIntervention() == 1) {
            return Result.error("该售后已申请管理员介入，需由管理员处理");
        }
        if (!shopId.equals(refund.getShopId())) {
            return Result.error("无权操作此售后");
        }
        if (status != 2 && status != 3) {
            return Result.error("无效的处理状态");
        }
        boolean ok = refundService.processRefund(id, status, remark, "shop");
        if (ok) {
            sendUserNotification(refund.getUserId(), status, remark, refund.getOrderId());
            return Result.success("处理成功");
        }
        return Result.error("处理失败");
    }

    // ============================================================
    // 管理员端
    // ============================================================

    @GetMapping("/admin/list")
    @Operation(summary = "获取所有售后列表（管理员）")
    public Result<List<Map<String, Object>>> getAdminRefunds() {
        List<Refund> refunds = refundService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund>()
                        .ne(Refund::getStatus, 4) // 管理员端也不显示用户已取消的售后记录
                        .orderByDesc(Refund::getCreateTime));
        List<Map<String, Object>> result = buildRefundListWithDetail(refunds);
        return Result.success(result);
    }

    /**
     * 管理员处理售后申请
     * 处理方式：
     *   action=refund        -> 商家退款（status=2）
     *   action=refund_return -> 商家退货退款（status=4）
     *   action=reject        -> 驳回（status=3）
     * 处理后同步更新 complaint 记录状态
     */
    @PutMapping("/admin/{id}/process")
    @Operation(summary = "管理员处理售后申请")
    public Result<String> adminProcessRefund(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String remark) {
        if (action == null) return Result.error("请指定处理方式");
        Integer status;
        String actionName;
        switch (action) {
            case "refund"        -> { status = 2; actionName = "同意退款"; }
            case "refund_return" -> { status = 4; actionName = "同意退货退款"; }
            case "reject"        -> { status = 3; actionName = "驳回"; }
            default              -> { return Result.error("无效的处理方式：refund / refund_return / reject"); }
        }
        Refund refund = refundService.getById(id);
        if (refund == null) return Result.error("售后记录不存在");

        boolean ok = refundService.processRefund(id, status, remark, "admin");
        if (!ok) return Result.error("处理失败");

        // 同步更新投诉记录状态
        refundService.syncComplaintStatus(id, status, remark);

        // 通知用户处理结果
        sendUserNotification(refund.getUserId(), status, remark, refund.getOrderId());

        return Result.success(actionName + "成功");
    }

    // ============================================================
    // 用户申请管理员介入
    // ============================================================

    @PostMapping("/{id}/request-intervention")
    @Operation(summary = "用户申请管理员介入售后")
    public Result<String> requestAdminIntervention(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.error("请先登录");
        Refund refund = refundService.getById(id);
        if (refund == null) return Result.error("售后记录不存在");
        if (!userId.equals(refund.getUserId())) return Result.error("无权操作此售后");
        if (refund.getAdminIntervention() != null && refund.getAdminIntervention() == 1) {
            return Result.error("该售后已在管理员介入处理中");
        }
        String reason = (body != null && body.get("reason") != null)
                ? body.get("reason") : "售后客服介入：商品质量问题";
        String description = (body != null && body.get("description") != null)
                ? body.get("description") : null;
        User user = userService.getById(userId);
        boolean ok = refundService.requestAdminIntervention(id, userId, user, reason, description);
        return ok ? Result.success("已提交，管理员将尽快处理") : Result.error("提交失败");
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "用户取消售后申请")
    public Result<String> cancelRefund(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.error("请先登录");
        Refund refund = refundService.getById(id);
        if (refund == null) return Result.error("售后记录不存在");
        if (!userId.equals(refund.getUserId())) return Result.error("无权操作此售后");
        if (refund.getStatus() != 0 && refund.getStatus() != 1) {
            return Result.error("售后已在处理中或已结束，无法取消");
        }
        boolean ok = refundService.cancelRefund(id);
        return ok ? Result.success("已取消售后申请") : Result.error("操作失败");
    }

    @PostMapping("/re-appeal")
    @Operation(summary = "用户再次申诉（重新创建售后并介入）")
    public Result<String> reAppeal(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.error("请先登录");
        
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        String reason = body.get("reason") != null ? body.get("reason").toString() : "用户再次申诉";
        String description = body.get("description") != null ? body.get("description").toString() : "";
        
        if (orderId == null) return Result.error("缺少订单ID");
        
        boolean ok = refundService.reAppeal(orderId, userId, reason, description);
        return ok ? Result.success("已重新提交售后申诉，管理员将尽快处理") : Result.error("提交失败");
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 统一构建售后列表的详情（订单信息、用户信息、处理状态等）
     */
    private List<Map<String, Object>> buildRefundListWithDetail(List<Refund> refunds) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (refunds == null) return result;
        for (Refund r : refunds) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("orderId", r.getOrderId());
            map.put("userId", r.getUserId());
            map.put("shopId", r.getShopId());
            map.put("refundType", r.getRefundType());
            map.put("reason", r.getReason());
            map.put("description", r.getDescription());
            map.put("amount", r.getAmount());
            map.put("status", r.getStatus());
            map.put("adminIntervention", r.getAdminIntervention());
            map.put("adminRemark", r.getAdminRemark());
            map.put("createTime", r.getCreateTime());
            map.put("updateTime", r.getUpdateTime());
            // 前端显示判断字段：是否需要管理员介入
            map.put("needAdminIntervention",
                    r.getAdminIntervention() != null && r.getAdminIntervention() == 1);
            // 商家是否可处理（未介入 且 待处理状态）
            map.put("shopCanHandle",
                    (r.getAdminIntervention() == null || r.getAdminIntervention() != 1)
                    && r.getStatus() != null && r.getStatus() == 0);

            // 订单信息
            if (r.getOrderId() != null) {
                Orders order = orderService.getById(r.getOrderId());
                if (order != null) {
                    map.put("orderNo", order.getOrderNo());
                    map.put("orderStatus", order.getStatus());
                }
            }
            // 用户信息
            if (r.getUserId() != null) {
                User user = userService.getById(r.getUserId());
                if (user != null) {
                    map.put("username", user.getUsername());
                    map.put("nickname", user.getNickname());
                    map.put("phone", user.getPhone());
                    map.put("userAvatar", user.getAvatar());
                }
            }
            // 商家信息
            if (r.getShopId() != null) {
                User shop = userService.getById(r.getShopId());
                if (shop != null) {
                    map.put("shopName",
                            shop.getShopName() != null && !shop.getShopName().isBlank()
                                    ? shop.getShopName() : shop.getUsername());
                }
            }
            result.add(map);
        }
        return result;
    }

    /**
     * 发送售后处理结果通知给用户
     */
    private void sendUserNotification(Long userId, Integer status, String remark, Long orderId) {
        if (userId == null) return;
        String content;
        String title;
        switch (status) {
            case 2 -> { title = "售后申请已通过"; content = "您的售后申请已处理，款项将退还到您的账户。"; }
            case 3 -> { title = "售后申请已驳回"; content = "您的售后申请已驳回。" + (remark != null && !remark.isBlank() ? "原因：" + remark : ""); }
            case 4 -> { title = "售后申请已通过"; content = "您的售后申请已通过，请按商家指引寄回商品，款项将在收到退货后退还。"; }
            default -> { title = "售后申请状态更新"; content = "您的售后申请状态已更新。"; }
        }
        try {
            messageService.sendMessage(Message.builder()
                    .userId(userId)
                    .type(2)
                    .title(title)
                    .content(content)
                    .relatedId(orderId)
                    .build());
        } catch (Exception ignored) {}
    }
}
