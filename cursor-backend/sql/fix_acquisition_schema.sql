-- =========================================
-- 收购模块数据库修复脚本 v5
-- 修复时间: 2026-04-05
-- 适用于 Entity 结构: quantity_needed, quantity_remaining, photo_urls
-- =========================================

USE moli_mall;

-- =========================================
-- 1. 创建 acquisition_demand 表
-- =========================================
CREATE TABLE IF NOT EXISTS `acquisition_demand` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
    `shop_id` BIGINT NOT NULL COMMENT '收购商商家ID',
    `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '收购商名称',
    `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
    `price_min` DECIMAL(10,2) NOT NULL COMMENT '最低收购价(元/斤)',
    `price_max` DECIMAL(10,2) NOT NULL COMMENT '最高收购价(元/斤)',
    `unit` VARCHAR(20) DEFAULT '斤' COMMENT '单位',
    `quantity_needed` DECIMAL(10,2) DEFAULT 0 COMMENT '计划收购量(斤)',
    `quantity_remaining` DECIMAL(10,2) DEFAULT 0 COMMENT '剩余收购量(斤)',
    `market_address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `description` TEXT DEFAULT NULL COMMENT '备注说明',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已下架 1-收购中 2-已结束',
    `demand_date` DATE DEFAULT NULL COMMENT '收购日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_id` (`shop_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_demand_date` (`demand_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='茉莉花收购需求表';

-- =========================================
-- 2. 创建 acquisition_application 表
-- =========================================
CREATE TABLE IF NOT EXISTS `acquisition_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `demand_id` BIGINT NOT NULL COMMENT '需求ID',
    `user_id` BIGINT NOT NULL COMMENT '花农用户ID',
    `user_nickname` VARCHAR(100) DEFAULT NULL COMMENT '花农昵称',
    `farmer_name` VARCHAR(50) DEFAULT NULL COMMENT '花农姓名',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '申请收购量(斤)',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `remark` TEXT DEFAULT NULL COMMENT '备注说明',
    `photo_urls` LONGTEXT DEFAULT NULL COMMENT '茉莉花照片(JSON数组)',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已同意 2-交付中 3-已完成 4-已拒绝 5-已取消',
    `agree_time` DATETIME DEFAULT NULL COMMENT '同意时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '交付时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `actual_quantity` DECIMAL(10,2) DEFAULT NULL COMMENT '实际收购量(斤)',
    `actual_price` DECIMAL(10,2) DEFAULT NULL COMMENT '实际成交单价(元/斤)',
    `total_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '总金额',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_demand_id` (`demand_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='茉莉花收购申请表';

-- =========================================
-- 3. 使用存储过程添加缺失列（兼容所有MySQL版本）
-- =========================================

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER //

CREATE PROCEDURE add_column_if_not_exists(
    IN table_name VARCHAR(255),
    IN column_name VARCHAR(255),
    IN column_definition VARCHAR(1000)
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;
    
    IF column_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD COLUMN `', column_name, '` ', column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Added column: ', column_name, ' to ', table_name) AS result;
    ELSE
        SELECT CONCAT('Column already exists: ', column_name) AS result;
    END IF;
END //

DELIMITER ;

-- =========================================
-- 4. 为 acquisition_demand 表添加缺失列
-- =========================================
CALL add_column_if_not_exists('acquisition_demand', 'shop_name', 'VARCHAR(100) DEFAULT NULL COMMENT ''收购商名称''');
CALL add_column_if_not_exists('acquisition_demand', 'unit', 'VARCHAR(20) DEFAULT ''斤'' COMMENT ''单位''');
CALL add_column_if_not_exists('acquisition_demand', 'demand_date', 'DATE DEFAULT NULL COMMENT ''收购日期''');
CALL add_column_if_not_exists('acquisition_demand', 'contact_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''联系人''');
CALL add_column_if_not_exists('acquisition_demand', 'description', 'TEXT DEFAULT NULL COMMENT ''备注说明''');
CALL add_column_if_not_exists('acquisition_demand', 'quantity_needed', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''计划收购量''');
CALL add_column_if_not_exists('acquisition_demand', 'quantity_remaining', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''剩余收购量''');

-- =========================================
-- 5. 为 acquisition_application 表添加缺失列
-- =========================================
CALL add_column_if_not_exists('acquisition_application', 'user_nickname', 'VARCHAR(100) DEFAULT NULL COMMENT ''花农昵称''');
CALL add_column_if_not_exists('acquisition_application', 'farmer_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''花农姓名''');
CALL add_column_if_not_exists('acquisition_application', 'remark', 'TEXT DEFAULT NULL COMMENT ''备注''');
CALL add_column_if_not_exists('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片''');
CALL add_column_if_not_exists('acquisition_application', 'actual_quantity', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''实际收购量''');
CALL add_column_if_not_exists('acquisition_application', 'actual_price', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''实际成交单价''');
CALL add_column_if_not_exists('acquisition_application', 'total_amount', 'DECIMAL(12,2) DEFAULT NULL COMMENT ''总金额''');
CALL add_column_if_not_exists('acquisition_application', 'agree_time', 'DATETIME DEFAULT NULL COMMENT ''同意时间''');
CALL add_column_if_not_exists('acquisition_application', 'delivery_time', 'DATETIME DEFAULT NULL COMMENT ''交付时间''');
CALL add_column_if_not_exists('acquisition_application', 'complete_time', 'DATETIME DEFAULT NULL COMMENT ''完成时间''');
CALL add_column_if_not_exists('acquisition_application', 'reject_reason', 'VARCHAR(500) DEFAULT NULL COMMENT ''拒绝原因''');

-- =========================================
-- 6. 显示表结构验证
-- =========================================
SELECT 'acquisition_demand 表结构:' AS info;
DESCRIBE acquisition_demand;

SELECT 'acquisition_application 表结构:' AS info;
DESCRIBE acquisition_application;

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

SELECT '收购模块数据库修复完成！' AS result;
