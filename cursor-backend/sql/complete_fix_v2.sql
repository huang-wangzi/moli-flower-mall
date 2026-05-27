-- ============================================
-- 横州茉莉花商城 - 完整数据库修复脚本 v2.0
-- 执行时间：2026-04-04
-- 修复：表结构、缺失字段、数据类型、索引
-- ============================================

USE moli_mall;

-- ============================================
-- 辅助存储过程
-- ============================================
DROP PROCEDURE IF EXISTS add_column;
DELIMITER //
CREATE PROCEDURE add_column(IN tbl VARCHAR(64), IN col VARCHAR(64), IN def VARCHAR(1000))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', def);
        PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS fix_col;
DELIMITER //
CREATE PROCEDURE fix_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN new_def VARCHAR(1000))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' MODIFY COLUMN ', col, ' ', new_def);
        PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS add_index;
DELIMITER //
CREATE PROCEDURE add_index(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN cols VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND INDEX_NAME = idx
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD INDEX ', idx, ' (', cols, ')');
        PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS create_table_safe;
DELIMITER //
CREATE PROCEDURE create_table_safe(IN tbl VARCHAR(64), IN sql_stmt VARCHAR(8000))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl) THEN
        SET @s = sql_stmt;
        PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
    END IF;
END//
DELIMITER ;

