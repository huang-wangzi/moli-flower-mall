package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.Review;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评价Service
 */
@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    /**
     * 根据商品ID获取评价列表
     */
    public List<Review> getReviewsByProductId(Long productId) {
        try {
            return list(new LambdaQueryWrapper<Review>()
                    .eq(Review::getProductId, productId)
                    .eq(Review::getStatus, 1)
                    .orderByDesc(Review::getCreateTime));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 根据商品ID列表获取评价
     */
    public List<Review> getReviewsByShopId(List<Long> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                return new ArrayList<>();
            }
            return list(new LambdaQueryWrapper<Review>()
                    .in(Review::getProductId, productIds)
                    .orderByDesc(Review::getCreateTime));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 根据用户ID获取评价
     */
    public List<Review> getReviewsByUserId(Long userId) {
        try {
            return list(new LambdaQueryWrapper<Review>()
                    .eq(Review::getUserId, userId)
                    .orderByDesc(Review::getCreateTime));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有评价
     */
    public List<Review> getAllReviews() {
        try {
            return list(new LambdaQueryWrapper<Review>()
                    .orderByDesc(Review::getCreateTime));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 添加评价
     */
    public boolean addReview(Review review) {
        try {
            System.out.println("[ReviewService] 开始添加评价, productId=" + review.getProductId() + ", userId=" + review.getUserId());
            
            // 设置默认字段
            review.setStatus(1); // 默认显示
            // 不手动设置时间，让自动填充处理
            
            System.out.println("[ReviewService] 准备保存评价");
            boolean result = save(review);
            System.out.println("[ReviewService] save结果: " + result + ", 新评价ID: " + review.getId());
            return result;
        } catch (Exception e) {
            System.err.println("[ReviewService] 添加评价异常: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 回复评价
     */
    public boolean replyReview(Long id, String reply) {
        try {
            Review review = getById(id);
            if (review == null) {
                return false;
            }
            review.setReply(reply);
            review.setReplyTime(LocalDateTime.now());
            review.setUpdateTime(LocalDateTime.now());
            return updateById(review);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除评价
     */
    public boolean deleteReview(Long id) {
        try {
            Review review = getById(id);
            if (review == null) {
                return false;
            }
            review.setDeleted(1);
            review.setUpdateTime(LocalDateTime.now());
            return updateById(review);
        } catch (Exception e) {
            return false;
        }
    }
}
