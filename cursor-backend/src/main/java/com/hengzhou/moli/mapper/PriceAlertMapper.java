package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.PriceAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格预警Mapper
 */
@Mapper
public interface PriceAlertMapper extends BaseMapper<PriceAlert> {
}