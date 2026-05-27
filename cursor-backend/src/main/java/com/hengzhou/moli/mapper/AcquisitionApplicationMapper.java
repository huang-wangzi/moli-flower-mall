package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.entity.AcquisitionApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 茉莉花收购申请 Mapper 接口
 */
@Mapper
public interface AcquisitionApplicationMapper extends BaseMapper<AcquisitionApplication> {

    /**
     * 分页查询申请记录（用户端 - 按用户ID筛选）
     * 返回完整的Application信息，包括关联的Demand信息
     */
    @Select("SELECT a.id, a.demand_id, a.user_id, a.user_nickname, a.farmer_name, a.quantity, " +
            "a.contact_phone, a.remark, a.apply_weight, a.status, a.actual_quantity, " +
            "a.actual_price, a.total_amount, a.agree_time, a.delivery_time, a.complete_time, " +
            "a.reject_reason, a.created_at, a.updated_at, " +
            "COALESCE(a.photo_urls, a.jasmine_photos) as photo_urls, " +
            "d.title as demand_title, d.shop_name, d.market_address, d.price_min, d.price_max " +
            "FROM acquisition_application a " +
            "LEFT JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE a.user_id = #{userId} ORDER BY a.created_at DESC")
    IPage<AcquisitionApplication> selectByUserId(Page<AcquisitionApplication> page, @Param("userId") Long userId);

    /**
     * 分页查询申请记录（商家端 - 按需求ID筛选）
     */
    @Select("SELECT a.*, u.username, u.phone, " +
            "COALESCE(a.photo_urls, a.jasmine_photos) as photo_urls " +
            "FROM acquisition_application a " +
            "LEFT JOIN sys_user u ON a.user_id = u.id " +
            "WHERE a.demand_id = #{demandId} ORDER BY a.created_at DESC")
    IPage<AcquisitionApplication> selectByDemandId(Page<AcquisitionApplication> page, @Param("demandId") Long demandId);

    /**
     * 收购商获取所有订单（查询自己发布的需求下的所有申请）
     */
    @Select("SELECT a.id, a.demand_id, a.user_id, a.user_nickname, a.farmer_name, a.quantity, " +
            "a.contact_phone, a.remark, a.apply_weight, a.status, a.actual_quantity, " +
            "a.actual_price, a.total_amount, a.agree_time, a.delivery_time, a.complete_time, " +
            "a.reject_reason, a.created_at, a.updated_at, " +
            "COALESCE(a.photo_urls, a.jasmine_photos) as photo_urls, " +
            "d.title as demand_title, d.shop_name, d.market_address, d.price_min, d.price_max, " +
            "u.username, u.phone " +
            "FROM acquisition_application a " +
            "LEFT JOIN acquisition_demand d ON a.demand_id = d.id " +
            "LEFT JOIN sys_user u ON a.user_id = u.id " +
            "WHERE a.demand_id IN (SELECT id FROM acquisition_demand WHERE shop_id = #{shopId}) " +
            "ORDER BY a.created_at DESC")
    IPage<AcquisitionApplication> selectByShopId(Page<AcquisitionApplication> page, @Param("shopId") Long shopId);

    /**
     * 根据需求ID查询所有申请（商家端）
     */
    @Select("SELECT * FROM acquisition_application WHERE demand_id = #{demandId} ORDER BY created_at DESC")
    List<AcquisitionApplication> selectAllByDemandId(@Param("demandId") Long demandId);

    /**
     * 根据ID查询申请（只查实际表字段，避免映射错误）
     */
    @Select("SELECT id, demand_id, user_id, user_nickname, farmer_name, quantity, photo_urls, contact_phone, " +
            "remark, apply_weight, status, actual_quantity, actual_price, total_amount, agree_time, " +
            "delivery_time, complete_time, reject_reason, created_at, updated_at " +
            "FROM acquisition_application WHERE id = #{id}")
    AcquisitionApplication selectByIdOnly(@Param("id") Long id);

    /**
     * 用户统计 - 总收入
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM acquisition_application WHERE user_id = #{userId} AND status = 3")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * 用户统计 - 总斤数
     */
    @Select("SELECT COALESCE(SUM(actual_quantity), 0) FROM acquisition_application WHERE user_id = #{userId} AND status = 3")
    BigDecimal sumTotalQuantityByUserId(@Param("userId") Long userId);

    /**
     * 用户统计 - 完成订单数
     */
    @Select("SELECT COUNT(*) FROM acquisition_application WHERE user_id = #{userId} AND status = 3")
    int countCompletedByUserId(@Param("userId") Long userId);

    /**
     * 用户统计 - 待处理订单数
     */
    @Select("SELECT COUNT(*) FROM acquisition_application WHERE user_id = #{userId} AND status IN (0, 1, 2)")
    int countPendingByUserId(@Param("userId") Long userId);

