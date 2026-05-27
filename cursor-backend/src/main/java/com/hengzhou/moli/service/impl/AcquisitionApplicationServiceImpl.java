package com.hengzhou.moli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.AcquisitionApplication;
import com.hengzhou.moli.entity.AcquisitionDemand;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.AcquisitionApplicationMapper;
import com.hengzhou.moli.mapper.AcquisitionDemandMapper;
import com.hengzhou.moli.service.AcquisitionApplicationService;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AcquisitionApplicationServiceImpl extends ServiceImpl<AcquisitionApplicationMapper, AcquisitionApplication>
        implements AcquisitionApplicationService {

    @Autowired
    private AcquisitionDemandMapper demandMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Override
    public IPage<AcquisitionApplication> selectByUserId(Page<AcquisitionApplication> page, Long userId) {
        System.out.println("[ApplicationService] selectByUserId called, userId=" + userId);
        try {
            // 使用自定义 Mapper 方法进行分页查询（带关联信息）
            IPage<AcquisitionApplication> result = baseMapper.selectByUserId(page, userId);
            System.out.println("[ApplicationService] Query result, total: " + result.getTotal() + ", records: " + result.getRecords().size());
            return result;
        } catch (Exception e) {
            System.out.println("[ApplicationService] Query error: " + e.getMessage());
            e.printStackTrace();
            return new Page<>();
        }
    }

    @Override
    public IPage<AcquisitionApplication> selectByDemandId(Page<AcquisitionApplication> page, Long demandId) {
        try {
            // 使用自定义 Mapper 方法进行分页查询（带关联信息）
            return baseMapper.selectByDemandId(page, demandId);
        } catch (Exception e) {
            return new Page<>();
        }
    }

    @Override
    public List<AcquisitionApplication> selectAllByDemandId(Long demandId) {
        try {
            return baseMapper.selectAllByDemandId(demandId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<AcquisitionApplication> selectByShopId(Page<AcquisitionApplication> page, Long shopId, Integer status) {
        try {
            IPage<AcquisitionApplication> result;
            if (status != null) {
                // 使用 Mapper 的自定义方法获取所有数据，然后手动筛选
                result = baseMapper.selectByShopId(page, shopId);
                // 在内存中筛选
                if (result.getRecords() != null) {
                    result.getRecords().removeIf(app -> app.getStatus() != status);
                }
            } else {
                result = baseMapper.selectByShopId(page, shopId);
            }
            System.out.println("[ApplicationService] selectByShopId with status=" + status + ", total=" + result.getTotal());
            return result;
        } catch (Exception e) {
            System.out.println("[ApplicationService] selectByShopId error: " + e.getMessage());
            e.printStackTrace();
            return new Page<>();
        }
    }

    @Override
    public IPage<AcquisitionApplication> selectByShopId(Page<AcquisitionApplication> page, Long shopId) {
        try {
            IPage<AcquisitionApplication> result = baseMapper.selectByShopId(page, shopId);
            System.out.println("[ApplicationService] selectByShopId, total=" + result.getTotal());
            return result;
        } catch (Exception e) {
            System.out.println("[ApplicationService] selectByShopId error: " + e.getMessage());
            e.printStackTrace();
            return new Page<>();
        }
    }

    @Override
    @Transactional
    public AcquisitionApplication createApplication(AcquisitionApplication application) {
        if (application.getDemandId() == null || application.getUserId() == null) {
            throw new IllegalArgumentException("缺少必要参数");
        }
        if (application.getQuantity() == null || application.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("请填写正确的收购数量");
        }
        if (application.getContactPhone() == null || application.getContactPhone().isBlank()) {
            throw new IllegalArgumentException("请填写联系电话");
        }

        // 同步茉莉花照片字段（兼容旧字段名 jasmine_photos）
        // 确保两个字段都设置
        if (application.getPhotoUrls() != null && application.getJasminePhotos() == null) {
            application.setJasminePhotos(application.getPhotoUrls());
        }
        if (application.getJasminePhotos() != null && application.getPhotoUrls() == null) {
            application.setPhotoUrls(application.getJasminePhotos());
        }

        // 检查需求是否存在且状态正常
        AcquisitionDemand demand = demandMapper.selectById(application.getDemandId());
        if (demand == null) {
            throw new IllegalArgumentException("需求不存在");
        }
        // 只有状态为1（收购中）的需求才能接收申请
        if (demand.getStatus() != 1) {
            throw new IllegalArgumentException("该需求暂不接收申请");
        }

        // 检查是否已提交过申请
        LambdaQueryWrapper<AcquisitionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AcquisitionApplication::getDemandId, application.getDemandId())
               .eq(AcquisitionApplication::getUserId, application.getUserId())
               .notIn(AcquisitionApplication::getStatus, 4, 5); // 排除已拒绝和已取消
        if (count(wrapper) > 0) {
            throw new IllegalArgumentException("您已提交过申请，请勿重复提交");
        }

        application.setStatus(0); // 待审核
        // 设置申请重量
        if (application.getApplyWeight() == null && application.getQuantity() != null) {
            application.setApplyWeight(application.getQuantity());
        }
        
        // 调试：打印要保存的照片
        System.out.println("[ApplicationService] 保存申请，photoUrls=" + application.getPhotoUrls() + ", jasminePhotos=" + application.getJasminePhotos());
        
        save(application);
        
        // 再次打印保存后的ID
        System.out.println("[ApplicationService] 保存成功，ID=" + application.getId());

        // ==================== 发送消息通知收购商 ====================
        // 获取收购商信息
        User acquirer = userService.getById(demand.getShopId());
        User applicant = userService.getById(application.getUserId());

        if (acquirer != null) {
            String shopName = acquirer.getMerchantName() != null ? acquirer.getMerchantName() :
                             acquirer.getShopName() != null ? acquirer.getShopName() : "收购商";
            String applicantName = applicant != null && applicant.getNickname() != null ? applicant.getNickname() : "用户";

            // 发送消息给收购商
            Message message = Message.builder()
                    .userId(demand.getShopId())  // 接收者是收购商
                    .senderId(application.getUserId())  // 发送者是申请人
                    .type(2)  // 订单通知
                    .title("新的收购申请")
                    .content("「" + applicantName + "」申请向您供货：" + demand.getTitle() + "，" + application.getQuantity() + "斤，请及时处理。")
                    .relatedId(application.getId())  // 关联申请ID
                    .isRead(0)
                    .createTime(LocalDateTime.now())
                    .build();
            messageService.sendMessage(message);

            // 发送消息给申请人确认
            if (applicant != null) {
                Message confirmMsg = Message.builder()
                        .userId(application.getUserId())  // 接收者是申请人
                        .senderId(demand.getShopId())  // 发送者是收购商
                        .type(2)  // 订单通知
                        .title("收购申请已提交")
                        .content("您向「" + shopName + "」提交的收购申请（" + demand.getTitle() + "，" + application.getQuantity() + "斤）已提交，请等待审核。")
                        .relatedId(application.getId())  // 关联申请ID
                        .isRead(0)
                        .createTime(LocalDateTime.now())
                        .build();
                messageService.sendMessage(confirmMsg);
            }
        }

        return application;
    }

    @Override
    @Transactional
    public boolean agreeApplication(Long applicationId, Long shopId) {
        // 使用只查实际表字段的方法，避免映射错误
        AcquisitionApplication application = baseMapper.selectByIdOnly(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        AcquisitionDemand demand = demandMapper.selectById(application.getDemandId());
        if (demand == null || !demand.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("无权操作此申请");
        }

        application.setStatus(1); // 已同意
        updateById(application);

        // 发送消息通知申请人
        User acquirer = userService.getById(shopId);
        String shopName = acquirer != null && acquirer.getMerchantName() != null ? acquirer.getMerchantName() :
                         acquirer != null && acquirer.getShopName() != null ? acquirer.getShopName() : "收购商";

        Message message = Message.builder()
                .userId(application.getUserId())
                .senderId(shopId)
                .type(2)
                .title("收购申请已通过")
                .content("恭喜！您向「" + shopName + "」提交的收购申请（" + demand.getTitle() + "，" + application.getQuantity() + "斤）已通过审核。请按约定将茉莉花送至市场。")
                .relatedId(applicationId)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        messageService.sendMessage(message);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectApplication(Long applicationId, Long shopId, String reason) {
        AcquisitionApplication application = baseMapper.selectByIdOnly(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        AcquisitionDemand demand = demandMapper.selectById(application.getDemandId());
        if (demand == null || !demand.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("无权操作此申请");
        }

        application.setStatus(4); // 已拒绝
        application.setRejectReason(reason);
        updateById(application);

        // 发送消息通知申请人
        User acquirer = userService.getById(shopId);
        String shopName = acquirer != null && acquirer.getMerchantName() != null ? acquirer.getMerchantName() :
                         acquirer != null && acquirer.getShopName() != null ? acquirer.getShopName() : "收购商";

        Message message = Message.builder()
                .userId(application.getUserId())
                .senderId(shopId)
                .type(2)
                .title("收购申请未通过")
                .content("很遗憾，您向「" + shopName + "」提交的收购申请（" + demand.getTitle() + "，" + application.getQuantity() + "斤）未通过审核。原因：" + (reason != null ? reason : "请联系收购商了解详情"))
                .relatedId(applicationId)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        messageService.sendMessage(message);

        return true;
    }

    @Override
    @Transactional
    public boolean completeDelivery(Long applicationId, Long shopId, BigDecimal actualQuantity, BigDecimal actualPrice) {
        AcquisitionApplication application = baseMapper.selectByIdOnly(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (application.getStatus() != 1) {
            throw new IllegalArgumentException("只有已同意的申请才能完成交付");
        }

        BigDecimal finalQuantity = actualQuantity != null ? actualQuantity : application.getQuantity();
        BigDecimal finalPrice = actualPrice != null ? actualPrice : BigDecimal.ZERO;
        BigDecimal totalAmount = finalQuantity.multiply(finalPrice);

        application.setActualQuantity(finalQuantity);
        application.setActualPrice(finalPrice);
        application.setTotalAmount(totalAmount);
        application.setStatus(3); // 已完成
        application.setDeliveryTime(LocalDateTime.now());
        application.setCompleteTime(LocalDateTime.now());
        
        boolean result = updateById(application);
        System.out.println("[ApplicationService] 完成交付, applicationId=" + applicationId + ", actualQuantity=" + finalQuantity + ", actualPrice=" + finalPrice + ", totalAmount=" + totalAmount);

        // 更新需求的剩余收购量
        if (result) {
            AcquisitionDemand demand = demandMapper.selectById(application.getDemandId());
            if (demand != null && demand.getQuantityRemaining() != null) {
                BigDecimal remaining = demand.getQuantityRemaining().subtract(finalQuantity);
                demand.setQuantityRemaining(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining);
                demandMapper.updateById(demand);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean cancelApplication(Long applicationId, Long userId) {
        AcquisitionApplication application = baseMapper.selectByIdOnly(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!application.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权取消此申请");
        }
        if (application.getStatus() != 0) {
            throw new IllegalArgumentException("只能取消待审核的申请");
        }

        application.setStatus(5); // 已取消
        return updateById(application);
    }

    @Override
    @Transactional
    public AcquisitionApplication updateApplication(Long applicationId, Long userId, BigDecimal newQuantity, String newContactPhone, String newPhotoUrls) {
        // 使用只查实际表字段的方法，避免映射错误
        AcquisitionApplication application = baseMapper.selectByIdOnly(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!application.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此申请");
        }
        // 只能修改待审核状态的申请
        if (application.getStatus() != 0) {
            throw new IllegalArgumentException("只能修改待审核状态的申请");
        }
        
        // 更新字段
        if (newQuantity != null && newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            application.setQuantity(newQuantity);
            // 如果没有设置applyWeight，也一并更新
            if (application.getApplyWeight() == null) {
                application.setApplyWeight(newQuantity);
            }
        }
        if (newContactPhone != null && !newContactPhone.isBlank()) {
            application.setContactPhone(newContactPhone);
        }
        if (newPhotoUrls != null) {
            application.setPhotoUrls(newPhotoUrls);
            // 同步茉莉花照片字段
            application.setJasminePhotos(newPhotoUrls);
        }
        
        application.setUpdatedAt(LocalDateTime.now());
        updateById(application);
        
        System.out.println("[ApplicationService] 修改申请成功, id=" + applicationId);
        return application;
    }

    @Override
    public boolean hasApplied(Long demandId, Long userId) {
        try {
            LambdaQueryWrapper<AcquisitionApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionApplication::getDemandId, demandId)
                   .eq(AcquisitionApplication::getUserId, userId)
                   .notIn(AcquisitionApplication::getStatus, 4, 5); // 排除已拒绝和已取消
            return count(wrapper) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        try {
            LambdaQueryWrapper<AcquisitionApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionApplication::getUserId, userId);
            long total = count(wrapper);

            wrapper.clear();
            wrapper.eq(AcquisitionApplication::getUserId, userId).eq(AcquisitionApplication::getStatus, 0);
            long pending = count(wrapper); // 待审核

            wrapper.clear();
            wrapper.eq(AcquisitionApplication::getUserId, userId).eq(AcquisitionApplication::getStatus, 1);
            long agreed = count(wrapper); // 已同意

            wrapper.clear();
            wrapper.eq(AcquisitionApplication::getUserId, userId).eq(AcquisitionApplication::getStatus, 3);
            long completed = count(wrapper); // 已完成

            // 使用 Mapper 中的方法获取金额和斤数统计（仅已完成订单）
            BigDecimal totalAmount = baseMapper.sumTotalAmountByUserId(userId);
            BigDecimal totalQuantity = baseMapper.sumTotalQuantityByUserId(userId);

            // 待处理订单：status IN (0, 1, 2)
            long pendingOrders = baseMapper.countPendingByUserId(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("pending", pending); // 待审核数
            stats.put("agreed", agreed);   // 已同意数
            stats.put("completed", completed); // 已完成数
            stats.put("pendingOrders", pendingOrders); // 待处理订单总数
            stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
            stats.put("totalQuantity", totalQuantity != null ? totalQuantity : BigDecimal.ZERO);
            System.out.println("[ApplicationService] getUserStats, userId=" + userId +
                ", totalAmount=" + totalAmount + ", totalQuantity=" + totalQuantity +
                ", pendingOrders=" + pendingOrders);
            return stats;
        } catch (Exception e) {
            System.out.println("[ApplicationService] getUserStats error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("agreed", 0);
            stats.put("completed", 0);
            stats.put("pendingOrders", 0);
            stats.put("totalAmount", BigDecimal.ZERO);
            stats.put("totalQuantity", BigDecimal.ZERO);
            return stats;
        }
    }

    @Override
    public List<Map<String, Object>> getDailyStats(Long userId, String startDate, String endDate) {
        System.out.println("[getDailyStats] 接收请求: userId=" + userId + ", startDate=" + startDate + ", endDate=" + endDate);
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 使用 Mapper 中的新方法，直接查询数据库获取每日统计数据
            List<AcquisitionApplicationMapper.DailyStatsResult> dailyResults = 
                baseMapper.selectDailyStatsByUserId(userId, startDate, endDate + " 23:59:59");

            System.out.println("[getDailyStats] 查询到的每日统计条数: " + (dailyResults != null ? dailyResults.size() : 0));
            if (dailyResults != null) {
                for (AcquisitionApplicationMapper.DailyStatsResult r : dailyResults) {
                    System.out.println("[getDailyStats] 日期: " + r.getDate() + 
                        ", amount: " + r.getAmount() + 
                        ", quantity: " + r.getQuantity() +
                        ", count: " + r.getOrderCount());
                }
            }

            // 初始化30天的数据（确保即使没有数据也能显示）
            Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
            for (int i = 0; i <= 30; i++) {
                LocalDate day = start.plusDays(i);
                String dateStr = day.toString();
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                dayData.put("count", 0);
                dayData.put("quantity", BigDecimal.ZERO);
                dayData.put("amount", BigDecimal.ZERO);
                dailyMap.put(dateStr, dayData);
            }

            // 填充查询到的数据
            if (dailyResults != null) {
                for (AcquisitionApplicationMapper.DailyStatsResult r : dailyResults) {
                    String dateStr = r.getDate();
                    if (dateStr != null && dailyMap.containsKey(dateStr)) {
                        Map<String, Object> dayData = dailyMap.get(dateStr);
                        dayData.put("count", r.getOrderCount() != null ? r.getOrderCount() : 0);
                        dayData.put("quantity", r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO);
                        dayData.put("amount", r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
                    }
                }
            }

            return new ArrayList<>(dailyMap.values());
        } catch (Exception e) {
            System.err.println("[getDailyStats] 异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<AcquisitionApplication> getAllApplicationsForAdmin(Page<AcquisitionApplication> page, Integer status) {
        try {
            LambdaQueryWrapper<AcquisitionApplication> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(AcquisitionApplication::getStatus, status);
            }
            wrapper.orderByDesc(AcquisitionApplication::getCreatedAt);
            return page(page, wrapper);
        } catch (Exception e) {
            return new Page<>();
        }
    }

    @Override
    public Map<String, Object> getMerchantStats(Long shopId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 今日收购量
            BigDecimal todayQuantity = baseMapper.sumTodayQuantityByShopId(shopId);
            stats.put("todayQuantity", todayQuantity != null ? todayQuantity : BigDecimal.ZERO);

            // 今日收购额
            BigDecimal todayAmount = baseMapper.sumTodayAmountByShopId(shopId);
            stats.put("todayAmount", todayAmount != null ? todayAmount : BigDecimal.ZERO);

            // 待处理订单数（待审核、已同意、交付中）
            int pendingCount = baseMapper.countPendingByShopId(shopId);
            stats.put("pendingOrders", pendingCount);

            // 累计收购量
            BigDecimal totalQuantity = baseMapper.sumTotalQuantityByShopId(shopId);
            stats.put("totalQuantity", totalQuantity != null ? totalQuantity : BigDecimal.ZERO);

            // 累计收购额
            BigDecimal totalAmount = baseMapper.sumTotalAmountByShopId(shopId);
            stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

            System.out.println("[ApplicationService] getMerchantStats, shopId=" + shopId +
                ", todayQuantity=" + todayQuantity + ", todayAmount=" + todayAmount +
                ", pendingOrders=" + pendingCount + ", totalQuantity=" + totalQuantity);
            return stats;
        } catch (Exception e) {
            System.out.println("[ApplicationService] getMerchantStats error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> stats = new HashMap<>();
            stats.put("todayQuantity", BigDecimal.ZERO);
            stats.put("todayAmount", BigDecimal.ZERO);
            stats.put("pendingOrders", 0);
            stats.put("totalQuantity", BigDecimal.ZERO);
            stats.put("totalAmount", BigDecimal.ZERO);
            return stats;
        }
    }

    @Override
    public List<Map<String, Object>> getMerchantDailyStats(Long shopId, String startDate, String endDate) {
        try {
            List<AcquisitionApplicationMapper.DailyStats> dailyStats = baseMapper.selectMerchantDailyStats(
                shopId, startDate, LocalDate.parse(endDate).plusDays(1).toString() + " 23:59:59"
            );

            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (AcquisitionApplicationMapper.DailyStats stat : dailyStats) {
                Map<String, Object> day = new HashMap<>();
                day.put("date", stat.getDate());
                day.put("quantity", stat.getTotalQuantity());
                day.put("amount", stat.getTotalAmount());
                day.put("count", stat.getOrderCount());
                result.add(day);
            }
            return result;
        } catch (Exception e) {
            System.out.println("[ApplicationService] getMerchantDailyStats error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
