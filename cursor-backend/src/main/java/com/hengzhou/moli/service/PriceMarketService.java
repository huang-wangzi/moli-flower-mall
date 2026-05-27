package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.PriceMarket;
import com.hengzhou.moli.mapper.PriceMarketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 市场Service层
 * 功能：提供市场管理的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class PriceMarketService extends ServiceImpl<PriceMarketMapper, PriceMarket> {

    /**
     * 获取所有启用的市场列表
     * @return 市场列表，按排序权重降序
     */
    public List<PriceMarket> getEnabledMarkets() {
        return this.list(new LambdaQueryWrapper<PriceMarket>()
                .eq(PriceMarket::getStatus, 1)
                .orderByDesc(PriceMarket::getSort)
                .orderByDesc(PriceMarket::getCreateTime));
    }

    /**
     * 根据ID获取市场详情
     * @param id 市场ID
     * @return 市场信息
     */
    public PriceMarket getMarketById(Long id) {
        return this.getById(id);
    }

    /**
     * 添加市场
     * @param market 市场信息
     * @return 是否成功
     */
    public boolean addMarket(PriceMarket market) {
        // 设置默认值
        if (market.getStatus() == null) {
            market.setStatus(1);
        }
        if (market.getSort() == null) {
            market.setSort(0);
        }
        return this.save(market);
    }

    /**
     * 更新市场信息
     * @param market 市场信息
     * @return 是否成功
     */
    public boolean updateMarket(PriceMarket market) {
        return this.updateById(market);
    }

    /**
     * 删除市场（物理删除）
     * @param id 市场ID
     * @return 是否成功
     */
    public boolean deleteMarket(Long id) {
        // 物理删除：从数据库中彻底删除
        return this.removeById(id);
    }

    /**
     * 批量删除市场（物理删除）
     * @param ids 市场ID列表
     * @return 是否成功
     */
    public boolean deleteMarkets(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        // 批量物理删除
        return this.removeByIds(ids);
    }

    /**
     * 获取所有市场（包括禁用的）
     * @return 所有市场列表
     */
    public List<PriceMarket> getAllMarkets() {
        return this.list(new LambdaQueryWrapper<PriceMarket>()
                .orderByDesc(PriceMarket::getSort)
                .orderByDesc(PriceMarket::getCreateTime));
    }

    /**
     * 根据城市获取市场列表
     * @param city 城市名称
     * @return 市场列表
     */
    public List<PriceMarket> getMarketsByCity(String city) {
        return this.list(new LambdaQueryWrapper<PriceMarket>()
                .eq(PriceMarket::getCity, city)
                .eq(PriceMarket::getStatus, 1)
                .orderByDesc(PriceMarket::getSort));
    }

    /**
     * 根据省份获取市场列表
     * @param province 省份名称
     * @return 市场列表
     */
    public List<PriceMarket> getMarketsByProvince(String province) {
        return this.list(new LambdaQueryWrapper<PriceMarket>()
                .eq(PriceMarket::getProvince, province)
                .eq(PriceMarket::getStatus, 1)
                .orderByDesc(PriceMarket::getSort));
    }

    /**
     * 检查市场名称是否存在
     * @param name 市场名称
     * @param excludeId 排除的市场ID（用于更新时检查）
     * @return 是否存在
     */
    public boolean isNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<PriceMarket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceMarket::getName, name);
        if (excludeId != null) {
            wrapper.ne(PriceMarket::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}
