package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.Product;
import com.hengzhou.moli.entity.Review;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.ReviewMapper;
import com.hengzhou.moli.service.MessageService;
import com.hengzhou.moli.service.ProductService;
import com.hengzhou.moli.service.ReviewService;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 * 负责处理商品评价相关的所有HTTP请求
 * 支持评价列表查询、添加评价、商家回复、评价管理等功能
 * 用于用户对已购买的商品发表评价，商家回复用户评价等场景
 *
 * @author 横州茉莉花
 */
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Tag(name = "评价管理")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewMapper reviewMapper;
    private final ProductService productService;
    private final MessageService messageService;
    private final UserService userService;
    private final ReviewService reviewService;

    /**
     * 获取商品评价列表
     * 根据商品ID查询该商品下的所有评价
     * 接口: GET /review/product/{productId}
     * @param productId 商品ID
     * @return 该商品的所有评价列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评价列表")
    public Result<List<Review>> getProductReviews(@PathVariable Long productId) {
        try {
            List<Review> reviews = reviewService.getReviewsByProductId(productId);
            if (reviews == null) reviews = new ArrayList<>();
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 添加商品评价
     * 用户对已购买的商品发表评价，包含评分和评价内容
     * 评价提交成功后会自动发送消息通知商家有新评价
     * 接口: POST /review
     * @param review 评价信息，包含 productId、rating、content 等
     * @param userId 当前登录用户ID（从请求头获取）
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加评价")
    public Result<String> addReview(@RequestBody Review review,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            System.out.println("[ReviewController] 添加评价请求: productId=" + review.getProductId() + 
                ", userId=" + review.getUserId() + ", rating=" + review.getRating() + 
                ", content=" + (review.getContent() != null ? review.getContent().substring(0, Math.min(20, review.getContent().length())) : "null"));
            
            // 设置用户ID
            if (review.getUserId() == null && userId != null) {
                review.setUserId(userId);
            }
            
            // 验证必需字段
            if (review.getProductId() == null) {
                return Result.error("商品ID不能为空");
            }
            if (review.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                return Result.error("评分必须在1-5之间");
            }
            if (review.getContent() == null || review.getContent().trim().isEmpty()) {
                return Result.error("评价内容不能为空");
            }
            
            // 获取用户名
            if (review.getUserId() != null && review.getUsername() == null) {
                User user = userService.getById(review.getUserId());
                if (user != null) {
                    review.setUsername(user.getUsername());
                    review.setAvatar(user.getAvatar());
                }
            }
            
            // 设置默认字段
            review.setStatus(1); // 默认显示
            review.setCreateTime(LocalDateTime.now());
            review.setUpdateTime(LocalDateTime.now());
            
            System.out.println("[ReviewController] 准备插入评价到数据库");
            
            // 直接使用 Mapper 插入
            int rows = reviewMapper.insert(review);
            System.out.println("[ReviewController] 插入结果: rows=" + rows + ", 新评价ID=" + review.getId());
            
            if (rows > 0) {
                // 发送消息通知商家
                try {
                    if (review.getProductId() != null) {
                        Product product = productService.getById(review.getProductId());
                        if (product != null && product.getShopId() != null) {
                            messageService.sendMessage(Message.builder()
                                    .userId(product.getShopId())
                                    .type(3)
                                    .title("您有新评价")
                                    .content("用户「" + (review.getUsername() != null ? review.getUsername() : "用户")
                                            + "」对商品「" + (product.getName() != null ? product.getName() : "商品")
                                            + "」发表了评价（" + review.getRating() + "星）")
                                    .relatedId(product.getId())
                                    .senderId(review.getUserId())
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ReviewController] 发送消息异常: " + e.getMessage());
                }
                System.out.println("[ReviewController] 评价添加成功, id=" + review.getId());
                return Result.success("评价提交成功");
            } else {
                System.out.println("[ReviewController] 评价添加失败, rows=" + rows);
                return Result.error("评价提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ReviewController] 评价添加异常: " + e.getClass().getName() + ": " + e.getMessage());
            return Result.error("评价提交失败：" + e.getMessage());
        }
    }

    /**
     * 商家回复用户评价
     * 商家对用户发表的评价进行回复，回复内容会展示在评价详情中
     * 回复成功后会自动发送消息通知用户有商家回复
     * 接口: PUT /review/{id}/reply
     * @param id 评价ID
     * @param reply 商家回复内容
     * @param userId 当前登录商家用户ID（从请求头获取）
     * @return 操作结果
     */
    @PutMapping("/{id}/reply")
    @Operation(summary = "商家回复评价")
    public Result<String> replyReview(@PathVariable Long id, 
                                      @RequestBody String reply,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Review existing = reviewService.getById(id);
            if (existing == null) {
                return Result.error("评价不存在");
            }
            boolean success = reviewService.replyReview(id, reply);
            if (success) {
                try {
                    if (existing.getUserId() != null && existing.getProductId() != null) {
                        Product product = productService.getById(existing.getProductId());
                        String productName = product != null ? product.getName() : "商品";
                        messageService.sendMessage(Message.builder()
                                .userId(existing.getUserId())
                                .type(3)
                                .title("商家回复了您的评价")
                                .content("商家已回复您对「" + productName + "」的评价")
                                .relatedId(existing.getProductId())
                                .build());
                    }
                } catch (Exception ignored) {}
                return Result.success("回复成功");
            }
            return Result.error("回复失败");
        } catch (Exception e) {
            return Result.error("回复失败：" + e.getMessage());
        }
    }

    /**
     * 获取商家的所有评价
     * 查询该商家所有商品下的评价汇总，用于商家评价管理页面
     * 接口: GET /review/shop/{shopId}
     * @param shopId 商家ID
     * @return 该商家所有商品的评价列表（含商品信息）
     */
    @GetMapping("/shop/{shopId}")
    @Operation(summary = "获取商家的所有评价")
    public Result<List<Map<String, Object>>> getShopReviews(@PathVariable Long shopId) {
        try {
            List<Product> shopProducts = productService.getProductsByShopId(shopId);
            if (shopProducts == null || shopProducts.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            List<Long> productIds = shopProducts.stream().map(Product::getId).toList();
            List<Review> reviews = reviewService.getReviewsByShopId(productIds);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Review r : reviews) {
                Map<String, Object> map = buildReviewMap(r);
                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取用户的评价列表
     * 查询指定用户发表过的所有评价，用于用户个人中心评价记录展示
     * 接口: GET /review/user/{userId}
     * @param userId 用户ID
     * @return 该用户发表的所有评价列表（含关联商品信息）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的评价列表")
    public Result<List<Map<String, Object>>> getUserReviews(@PathVariable Long userId) {
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Review r : reviews) {
                Map<String, Object> map = buildReviewMap(r);
                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取所有评价（管理员）
     * 后台管理接口，获取平台所有商品的评价，用于管理员审核和统计
     * 接口: GET /review/admin/list
     * @return 所有评价列表
     */
    @GetMapping("/admin/list")
    @Operation(summary = "获取所有评价（管理员）")
    public Result<List<Review>> getAdminReviews() {
        try {
            List<Review> reviews = reviewService.getAllReviews();
            if (reviews == null) reviews = new ArrayList<>();
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 获取所有评价（管理员）- 别名接口
     * 与 /review/admin/list 功能相同，提供另一个访问路径
     * 接口: GET /review/admin/reviews
     * @return 所有评价列表
     */
    @GetMapping("/admin/reviews")
    @Operation(summary = "获取所有评价（管理员）- 别名")
    public Result<List<Review>> getAdminReviewsAlias() {
        return getAdminReviews();
    }

    /**
     * 获取所有评价（总览）
     * 用于数据统计或全站评价展示
     * 接口: GET /review/all
     * @return 所有评价列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有评价")
    public Result<List<Review>> getAllReviews() {
        return getAdminReviews();
    }

    /**
     * 删除评价（管理员）
     * 管理员删除违规或不当的评价内容
     * 接口: DELETE /review/{id}
     * @param id 评价ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价（管理员）")
    public Result<String> deleteReview(@PathVariable Long id) {
        try {
            boolean success = reviewService.deleteReview(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 构建评价数据Map
     * 将评价实体对象转换为前端需要的Map格式，补充商品信息
     * @param r 评价实体
     * @return 包含评价和关联商品信息的Map
     */
    private Map<String, Object> buildReviewMap(Review r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("productId", r.getProductId());
        map.put("userId", r.getUserId());
        map.put("username", r.getUsername());
        map.put("avatar", r.getAvatar());
        map.put("orderId", r.getOrderId());
        map.put("rating", r.getRating());
        map.put("content", r.getContent());
        map.put("images", r.getImages());
        map.put("reply", r.getReply());
        map.put("replyTime", r.getReplyTime());
        map.put("status", r.getStatus());
        map.put("createTime", r.getCreateTime());
        if (r.getProductId() != null) {
            Product product = productService.getById(r.getProductId());
            map.put("productName", product != null ? product.getName() : "");
            map.put("productImage", product != null ? product.getImages() : "");
        }
        return map;
    }
}
