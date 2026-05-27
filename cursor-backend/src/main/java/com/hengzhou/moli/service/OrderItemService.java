package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.OrderItem;
import com.hengzhou.moli.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单商品Service层
 */
@Service
@RequiredArgsConstructor
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> {

    /**
     * 获取订单下的商品列表
     */
    public List<OrderItem> getByOrderId(Long orderId) {
        return this.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }
}
