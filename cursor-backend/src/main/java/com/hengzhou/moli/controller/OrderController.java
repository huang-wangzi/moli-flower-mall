package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.*;
import com.hengzhou.moli.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单控制器
 * 负责处理订单相关的所有HTTP请求，包括订单创建、查询、支付、发货、收货、取消等完整流程
 * 订单采用按商家分组的设计，同一订单中的商品可能来自不同商家
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单管理")
@CrossOrigin(origins = "*")
public class OrderController {

    /**
     * 订单服务：处理订单核心业务逻辑
     */
    private final OrderService orderService;
    /**
     * 订单项服务：处理订单商品明细
     */
    private final OrderItemService orderItemService;
    /**
     * 商品服务：处理商品信息查询和库存更新
     */
    private final ProductService productService;
    /**
     * 消息服务：发送订单状态变更通知
     */
    private final MessageService messageService;
    /**
     * 用户服务：获取用户信息
     */
    private final UserService userService;

    /**
     * 获取用户订单列表
     * 分页查询指定用户的所有订单，支持按状态筛选
     * @param userId 用户ID
     * @param status 订单状态筛选（可选）：0-待支付，1-待发货，2-已发货，3-已完成，4-已取消
     * @param pageNum 页码，默认第1页
     * @param pageSize 每页数量，默认20条
     * @return 用户的订单分页列表
     */
    @GetMapping("/user/list")
    @Operation(summary = "用户订单列表")
    public Result<?> userOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(orderService.getUserOrders(userId, status, pageNum, pageSize));
    }

    /**
     * 获取商家订单列表
     * 分页查询指定商家的所有订单，支持按状态筛选
     * @param shopId 商家用户ID
     * @param status 订单状态筛选（可选）
     * @param pageNum 页码，默认第1页
     * @param pageSize 每页数量，默认20条
     * @return 商家的订单分页列表
     */
    @GetMapping("/shop/list")
    @Operation(summary = "商家订单列表")
    public Result<?> shopOrders(
            @RequestParam Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(orderService.getShopOrders(shopId, status, pageNum, pageSize));
    }

    /**
     * 获取商家订单列表（带商品明细和买家信息）
     * 用于商家中心的订单管理页面，显示完整的订单信息
     * @param shopId 商家ID
     * @param status 订单状态筛选（可选）
     * @return 包含订单、商品明细和买家信息的完整列表
     */
    @GetMapping("/shop/list-with-items")
    @Operation(summary = "商家订单列表（带商品明细+买家信息）")
    public Result<?> shopOrdersWithItems(
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status) {
        try {
            // 验证商家ID
            if (shopId == null) {
                return Result.error("商家ID不能为空");
            }
            // 获取订单列表（包含商品明细）
            List<Map<String, Object>> orders = orderService.getShopOrdersWithItems(shopId, status);
            // 附加上买家用户名信息
            for (Map<String, Object> o : orders) {
                // 从订单中获取买家ID
                Long buyerId = o.get("userId") != null ? ((Number) o.get("userId")).longValue() : null;
                if (buyerId != null) {
                    // 查询买家信息并附加上
                    User buyer = userService.getById(buyerId);
                    o.put("buyerUsername", buyer != null ? buyer.getUsername() : "用户" + buyerId);
                    o.put("buyerPhone", buyer != null && buyer.getPhone() != null ? buyer.getPhone() : "");
                }
            }
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("获取订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单详情（含商品）
     * 根据订单ID查询订单的完整信息，包括订单基本信息和商品明细
     * @param id 订单ID
     * @return 包含订单信息和商品列表的Map对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "订单详情（含商品）")
    public Result<?> detail(@PathVariable("id") Long id) {
        // 查询订单基本信息
        Orders order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 查询订单包含的商品明细
        List<OrderItem> items = orderItemService.getByOrderId(id);
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        // 如果有买家ID，附加上买家信息
        if (order.getUserId() != null) {
            User buyer = userService.getById(order.getUserId());
            result.put("buyerUsername", buyer != null ? buyer.getUsername() : "用户" + order.getUserId());
            result.put("buyerPhone", buyer != null && buyer.getPhone() != null ? buyer.getPhone() : "");
        }
        return Result.success(result);
    }

    /**
     * 创建订单（按商家分组）
     * 接收购物车提交的商品列表，按商家分组创建多个订单
     * @param params 订单参数，包含商品列表和收货信息
     * @param userId 用户ID，从请求头获取
     * @return 创建的订单列表
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单（按商家分组）")
    @Transactional
    public Result<?> create(@RequestBody Map<String, Object> params,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            return doCreateOrder(params, userId);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 打印完整堆栈，方便定位未知500根因
            e.printStackTrace();
            return Result.error("下单失败：" + e.getMessage());
        }
    }

    /**
     * 执行订单创建的核心逻辑
     * @param params 订单参数
     * @param userId 用户ID
     * @return 创建结果
     */
    private Result<?> doCreateOrder(Map<String, Object> params, Long userId) {
        // 验证用户登录状态
        if (userId == null) {
            return Result.error("请先登录");
        }

        // 获取订单商品列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) params.get("items");
        if (itemsRaw == null || itemsRaw.isEmpty()) {
            return Result.error("购物车为空");
        }

        // 获取收货信息（可选）
        String receiverName = (String) params.get("receiverName");
        String receiverPhone = (String) params.get("receiverPhone");
        String receiverAddress = (String) params.get("receiverAddress");
        String remark = (String) params.get("remark");

        // 按商家分组商品
        Map<Long, List<Map<String, Object>>> byShop = new LinkedHashMap<>();
        for (Map<String, Object> item : itemsRaw) {
            Object pidObj = item.get("productId");
            if (pidObj == null) continue;
            Long productId;
            try {
                // 兼容不同格式的商品ID
                productId = (pidObj instanceof Number) ? ((Number) pidObj).longValue() : Long.parseLong(pidObj.toString());
            } catch (Exception e) {
                continue;
            }
            // 查询商品信息
            Product p = productService.getById(productId);
            if (p == null) continue;
            // 按商家ID分组
            byShop.computeIfAbsent(p.getShopId(), k -> new ArrayList<>()).add(item);
        }

        // 存储创建成功的订单
        List<Map<String, Object>> createdOrders = new ArrayList<>();

        // 遍历每个商家的商品，创建独立订单
        for (Map.Entry<Long, List<Map<String, Object>>> entry : byShop.entrySet()) {
            Long shopId = entry.getKey();
            List<Map<String, Object>> shopItems = entry.getValue();

            // 计算该商家订单的总金额
            BigDecimal total = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            // 遍历该商家的商品
            for (Map<String, Object> item : shopItems) {
                // 获取数量
                Object qtyObj = item.get("quantity");
                if (qtyObj == null) continue;
                int qty = ((Number) qtyObj).intValue();
                if (qty <= 0) continue;

                // 获取单价
                Object priceObj = item.get("price");
                if (priceObj == null) continue;
                BigDecimal price = new BigDecimal(priceObj.toString());
                // 累加金额 = 单价 * 数量
                total = total.add(price.multiply(BigDecimal.valueOf(qty)));

                // 不在这里扣减库存和增加销量，移至支付环节或单独方法
                // 修复：删除以下重复扣减库存和增加销量的代码
                /*
                Product p = productService.getById(((Number) item.get("productId")).longValue());
                if (p != null) {
                    p.setStock(p.getStock() - qty);  // 扣减库存
                    p.setSales(p.getSales() + qty);   // 增加销量
                    productService.updateById(p);
                }
                */

                // 创建订单项记录
                OrderItem oi = new OrderItem();
                oi.setProductId(((Number) item.get("productId")).longValue());
                oi.setProductName((String) item.get("name"));
                oi.setProductImage((String) item.get("image"));
                oi.setPrice(price);
                oi.setQuantity(qty);
                oi.setSpecs((String) item.get("specs"));
                orderItems.add(oi);
            }

            // 创建订单主记录
            Orders order = new Orders();
            // 生成订单号：HJ + 16位UUID
            order.setOrderNo("HJ" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
            order.setUserId(userId);  // 买家ID
            order.setShopId(shopId);  // 商家ID
            order.setTotalAmount(total);      // 订单总金额
            order.setFreight(BigDecimal.ZERO);  // 运费（默认为0）
            order.setDiscount(BigDecimal.ZERO); // 优惠金额（默认为0）
            order.setActualAmount(total);       // 实付金额
            order.setStatus(0);               // 订单状态：0-待支付
            order.setCreateTime(LocalDateTime.now());
            // 收货信息
            if (receiverName != null) order.setReceiverName(receiverName);
            if (receiverPhone != null) order.setReceiverPhone(receiverPhone);
            if (receiverAddress != null) order.setReceiverAddress(receiverAddress);
            if (remark != null) order.setRemark(remark);
            // 保存订单
            orderService.save(order);

            // 保存订单项并关联到订单
            for (OrderItem oi : orderItems) {
                oi.setOrderId(order.getId());
                orderItemService.save(oi);
            }

            // 发送消息通知商家有新订单
            try {
                User buyer = userService.getById(userId);
                String buyerName = buyer != null ? buyer.getUsername() : "用户" + userId;
                messageService.sendMessage(Message.builder()
                        .userId(shopId)  // 发送给商家
                        .type(2)         // 消息类型：订单通知
                        .title("新订单")
                        .content("用户「" + buyerName + "」下单了订单 " + order.getOrderNo() + "，金额 ¥" + total + "，请及时处理。")
                        .relatedId(order.getId())  // 关联订单ID
                        .senderId(userId)         // 发送者ID
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 构建返回给前端的数据
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("shopId", shopId);
            orderMap.put("totalAmount", total);
            orderMap.put("actualAmount", total);
            orderMap.put("status", 0);
            orderMap.put("items", orderItems);
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("receiverName", order.getReceiverName());
            orderMap.put("receiverPhone", order.getReceiverPhone());
            orderMap.put("receiverAddress", order.getReceiverAddress());
            createdOrders.add(orderMap);
        }

        return Result.success(createdOrders);
    }

    /**
     * 支付订单
     * 支持更新收货信息
     * @param id 订单ID
     * @param payType 支付方式
     * @param receiverName 收货人姓名（可选）
     * @param receiverPhone 收货人电话（可选）
     * @param receiverAddress 收货地址（可选）
     * @param remark 订单备注（可选）
     * @return 支付结果
     */
    @PutMapping("/{id}/pay")
    @Operation(summary = "支付订单（可更新收货信息）")
    public Result<?> pay(@PathVariable("id") Long id,
                       @RequestParam Integer payType,
                       @RequestParam(required = false) String receiverName,
                       @RequestParam(required = false) String receiverPhone,
                       @RequestParam(required = false) String receiverAddress,
                       @RequestParam(required = false) String remark) {
        boolean ok = orderService.payOrder(id, payType, receiverName, receiverPhone, receiverAddress, remark);
        return ok ? Result.success("支付成功") : Result.error("支付失败");
    }

    /**
     * 商家发货
     * 商家确认发货后，订单状态变更为已发货
     * @param id 订单ID
     * @param remark 发货备注（可选）
     * @return 发货结果
     */
    @PutMapping("/{id}/ship")
    @Operation(summary = "商家发货")
    public Result<?> ship(@PathVariable("id") Long id, @RequestParam(required = false) String remark) {
        boolean ok = orderService.shipOrder(id, remark);
        if (ok) {
            // 获取订单信息，用于发送通知
            Orders order = orderService.getById(id);
            if (order != null) {
                try {
                    // 发送消息通知买家已发货
                    messageService.sendMessage(Message.builder()
                            .userId(order.getUserId())
                            .type(2)
                            .title("商家已发货")
                            .content("您的订单 " + order.getOrderNo() + " 商家已发货，请注意查收。" + (remark != null ? "备注：" + remark : ""))
                            .relatedId(id)
                            .build());
                } catch (Exception ignored) {
                }
            }
        }
        return ok ? Result.success("发货成功") : Result.error("发货失败");
    }

    /**
     * 确认收货
     * 买家收到商品后确认收货，订单完成
     * @param id 订单ID
     * @return 确认结果
     */
    @PutMapping("/{id}/receive")
    @Operation(summary = "确认收货")
    public Result<?> receive(@PathVariable("id") Long id) {
        boolean ok = orderService.confirmReceive(id);
        return ok ? Result.success("确认收货成功") : Result.error("确认收货失败");
    }

    /**
     * 取消订单
     * 买家可在订单未发货前取消订单
     * @param id 订单ID
     * @param remark 取消原因（可选）
     * @return 取消结果
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<?> cancel(@PathVariable("id") Long id, @RequestParam(required = false) String remark) {
        boolean ok = orderService.cancelOrder(id, remark);
        return ok ? Result.success("取消成功") : Result.error("取消失败");
    }
}
