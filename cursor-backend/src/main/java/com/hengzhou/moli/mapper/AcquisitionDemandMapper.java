package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.entity.AcquisitionDemand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 茉莉花收购需求 Mapper 接口
 */
@Mapper
public interface AcquisitionDemandMapper extends BaseMapper<AcquisitionDemand> {

    /**
     * 减少剩余收购量
     */
    @Select("UPDATE acquisition_demand SET quantity_remaining = quantity_remaining - #{quantity}, " +
            "status = CASE WHEN quantity_remaining - #{quantity} <= 0 THEN 2 ELSE status END " +
            "WHERE id = #{id} AND quantity_remaining >= #{quantity}")
    int reduceRemainingQuantity(@Param("id") Long id, @Param("quantity") java.math.BigDecimal quantity);

    /**
     * 增加剩余收购量
     */
    @Select("UPDATE acquisition_demand SET quantity_remaining = LEAST(quantity_remaining + #{quantity}, quantity_needed), " +
            "status = CASE WHEN status = 2 THEN 1 ELSE status END " +
            "WHERE id = #{id}")
    int increaseRemainingQuantity(@Param("id") Long id, @Param("quantity") java.math.BigDecimal quantity);

    /**
     * 分页查询收购需求（用户端 - 仅显示收购中的）
     */
    @Select("SELECT * FROM acquisition_demand WHERE status = 1 AND demand_date >= #{today} ORDER BY created_at DESC")
    IPage<AcquisitionDemand> selectActiveDemands(Page<AcquisitionDemand> page, @Param("today") LocalDate today);

    /**
     * 分页查询收购需求（商家端 - 按商家ID筛选）
     */
    @Select("SELECT * FROM acquisition_demand WHERE shop_id = #{shopId} ORDER BY created_at DESC")
    IPage<AcquisitionDemand> selectByShopId(Page<AcquisitionDemand> page, @Param("shopId") Long shopId);

    /**
     * 获取用户端收购需求列表（所有状态）
     */
    @Select("SELECT * FROM acquisition_demand WHERE status = 1 ORDER BY created_at DESC")
    List<AcquisitionDemand> selectAllActiveDemands();

    /**
     * 根据ID查询详情（包含关联信息）
     */
    @Select("SELECT d.*, " +
            "(SELECT COUNT(*) FROM acquisition_application a WHERE a.demand_id = d.id AND a.status != 4 AND a.status != 5) as application_count " +
            "FROM acquisition_demand d WHERE d.id = #{id}")
    AcquisitionDemand selectDemandWithCount(@Param("id") Long id);

    /**
     * 查询商家今日的需求数量
     */
    @Select("SELECT COUNT(*) FROM acquisition_demand WHERE shop_id = #{shopId} AND demand_date = #{date} AND status = 1")
    int countTodayDemands(@Param("shopId") Long shopId, @Param("date") LocalDate date);
}
