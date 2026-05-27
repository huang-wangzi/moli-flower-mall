package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.*;
import com.hengzhou.moli.mapper.OrdersMapper;
import com.hengzhou.moli.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员功能")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final MessageService messageService;
    private final ComplaintService complaintService;
    private final RefundService refundService;
    private final OrdersMapper ordersMapper;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户列表")
    public Result<?> getUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        var page = userService.getUserPage(pageNum, pageSize, keyword, 1);
        return Result.success(page);
    }

    /**
     * 获取商家列表
     */
    @GetMapping("/shops")
    @Operation(summary = "获取商家列表")
    public Result<?> getShops(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        var page = userService.getShopPage(pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 审核商家通过
     */
    @PutMapping("/shop/{id}/approve")
    @Operation(summary = "审核商家通过")
    public Result<String> approveShop(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("商家不存在");
        }
        if (user.getRole() != 2) {
            return Result.error("该用户不是商家");
        }

        user.setStatus(1);
        user.setUpdateTime(java.time.LocalDateTime.now());

        if (user.getShopType() != null && user.getShopType() == 2) {
            user.setShopQualificationStatus(2);
        } else {
            user.setShopQualificationStatus(0);
        }

        boolean success = userService.updateById(user);

        if (success) {
            String message = user.getShopType() != null && user.getShopType() == 2
                    ? "收购商审核通过！您现在可以登录并发布收购需求了。"
                    : "商家入驻审核通过！请登录后提交资质材料以解锁全部功能。";
            messageService.sendAuditNotification(id, true, message);
            return Result.success(message);
        }
        return Result.error("操作失败");
    }

    /**
     * 审核商家拒绝
     */
    @PutMapping("/shop/{id}/reject")
    @Operation(summary = "审核商家拒绝")
    public Result<String> rejectShop(@PathVariable Long id, @RequestBody(required = false) String reason) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("商家不存在");
        }
        if (user.getRole() != 2) {
            return Result.error("该用户不是商家");
        }

        user.setStatus(0);
        user.setUpdateTime(java.time.LocalDateTime.now());
        boolean success = userService.updateById(user);

        if (success) {
            String rejectMsg = user.getShopType() != null && user.getShopType() == 2
                    ? "收购商入驻审核未通过。原因：" + (reason != null ? reason : "请重新提交申请。")
                    : "商家入驻审核未通过。原因：" + (reason != null ? reason : "请重新提交申请。");
            messageService.sendAuditNotification(id, false, rejectMsg);
            return Result.success(user.getShopType() != null && user.getShopType() == 2
                    ? "收购商审核已拒绝"
                    : "商家审核已拒绝");
        }
        return Result.error("操作失败");
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/user/{id}/status")
    @Operation(summary = "修改用户状态")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(status);
        boolean success = userService.updateById(user);

        if (success) {
            String msg = status == 1 ? "用户已启用" : "用户已禁用";
            return Result.success(msg);
        }
        return Result.error("操作失败");
    }

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/products/pending")
    @Operation(summary = "获取待审核商品")
    public Result<?> getPendingProducts() {
        List<Product> products = productService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 0)
                        .eq(Product::getDeleted, 0)
                        .orderByDesc(Product::getCreateTime)
        );
        return Result.success(products);
    }

    /**
     * 获取所有商品
     */
    @GetMapping("/products")
    @Operation(summary = "获取所有商品")
    public Result<?> getAllProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(productService.page(page, wrapper));
    }

    /**
     * 审核商品通过
     */
    @PutMapping("/product/{id}/approve")
    @Operation(summary = "审核商品通过")
    public Result<String> approveProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        product.setStatus(1);
        boolean success = productService.updateById(product);

        if (success) {
            messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                    .userId(product.getShopId())
                    .type(4)
                    .title("商品审核通过")
                    .content("您发布的商品「" + product.getName() + "」已审核通过，现在可以对外销售了。")
                    .relatedId(id)
                    .build());
            return Result.success("商品已审核通过");
        }
        return Result.error("操作失败");
    }

    /**
     * 审核商品拒绝
     */
    @PutMapping("/product/{id}/reject")
    @Operation(summary = "审核商品拒绝")
    public Result<String> rejectProduct(@PathVariable Long id, @RequestBody(required = false) String reason) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        product.setStatus(2);
        boolean success = productService.updateById(product);

        if (success) {
            messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                    .userId(product.getShopId())
                    .type(4)
                    .title("商品审核未通过")
                    .content("您发布的商品「" + product.getName() + "」未通过审核，请修改后重新提交。" + (reason != null ? "原因：" + reason : ""))
                    .relatedId(id)
                    .build());
            return Result.success("商品已拒绝");
        }
        return Result.error("操作失败");
    }

    /**
     * 下架商品
     */
    @PutMapping("/product/{id}/offline")
    @Operation(summary = "下架商品")
    public Result<String> offlineProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        product.setStatus(0);
        boolean success = productService.updateById(product);

        if (success) {
            return Result.success("商品已下架");
        }
        return Result.error("操作失败");
    }

    /**
     * 获取所有评价
     */
    @GetMapping("/reviews")
    @Operation(summary = "获取所有评价")
    public Result<?> getAllReviews() {
        try {
            List<Review> reviews = reviewService.getAllReviews();
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/review/{id}")
    @Operation(summary = "删除评价")
    public Result<String> deleteReview(@PathVariable Long id) {
        boolean success = reviewService.deleteReview(id);
        if (success) {
            return Result.success("评价已删除");
        }
        return Result.error("删除失败");
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long userCount = userService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, 1)
                        .eq(User::getDeleted, 0)
        );
        stats.put("userCount", userCount);

        long shopCount = userService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, 2)
                        .eq(User::getShopType, 1)
                        .eq(User::getDeleted, 0)
        );
        stats.put("shopCount", shopCount);

        long acquirerCount = userService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, 2)
                        .eq(User::getShopType, 2)
                        .eq(User::getStatus, 1)
                        .eq(User::getDeleted, 0)
        );
        stats.put("acquirerCount", acquirerCount);

        long pendingShopCount = userService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, 2)
                        .eq(User::getStatus, 2)
                        .eq(User::getDeleted, 0)
        );
        stats.put("pendingShopCount", pendingShopCount);

        long productCount = productService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getDeleted, 0)
        );
        stats.put("productCount", productCount);

        long pendingProductCount = productService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 0)
                        .eq(Product::getDeleted, 0)
        );
        stats.put("pendingProductCount", pendingProductCount);

        return Result.success(stats);
    }

    /**
     * 客诉列表
     */
    @GetMapping("/complaints")
    @Operation(summary = "客诉列表")
    public Result<?> listComplaints() {
        try {
            var complaints = complaintService.listForAdmin();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Complaint c : complaints) {
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
                        // 忽略
                    }
                }

                // 补充售后信息
                if (c.getRefundId() != null) {
                    try {
                        Refund refund = refundService.getById(c.getRefundId());
                        if (refund != null) {
                            map.put("refundAmount", refund.getAmount());
                            map.put("refundReason", refund.getReason());
                            map.put("refundStatus", refund.getStatus());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }

                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 处理客诉
     */
    @PutMapping("/complaints/{id}")
    @Operation(summary = "处理客诉")
    public Result<String> handleComplaint(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        try {
            Complaint complaint = complaintService.getById(id);
            if (complaint == null) {
                return Result.error("投诉不存在");
            }

            // 投诉表状态映射：1 对应已处理(1)，2 对应已驳回(2)
            Integer complaintStatus = (status == 2) ? 2 : 1;
            boolean ok = complaintService.handleComplaint(id, complaintStatus, remark);
            if (!ok) {
                return Result.error("处理失败");
            }

            // 如果是售后客服介入的投诉，同步更新售后记录
            if (complaint.getSourceType() == 1) {
                Long refundId = complaint.getRefundId();
                // 兜底查找
                if (refundId == null) {
                    try {
                        List<Refund> refunds = refundService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund>()
                                .eq(Refund::getOrderId, complaint.getOrderId())
                                .eq(Refund::getUserId, complaint.getUserId())
                                .ne(Refund::getStatus, 4)
                                .orderByDesc(Refund::getCreateTime));
                        if (!refunds.isEmpty()) {
                            refundId = refunds.get(0).getId();
                        }
                    } catch (Exception ignored) {}
                }

                if (refundId != null) {
                    // 售后状态同步逻辑
                    Integer refundStatus = (status == 1) ? 2 : 3;
                    refundService.processRefund(refundId, refundStatus, remark, "admin");
                    
                    // 发送消息通知
                    String userMsg = (status == 1) ? "管理员已同意您的退款申请" : "管理员已驳回您的售后申请";
                    String shopMsg = (status == 1) ? "管理员已介入并同意退款" : "管理员已介入并驳回售后";
                    
                    try {
                        messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                                .userId(complaint.getUserId()).type(2).title("投诉处理通知").content(userMsg).relatedId(complaint.getOrderId()).build());
                        messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                                .userId(complaint.getShopId()).type(2).title("投诉处理通知").content(shopMsg).relatedId(complaint.getOrderId()).build());
                    } catch (Exception ignored) {}
                }
            }
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error("处理失败：" + e.getMessage());
        }
    }

    /**
     * 发送系统消息
     */
    @PostMapping("/system-message")
    @Operation(summary = "发送系统消息")
    public Result<?> sendSystemMessage(
            @RequestParam String scope,
            @RequestParam String title,
            @RequestParam String content,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return Result.error("标题和内容不能为空");
        }
        try {
            messageService.sendBroadcastSystemMessage(scope, title.trim(), content.trim(), adminId);
            return Result.success("系统消息已发送，商家可在消息中心查看并回复");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取系统消息列表
     */
    @GetMapping("/system-messages")
    @Operation(summary = "获取系统消息列表")
    public Result<?> getSystemMessages() {
        return Result.success(messageService.listForAdmin());
    }

    /**
     * 删除系统消息
     */
    @DeleteMapping("/system-message/{id}")
    @Operation(summary = "删除系统消息")
    public Result<String> deleteSystemMessage(@PathVariable Long id) {
        boolean ok = messageService.deleteMessage(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-growth-trend")
    @Operation(summary = "获取用户增长趋势")
    public Result<Map<String, Object>> getUserGrowthTrend() {
        List<User> users = userService.list();
        
        Map<String, Long> dateCount = new HashMap<>();
        for (User user : users) {
            if (user.getCreateTime() != null) {
                String date = user.getCreateTime().toLocalDate().toString();
                dateCount.put(date, dateCount.getOrDefault(date, 0L) + 1);
            }
        }
        
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.toString();
            dates.add(dateStr.substring(5));
            counts.add(dateCount.getOrDefault(dateStr, 0L));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return Result.success(result);
    }

    /**
     * 获取商品增长趋势
     */
    @GetMapping("/product-trend")
    @Operation(summary = "获取商品增长趋势")
    public Result<Map<String, Object>> getProductTrend() {
        List<Product> products = productService.list();
        
        Map<String, Long> dateCount = new HashMap<>();
        for (Product product : products) {
            if (product.getCreateTime() != null) {
                String date = product.getCreateTime().toLocalDate().toString();
                dateCount.put(date, dateCount.getOrDefault(date, 0L) + 1);
            }
        }
        
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.toString();
            dates.add(dateStr.substring(5));
            counts.add(dateCount.getOrDefault(dateStr, 0L));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return Result.success(result);
    }
}
