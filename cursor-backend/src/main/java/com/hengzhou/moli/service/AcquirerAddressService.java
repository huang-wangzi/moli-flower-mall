package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.AcquirerAddress;
import com.hengzhou.moli.mapper.AcquirerAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收购商地址 Service
 */
@Service
@RequiredArgsConstructor
public class AcquirerAddressService extends ServiceImpl<AcquirerAddressMapper, AcquirerAddress> {

    /**
     * 查询某收购商的地址列表
     */
    public List<AcquirerAddress> getByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<AcquirerAddress>()
                .eq(AcquirerAddress::getUserId, userId)
                .eq(AcquirerAddress::getDeleted, 0)
                .orderByDesc(AcquirerAddress::getIsDefault)
                .orderByDesc(AcquirerAddress::getCreateTime));
    }

    /**
     * 获取地址详情（带权限校验）
     */
    public AcquirerAddress getByIdAndUserId(Long addressId, Long userId) {
        return this.getOne(new LambdaQueryWrapper<AcquirerAddress>()
                .eq(AcquirerAddress::getId, addressId)
                .eq(AcquirerAddress::getUserId, userId)
                .eq(AcquirerAddress::getDeleted, 0));
    }

    /**
     * 新增地址
     */
    @Transactional
    public AcquirerAddress add(AcquirerAddress addr) {
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addr.setCreateTime(java.time.LocalDateTime.now());
        this.save(addr);
        return addr;
    }

    /**
     * 更新地址
     */
    @Transactional
    public boolean update(AcquirerAddress addr) {
        AcquirerAddress exist = this.getByIdAndUserId(addr.getId(), addr.getUserId());
        if (exist == null) return false;
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addr.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(addr);
    }

    /**
     * 删除地址
     */
    @Transactional
    public boolean remove(Long addressId, Long userId) {
        AcquirerAddress exist = this.getByIdAndUserId(addressId, userId);
        if (exist == null) return false;
        return this.removeById(addressId);
    }

    /**
     * 设为默认地址
     */
    @Transactional
    public boolean setDefault(Long addressId, Long userId) {
        AcquirerAddress exist = this.getByIdAndUserId(addressId, userId);
        if (exist == null) return false;
        clearDefault(userId);
        exist.setIsDefault(1);
        exist.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(exist);
    }

    /**
     * 取消用户所有地址的默认状态
     */
    private void clearDefault(Long userId) {
        AcquirerAddress zero = new AcquirerAddress();
        zero.setIsDefault(0);
        this.update(zero,
                new LambdaQueryWrapper<AcquirerAddress>()
                        .eq(AcquirerAddress::getUserId, userId)
                        .eq(AcquirerAddress::getIsDefault, 1));
    }
}