    /**
     * 用户统计 - 每日收入和斤数（用于报表）
     */
    @Select("SELECT DATE(complete_time) as date, " +
            "SUM(actual_quantity) as total_quantity, " +
            "SUM(total_amount) as total_amount, " +
            "COUNT(*) as order_count " +
            "FROM acquisition_application " +
            "WHERE user_id = #{userId} AND status = 3 " +
            "AND complete_time >= #{startDate} AND complete_time <= #{endDate} " +
            "GROUP BY DATE(complete_time) " +
            "ORDER BY date ASC")
    List<DailyStats> selectDailyStats(@Param("userId") Long userId, 
                                       @Param("startDate") String startDate, 
                                       @Param("endDate") String endDate);

    /**
     * 用户统计 - 每日收入和斤数（使用delivery_time或created_at作为日期）
     * 解决 complete_time 为 NULL 导致查询失败的问题
     */
    @Select("SELECT " +
            "COALESCE(DATE(delivery_time), DATE(complete_time), DATE(created_at)) as date, " +
            "SUM(actual_quantity) as quantity, " +
            "SUM(total_amount) as amount, " +
            "COUNT(*) as order_count " +
            "FROM acquisition_application " +
            "WHERE user_id = #{userId} AND status = 3 " +
            "GROUP BY COALESCE(DATE(delivery_time), DATE(complete_time), DATE(created_at)) " +
            "HAVING date >= #{startDate} AND date <= #{endDate} " +
            "ORDER BY date ASC")
    List<DailyStatsResult> selectDailyStatsByUserId(@Param("userId") Long userId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    /**
     * 检查用户是否已申请过该需求
     */
    @Select("SELECT COUNT(*) FROM acquisition_application WHERE demand_id = #{demandId} AND user_id = #{userId} AND status NOT IN (4, 5)")
    int countByDemandAndUser(@Param("demandId") Long demandId, @Param("userId") Long userId);

    /**
     * 每日统计内部类
     */
    public static class DailyStats {
        private String date;
        private BigDecimal totalQuantity;
        private BigDecimal totalAmount;
        private Integer orderCount;

        public DailyStats() {
            // MyBatis 需要无参构造函数
        }

        public DailyStats(String date, BigDecimal totalQuantity, BigDecimal totalAmount, Integer orderCount) {
            this.date = date;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
            this.orderCount = orderCount;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public BigDecimal getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    }

    /**
     * 每日统计结果类（用于用户端，前端期望的字段名）
     */
    public static class DailyStatsResult {
        private String date;
        private BigDecimal quantity;
        private BigDecimal amount;
        private Integer orderCount;

        public DailyStatsResult() {
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    }

    // ==================== 收购商统计查询 ====================

    /**
     * 收购商 - 今日收购量
     */
    @Select("SELECT COALESCE(SUM(actual_quantity), 0) FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status = 3 " +
            "AND DATE(a.complete_time) = CURDATE()")
    BigDecimal sumTodayQuantityByShopId(@Param("shopId") Long shopId);

    /**
     * 收购商 - 今日收购额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status = 3 " +
            "AND DATE(a.complete_time) = CURDATE()")
    BigDecimal sumTodayAmountByShopId(@Param("shopId") Long shopId);

    /**
     * 收购商 - 待处理订单数（status 0,1,2）
     */
    @Select("SELECT COUNT(*) FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status IN (0, 1, 2)")
    int countPendingByShopId(@Param("shopId") Long shopId);

    /**
     * 收购商 - 累计收购量
     */
    @Select("SELECT COALESCE(SUM(actual_quantity), 0) FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status = 3")
    BigDecimal sumTotalQuantityByShopId(@Param("shopId") Long shopId);

    /**
     * 收购商 - 累计收购额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status = 3")
    BigDecimal sumTotalAmountByShopId(@Param("shopId") Long shopId);

    /**
     * 收购商 - 每日统计（用于报表）
     */
    @Select("SELECT DATE(a.complete_time) as date, " +
            "SUM(a.actual_quantity) as total_quantity, " +
            "SUM(a.total_amount) as total_amount, " +
            "COUNT(*) as order_count " +
            "FROM acquisition_application a " +
            "INNER JOIN acquisition_demand d ON a.demand_id = d.id " +
            "WHERE d.shop_id = #{shopId} AND a.status = 3 " +
            "AND a.complete_time >= #{startDate} AND a.complete_time <= #{endDate} " +
            "GROUP BY DATE(a.complete_time) " +
            "ORDER BY date ASC")
    List<DailyStats> selectMerchantDailyStats(@Param("shopId") Long shopId,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);
}
