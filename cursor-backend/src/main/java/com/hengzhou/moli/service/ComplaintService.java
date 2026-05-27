package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.Complaint;
import com.hengzhou.moli.entity.Orders;
import com.hengzhou.moli.mapper.ComplaintMapper;
import com.hengzhou.moli.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 投诉Service
 */
@Service
@RequiredArgsConstructor
public class ComplaintService extends ServiceImpl<ComplaintMapper, Complaint> {

    /**
     * 获取所有投诉
     */
    public List<Complaint> getAllComplaints() {
        try {
            return list(new LambdaQueryWrapper<Complaint>()
                    .orderByDesc(Complaint::getCreateTime));
        } catch (Exception e) {
            return List.of();
        }
    }

    private final OrdersMapper ordersMapper;

    /**
     * 创建投诉
     */
    @Transactional
    public boolean createComplaint(Complaint complaint) {
        try {
            System.out.println("=== 创建投诉开始 ===");
            System.out.println("userId: " + complaint.getUserId());
            System.out.println("complaint: " + complaint);
            System.out.println("shopId: " + complaint.getShopId());
            System.out.println("reason: " + complaint.getReason());
            
            if (complaint.getCreateTime() == null) {
                complaint.setCreateTime(java.time.LocalDateTime.now());
            }
            complaint.setUpdateTime(java.time.LocalDateTime.now());
            
            // 确保 content 字段已填充（兼容旧表结构中的 NOT NULL 约束）
            // 注意：title 在实体类中已被标记为 @TableField(exist = false)，不写入数据库
            if (complaint.getContent() == null) {
                complaint.setContent(complaint.getDescription() != null ? complaint.getDescription() : (complaint.getReason() != null ? complaint.getReason() : "无内容"));
            }
            
            System.out.println("开始保存投诉...");
            boolean result = save(complaint);
            
            System.out.println("保存结果: " + result);
            System.out.println("投诉ID: " + complaint.getId());
            System.out.println("=== 创建投诉结束 ===");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("创建投诉异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 处理投诉
     */
    public boolean handleComplaint(Long id, Integer status, String remark) {
        try {
            Complaint complaint = getById(id);
            if (complaint == null) {
                return false;
            }
            complaint.setStatus(status);
            complaint.setAdminRemark(remark);
            complaint.setUpdateTime(java.time.LocalDateTime.now());
            return updateById(complaint);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取管理员列表
     */
    public List<Complaint> listForAdmin() {
        return getAllComplaints();
    }
}
