package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.Complaint;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.Orders;
import com.hengzhou.moli.entity.Refund;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.OrdersMapper;
import com.hengzhou.moli.service.ComplaintService;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.RefundService;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/complaint")
@RequiredArgsConstructor
@Tag(name = "客诉")
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;
    private final RefundService refundService;
    private final MessageService messageService;
    private final OrdersMapper ordersMapper;

    /**
     * 提交客诉
     */
    @PostMapping("/create")
    @Operation(summary = "提交客诉")
    public Result<String> create(
            @RequestBody Complaint body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("收到投诉创建请求 - userId: {}, body: {}", userId, body);

            if (userId == null) {
                log.warn("投诉创建失败：用户未登录");
                return Result.error("请先登录");
            }
            if (body.getShopId() == null) {
                log.warn("投诉创建失败：商家ID为空");
                return Result.error("请选择商家");
            }
            if (body.getReason() == null || body.getReason().isBlank()) {
                log.warn("投诉创建失败：投诉原因为空");
                return Result.error("请填写投诉原因");
            }

            // 补充用户名
            if (body.getUserId() == null) {
                body.setUserId(userId);
            }
            if (body.getUsername() == null || body.getUsername().isBlank()) {
                User user = userService.getById(userId);
                if (user != null) {
                    body.setUsername(user.getUsername());
                }
            }
            body.setAccusedId(body.getShopId());
            if (body.getType() == null) body.setType(0);
            if (body.getStatus() == null) body.setStatus(0);

            boolean ok = complaintService.createComplaint(body);
            if (ok) {
                log.info("投诉创建成功 - id: {}", body.getId());
                return Result.success("已提交，管理员将尽快处理");
            }
            log.error("投诉创建失败：service返回false");
            return Result.error("提交失败，请检查商家信息与事由是否填写完整");
        } catch (Exception e) {
            log.error("投诉创建异常", e);
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 获取商家（被告）的投诉列表 - 商家端查看
     * 商家只能看到涉及自己的投诉（shopId = 当前商家ID）
     */
    @GetMapping("/shop/{shopId}/list")
    @Operation(summary = "获取商家的投诉列表")
    public Result<List<Map<String, Object>>> getShopComplaints(@PathVariable Long shopId) {
        try {
            List<Complaint> all = complaintService.getAllComplaints();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Complaint c : all) {
                if (c.getShopId() != null && c.getShopId().equals(shopId)) {
                    result.add(buildComplaintMap(c, true));
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取用户的投诉列表 - 用户端查看
     */
    @GetMapping("/user/{userId}/list")
    @Operation(summary = "获取用户的投诉列表")
    public Result<List<Map<String, Object>>> getUserComplaints(@PathVariable Long userId) {
        try {
            List<Complaint> all = complaintService.getAllComplaints();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Complaint c : all) {
                if (c.getUserId() != null && c.getUserId().equals(userId)) {
                    result.add(buildComplaintMap(c, false));
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取所有投诉列表（管理员）
     */
    @GetMapping("/admin/list")
    @Operation(summary = "获取所有投诉列表（管理员）")
    public Result<List<Map<String, Object>>> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintService.getAllComplaints();
            if (complaints == null) complaints = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Complaint c : complaints) {
                result.add(buildComplaintMap(c, true));
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 处理投诉（管理员）
     * 当处理售后客服介入类投诉时，同时更新对应的售后记录状态
     * status: 1=仅退款, 2=驳回, 3=退货退款
     */
    @PutMapping("/admin/{id}/handle")
    @Operation(summary = "处理投诉（管理员）")
    public Result<String> handleComplaint(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String adminRemark) {
        try {
            if (status == null || (status != 1 && status != 2 && status != 3)) {
                return Result.error("无效的处理状态：1=仅退款, 2=驳回, 3=退货退款");
            }
            Complaint complaint = complaintService.getById(id);
            if (complaint == null) {
                return Result.error("投诉不存在");
            }

            // 投诉表状态映射：1,3 对应已处理(1)，2 对应已驳回(2)
            Integer complaintStatus = (status == 2) ? 2 : 1;
            boolean ok = complaintService.handleComplaint(id, complaintStatus, adminRemark);
            if (!ok) {
                return Result.error("处理失败");
            }

            // 如果是售后客服介入的投诉，同步更新售后记录
            if (complaint.getSourceType() == 1) {
                Long refundId = complaint.getRefundId();
                // 兜底逻辑：如果 refundId 缺失，尝试通过 orderId 查找该用户的活跃售后记录
                if (refundId == null) {
                    try {
                        List<Refund> refunds = refundService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund>()
                                .eq(Refund::getOrderId, complaint.getOrderId())
                                .eq(Refund::getUserId, complaint.getUserId())
                                .ne(Refund::getStatus, 4) // 非已取消
                                .orderByDesc(Refund::getCreateTime));
                        if (!refunds.isEmpty()) {
                            refundId = refunds.get(0).getId();
                        }
                    } catch (Exception ignored) {}
                }

                if (refundId != null) {
                    // 售后表状态映射：1=退款(2), 2=驳回(3)
                    Integer refundStatus;
                    String statusName;
                    String userMessage;
                    String shopMessage;

                    switch (status) {
                        case 1 -> {
                            refundStatus = 2; // 已同意
                            statusName = "已退款";
                            userMessage = "管理员已处理您的投诉，判定同意退款。款项将原路退回。";
                            shopMessage = "管理员已介入判定：同意用户退款。该订单已标记为已退款。";
                        }
                        default -> {
                            refundStatus = 3; // 已拒绝
                            statusName = "驳回";
                            userMessage = "管理员已处理您的投诉，驳回您的售后请求。" + (adminRemark != null ? "原因：" + adminRemark : "");
                            shopMessage = "管理员已介入判定：驳回用户的售后申请。该订单状态已更新。";
                        }
                    }

                    refundService.processRefund(refundId, refundStatus, adminRemark, "admin");

                    // 通知用户处理结果
                    try {
                        messageService.sendMessage(Message.builder()
                                .userId(complaint.getUserId())
                                .type(2)
                                .title("售后投诉处理结果")
                                .content(userMessage)
                                .relatedId(complaint.getOrderId())
                                .build());
                    } catch (Exception ignored) {}

                    // 通知商家处理结果
                    try {
                        messageService.sendMessage(Message.builder()
                                .userId(complaint.getShopId())
                                .type(2)
                                .title("售后投诉处理通知")
                                .content(shopMessage)
                                .relatedId(complaint.getOrderId())
                                .build());
                    } catch (Exception ignored) {}
                }
            }

            return Result.success("处理成功");
        } catch (Exception e) {
            return Result.error("处理失败：" + e.getMessage());
        }
    }

    /**
     * 构建投诉的 Map 返回结构（含关联信息）
     * @param includeShopInfo 是否补充商家信息（管理员端需要，商家/用户端按需）
     */
    private Map<String, Object> buildComplaintMap(Complaint c, boolean includeShopInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("userId", c.getUserId());
        map.put("username", c.getUsername());
        map.put("shopId", c.getShopId());
        map.put("accusedId", c.getAccusedId());
        map.put("orderId", c.getOrderId());
        map.put("refundId", c.getRefundId());
        map.put("sourceType", c.getSourceType());
        map.put("type", c.getType());
        map.put("reason", c.getReason());
        map.put("content", c.getDescription());
        map.put("description", c.getDescription());
        map.put("status", c.getStatus());
        map.put("adminRemark", c.getAdminRemark());
        map.put("createTime", c.getCreateTime());
        map.put("updateTime", c.getUpdateTime());

        // 补充订单信息
        if (c.getOrderId() != null) {
            try {
                Orders order = ordersMapper.selectById(c.getOrderId());
                if (order != null) {
                    map.put("orderNo", order.getOrderNo());
                    map.put("orderTotalAmount", order.getTotalAmount());
                    map.put("orderActualAmount", order.getActualAmount());
                    map.put("orderStatus", order.getStatus());
                    map.put("orderCreateTime", order.getCreateTime());
                }
            } catch (Exception e) {
                log.warn("获取订单信息失败，orderId={}", c.getOrderId(), e);
            }
        }

        // 补充售后信息（如果是售后客服介入）
        if (c.getRefundId() != null) {
            try {
                Refund refund = refundService.getById(c.getRefundId());
                if (refund != null) {
                    map.put("refundAmount", refund.getAmount());
                    map.put("refundReason", refund.getReason());
                    map.put("refundStatus", refund.getStatus());
                    map.put("refundAdminIntervention", refund.getAdminIntervention());
                }
            } catch (Exception e) {
                log.warn("获取售后信息失败，refundId={}", c.getRefundId(), e);
            }
        }

        return map;
    }
}
