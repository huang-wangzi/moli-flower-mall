package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hengzhou.moli.entity.AcquisitionApplication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/*** 茉莉花收购申请服务接口*/
public interface AcquisitionApplicationService extends IService<AcquisitionApplication> {
    /*** 分页查询申请记录（用户端 - 按用户ID筛选）*/
    IPage<AcquisitionApplication> selectByUserId(Page<AcquisitionApplication> page, Long userId);
    /*** 分页查询申请记录（商家端 - 按需求ID筛选）*/
    IPage<AcquisitionApplication> selectByDemandId(Page<AcquisitionApplication> page, Long demandId);
    /*** 根据需求ID查询所有申请（商家端）*/
    List<AcquisitionApplication> selectAllByDemandId(Long demandId);
    /*** 收购商获取所有订单（查询自己发布的需求下的所有申请）*/
    IPage<AcquisitionApplication> selectByShopId(Page<AcquisitionApplication> page, Long shopId);
    /*** 收购商获取所有订单（带状态筛选）*/
    IPage<AcquisitionApplication> selectByShopId(Page<AcquisitionApplication> page, Long shopId, Integer status);
    /*** 创建申请（花农申请供货）*/
    AcquisitionApplication createApplication(AcquisitionApplication application);
    /*** 修改申请（花农修改供货数量）*/
    AcquisitionApplication updateApplication(Long applicationId, Long userId, BigDecimal newQuantity,
                                             String newContactPhone, String newPhotoUrls);
    /*** 同意申请（商家操作）*/
    boolean agreeApplication(Long applicationId, Long shopId);
    /*** 拒绝申请（商家操作）*/
    boolean rejectApplication(Long applicationId, Long shopId, String reason);
    /*** 取消申请（用户操作）*/
    boolean cancelApplication(Long applicationId, Long userId);
    /*** 完成交付（商家确认收货）*/
    boolean completeDelivery(Long applicationId, Long shopId, BigDecimal actualQuantity, BigDecimal actualPrice);
/*** 获取用户统计信息*/
Map<String, Object> getUserStats(Long userId);
    /*** 获取用户每日统计数据（用于报表）*/
    List<Map<String, Object>> getDailyStats(Long userId, String startDate, String endDate);
    /*** 检查用户是否已申请过该需求*/
    boolean hasApplied(Long demandId, Long userId);
    /*** 获取所有申请记录（管理员用）*/
    IPage<AcquisitionApplication> getAllApplicationsForAdmin(Page<AcquisitionApplication> page, Integer status);
    /*** 获取收购商统计数据*/
    Map<String, Object> getMerchantStats(Long shopId);
    /*** 获取收购商每日统计数据*/
    List<Map<String, Object>> getMerchantDailyStats(Long shopId, String startDate, String endDate);}
