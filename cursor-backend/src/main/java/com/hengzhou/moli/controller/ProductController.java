package com.hengzhou.moli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.Product;
import com.hengzhou.moli.entity.Review;
import com.hengzhou.moli.service.ProductService;
import com.hengzhou.moli.service.ReviewService;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品控制器
 * 负责处理商品相关的所有HTTP请求，包括商品列表查询、商品详情、商品发布、商品审核等
 *
 * @author 横州茉莉花
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Tag(name = "商品管理")
@CrossOrigin(origins = "*")
public class ProductController {

    /**
     * 商品服务：处理商品相关的业务逻辑
     */
    private final ProductService productService;
    /**
     * 评价服务：处理商品评价相关业务
     */
    private final ReviewService reviewService;
    /**
     * 消息服务：用于发送系统通知消息
     */
    private final MessageService messageService;
    /**
     * 用户服务：处理用户相关操作
     */
    private final UserService userService;

    /**
     * JWT token有效期配置
     * 用于处理需要token验证的接口
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 获取商品列表
     * 支持分页、分类筛选、关键词搜索和状态筛选
     * @param pageNum 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @param categoryId 分类ID筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @param status 商品状态筛选（可选，0-待审核，1-已上架，2-已下架）
     * @return 分页后的商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商品列表")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        // 调用服务层获取分页商品数据
        Page<Product> page = productService.getProductPage(pageNum, pageSize, categoryId, keyword, status);
        return Result.success(page);
    }

    /**
     * 获取商品详情
     * 根据商品ID查询商品详细信息，同时返回该商品的所有评价
     * @param id 商品ID
     * @return 包含商品详情和评价列表的Map对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> detail(HttpServletRequest request, @PathVariable Long id) {
        try {
            // 从 request attribute 中获取 userId (由 JwtAuthenticationFilter 设置)
            Object userIdAttr = request.getAttribute("userId");
            boolean isUserClient = true;

            if (userIdAttr != null) {
                // 如果已登录，检查用户角色
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() 
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                    
                    // 只有商家、管理员、收购商才被视为“非普通用户”，可以查看非上架商品
                    boolean isManagement = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_SHOP") || 
                                          a.getAuthority().equals("ROLE_ADMIN") ||
                                          a.getAuthority().equals("ROLE_ACQUIRER"));
                    
                    if (isManagement) {
                        isUserClient = false;
                    }
                }
            }

            // 根据ID查询商品详情
            Product product = productService.getProductDetail(id, isUserClient);
            // 如果商品不存在，返回错误
            if (product == null) {
                return Result.error("商品不存在或已下架");
            }

            // 获取该商品的的所有评价
            var reviews = reviewService.getReviewsByProductId(id);

            // 构建返回数据Map
            Map<String, Object> data = new HashMap<>();
            data.put("product", product);  // 商品详情
            data.put("reviews", reviews);  // 商品评价列表

            return Result.success(data);
        } catch (Exception e) {
            // 发生异常时返回错误信息
            return Result.error("获取商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定商家的商品列表
     * 用于商家中心查看自己发布的商品
     * @param shopId 商家用户ID
     * @return 该商家发布的所有商品列表
     */
    @GetMapping("/shop/{shopId}")
    @Operation(summary = "获取商家商品列表")
    public Result<?> shopProducts(@PathVariable Long shopId) {
        return Result.success(productService.getProductsByShopId(shopId));
    }

    /**
     * 获取热门商品
     * 根据销量排序，返回销量最高的商品
     * @param limit 返回数量，默认返回10个
     * @return 热门商品列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门商品")
    public Result<?> hot(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    /**
     * 新增商品
     * 商家发布新商品，商品需要经过管理员审核才能上架销售
     * @param product 商品信息（包含名称、价格、库存、描述等）
     * @param userId 用户ID，从请求头X-User-Id中获取
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "新增商品")
    public Result<?> add(@RequestBody Product product,
                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // 打印调试日志
            System.out.println("=== 商品添加调试 ===");
            System.out.println("接收到的 X-User-Id: " + userId);
            System.out.println("商品名称: " + product.getName());
            System.out.println("前端传来的 shopId: " + product.getShopId());

            // 必须从请求头获取userId（由拦截器从token解析并设置）
            if (userId == null) {
                return Result.error("请先登录");
            }
            // 调用服务层添加商品
            boolean success = productService.addProduct(product, userId);
            System.out.println("商品保存结果: " + success + ", 最终shopId: " + product.getShopId());
            // 返回成功或失败结果
            return success ? Result.success("商品已提交，等待审核") : Result.error("添加失败");
        } catch (IllegalArgumentException e) {
            // 参数错误
            System.err.println("参数错误: " + e.getMessage());
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            // 运行时错误
            System.err.println("运行时错误: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品
     * 商家编辑已发布的商品信息
     * 注意：只能编辑待审核状态的商品，审核通过或拒绝的商品不能直接编辑
     * @param product 更新后的商品信息
     * @param userId 用户ID，用于权限验证
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新商品")
    public Result<?> update(@RequestBody Product product,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            boolean success = productService.updateProduct(product, userId);
            return success ? Result.success("更新成功") : Result.error("更新失败，商品不存在或已删除");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品状态
     * 商家自主上架/下架商品，或管理员修改商品状态
     * @param id 商品ID
     * @param status 新状态（0-待审核，1-已上架，2-已下架）
     * @param userId 操作用户ID（从请求头获取）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态")
    public Result<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        boolean success = productService.updateProductStatus(id, status, userId);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    /**
     * 审核商品通过
     * 管理员审核商家发布的商品，审核通过后商品可上架销售
     * 审核通过后会发送消息通知商家
     * @param id 商品ID
     * @return 审核结果
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "审核商品通过")
    public Result<?> approve(@PathVariable Long id) {
        // 先查询商品信息，用于发送通知
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        // 执行审核通过操作
        boolean success = productService.approveProduct(id);
        if (success) {
            // 发送消息通知商家商品已通过审核
            messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                    .userId(product.getShopId())  // 发送给商品所属商家
                    .type(4)  // 消息类型：系统通知
                    .title("商品审核通过")
                    .content("您发布的商品「" + product.getName() + "」已审核通过，现在可以对外销售了。")
                    .relatedId(id)  // 关联商品ID
                    .build());
            return Result.success("商品已审核通过");
        }
        return Result.error("审核失败");
    }

    /**
     * 审核商品拒绝
     * 管理员拒绝商家发布的商品，拒绝后会发送消息通知商家原因
     * @param id 商品ID
     * @return 审核结果
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "审核商品拒绝")
    public Result<?> reject(@PathVariable Long id) {
        // 先查询商品信息
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        // 执行审核拒绝操作
        boolean success = productService.rejectProduct(id);
        if (success) {
            // 发送消息通知商家商品未通过审核
            messageService.sendMessage(com.hengzhou.moli.entity.Message.builder()
                    .userId(product.getShopId())
                    .type(4)
                    .title("商品审核未通过")
                    .content("您发布的商品「" + product.getName() + "」未通过审核，请修改后重新提交。")
                    .relatedId(id)
                    .build());
            return Result.success("商品已拒绝");
        }
        return Result.error("审核失败");
    }

    /**
     * 删除商品
     * 商家只能删除自己的商品
     * @param id 商品ID
     * @param userId 当前登录用户ID（从请求头获取）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<?> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("请先登录");
            }
            // 商家只能删除自己的商品
            boolean success = productService.deleteProduct(id, userId);
            return success ? Result.success("删除成功") : Result.error("删除失败，商品不存在或无权删除");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
