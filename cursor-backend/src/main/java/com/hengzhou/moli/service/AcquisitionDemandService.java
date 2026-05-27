package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hengzhou.moli.entity.AcquisitionDemand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 茉莉花收购需求服务接口
 */
public interface AcquisitionDemandService extends IService<AcquisitionDemand> {

    /**
     * 分页查询收购需求（用户端 - 仅显示收购中的）
     */
    IPage<AcquisitionDemand> selectActiveDemands(Page<AcquisitionDemand> page, LocalDate today);

    /**
     * 分页查询收购需求（商家端 - 按商家ID筛选）
     */
    IPage<AcquisitionDemand> selectByShopId(Page<AcquisitionDemand> page, Long shopId);

    /**
     * 获取用户端收购需求列表
     */
    List<AcquisitionDemand> selectAllActiveDemands();

    /**
     * 根据ID查询详情
     */
    AcquisitionDemand getDemandById(Long id);

    /**
     * 创建收购需求
     */
    AcquisitionDemand createDemand(AcquisitionDemand demand);

    /**
     * 更新收购需求
     */
    boolean updateDemand(AcquisitionDemand demand);

    /**
     * 删除收购需求
     */
    boolean deleteDemand(Long id);

    /**
     * 下架收购需求
     */
    boolean offShelfDemand(Long id);

    /**
     * 更新剩余收购量（减少）
     */
    boolean reduceRemainingQuantity(Long id, BigDecimal quantity);

    /**
     * 恢复剩余收购量（增加，用于订单取消）
     */
    boolean increaseRemainingQuantity(Long id, BigDecimal quantity);

    /**
     * 查询商家今日的需求数量
     */
    int countTodayDemands(Long shopId, LocalDate date);

    /**
     * 获取商家某需求下的申请数量
     */
    int countApplicationsByDemandId(Long demandId);

    /**
     * 获取所有收购需求（管理员用）
     */
    List<AcquisitionDemand> getAllDemandsForAdmin(Integer status);

    /**
     * 获取收购价格统计（最低价、最高价）
     */
    Map<String, Object> getPriceStats();

    /**
     * 获取每日价格变化趋势
     */
    List<Map<String, Object>> getDailyPriceTrend(int days);

    /**
     * 获取收购商的所有需求ID列表
     */
    List<Long> getDemandIdsByShopId(Long shopId);
}
