-- ============================================
-- 横州茉莉花商城 - 数据库修复脚本（兼容MySQL 5.7+）
-- 修复时间: 2026-04-15
-- 问题: photo_urls字段不存在导致SQL语法错误
-- ============================================

USE moli_mall;

-- ============================================
-- 辅助存储过程（兼容所有MySQL版本）
-- ============================================

DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table VARCHAR(255),
    IN p_column VARCHAR(255),
    IN p_definition VARCHAR(1000)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column;
    IF col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS modify_column_type;
DELIMITER //
CREATE PROCEDURE modify_column_type(
    IN p_table VARCHAR(255),
    IN p_column VARCHAR(255),
    IN p_new_type VARCHAR(1000)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN `', p_column, '` ', p_new_type);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_index_if_not_exists(
    IN p_table VARCHAR(255),
    IN p_index_name VARCHAR(255),
    IN p_columns VARCHAR(500)
)
BEGIN
    DECLARE idx_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_exists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index_name;
    IF idx_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index_name, '` (', p_columns, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS create_table_if_not_exists;
DELIMITER //
CREATE PROCEDURE create_table_if_not_exists(
    IN p_table VARCHAR(255),
    IN p_create_sql VARCHAR(8000)
)
BEGIN
    DECLARE tbl_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO tbl_exists
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table;
    IF tbl_exists = 0 THEN
        SET @sql = p_create_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ============================================
-- 1. 修复 acquisition_demand 表
-- ============================================

