package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.Complaint;
import com.hengzhou.moli.entity.Message;
import com.hengzhou.moli.entity.Refund;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.entity.Orders;
import com.hengzhou.moli.mapper.ComplaintMapper;
import com.hengzhou.moli.mapper.RefundMapper;
import com.hengzhou.moli.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后/退款Service层
 */
@Service
@RequiredArgsConstructor
public class RefundService extends ServiceImpl<RefundMapper, Refund> {

    private final ComplaintMapper complaintMapper;
    private final MessageService messageService;
    private final OrdersMapper ordersMapper;

    /**
     * 获取商家待处理的售后列表
     */
    public List<Refund> getPendingRefundsByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getStatus, 0)
                .orderByDesc(Refund::getCreateTime));
    }

    /**
     * 获取商家所有售后列表 (过滤掉用户已取消的)
     */
    public List<Refund> getAllRefundsByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .ne(Refund::getStatus, 4) // 商家端不显示已取消的售后
                .orderByDesc(Refund::getCreateTime));
    }

    /**
     * 获取用户的售后列表
     */
    public List<Refund> getRefundsByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getUserId, userId)
                .orderByDesc(Refund::getCreateTime));
    }

    /**
     * 创建售后申请
     */
    @Transactional
    public boolean createRefund(Refund refund) {
        if (refund.getOrderId() == null || refund.getUserId() == null || refund.getShopId() == null) {
            return false;
        }
        if (refund.getAmount() == null) {
            refund.setAmount(BigDecimal.ZERO);
        }
        refund.setStatus(0);
        refund.setRefundType("refund");
        refund.setCreateTime(LocalDateTime.now());
        if (refund.getAdminIntervention() == null) {
            refund.setAdminIntervention(0);
        }

        // 1. 同步更新订单状态为 5-退款中
        try {
            Orders order = new Orders();
            order.setId(refund.getOrderId());
            order.setStatus(5);
            ordersMapper.updateById(order);
        } catch (Exception ignored) {}

        return this.save(refund);
    }

    /**
     * 检查售后是否需要管理员介入
     */
    public boolean isAdminIntervention(Long refundId) {
        Refund refund = this.getById(refundId);
        return refund != null && refund.getAdminIntervention() != null && refund.getAdminIntervention() == 1;
    }

    /**
     * 处理售后申请
     * @param handler 处理器类型：shop=商家, admin=管理员
     */
    @Transactional
    public boolean processRefund(Long refundId, Integer status, String remark, String handler) {
        Refund refund = this.getById(refundId);
        if (refund == null) return false;

        refund.setStatus(status);
        refund.setAdminRemark(remark);
        refund.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(refund);

        // 如果售后被同意（2=已同意，4=退货退款已完成），同步更新订单状态为 6-已退款
        if (ok && (status == 2 || status == 4)) {
            try {
                Orders order = new Orders();
                order.setId(refund.getOrderId());
                order.setStatus(6);
                order.setAdminRemark(remark); // 同步备注
                ordersMapper.updateById(order);
            } catch (Exception ignored) {}
        }
        // 如果售后被拒绝 (status=3)，同步更新订单状态为 7-退款驳回
        else if (ok && status == 3) {
            try {
                Orders order = new Orders();
                order.setId(refund.getOrderId());
                order.setStatus(7);
                order.setAdminRemark(remark); // 同步备注
                ordersMapper.updateById(order);
            } catch (Exception ignored) {}
        }

        return ok;
    }

    /**
     * 同步更新关联的投诉记录状态
     * 当管理员处理售后后，同步更新对应 complaint 的状态
     */
    public void syncComplaintStatus(Long refundId, Integer refundStatus, String remark) {
        try {
            Complaint complaint = complaintMapper.selectOne(
                    new LambdaQueryWrapper<Complaint>()
                            .eq(Complaint::getRefundId, refundId)
                            .eq(Complaint::getSourceType, 1)
                            .last("LIMIT 1"));
            if (complaint == null) return;
            // 管理员处理：2/4=已处理(1)，3=驳回(2)
            Integer complaintStatus = (refundStatus == 3) ? 2 : 1;
            complaint.setStatus(complaintStatus);
            complaint.setAdminRemark(remark);
            complaint.setUpdateTime(LocalDateTime.now());
            complaintMapper.updateById(complaint);

            // 通知商家投诉已处理
            try {
                String content = (complaintStatus == 1)
                        ? "管理员已处理该售后投诉并给出结论，商家请知悉。"
                        : "管理员已驳回该售后投诉，如有疑问请联系平台。";
                messageService.sendMessage(Message.builder()
                        .userId(complaint.getShopId())
                        .type(2)
                        .title("售后投诉处理结果通知")
                        .content(content)
                        .relatedId(complaint.getOrderId())
                        .build());
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    /**
     * 用户针对已拒绝的售后进行“再次申诉”
     * 1. 重新创建一个售后单 (status=0, admin_intervention=1)
     * 2. 自动创建一个关联投诉 (sourceType=1)
     * 3. 同步更新订单状态为 5-退款中
     */
    @Transactional
    public boolean reAppeal(Long orderId, Long userId, String reason, String description) {
        try {
            // 获取订单信息
            Orders order = ordersMapper.selectById(orderId);
            if (order == null) return false;

            // 1. 创建新售后单
            Refund refund = new Refund();
            refund.setOrderId(orderId);
            refund.setUserId(userId);
            refund.setShopId(order.getShopId());
            refund.setAmount(order.getActualAmount());
            refund.setReason(reason);
            refund.setDescription(description);
            refund.setStatus(0); // 待处理
            refund.setRefundType("refund");
            refund.setAdminIntervention(1); // 再次申诉默认管理员介入
            refund.setCreateTime(LocalDateTime.now());
            this.save(refund);

            // 2. 创建关联投诉记录
            Complaint complaint = new Complaint();
            complaint.setUserId(userId);
            complaint.setShopId(order.getShopId());
            complaint.setAccusedId(order.getShopId());
            complaint.setOrderId(orderId);
            complaint.setRefundId(refund.getId());
            complaint.setSourceType(1); // 售后介入
            complaint.setType(0);
            complaint.setReason("再次申诉：" + reason);
            complaint.setDescription(description);
            complaint.setContent(description); // 冗余字段
            complaint.setStatus(0);
            complaint.setCreateTime(LocalDateTime.now());
            complaint.setUpdateTime(LocalDateTime.now());
            complaintMapper.insert(complaint);

            // 3. 更新订单状态为 5-退款中
            order.setStatus(5);
            ordersMapper.updateById(order);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户取消售后申请
     * 1. 更新售后状态为已取消 (status=4)
     * 2. 删除关联的投诉记录 (如果有)
     * 3. 恢复订单状态 (此处简化处理，恢复为已完成，实际应按需判断)
     */
    @Transactional
    public boolean cancelRefund(Long refundId) {
        try {
            Refund refund = this.getById(refundId);
            if (refund == null) return false;

            // 1. 更新售后状态
            refund.setStatus(4); // 已取消
            refund.setUpdateTime(LocalDateTime.now());
            this.updateById(refund);

            // 2. 删除关联的投诉记录（物理删除或逻辑删除，此处使用逻辑删除）
            complaintMapper.delete(new LambdaQueryWrapper<Complaint>()
                    .eq(Complaint::getRefundId, refundId));

            // 3. 恢复订单状态 (从 5-退款中 恢复。由于没有保存原状态，此处根据常理恢复为 3-已完成)
            // 备注：如果系统有严谨的状态流转记录，应恢复到申请售后前的状态。
            Orders order = new Orders();
            order.setId(refund.getOrderId());
            order.setStatus(3); // 恢复为已完成
            ordersMapper.updateById(order);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户申请管理员介入售后
     * 1. 标记售后需要介入（admin_intervention=1）
     * 2. 创建一条 sourceType=1 的投诉记录
     * 3. 通知商家有售后需管理员介入
     */
    public boolean requestAdminIntervention(Long refundId, Long userId, User user,
                                            String reason, String description) {
        try {
            Refund refund = this.getById(refundId);
            if (refund == null) return false;

            // 1. 标记售后需要介入
            refund.setAdminIntervention(1);
            refund.setUpdateTime(LocalDateTime.now());
            this.updateById(refund);

            // 2. 创建投诉记录（sourceType=1 表示售后客服介入）
            Complaint complaint = new Complaint();
            complaint.setUserId(userId);
            complaint.setUsername(user != null ? user.getUsername() : null);
            complaint.setShopId(refund.getShopId());
            complaint.setAccusedId(refund.getShopId());
            complaint.setOrderId(refund.getOrderId());
            complaint.setRefundId(refundId);
            complaint.setSourceType(1);
            complaint.setType(0);
            complaint.setReason(reason != null ? reason : "售后客服介入");
            complaint.setDescription(description);
            
            // 填充冗余字段以兼容旧表结构中的 NOT NULL 约束 (title 在实体类中标记为 exist=false，不写入)
            complaint.setContent(complaint.getDescription() != null ? complaint.getDescription() : complaint.getReason());
            
            complaint.setStatus(0); // 待处理
            complaint.setCreateTime(LocalDateTime.now());
            complaint.setUpdateTime(LocalDateTime.now());
            complaintMapper.insert(complaint);

            // 3. 通知商家
            try {
                messageService.sendMessage(Message.builder()
                        .userId(refund.getShopId())
                        .type(2)
                        .title("售后需管理员介入")
                        .content("用户申请管理员介入处理售后（ID:" + refundId + "），管理员将公正处理。商家可在售后列表查看详情，但处理权归管理员。")
                        .relatedId(refund.getOrderId())
                        .senderId(userId)
                        .build());
            } catch (Exception ignored) {}

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
