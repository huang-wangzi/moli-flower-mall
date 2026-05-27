package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.ReceiverAddress;
import com.hengzhou.moli.mapper.ReceiverAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiverAddressService extends ServiceImpl<ReceiverAddressMapper, ReceiverAddress> {

    /**
     * 查询某用户的收货地址列表
     */
    public List<ReceiverAddress> getByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<ReceiverAddress>()
                .eq(ReceiverAddress::getUserId, userId)
                .eq(ReceiverAddress::getDeleted, 0)
                .orderByDesc(ReceiverAddress::getIsDefault)
                .orderByDesc(ReceiverAddress::getCreateTime));
    }

    /**
     * 获取地址详情
     */
    public ReceiverAddress getByIdAndUserId(Long addressId, Long userId) {
        return this.getOne(new LambdaQueryWrapper<ReceiverAddress>()
                .eq(ReceiverAddress::getId, addressId)
                .eq(ReceiverAddress::getUserId, userId)
                .eq(ReceiverAddress::getDeleted, 0));
    }

    /**
     * 新增收稿地址
     */
    @Transactional
    public ReceiverAddress add(ReceiverAddress addr) {
        // 如果设为默认，先取消该用户的其他默认
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addr.setCreateTime(java.time.LocalDateTime.now());
        this.save(addr);
        return addr;
    }

    /**
     * 更新收货地址
     */
    @Transactional
    public boolean update(ReceiverAddress addr) {
        // 防止跨用户修改
        ReceiverAddress exist = this.getByIdAndUserId(addr.getId(), addr.getUserId());
        if (exist == null) return false;
        // 如果设为默认，先取消该用户的其他默认
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addr.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(addr);
    }

    /**
     * 删除收货地址
     */
    @Transactional
    public boolean remove(Long addressId, Long userId) {
        ReceiverAddress exist = this.getByIdAndUserId(addressId, userId);
        if (exist == null) return false;
        return this.removeById(addressId);
    }

    /**
     * 设为默认地址
     */
    @Transactional
    public boolean setDefault(Long addressId, Long userId) {
        ReceiverAddress exist = this.getByIdAndUserId(addressId, userId);
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
        ReceiverAddress zero = new ReceiverAddress();
        zero.setIsDefault(0);
        this.update(zero,
                new LambdaQueryWrapper<ReceiverAddress>()
                        .eq(ReceiverAddress::getUserId, userId)
                        .eq(ReceiverAddress::getIsDefault, 1));
    }
}
