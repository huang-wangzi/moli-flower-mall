package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.WeatherData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 天气数据Mapper
 */
@Mapper
public interface WeatherDataMapper extends BaseMapper<WeatherData> {
}