-- ============================================
-- 【1】sys_user 表 - 用户表
-- ============================================
CALL add_column('sys_user', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('sys_user', 'member_level', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('sys_user', 'member_points', 'INT DEFAULT 0');
CALL add_column('sys_user', 'balance', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column('sys_user', 'shop_name', 'VARCHAR(100) DEFAULT NULL');
CALL add_column('sys_user', 'shop_category', 'VARCHAR(100) DEFAULT NULL');
CALL add_column('sys_user', 'merchant_name', 'VARCHAR(100) DEFAULT NULL');
CALL add_column('sys_user', 'shop_qualification_status', 'INT DEFAULT 0');
CALL add_column('sys_user', 'specialty', 'VARCHAR(100) DEFAULT NULL');
CALL add_column('sys_user', 'experience', 'INT DEFAULT NULL');
CALL add_column('sys_user', 'bio', 'TEXT DEFAULT NULL');
CALL fix_col('sys_user', 'avatar', 'LONGTEXT DEFAULT NULL');
CALL fix_col('sys_user', 'phone', 'VARCHAR(20) DEFAULT NULL');

-- ============================================
-- 【2】product 表 - 商品表
-- ============================================
CALL add_column('product', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL fix_col('product', 'images', 'LONGTEXT DEFAULT NULL');
CALL fix_col('product', 'description', 'LONGTEXT DEFAULT NULL');
CALL add_column('product', 'sales', 'INT DEFAULT 0');
CALL add_column('product', 'views', 'INT DEFAULT 0');

-- ============================================
-- 【3】orders 表 - 订单表
-- ============================================
CALL add_column('orders', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('orders', 'freight', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column('orders', 'discount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column('orders', 'actual_amount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column('orders', 'pay_time', 'DATETIME DEFAULT NULL');
CALL add_column('orders', 'ship_time', 'DATETIME DEFAULT NULL');
CALL add_column('orders', 'receive_time', 'DATETIME DEFAULT NULL');
CALL add_column('orders', 'receiver_name', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('orders', 'receiver_phone', 'VARCHAR(20) DEFAULT NULL');
CALL add_column('orders', 'receiver_address', 'VARCHAR(255) DEFAULT NULL');
CALL add_column('orders', 'remark', 'VARCHAR(500) DEFAULT NULL');

-- ============================================
-- 【4】order_item 表 - 订单商品表
-- ============================================
CALL add_column('order_item', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL fix_col('order_item', 'product_image', 'LONGTEXT DEFAULT NULL');
CALL add_column('order_item', 'specs', 'VARCHAR(255) DEFAULT NULL');

-- ============================================
-- 【5】cart 表 - 购物车表
-- ============================================
CALL add_column('cart', 'deleted', 'TINYINT NOT NULL DEFAULT 0');

-- ============================================
-- 【6】product_review 表 - 商品评价表
-- ============================================
CALL add_column('product_review', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL fix_col('product_review', 'avatar', 'LONGTEXT DEFAULT NULL');
CALL fix_col('product_review', 'images', 'LONGTEXT DEFAULT NULL');
CALL fix_col('product_review', 'content', 'LONGTEXT NOT NULL');
CALL fix_col('product_review', 'reply', 'LONGTEXT DEFAULT NULL');
CALL add_column('product_review', 'status', 'TINYINT DEFAULT 1');
CALL add_column('product_review', 'reply_time', 'DATETIME DEFAULT NULL');

-- ============================================
-- 【7】message 表 - 消息表
-- ============================================
CALL add_column('message', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('message', 'is_broadcast', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('message', 'scope', 'VARCHAR(20) DEFAULT NULL');
CALL add_column('message', 'sender_id', 'BIGINT DEFAULT NULL');
CALL add_column('message', 'is_read', 'TINYINT DEFAULT 0');

-- ============================================
-- 【8】chat_record 表 - 聊天记录表
-- ============================================
CALL add_column('chat_record', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('chat_record', 'type', 'VARCHAR(20) DEFAULT "chat"');
CALL add_column('chat_record', 'related_msg_id', 'BIGINT DEFAULT NULL');
CALL add_column('chat_record', 'product_id', 'BIGINT DEFAULT NULL');
CALL add_column('chat_record', 'is_read', 'TINYINT DEFAULT 0');

-- ============================================
-- 【9】refund 表 - 退款表
-- ============================================
CALL add_column('refund', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('refund', 'refund_type', 'VARCHAR(20) NOT NULL DEFAULT "refund"');
CALL add_column('refund', 'admin_remark', 'VARCHAR(500) DEFAULT NULL');

-- ============================================
-- 【10】complaint 表 - 投诉表
-- ============================================
CALL create_table_safe('complaint', 
'CREATE TABLE `complaint` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `shop_id` BIGINT NOT NULL,
  `order_id` BIGINT DEFAULT NULL,
  `refund_id` BIGINT DEFAULT NULL,
  `source_type` TINYINT DEFAULT 0,
  `reason` TEXT NOT NULL,
  `description` TEXT DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `admin_remark` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');
CALL add_column('complaint', 'source_type', 'TINYINT DEFAULT 0');
CALL add_column('complaint', 'admin_remark', 'VARCHAR(500) DEFAULT NULL');
CALL add_column('complaint', 'create_time', 'DATETIME DEFAULT NULL');
CALL add_column('complaint', 'update_time', 'DATETIME DEFAULT NULL');
CALL add_column('complaint', 'deleted', 'TINYINT NOT NULL DEFAULT 0');

-- ============================================
-- 【11】shop_qualification 表 - 商家资质表
-- ============================================
CALL create_table_safe('shop_qualification', 
'CREATE TABLE `shop_qualification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `shop_name` VARCHAR(100) NOT NULL,
  `business_license` LONGTEXT DEFAULT NULL,
  `id_card_front` LONGTEXT DEFAULT NULL,
  `id_card_back` LONGTEXT DEFAULT NULL,
  `quality_cert` LONGTEXT DEFAULT NULL,
  `contact` VARCHAR(50) DEFAULT NULL,
  `status` INT DEFAULT 0,
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');
CALL add_column('shop_qualification', 'contact', 'VARCHAR(50) DEFAULT NULL');

-- ============================================
-- 【12】acquisition_demand 表 - 收购需求表
-- ============================================
CALL create_table_safe('acquisition_demand', 
'CREATE TABLE `acquisition_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL,
  `shop_name` VARCHAR(100) DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `price_min` DECIMAL(10,2) DEFAULT NULL,
  `price_max` DECIMAL(10,2) DEFAULT NULL,
  `unit` VARCHAR(20) DEFAULT "斤",
  `quantity_needed` DECIMAL(10,2) DEFAULT 0,
  `quantity_remaining` DECIMAL(10,2) DEFAULT 0,
  `market_address` VARCHAR(255) DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `status` TINYINT DEFAULT 1 COMMENT "1=收购中, 0=已下架, 2=已结束",
  `demand_date` DATE DEFAULT NULL,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_demand_date` (`demand_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

-- ============================================
-- 【13】acquisition_application 表 - 收购申请表
-- ============================================
CALL create_table_safe('acquisition_application', 
'CREATE TABLE `acquisition_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `demand_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `user_nickname` VARCHAR(50) DEFAULT NULL,
  `farmer_name` VARCHAR(50) DEFAULT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `photo_urls` LONGTEXT DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `remark` TEXT DEFAULT NULL,
  `status` TINYINT DEFAULT 0 COMMENT "0=待审核, 1=已同意, 2=交付中, 3=已完成, 4=已拒绝, 5=已取消",
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
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');
CALL fix_col('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 【14】receiver_address 表 - 收货地址表
-- ============================================
CALL add_column('receiver_address', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column('receiver_address', 'province', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('receiver_address', 'city', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('receiver_address', 'district', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('receiver_address', 'detail', 'VARCHAR(255) DEFAULT NULL');
CALL add_column('receiver_address', 'tag', 'VARCHAR(50) DEFAULT NULL');
CALL add_column('receiver_address', 'is_default', 'TINYINT DEFAULT 0');

-- ============================================
-- 【15】coupon 表 - 优惠券表
-- ============================================
CALL add_column('coupon', 'valid_from', 'DATETIME DEFAULT NULL');
CALL add_column('coupon', 'valid_to', 'DATETIME DEFAULT NULL');
CALL add_column('coupon', 'total_count', 'INT DEFAULT 0');
CALL add_column('coupon', 'receive_count', 'INT DEFAULT 0');
CALL add_column('coupon', 'use_count', 'INT DEFAULT 0');

-- ============================================
-- 【16】user_coupon 表 - 用户优惠券表
-- ============================================
CALL add_column('user_coupon', 'order_id', 'BIGINT DEFAULT NULL');
CALL add_column('user_coupon', 'use_time', 'DATETIME DEFAULT NULL');

-- ============================================
-- 【17】category 表 - 商品分类表
-- ============================================
CALL fix_col('category', 'icon', 'LONGTEXT DEFAULT NULL');
CALL add_column('category', 'sort_order', 'INT DEFAULT 0');

-- ============================================
-- 【18】product_browse 表 - 商品浏览记录表
-- ============================================
CALL create_table_safe('product_browse', 
'CREATE TABLE `product_browse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `browse_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_browse_time` (`browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS add_column;
DROP PROCEDURE IF EXISTS fix_col;
DROP PROCEDURE IF EXISTS add_index;
DROP PROCEDURE IF EXISTS create_table_safe;

-- ============================================
-- 插入测试数据（如果表为空）
-- ============================================
INSERT IGNORE INTO sys_user (id, username, password, phone, role, status, create_time) VALUES
(1, 'admin', '$2a$10$X/5J8Z5Z5Z5Z5Z5Z5Z5Z5u5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', '13800138000', 4, 1, NOW()),
(11, 'shop11', '$2a$10$X/5J8Z5Z5Z5Z5Z5Z5Z5Zu5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', '13900139000', 2, 1, NOW());

INSERT IGNORE INTO category (id, name, icon, sort_order) VALUES
(1, '茉莉花茶', 'tea', 1),
(2, '茉莉盆栽', 'plant', 2);

INSERT IGNORE INTO product (id, name, images, description, price, stock, shop_id, category_id, status, deleted, sales, views) VALUES
(1, '优质茉莉花茶', '["https://picsum.photos/400"]', '正宗横州茉莉花茶', 99.00, 100, 11, 1, 1, 0, 10, 50);

-- ============================================
-- 修复完成
-- ============================================
SELECT '========================================' AS '';
SELECT '  数据库修复完成！所有表结构已对齐' AS result;
SELECT '  修复了：表结构、缺失字段、索引' AS detail;
SELECT '========================================' AS '';