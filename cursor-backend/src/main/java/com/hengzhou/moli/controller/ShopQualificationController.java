package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.ShopQualification;
import com.hengzhou.moli.mapper.UserMapper;
import com.hengzhou.moli.service.ShopQualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家资质控制器
 */
@RestController
@RequestMapping("/shop/qualification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "商家资质管理")
@CrossOrigin(origins = "*")
public class ShopQualificationController {

    private final ShopQualificationService shopQualificationService;
    private final UserMapper userMapper;

    @GetMapping("/my")
    @Operation(summary = "获取我的资质申请")
    public Result<ShopQualification> myQualification(Long userId) {
        try {
            return Result.success(shopQualificationService.getByUserId(userId));
        } catch (Exception e) {
            log.error("获取资质申请失败", e);
            return Result.error("获取资质申请失败: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待审核列表")
    public Result<List<ShopQualification>> pending() {
        try {
            return Result.success(shopQualificationService.getPendingList());
        } catch (Exception e) {
            log.error("获取待审核列表失败", e);
            return Result.error("获取待审核列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有资质记录")
    public Result<List<ShopQualification>> all() {
        try {
            return Result.success(shopQualificationService.getAllList());
        } catch (Exception e) {
            log.error("获取所有资质记录失败", e);
            return Result.error("获取所有资质记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/apply")
    @Operation(summary = "申请资质")
    public Result<?> apply(@RequestBody ShopQualification qualification) {
        try {
            log.info("收到资质申请: userId={}, shopName={}", qualification.getUserId(), qualification.getShopName());
            
            // 验证必填字段
            if (qualification.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (qualification.getShopName() == null || qualification.getShopName().trim().isEmpty()) {
                return Result.error("店铺名称不能为空");
            }
            
            // 防重复提交检查：检查是否存在待审核的资质申请
            ShopQualification existingPending = shopQualificationService.getPendingByUserId(qualification.getUserId());
            if (existingPending != null) {
                log.warn("用户 {} 已存在待审核的资质申请(ID={})，拒绝重复提交", qualification.getUserId(), existingPending.getId());
                return Result.error("您已提交过资质申请，请等待审核结果");
            }
            
            // 更新用户资质状态为"待审核"
            userMapper.updateQualificationStatus(qualification.getUserId(), 1);
            
            boolean success = shopQualificationService.apply(qualification);
            if (success) {
                return Result.success("申请已提交");
            } else {
                return Result.error("申请失败");
            }
        } catch (Exception e) {
            log.error("申请资质失败", e);
            return Result.error("申请资质失败: " + e.getMessage());
        }
    }

    @PutMapping("/audit")
    @Operation(summary = "审核资质")
    public Result<?> audit(@RequestParam Long id,
                          @RequestParam Integer status,
                          @RequestParam(required = false) String rejectReason,
                          @RequestParam Long auditBy) {
        try {
            // 前端状态: 1=通过, 2=拒绝
            // 资质记录状态: 1=通过, 2=拒绝 (一致)
            // 用户资质状态: 2=通过, 3=拒绝
            Integer recordStatus = status;
            // 更新资质记录
            boolean ok = shopQualificationService.audit(id, recordStatus, rejectReason, auditBy);
            if (!ok) {
                return Result.error("审核失败");
            }
            // 更新用户资质状态
            ShopQualification sq = shopQualificationService.getById(id);
            if (sq != null) {
                if (status == 1) {
                    // 通过: 更新用户资质状态为2
                    userMapper.updateQualificationStatus(sq.getUserId(), 2);
                } else {
                    // 拒绝: 更新用户资质状态为3
                    userMapper.updateQualificationStatus(sq.getUserId(), 3);
                }
            }
            return Result.success("审核完成");
        } catch (Exception e) {
            log.error("审核资质失败", e);
            return Result.error("审核资质失败: " + e.getMessage());
        }
    }
}
