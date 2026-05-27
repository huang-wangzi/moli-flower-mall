-- 茉莉花收购功能数据表
-- 创建时间: 2026-04-02

-- 收购需求表
CREATE TABLE IF NOT EXISTS `acquisition_demand` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
    `shop_id` BIGINT NOT NULL COMMENT '收购商商家ID',
    `shop_name` VARCHAR(100) COMMENT '收购商名称',
    `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
    `price_min` DECIMAL(10,2) NOT NULL COMMENT '最低收购价(元/斤)',
    `price_max` DECIMAL(10,2) NOT NULL COMMENT '最高收购价(元/斤)',
    `unit` VARCHAR(20) DEFAULT '斤' COMMENT '单位',
    `quantity_needed` DECIMAL(10,2) NOT NULL COMMENT '计划收购量(斤)',
    `quantity_remaining` DECIMAL(10,2) NOT NULL COMMENT '剩余收购量(斤)',
    `market_address` VARCHAR(255) COMMENT '市场地址',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `description` TEXT COMMENT '备注说明',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已下架 1-收购中 2-已结束',
    `demand_date` DATE COMMENT '收购日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_id` (`shop_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_demand_date` (`demand_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='茉莉花收购需求表';

-- 收购申请表
CREATE TABLE IF NOT EXISTS `acquisition_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `demand_id` BIGINT NOT NULL COMMENT '需求ID',
    `user_id` BIGINT NOT NULL COMMENT '花农用户ID',
    `user_nickname` VARCHAR(100) COMMENT '花农昵称',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '申请收购量(斤)',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `remark` TEXT COMMENT '备注说明',
    `photo_urls` TEXT COMMENT '茉莉花照片(JSON数组)',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已同意 2-交付中 3-已完成 4-已拒绝 5-已取消',
    `agree_time` DATETIME COMMENT '同意时间',
    `delivery_time` DATETIME COMMENT '交付时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `actual_quantity` DECIMAL(10,2) COMMENT '实际收购量(斤)',
    `actual_price` DECIMAL(10,2) COMMENT '实际成交单价(元/斤)',
    `total_amount` DECIMAL(12,2) COMMENT '总金额',
    `reject_reason` VARCHAR(255) COMMENT '拒绝原因',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_demand_id` (`demand_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='茉莉花收购申请表';
