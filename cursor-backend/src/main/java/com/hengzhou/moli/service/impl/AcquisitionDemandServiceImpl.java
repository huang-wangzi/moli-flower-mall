package com.hengzhou.moli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.AcquisitionApplication;
import com.hengzhou.moli.entity.AcquisitionDemand;
import com.hengzhou.moli.mapper.AcquisitionApplicationMapper;
import com.hengzhou.moli.mapper.AcquisitionDemandMapper;
import com.hengzhou.moli.service.AcquisitionDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AcquisitionDemandServiceImpl extends ServiceImpl<AcquisitionDemandMapper, AcquisitionDemand>
        implements AcquisitionDemandService {

    @Autowired(required = false)
    private AcquisitionApplicationMapper applicationMapper;

    @Override
    public IPage<AcquisitionDemand> selectActiveDemands(Page<AcquisitionDemand> page, LocalDate today) {
        try {
            LambdaQueryWrapper<AcquisitionDemand> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionDemand::getStatus, 1)
                   .ge(AcquisitionDemand::getDemandDate, today)
                   .orderByDesc(AcquisitionDemand::getCreatedAt);
            return page(page, wrapper);
        } catch (Exception e) {
            return new Page<>();
        }
    }

    @Override
    public IPage<AcquisitionDemand> selectByShopId(Page<AcquisitionDemand> page, Long shopId) {
        try {
            LambdaQueryWrapper<AcquisitionDemand> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionDemand::getShopId, shopId)
                   .orderByDesc(AcquisitionDemand::getCreatedAt);
            return page(page, wrapper);
        } catch (Exception e) {
            return new Page<>();
        }
    }

    @Override
    public List<AcquisitionDemand> selectAllActiveDemands() {
        try {
            LambdaQueryWrapper<AcquisitionDemand> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionDemand::getStatus, 1)
                   .ge(AcquisitionDemand::getDemandDate, LocalDate.now())
                   .orderByDesc(AcquisitionDemand::getCreatedAt);
            return list(wrapper);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public AcquisitionDemand getDemandById(Long id) {
        try {
            AcquisitionDemand demand = getById(id);
            if (demand != null) {
                int applicationCount = countApplicationsByDemandId(id);
                demand.setDescription(applicationCount + "");
            }
            return demand;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public AcquisitionDemand createDemand(AcquisitionDemand demand) {
        try {
            demand.setStatus(1);
            if (demand.getQuantityRemaining() == null && demand.getQuantityNeeded() != null) {
                demand.setQuantityRemaining(demand.getQuantityNeeded());
            }
            if (demand.getUnit() == null || demand.getUnit().isEmpty()) {
                demand.setUnit("斤");
            }
            // 确保时间字段有值
            if (demand.getCreatedAt() == null) {
                demand.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (demand.getUpdatedAt() == null) {
                demand.setUpdatedAt(java.time.LocalDateTime.now());
            }
            // 设置需求总申请量默认值
            if (demand.getTotalDemand() == null) {
                demand.setTotalDemand(0);
            }
            if (demand.getRemainingDemand() == null) {
                demand.setRemainingDemand(0);
            }
            // 允许收购商发布多条收购需求（不同市场）
            save(demand);
            return demand;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建需求失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateDemand(AcquisitionDemand demand) {
        try {
            AcquisitionDemand existing = getById(demand.getId());
            if (existing == null) {
                throw new RuntimeException("需求不存在");
            }
            if (demand.getQuantityNeeded() != null && existing.getQuantityNeeded() != null) {
                if (demand.getQuantityNeeded().compareTo(existing.getQuantityNeeded()) < 0) {
                    BigDecimal agreed = existing.getQuantityNeeded().subtract(existing.getQuantityRemaining());
                    BigDecimal newRemaining = demand.getQuantityNeeded().subtract(agreed);
                    if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("计划收购量不能少于已同意的数量");
                    }
                    demand.setQuantityRemaining(newRemaining);
                } else {
                    BigDecimal diff = demand.getQuantityNeeded().subtract(existing.getQuantityNeeded());
                    if (existing.getQuantityRemaining() != null) {
                        demand.setQuantityRemaining(existing.getQuantityRemaining().add(diff));
                    }
                }
            }
            return updateById(demand);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("更新需求失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteDemand(Long id) {
        try {
            AcquisitionDemand demand = getById(id);
            if (demand == null) {
                throw new RuntimeException("需求不存在");
            }
            return removeById(id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("删除需求失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean offShelfDemand(Long id) {
        try {
            AcquisitionDemand demand = getById(id);
            if (demand == null) {
                throw new RuntimeException("需求不存在");
            }
            demand.setStatus(0);
            return updateById(demand);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("下架需求失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean reduceRemainingQuantity(Long id, BigDecimal quantity) {
        try {
            AcquisitionDemand demand = getById(id);
            if (demand == null) {
                throw new RuntimeException("需求不存在");
            }
            BigDecimal currentRemaining = demand.getQuantityRemaining();
            if (currentRemaining == null) {
                currentRemaining = BigDecimal.ZERO;
            }
            BigDecimal newRemaining = currentRemaining.subtract(quantity);
            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("剩余收购量不足");
            }
            demand.setQuantityRemaining(newRemaining);
            if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                demand.setStatus(2);
            }
            return updateById(demand);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("减少剩余量失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean increaseRemainingQuantity(Long id, BigDecimal quantity) {
        try {
            AcquisitionDemand demand = getById(id);
            if (demand == null) {
                throw new RuntimeException("需求不存在");
            }
            BigDecimal currentRemaining = demand.getQuantityRemaining();
            if (currentRemaining == null) {
                currentRemaining = BigDecimal.ZERO;
            }
            BigDecimal currentNeeded = demand.getQuantityNeeded();
            if (currentNeeded == null) {
                currentNeeded = BigDecimal.ZERO;
            }
            BigDecimal newRemaining = currentRemaining.add(quantity);
            if (newRemaining.compareTo(currentNeeded) > 0) {
                newRemaining = currentNeeded;
            }
            if (demand.getStatus() != null && demand.getStatus() == 2) {
                demand.setStatus(1);
            }
            demand.setQuantityRemaining(newRemaining);
            return updateById(demand);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("增加剩余量失败：" + e.getMessage());
        }
    }

    @Override
    public int countTodayDemands(Long shopId, LocalDate date) {
        try {
            if (shopId == null || date == null) return 0;
            LambdaQueryWrapper<AcquisitionDemand> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionDemand::getShopId, shopId)
                    .eq(AcquisitionDemand::getDemandDate, date)
                    .eq(AcquisitionDemand::getStatus, 1);
            return (int) count(wrapper);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int countApplicationsByDemandId(Long demandId) {
        try {
            if (applicationMapper == null || demandId == null) {
                return 0;
            }
            LambdaQueryWrapper<AcquisitionApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AcquisitionApplication::getDemandId, demandId);
            wrapper.notIn(AcquisitionApplication::getStatus, 4, 5);
            return applicationMapper.selectCount(wrapper).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取所有需求（管理员）
     */
    @Override
    public List<AcquisitionDemand> getAllDemandsForAdmin(Integer status) {
        try {
            LambdaQueryWrapper<AcquisitionDemand> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(AcquisitionDemand::getStatus, status);
            }
            wrapper.orderByDesc(AcquisitionDemand::getCreatedAt);
            return list(wrapper);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getPriceStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            List<AcquisitionDemand> activeDemands = list(new LambdaQueryWrapper<AcquisitionDemand>()
                    .eq(AcquisitionDemand::getStatus, 1)
                    .ge(AcquisitionDemand::getDemandDate, LocalDate.now()));

            if (activeDemands.isEmpty()) {
                stats.put("minPrice", null);
                stats.put("maxPrice", null);
                stats.put("avgPrice", null);
                stats.put("totalCount", 0);
                stats.put("minMarket", null);
                stats.put("maxMarket", null);
                stats.put("merchantCount", 0);
                return stats;
            }

            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            BigDecimal totalMinPrice = BigDecimal.ZERO;
            BigDecimal totalMaxPrice = BigDecimal.ZERO;
            int validCount = 0;
            String minMarket = null;
            String maxMarket = null;
            // 统计不同收购商家数
            java.util.Set<Long> merchantIds = new java.util.HashSet<>();

            for (AcquisitionDemand demand : activeDemands) {
                if (demand.getShopId() != null) {
                    merchantIds.add(demand.getShopId());
                }
                // 取所有需求的最低价中的最小值
                if (demand.getPriceMin() != null) {
                    if (minPrice == null || demand.getPriceMin().compareTo(minPrice) < 0) {
                        minPrice = demand.getPriceMin();
                        minMarket = demand.getMarketAddress();
                    }
                    totalMinPrice = totalMinPrice.add(demand.getPriceMin());
                    validCount++;
                }
                // 取所有需求的最高价中的最大值
                if (demand.getPriceMax() != null) {
                    if (maxPrice == null || demand.getPriceMax().compareTo(maxPrice) > 0) {
                        maxPrice = demand.getPriceMax();
                        maxMarket = demand.getMarketAddress();
                    }
                    totalMaxPrice = totalMaxPrice.add(demand.getPriceMax());
                }
            }

            // 平均价 = (最低价总和 + 最高价总和) / (2 * 有效记录数)
            BigDecimal avgPrice = null;
            if (validCount > 0) {
                avgPrice = totalMinPrice.add(totalMaxPrice)
                        .divide(BigDecimal.valueOf(validCount * 2), java.math.RoundingMode.HALF_UP);
            }

            stats.put("minPrice", minPrice);
            stats.put("maxPrice", maxPrice);
            stats.put("avgPrice", avgPrice);
            stats.put("totalCount", activeDemands.size());
            stats.put("minMarket", minMarket);
            stats.put("maxMarket", maxMarket);
            stats.put("merchantCount", merchantIds.size());
            return stats;
        } catch (Exception e) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("minPrice", null);
            stats.put("maxPrice", null);
            stats.put("avgPrice", null);
            stats.put("totalCount", 0);
            stats.put("minMarket", null);
            stats.put("maxMarket", null);
            stats.put("merchantCount", 0);
            return stats;
        }
    }

    @Override
    public List<Map<String, Object>> getDailyPriceTrend(int days) {
        try {
            List<Map<String, Object>> trend = new ArrayList<>();
            LocalDate today = LocalDate.now();

            // 记录最近的有效价格（用于填充空白日期）
            BigDecimal lastMinPrice = null;
            BigDecimal lastMaxPrice = null;

            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                List<AcquisitionDemand> demands = list(new LambdaQueryWrapper<AcquisitionDemand>()
                        .eq(AcquisitionDemand::getStatus, 1)
                        .eq(AcquisitionDemand::getDemandDate, date));

                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("date", date.toString());

                if (demands.isEmpty()) {
                    // 如果今天没数据，使用最近的有效价格，保持折线连续
                    if (lastMinPrice != null) {
                        dayStats.put("minPrice", lastMinPrice);
                        dayStats.put("maxPrice", lastMaxPrice);
                        dayStats.put("count", 0);
                        dayStats.put("filled", true); // 标记为填充数据
                    } else {
                        dayStats.put("minPrice", null);
                        dayStats.put("maxPrice", null);
                        dayStats.put("count", 0);
                    }
                } else {
                    BigDecimal minPrice = null;
                    BigDecimal maxPrice = null;

                    for (AcquisitionDemand demand : demands) {
                        // 取所有需求的最低价中的最小值
                        if (demand.getPriceMin() != null) {
                            if (minPrice == null || demand.getPriceMin().compareTo(minPrice) < 0) {
                                minPrice = demand.getPriceMin();
                            }
                        }
                        // 取所有需求的最高价中的最大值
                        if (demand.getPriceMax() != null) {
                            if (maxPrice == null || demand.getPriceMax().compareTo(maxPrice) > 0) {
                                maxPrice = demand.getPriceMax();
                            }
                        }
                    }

                    // 更新最近的有效价格
                    if (minPrice != null) lastMinPrice = minPrice;
                    if (maxPrice != null) lastMaxPrice = maxPrice;

                    dayStats.put("minPrice", minPrice);
                    dayStats.put("maxPrice", maxPrice);
                    dayStats.put("count", demands.size());
                    dayStats.put("filled", false);
                }

                trend.add(dayStats);
            }

            return trend;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> getDemandIdsByShopId(Long shopId) {
        try {
            List<AcquisitionDemand> demands = list(new LambdaQueryWrapper<AcquisitionDemand>()
                    .eq(AcquisitionDemand::getShopId, shopId));
            return demands.stream().map(AcquisitionDemand::getId).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
