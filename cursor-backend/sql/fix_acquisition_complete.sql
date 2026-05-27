-- ============================================
-- 茉莉花收购系统数据库修复脚本
-- 请在 MySQL 中执行
-- ============================================

-- 1. 创建 acquisition_demand 表（如果不存在）
CREATE TABLE IF NOT EXISTS `acquisition_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '收购商ID（关联用户表）',
  `title` VARCHAR(100) NOT NULL COMMENT '需求标题',
  `description` TEXT DEFAULT NULL COMMENT '需求描述',
  `market_name` VARCHAR(100) DEFAULT NULL COMMENT '市场名称',
  `market_address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址',
  `quantity_needed` DECIMAL(10,2) DEFAULT NULL COMMENT '需要收购量（斤）',
  `quantity_remaining` DECIMAL(10,2) DEFAULT NULL COMMENT '剩余收购量（斤）',
  `total_demand` DECIMAL(10,2) DEFAULT 0 COMMENT '总需求量（斤）',
  `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '收购单价最低（元/斤）',
  `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '收购单价最高（元/斤）',
  `price_range` VARCHAR(50) DEFAULT NULL COMMENT '价格区间显示文本',
  `demand_date` DATE DEFAULT NULL COMMENT '收购日期',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-已下架 1-收购中 2-已结束',
  `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_demand_date` (`demand_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收购需求表';

-- 2. 创建 acquisition_application 表（如果不存在）
CREATE TABLE IF NOT EXISTS `acquisition_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `demand_id` BIGINT NOT NULL COMMENT '收购需求ID',
  `user_id` BIGINT NOT NULL COMMENT '申请人用户ID',
  `user_nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
  `farmer_name` VARCHAR(50) DEFAULT NULL COMMENT '花农姓名',
  `quantity` DECIMAL(10,2) DEFAULT NULL COMMENT '申请供货量（斤）',
  `photo_urls` LONGTEXT DEFAULT NULL COMMENT '茉莉花照片URLs（JSON数组）',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `remark` TEXT DEFAULT NULL COMMENT '备注',
  `apply_weight` DECIMAL(10,2) DEFAULT NULL COMMENT '申请重量（斤）',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已同意 2-已发货 3-已完成 4-已拒绝 5-已取消',
  `actual_quantity` DECIMAL(10,2) DEFAULT NULL COMMENT '实际交付斤数',
  `actual_price` DECIMAL(10,2) DEFAULT NULL COMMENT '成交单价',
  `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '总金额',
  `agree_time` DATETIME DEFAULT NULL COMMENT '同意时间',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
  `demand_title` VARCHAR(200) DEFAULT NULL COMMENT '需求标题（冗余）',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '收购商名称（冗余）',
  `market_address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址（冗余）',
  `price_range` VARCHAR(50) DEFAULT NULL COMMENT '价格区间（冗余）',
  `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '最低价',
  `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '最高价',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余）',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话（冗余）',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收购申请表';

-- 3. 创建 message 表（如果不存在）
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID（系统消息为null）',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-系统通知 2-订单通知 3-评价提醒 4-审核通知 5-聊天消息',
  `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `is_broadcast` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为广播消息: 0-普通消息 1-管理员广播',
  `scope` VARCHAR(20) DEFAULT NULL COMMENT '广播范围: all-全部 users-普通用户 shops-商家',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 4. 查看当前数据
SELECT '===== 当前数据统计 =====' AS info;
SELECT '收购需求表数据量' AS info, COUNT(*) AS count FROM acquisition_demand WHERE deleted = 0;
SELECT '收购申请表数据量' AS info, COUNT(*) AS count FROM acquisition_application WHERE deleted = 0;
SELECT '消息表数据量' AS info, COUNT(*) AS count FROM message WHERE deleted = 0;

-- 5. 查看收购商发布的收购需求
SELECT '===== 收购需求列表 =====' AS info;
SELECT d.id, d.shop_id, d.title, d.status, d.quantity_needed, d.demand_date,
       u.username AS shop_username, u.shop_name, u.merchant_name
FROM acquisition_demand d
LEFT JOIN sys_user u ON d.shop_id = u.id
WHERE d.deleted = 0
ORDER BY d.created_at DESC;

-- 6. 查看所有收购申请
SELECT '===== 收购申请列表 =====' AS info;
SELECT a.id, a.demand_id, a.user_id, a.status, a.quantity, a.created_at,
       d.shop_id AS demand_shop_id,
       u1.username AS applicant_username,
       u2.username AS shop_username
FROM acquisition_application a
LEFT JOIN acquisition_demand d ON a.demand_id = d.id
LEFT JOIN sys_user u1 ON a.user_id = u1.id
LEFT JOIN sys_user u2 ON d.shop_id = u2.id
WHERE a.deleted = 0
ORDER BY a.created_at DESC;

-- 7. 检查收购商用户
SELECT '===== 收购商用户 =====' AS info;
SELECT id, username, role, shop_type, status FROM sys_user 
WHERE role = 2 AND shop_type = 2 AND deleted = 0;