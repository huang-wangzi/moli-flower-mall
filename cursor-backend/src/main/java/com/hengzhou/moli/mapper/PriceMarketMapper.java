package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.PriceMarket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 市场Mapper接口
 * 功能：提供市场数据的数据库操作
 */
@Mapper
public interface PriceMarketMapper extends BaseMapper<PriceMarket> {
    // 继承BaseMapper的所有CRUD方法
    // 无需额外方法
}
