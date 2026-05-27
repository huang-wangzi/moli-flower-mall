package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.ShopQualification;
import com.hengzhou.moli.mapper.ShopQualificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商家资质Service层
 */
@Service
@RequiredArgsConstructor
public class ShopQualificationService extends ServiceImpl<ShopQualificationMapper, ShopQualification> {

    /**
     * 获取用户资质申请（最近一条）
     */
    public ShopQualification getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<ShopQualification>()
                .eq(ShopQualification::getUserId, userId)
                .orderByDesc(ShopQualification::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 检查用户是否存在待审核的资质申请
     * 用于防重复提交
     */
    public ShopQualification getPendingByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<ShopQualification>()
                .eq(ShopQualification::getUserId, userId)
                .eq(ShopQualification::getStatus, 0) // 0=待审核
                .orderByDesc(ShopQualification::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 获取待审核列表
     */
    public List<ShopQualification> getPendingList() {
        return this.list(new LambdaQueryWrapper<ShopQualification>()
                .eq(ShopQualification::getStatus, 0)
                .orderByDesc(ShopQualification::getApplyTime));
    }

    /**
     * 获取所有资质记录（全部）
     */
    public List<ShopQualification> getAllList() {
        return this.list(new LambdaQueryWrapper<ShopQualification>()
                .orderByDesc(ShopQualification::getApplyTime));
    }

    /**
     * 申请资质
     */
    public boolean apply(ShopQualification qualification) {
        qualification.setStatus(0); // 待审核
        qualification.setApplyTime(java.time.LocalDateTime.now());
        qualification.setCreateTime(java.time.LocalDateTime.now());
        return this.save(qualification);
    }

    /**
     * 审核资质
     * @param id 资质记录ID
     * @param status 前端传入状态：1=通过，2=拒绝
     * @param rejectReason 拒绝原因
     * @param auditBy 审核人ID
     * @return 是否成功
     */
    public boolean audit(Long id, Integer status, String rejectReason, Long auditBy) {
        // status: 1=通过，2=拒绝（直接使用，无需映射）
        ShopQualification qualification = new ShopQualification();
        qualification.setId(id);
        qualification.setStatus(status);
        qualification.setAuditBy(auditBy);
        qualification.setAuditTime(java.time.LocalDateTime.now());
        if (status == 2) { // 拒绝状态需要记录原因
            qualification.setRejectReason(rejectReason);
        }
        return this.updateById(qualification);
    }
}
