package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.Orders;
import com.hengzhou.moli.entity.OrderItem;
import com.hengzhou.moli.entity.Product;
import com.hengzhou.moli.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单Service层
 */
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrdersMapper, Orders> {

    private final OrderItemService orderItemService;
    private final ProductService productService;

    /**
     * 创建订单
     */
    @Transactional
    public Orders createOrder(Long userId, Long shopId, BigDecimal totalAmount,
                             BigDecimal freight, BigDecimal discount, List<OrderItem> items) {
        Orders order = new Orders();
        order.setOrderNo("HJ" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setTotalAmount(totalAmount);
        order.setFreight(freight);
        order.setDiscount(discount);
        order.setActualAmount(totalAmount.add(freight).subtract(discount));
        order.setStatus(0); // 待付款
        order.setCreateTime(LocalDateTime.now());
        this.save(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemService.save(item);}
        return order;}

    /**
     * 获取用户订单列表（含商品明细 items 数组）
     */
    public Page<Map<String, Object>> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        Page<Orders> result = this.page(page, wrapper);
        List<Orders> orders = result.getRecords();
        if (orders.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        // 批量查出所有商品明细
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        // 组装每个订单的 Map（含 items）
        List<Map<String, Object>> records = new ArrayList<>();
        for (Orders order : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("shopId", order.getShopId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("freight", order.getFreight());
            map.put("discount", order.getDiscount());
            map.put("actualAmount", order.getActualAmount());
            map.put("status", order.getStatus());
            map.put("payType", order.getPayType());
            map.put("payTime", order.getPayTime());
            map.put("shipTime", order.getShipTime());
            map.put("receiveTime", order.getReceiveTime());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("remark", order.getRemark());
            map.put("createTime", order.getCreateTime());
            map.put("items", itemsMap.getOrDefault(order.getId(), Collections.emptyList()));
            records.add(map);
        }
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setRecords(records);
        resultPage.setTotal(result.getTotal());
        return resultPage;
    }

    /**
     * 获取商家订单列表（带商品明细）
     * 返回 List<Map>，每个 Map 包含 order + items
     */
    public List<Map<String, Object>> getShopOrdersWithItems(Long shopId, Integer status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getShopId, shopId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);

        List<Orders> orders = this.list(wrapper);
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查出所有 orderItems
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        // 按 orderId 分组
        Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 组装返回
        List<Map<String, Object>> result = new ArrayList<>();
        for (Orders order : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("shopId", order.getShopId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("freight", order.getFreight());
            map.put("discount", order.getDiscount());
            map.put("actualAmount", order.getActualAmount());
            map.put("status", order.getStatus());
            map.put("payType", order.getPayType());
            map.put("payTime", order.getPayTime());
            map.put("shipTime", order.getShipTime());
            map.put("receiveTime", order.getReceiveTime());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("remark", order.getRemark());
            map.put("createTime", order.getCreateTime());
            // 过滤已删除的订单项
            List<OrderItem> validItems = itemsMap.getOrDefault(order.getId(), Collections.emptyList())
                    .stream()
                    .filter(item -> item.getDeleted() == null || item.getDeleted() == 0)
                    .collect(Collectors.toList());
            map.put("items", validItems);
            result.add(map);
        }
        return result;
    }

    /**
     * 获取商家订单列表（分页）
     */
    public Page<Orders> getShopOrders(Long shopId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getShopId, shopId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return this.page(page, wrapper);
    }
    /***支付订单*/
    @Transactional
    public boolean payOrder(Long orderId, Integer payType, String receiverName, String receiverPhone,
                String receiverAddress, String remark) {Orders order = this.getById(orderId);
        if (order == null) return false;
        // 扣减库存
        deductStock(orderId);
        order.setStatus(1); // 待发货
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        if (receiverName != null) order.setReceiverName(receiverName);
        if (receiverPhone != null) order.setReceiverPhone(receiverPhone);
        if (receiverAddress != null) order.setReceiverAddress(receiverAddress);
        if (remark != null) order.setRemark(remark);
        return this.updateById(order);}
    /**扣减库存*/
    private void deductStock(Long orderId) {
        List<OrderItem> items = orderItemService.list(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {if (item.getProductId() != null && item.getQuantity() != null) {
                Product product = productService.getById(item.getProductId());
                if (product != null && product.getStock() != null && product.getStock() > 0) {
                    int newStock = Math.max(0, product.getStock() - item.getQuantity());
                    product.setStock(newStock);
                    // 增加销量
                    product.setSales((product.getSales() == null ? 0 : product.getSales()) + item.getQuantity());
                    productService.updateById(product);}}}}
    /**发货*/
    public boolean shipOrder(Long orderId, String remark) {Orders order = new Orders();
        order.setId(orderId);
        order.setStatus(2); // 待收货
        order.setShipTime(LocalDateTime.now());
        if (remark != null) order.setAdminRemark(remark);
        return this.updateById(order);}
    /*** 确认收货*/
    public boolean confirmReceive(Long orderId) {Orders order = new Orders();
        order.setId(orderId);
        order.setStatus(3); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        return this.updateById(order);}
    /*** 取消订单*/
    public boolean cancelOrder(Long orderId, String remark) {
        Orders order = new Orders();
        order.setId(orderId);
        order.setStatus(4); // 已取消
        if (remark != null) order.setAdminRemark(remark);
        return this.updateById(order);
    }
    /**
     * 售后中（退款中）
     */
    public boolean setRefunding(Long orderId) {
        Orders order = new Orders();
        order.setId(orderId);
        order.setStatus(5); // 退款中
        return this.updateById(order);
    }
}
