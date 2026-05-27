package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.AcquisitionApplication;
import com.hengzhou.moli.entity.AcquisitionDemand;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.AcquisitionApplicationService;
import com.hengzhou.moli.service.AcquisitionDemandService;
import com.hengzhou.moli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/acquisition")
@CrossOrigin(origins = "*")
public class AcquisitionApplicationController {

    @Autowired
    private AcquisitionDemandService demandService;

    @Autowired
    private AcquisitionApplicationService applicationService;

    @Autowired(required = false)
    private UserService userService;

    // ==================== 首页接口 ====================
    
    /**
     * 获取收购需求列表（用户端 - 仅返回审核通过的）
     */
    @GetMapping("/demands/active")
    public Result<List<AcquisitionDemand>> getActiveDemands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AcquisitionDemand> pageParam = new Page<>(page, size);
            IPage<AcquisitionDemand> resultPage = demandService.selectActiveDemands(pageParam, LocalDate.now());
            return Result.success(resultPage.getRecords());
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取收购需求列表（别名）
     */
    @GetMapping("/demands")
    public Result<List<AcquisitionDemand>> getDemands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return getActiveDemands(page, size);
    }

    /**
     * 获取收购需求详情
     */
    @GetMapping("/demand/{id}")
    public Result<AcquisitionDemand> getDemand(@PathVariable Long id) {
        try {
            AcquisitionDemand demand = demandService.getDemandById(id);
            if (demand == null) {
                return Result.error("需求不存在");
            }
            return Result.success(demand);
        } catch (Exception e) {
            return Result.error("获取需求详情失败");
        }
    }

    // ==================== 申请相关 ====================

    /**
     * 获取我的申请列表
     */
    @GetMapping("/application/my")
    public Result<IPage<AcquisitionApplication>> getMyApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 如果没有传 userId，尝试从请求属性中获取
            if (userId == null) {
                Object userIdAttr = request.getAttribute("userId");
                if (userIdAttr != null) {
                    userId = Long.valueOf(userIdAttr.toString());
                }
            }
            System.out.println("[Controller] getMyApplications called, userId=" + userId + ", page=" + page);
            // userId 为空时返回空数据
            if (userId == null) {
                return Result.success(new Page<>());
            }
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            IPage<AcquisitionApplication> result = applicationService.selectByUserId(pageParam, userId);
            System.out.println("[Controller] getMyApplications result, total=" + result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            System.out.println("[Controller] getMyApplications error: " + e.getMessage());
            e.printStackTrace();
            // 数据库错误也返回空数据
            return Result.success(new Page<>());
        }
    }

    /**
     * 创建收购申请
     */
    @PostMapping("/application")
    public Result<AcquisitionApplication> createApplication(
            @RequestBody AcquisitionApplication application,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            application.setUserId(userId);
            AcquisitionApplication result = applicationService.createApplication(application);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建申请失败：" + e.getMessage());
        }
    }

    /**
     * 取消申请
     */
    @PutMapping("/application/{id}/cancel")
    public Result<Void> cancelApplication(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            applicationService.cancelApplication(id, userId);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消申请失败");
        }
    }

    /**
     * 修改申请（用户更新供货信息）
     */
    @PutMapping("/application/{id}")
    public Result<AcquisitionApplication> updateApplication(
            @PathVariable Long id,
            @RequestBody UpdateApplicationDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            if (dto.getQuantity() == null && dto.getContactPhone() == null && dto.getPhotoUrls() == null) {
                return Result.error("请填写要修改的内容");
            }
            AcquisitionApplication result = applicationService.updateApplication(
                id, userId, dto.getQuantity(), dto.getContactPhone(), dto.getPhotoUrls()
            );
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改申请失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的统计信息
     */
    @GetMapping("/application/my/stats")
    public Result<Map<String, Object>> getMyStats(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.success(Map.of("pending", 0, "agreed", 0, "completed", 0, "total", 0));
            }
            Map<String, Object> stats = applicationService.getUserStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.success(Map.of("pending", 0, "agreed", 0, "completed", 0, "total", 0));
        }
    }

    /**
     * 获取每日统计
     */
    @GetMapping("/application/my/stats/daily")
    public Result<List<Map<String, Object>>> getMyDailyStats(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        System.out.println("[Controller] getMyDailyStats: userId=" + userId + ", startDate=" + startDate + ", endDate=" + endDate);
        try {
            if (userId == null) {
                System.out.println("[Controller] userId为null，返回空列表");
                return Result.success(new ArrayList<>());
            }
            if (startDate == null) startDate = LocalDate.now().minusDays(30).toString();
            if (endDate == null) endDate = LocalDate.now().toString();
            List<Map<String, Object>> stats = applicationService.getDailyStats(userId, startDate, endDate);
            System.out.println("[Controller] 返回数据条数: " + (stats != null ? stats.size() : 0));
            return Result.success(stats);
        } catch (Exception e) {
            System.err.println("[Controller] getMyDailyStats异常: " + e.getMessage());
            e.printStackTrace();
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 检查是否已申请
     */
    @GetMapping("/application/check/{demandId}")
    public Result<Boolean> checkApplied(
            @PathVariable Long demandId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            boolean hasApplied = applicationService.hasApplied(demandId, userId);
            return Result.success(hasApplied);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

    // ==================== 收购商接口 ====================

    /**
     * 获取需求的申请列表（收购商）
     */
    @GetMapping("/merchant/application/{demandId}")
    public Result<IPage<AcquisitionApplication>> getDemandApplications(
            @PathVariable Long demandId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录商家账号");
            AcquisitionDemand demand = demandService.getById(demandId);
            if (demand == null || !demand.getShopId().equals(shopId)) return Result.error("无权查看");
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            IPage<AcquisitionApplication> result = applicationService.selectByDemandId(pageParam, demandId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new Page<>());
        }
    }

    /**
     * 获取申请列表（根据需求ID）
     */
    @GetMapping("/application/demand/{demandId}/list")
    public Result<List<AcquisitionApplication>> getApplicationList(@PathVariable Long demandId) {
        try {
            Page<AcquisitionApplication> pageParam = new Page<>(1, 1000);
            IPage<AcquisitionApplication> result = applicationService.selectByDemandId(pageParam, demandId);
            return Result.success(result.getRecords());
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 同意申请
     */
    @PutMapping("/merchant/application/{id}/agree")
    public Result<Void> agreeApplication(
            @PathVariable Long id,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            applicationService.agreeApplication(id, shopId);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("同意申请失败");
        }
    }

    /**
     * 拒绝申请
     */
    @PutMapping("/merchant/application/{id}/reject")
    public Result<Void> rejectApplication(
            @PathVariable Long id,
            @RequestBody(required = false) String rejectReason,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            applicationService.rejectApplication(id, shopId, rejectReason != null ? rejectReason : "商家拒绝");
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("拒绝申请失败");
        }
    }

    /**
     * 完成交付
     */
    @PutMapping("/merchant/application/{id}/complete")
    public Result<Void> completeApplication(
            @PathVariable Long id,
            @RequestBody CompleteDTO dto,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            applicationService.completeDelivery(id, shopId, dto.getActualQuantity(), dto.getActualPrice());
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("完成交付失败");
        }
    }

    /**
     * 获取待处理申请数量
     */
    @GetMapping("/merchant/application/pending/count")
    public Result<Map<String, Integer>> getPendingCount(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        // 如果没有传 shopId，尝试从请求属性中获取
        if (shopId == null) {
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                shopId = Long.valueOf(userIdAttr.toString());
            }
        }
        if (shopId == null) {
            return Result.success(Map.of("count", 0));
        }
        try {
            // 查询该收购商所有需求ID
            List<Long> demandIds = demandService.getDemandIdsByShopId(shopId);
            if (demandIds.isEmpty()) {
                return Result.success(Map.of("count", 0));
            }
            // 查询待审核申请数量
            long count = applicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AcquisitionApplication>()
                    .in(AcquisitionApplication::getDemandId, demandIds)
                    .eq(AcquisitionApplication::getStatus, 0)  // 待审核
            );
            return Result.success(Map.of("count", (int) count));
        } catch (Exception e) {
            return Result.success(Map.of("count", 0));
        }
    }

    // ==================== 收购商需求管理接口 ====================

    /**
     * 获取收购商的需求列表
     */
    @GetMapping("/merchant/demands")
    public Result<IPage<AcquisitionDemand>> getMerchantDemands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            Page<AcquisitionDemand> pageParam = new Page<>(page, size);
            IPage<AcquisitionDemand> result = demandService.selectByShopId(pageParam, shopId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new Page<>());
        }
    }

    /**
     * 获取今日需求数量
     */
    @GetMapping("/merchant/demands/today/count")
    public Result<Map<String, Integer>> getTodayDemandCount(@RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            int count = demandService.countTodayDemands(shopId, LocalDate.now());
            return Result.success(Map.of("count", count));
        } catch (Exception e) {
            return Result.success(Map.of("count", 0));
        }
    }

    /**
     * 创建收购需求
     */
    @PostMapping("/merchant/demand")
    public Result<AcquisitionDemand> createDemand(
            @RequestBody AcquisitionDemand demand,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 如果没有传 shopId，尝试从请求属性中获取（JWT 过滤器设置的）
            if (shopId == null) {
                Object userIdAttr = request.getAttribute("userId");
                if (userIdAttr != null) {
                    shopId = Long.valueOf(userIdAttr.toString());
                }
            }
            if (shopId == null) return Result.error("请登录");
            demand.setShopId(shopId);

            // 获取用户信息设置店铺名称
            if (userService != null) {
                User user = userService.getById(shopId);
                if (user != null) {
                    // 优先使用 merchantName，其次 shopName
                    String shopName = user.getMerchantName();
                    if (shopName == null || shopName.isEmpty()) {
                        shopName = user.getShopName();
                    }
                    demand.setShopName(shopName);
                }
            }

            AcquisitionDemand result = demandService.createDemand(demand);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建需求失败：" + e.getMessage());
        }
    }

    private Long getShopId(Long headerShopId, jakarta.servlet.http.HttpServletRequest request) {
        if (headerShopId != null) return headerShopId;
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr != null) {
            return Long.valueOf(userIdAttr.toString());
        }
        return null;
    }

    /**
     * 更新收购需求
     */
    @PutMapping("/merchant/demand/{id}")
    public Result<Void> updateDemand(
            @PathVariable Long id,
            @RequestBody AcquisitionDemand demand,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long actualShopId = getShopId(shopId, request);
            if (actualShopId == null) return Result.error("请登录");
            demand.setId(id);
            AcquisitionDemand existing = demandService.getById(id);
            if (existing == null || !existing.getShopId().equals(actualShopId)) {
                return Result.error("无权修改此需求");
            }
            demandService.updateDemand(demand);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新需求失败");
        }
    }

    /**
     * 删除收购需求
     */
    @DeleteMapping("/merchant/demand/{id}")
    public Result<Void> deleteDemand(
            @PathVariable Long id,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long actualShopId = getShopId(shopId, request);
            if (actualShopId == null) return Result.error("请登录");
            AcquisitionDemand existing = demandService.getById(id);
            if (existing == null || !existing.getShopId().equals(actualShopId)) {
                return Result.error("无权删除此需求");
            }
            demandService.deleteDemand(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除需求失败");
        }
    }

    /**
     * 下架收购需求
     */
    @PutMapping("/merchant/demand/{id}/offshelf")
    public Result<Void> offShelfDemand(
            @PathVariable Long id,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long actualShopId = getShopId(shopId, request);
            if (actualShopId == null) return Result.error("请登录");
            AcquisitionDemand existing = demandService.getById(id);
            if (existing == null || !existing.getShopId().equals(actualShopId)) {
                return Result.error("无权操作此需求");
            }
            demandService.offShelfDemand(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("下架需求失败");
        }
    }

    /**
     * 获取收购商统计信息
     */
    @GetMapping("/merchant/stats")
    public Result<Map<String, Object>> getMerchantStats(@RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            Map<String, Object> stats = applicationService.getMerchantStats(shopId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.success(Map.of(
                "todayQuantity", 0,
                "todayAmount", 0,
                "pendingOrders", 0,
                "totalQuantity", 0,
                "totalAmount", 0
            ));
        }
    }

    /**
     * 获取收购商每日统计数据
     */
    @GetMapping("/merchant/stats/daily")
    public Result<List<Map<String, Object>>> getMerchantDailyStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        try {
            if (shopId == null) return Result.error("请登录");
            if (startDate == null) startDate = LocalDate.now().minusDays(6).toString();
            if (endDate == null) endDate = LocalDate.now().toString();
            List<Map<String, Object>> stats = applicationService.getMerchantDailyStats(shopId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取收购商订单列表（已完成的申请）
     */
    @GetMapping("/merchant/orders")
    public Result<IPage<AcquisitionApplication>> getMerchantOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 如果没有传 shopId，尝试从请求属性中获取
            if (shopId == null) {
                Object userIdAttr = request.getAttribute("userId");
                if (userIdAttr != null) {
                    shopId = Long.valueOf(userIdAttr.toString());
                }
            }
            if (shopId == null) return Result.error("请登录");
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            // 使用 service 方法（支持状态筛选）
            IPage<AcquisitionApplication> result = applicationService.selectByShopId(pageParam, shopId, status);
            return Result.success(result);
        } catch (Exception e) {
            System.out.println("[Controller] getMerchantOrders error: " + e.getMessage());
            e.printStackTrace();
            return Result.success(new Page<>());
        }
    }

    // ==================== 用户订单和收入接口 ====================

    /**
     * 获取我的收购订单列表
     */
    @GetMapping("/order/my")
    public Result<IPage<AcquisitionApplication>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AcquisitionApplication> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionApplication::getUserId, userId);
            if (status != null) {
                wrapper.eq(AcquisitionApplication::getStatus, status);
            }
            wrapper.orderByDesc(AcquisitionApplication::getCreatedAt);
            IPage<AcquisitionApplication> result = applicationService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new Page<>());
        }
    }

    /**
     * 获取收入统计
     */
    @GetMapping("/income/stats")
    public Result<Map<String, Object>> getIncomeStats(
            @RequestParam(defaultValue = "daily") String type,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            Map<String, Object> stats = applicationService.getUserStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.success(Map.of("total", 0, "pending", 0, "agreed", 0, "completed", 0));
        }
    }

    /**
     * 获取收入明细
     */
    @GetMapping("/income/details")
    public Result<IPage<AcquisitionApplication>> getIncomeDetails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) return Result.error("请登录");
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AcquisitionApplication> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionApplication::getUserId, userId)
                   .eq(AcquisitionApplication::getStatus, 3) // 已完成的订单
                   .orderByDesc(AcquisitionApplication::getCompleteTime);
            IPage<AcquisitionApplication> result = applicationService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new Page<>());
        }
    }

    /**
     * 完成订单（别名）
     */
    @PutMapping("/order/{id}/complete")
    public Result<Void> completeOrder(
            @PathVariable Long id,
            @RequestBody CompleteDTO dto,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {
        return completeApplication(id, dto, shopId);
    }

    public static class CompleteDTO {
        private BigDecimal actualQuantity;
        private BigDecimal actualPrice;

        public BigDecimal getActualQuantity() { return actualQuantity; }
        public void setActualQuantity(BigDecimal actualQuantity) { this.actualQuantity = actualQuantity; }
        public BigDecimal getActualPrice() { return actualPrice; }
        public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
    }

    /**
     * 修改申请DTO
     */
    public static class UpdateApplicationDTO {
        private BigDecimal quantity;       // 新的供货数量
        private String contactPhone;        // 新的联系电话
        private String photoUrls;          // 新的茉莉花照片

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getPhotoUrls() { return photoUrls; }
        public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    }

    // ==================== 用户端价格统计接口 ====================

    /**
     * 获取收购商价格统计（从收购需求数据获取）
     */
    @GetMapping("/price-stats")
    public Result<Map<String, Object>> getAcquirerPriceStats() {
        try {
            Map<String, Object> stats = demandService.getPriceStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.success(Map.of(
                "minPrice", null,
                "maxPrice", null,
                "avgPrice", null,
                "totalCount", 0
            ));
        }
    }

    /**
     * 获取收购商每日价格趋势
     */
    @GetMapping("/price-trend")
    public Result<List<Map<String, Object>>> getAcquirerPriceTrend(
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<Map<String, Object>> trend = demandService.getDailyPriceTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取所有收购需求
     */
    @GetMapping("/admin/demands")
    public Result<List<AcquisitionDemand>> getAllDemands(
            @RequestParam(required = false) Integer status) {
        try {
            List<AcquisitionDemand> demands = demandService.getAllDemandsForAdmin(status);
            return Result.success(demands);
        } catch (Exception e) {
            return Result.error("获取需求列表失败：" + e.getMessage());
        }
    }

    /**
     * 管理员下架违规收购需求
     */
    @PutMapping("/admin/demand/{id}/offshelf")
    public Result<Void> adminOffShelfDemand(@PathVariable Long id) {
        try {
            demandService.offShelfDemand(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("下架需求失败：" + e.getMessage());
        }
    }

    /**
     * 管理员删除收购需求
     */
    @DeleteMapping("/admin/demand/{id}")
    public Result<Void> adminDeleteDemand(@PathVariable Long id) {
        try {
            demandService.deleteDemand(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除需求失败：" + e.getMessage());
        }
    }

    /**
     * 获取收购价格统计（最低价、最高价）
     */
    @GetMapping("/admin/price-stats")
    public Result<Map<String, Object>> getPriceStats() {
        try {
            Map<String, Object> stats = demandService.getPriceStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取价格统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取每日价格变化趋势
     */
    @GetMapping("/admin/price-trend")
    public Result<List<Map<String, Object>>> getPriceTrend(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> trend = demandService.getDailyPriceTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error("获取价格趋势失败：" + e.getMessage());
        }
    }

    /**
     * 管理员获取所有收购申请（订单）
     */
    @GetMapping("/admin/applications")
    public Result<IPage<AcquisitionApplication>> getAllApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<AcquisitionApplication> pageParam = new Page<>(page, size);
            IPage<AcquisitionApplication> result = applicationService.getAllApplicationsForAdmin(pageParam, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取申请列表失败：" + e.getMessage());
        }
    }
}
