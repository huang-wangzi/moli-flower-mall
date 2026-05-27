-- ============================================
-- 紧急修复脚本（兼容MySQL 5.7+）
-- 执行时间：2026-04-04
-- ============================================

USE moli_mall;

-- ============================================
-- 【修复1】product 表 - 添加 deleted 字段
-- ============================================

-- 使用存储过程方式添加（兼容MySQL 5.7）
DROP PROCEDURE IF EXISTS fix_product_table;
DELIMITER //
CREATE PROCEDURE fix_product_table()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'product' 
        AND COLUMN_NAME = 'deleted'
    ) THEN
        ALTER TABLE product ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除' AFTER `status`;
    END IF;
END//
DELIMITER ;
CALL fix_product_table();
DROP PROCEDURE IF EXISTS fix_product_table;

-- ============================================
-- 【修复2】acquisition_demand 表 - 检查并创建/修复
-- ============================================

DROP PROCEDURE IF EXISTS fix_acquisition_demand_table;
DELIMITER //
CREATE PROCEDURE fix_acquisition_demand_table()
BEGIN
    -- 检查表是否存在
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'acquisition_demand'
    ) THEN
        -- 创建表
        CREATE TABLE `acquisition_demand` (
          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
          `shop_id` BIGINT NOT NULL COMMENT '收购商商家ID',
          `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '收购商名称',
          `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
          `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '最低收购价',
          `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '最高收购价',
          `unit` VARCHAR(20) DEFAULT '斤' COMMENT '单位',
          `quantity_needed` DECIMAL(10,2) DEFAULT 0 COMMENT '计划收购量',
          `quantity_remaining` DECIMAL(10,2) DEFAULT 0 COMMENT '剩余收购量',
          `market_address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址',
          `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
          `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
          `description` TEXT DEFAULT NULL COMMENT '备注说明',
          `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已下架 1-收购中 2-已结束',
          `demand_date` DATE DEFAULT NULL COMMENT '收购日期',
          `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
          `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
          PRIMARY KEY (`id`),
          KEY `idx_shop_id` (`shop_id`),
          KEY `idx_status` (`status`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='茉莉花收购需求表';
    ELSE
        -- 表存在，添加缺失字段
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'shop_name'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '收购商名称';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'market_address'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `market_address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'contact_phone'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'contact_name'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'description'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `description` TEXT DEFAULT NULL COMMENT '备注说明';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'unit'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `unit` VARCHAR(20) DEFAULT '斤' COMMENT '单位';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'quantity_needed'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `quantity_needed` DECIMAL(10,2) DEFAULT 0 COMMENT '计划收购量';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'quantity_remaining'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `quantity_remaining` DECIMAL(10,2) DEFAULT 0 COMMENT '剩余收购量';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'created_at'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `created_at` DATETIME DEFAULT NULL COMMENT '创建时间';
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = 'acquisition_demand' 
            AND COLUMN_NAME = 'updated_at'
        ) THEN
            ALTER TABLE acquisition_demand ADD COLUMN `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间';
        END IF;
    END IF;
END//
DELIMITER ;
CALL fix_acquisition_demand_table();
DROP PROCEDURE IF EXISTS fix_acquisition_demand_table;

-- ============================================
-- 完成
-- ============================================

SELECT '========================================' AS '';
SELECT '  紧急修复完成！' AS result;
SELECT '========================================' AS '';