CALL create_table_if_not_exists('acquisition_demand',
'CREATE TABLE `acquisition_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL,
  `shop_name` VARCHAR(100) DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `price_min` DECIMAL(10,2) DEFAULT NULL,
  `price_max` DECIMAL(10,2) DEFAULT NULL,
  `unit` VARCHAR(20) DEFAULT ''斤'',
  `quantity_needed` DECIMAL(10,2) DEFAULT 0,
  `quantity_remaining` DECIMAL(10,2) DEFAULT 0,
  `market_address` VARCHAR(255) DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `demand_date` DATE DEFAULT NULL,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

CALL add_column_if_not_exists('acquisition_demand', 'shop_id', 'BIGINT NOT NULL COMMENT ''收购商ID''');
CALL add_column_if_not_exists('acquisition_demand', 'shop_name', 'VARCHAR(100) DEFAULT NULL COMMENT ''收购商名称''');
CALL add_column_if_not_exists('acquisition_demand', 'unit', 'VARCHAR(20) DEFAULT ''斤'' COMMENT ''单位''');
CALL add_column_if_not_exists('acquisition_demand', 'quantity_needed', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''计划收购量''');
CALL add_column_if_not_exists('acquisition_demand', 'quantity_remaining', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''剩余收购量''');
CALL add_column_if_not_exists('acquisition_demand', 'market_address', 'VARCHAR(255) DEFAULT NULL COMMENT ''市场地址''');
CALL add_column_if_not_exists('acquisition_demand', 'contact_phone', 'VARCHAR(20) DEFAULT NULL COMMENT ''联系电话''');
CALL add_column_if_not_exists('acquisition_demand', 'contact_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''联系人''');
CALL add_column_if_not_exists('acquisition_demand', 'description', 'TEXT DEFAULT NULL COMMENT ''备注说明''');
CALL add_column_if_not_exists('acquisition_demand', 'demand_date', 'DATE DEFAULT NULL COMMENT ''收购日期''');
CALL add_column_if_not_exists('acquisition_demand', 'created_at', 'DATETIME DEFAULT NULL COMMENT ''创建时间''');
CALL add_column_if_not_exists('acquisition_demand', 'updated_at', 'DATETIME DEFAULT NULL COMMENT ''更新时间''');
CALL add_index_if_not_exists('acquisition_demand', 'idx_shop_id', '(`shop_id`)');
CALL add_index_if_not_exists('acquisition_demand', 'idx_status', '(`status`)');

-- ============================================
-- 2. 修复 acquisition_application 表（关键！）
-- ============================================

CALL create_table_if_not_exists('acquisition_application',
'CREATE TABLE `acquisition_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `demand_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `user_nickname` VARCHAR(50) DEFAULT NULL,
  `farmer_name` VARCHAR(50) DEFAULT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `remark` TEXT DEFAULT NULL,
  `photo_urls` LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片URLs(JSON数组)'',
  `status` TINYINT DEFAULT 0,
  `actual_quantity` DECIMAL(10,2) DEFAULT NULL,
  `actual_price` DECIMAL(10,2) DEFAULT NULL,
  `total_amount` DECIMAL(10,2) DEFAULT NULL,
  `agree_time` DATETIME DEFAULT NULL,
  `delivery_time` DATETIME DEFAULT NULL,
  `complete_time` DATETIME DEFAULT NULL,
  `reject_reason` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

-- 添加关键字段
CALL add_column_if_not_exists('acquisition_application', 'user_nickname', 'VARCHAR(50) DEFAULT NULL COMMENT ''用户昵称''');
CALL add_column_if_not_exists('acquisition_application', 'farmer_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''花农姓名''');
CALL add_column_if_not_exists('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片URLs(JSON数组)''');
CALL add_column_if_not_exists('acquisition_application', 'remark', 'TEXT DEFAULT NULL COMMENT ''备注''');
CALL add_column_if_not_exists('acquisition_application', 'actual_quantity', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''实际斤数''');
CALL add_column_if_not_exists('acquisition_application', 'actual_price', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''成交单价''');
CALL add_column_if_not_exists('acquisition_application', 'total_amount', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''总金额''');
CALL add_column_if_not_exists('acquisition_application', 'agree_time', 'DATETIME DEFAULT NULL COMMENT ''同意时间''');
CALL add_column_if_not_exists('acquisition_application', 'delivery_time', 'DATETIME DEFAULT NULL COMMENT ''交付时间''');
CALL add_column_if_not_exists('acquisition_application', 'complete_time', 'DATETIME DEFAULT NULL COMMENT ''完成时间''');
CALL add_column_if_not_exists('acquisition_application', 'reject_reason', 'VARCHAR(500) DEFAULT NULL COMMENT ''拒绝原因''');
CALL add_column_if_not_exists('acquisition_application', 'created_at', 'DATETIME DEFAULT NULL COMMENT ''创建时间''');
CALL add_column_if_not_exists('acquisition_application', 'updated_at', 'DATETIME DEFAULT NULL COMMENT ''更新时间''');
CALL add_index_if_not_exists('acquisition_application', 'idx_demand_id', '(`demand_id`)');
CALL add_index_if_not_exists('acquisition_application', 'idx_user_id', '(`user_id`)');

-- 确保 photo_urls 是 LONGTEXT 类型
CALL modify_column_type('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片URLs(JSON数组)''');

-- ============================================
-- 3. 修复其他表
-- ============================================

-- sys_user 表
CALL add_column_if_not_exists('sys_user', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('sys_user', 'member_level', 'VARCHAR(50) DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'member_points', 'INT DEFAULT 0');
CALL add_column_if_not_exists('sys_user', 'balance', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('sys_user', 'shop_name', 'VARCHAR(100) DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'shop_category', 'VARCHAR(100) DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'merchant_name', 'VARCHAR(100) DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'shop_qualification_status', 'INT DEFAULT 0');
CALL add_column_if_not_exists('sys_user', 'specialty', 'VARCHAR(100) DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'experience', 'INT DEFAULT NULL');
CALL add_column_if_not_exists('sys_user', 'bio', 'TEXT DEFAULT NULL');
CALL modify_column_type('sys_user', 'avatar', 'LONGTEXT DEFAULT NULL');

-- product 表
CALL add_column_if_not_exists('product', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL modify_column_type('product', 'images', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('product', 'description', 'LONGTEXT DEFAULT NULL');
CALL add_column_if_not_exists('product', 'sales', 'INT DEFAULT 0');
CALL add_column_if_not_exists('product', 'views', 'INT DEFAULT 0');

-- orders 表
CALL add_column_if_not_exists('orders', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('orders', 'freight', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'discount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'actual_amount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'pay_time', 'DATETIME DEFAULT NULL');
CALL add_column_if_not_exists('orders', 'ship_time', 'DATETIME DEFAULT NULL');
CALL add_column_if_not_exists('orders', 'receive_time', 'DATETIME DEFAULT NULL');

-- order_item 表
CALL add_column_if_not_exists('order_item', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL modify_column_type('order_item', 'product_image', 'LONGTEXT DEFAULT NULL');

-- cart 表
CALL add_column_if_not_exists('cart', 'deleted', 'TINYINT NOT NULL DEFAULT 0');

-- product_review 表
CALL add_column_if_not_exists('product_review', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL modify_column_type('product_review', 'avatar', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('product_review', 'images', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('product_review', 'content', 'LONGTEXT NOT NULL');
CALL modify_column_type('product_review', 'reply', 'LONGTEXT DEFAULT NULL');

-- message 表
CALL add_column_if_not_exists('message', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('message', 'is_broadcast', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('message', 'scope', 'VARCHAR(20) DEFAULT NULL');
CALL add_column_if_not_exists('message', 'sender_id', 'BIGINT DEFAULT NULL');

-- chat_record 表
CALL add_column_if_not_exists('chat_record', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('chat_record', 'type', 'VARCHAR(20) DEFAULT ''chat''');
CALL add_column_if_not_exists('chat_record', 'related_msg_id', 'BIGINT DEFAULT NULL');

-- refund 表
CALL add_column_if_not_exists('refund', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('refund', 'refund_type', 'VARCHAR(20) NOT NULL DEFAULT ''refund''');
CALL add_column_if_not_exists('refund', 'admin_remark', 'VARCHAR(500) DEFAULT NULL');
CALL add_column_if_not_exists('refund', 'admin_intervention', 'TINYINT DEFAULT 0 COMMENT ''是否需要管理员介入: 0-否 1-是''');

-- category 表
CALL modify_column_type('category', 'icon', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_type;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS create_table_if_not_exists;

-- ============================================
-- 验证结果
-- ============================================
SELECT '========================================' AS '';
SELECT '  数据库修复完成！' AS result;
SELECT '========================================' AS '';

-- 检查 photo_urls 字段是否存在
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'moli_mall'
  AND TABLE_NAME = 'acquisition_application'
  AND COLUMN_NAME = 'photo_urls';
