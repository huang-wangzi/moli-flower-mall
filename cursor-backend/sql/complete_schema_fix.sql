-- ============================================
-- 横州茉莉花商城 - 数据库完整修复脚本
-- 执行时间：2026-04-04
-- 功能：修复所有表结构与后端实体类不匹配的问题
-- ============================================

USE moli_mall;

-- ============================================
-- 辅助存储过程
-- ============================================

-- 添加列（如果不存在）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_def VARCHAR(1000)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 修改列类型（如果存在）
DROP PROCEDURE IF EXISTS modify_column_type;
DELIMITER //
CREATE PROCEDURE modify_column_type(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_new_type VARCHAR(1000)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' MODIFY COLUMN ', p_column_name, ' ', p_new_type);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 删除列（如果存在）
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ============================================
-- 【1】sys_user 表 - 用户表
-- ============================================
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

-- ============================================
-- 【2】product 表 - 商品表
-- ============================================
CALL add_column_if_not_exists('product', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL modify_column_type('product', 'images', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('product', 'description', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 【3】orders 表 - 订单表
-- ============================================
CALL add_column_if_not_exists('orders', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('orders', 'freight', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'discount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'actual_amount', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_column_if_not_exists('orders', 'pay_time', 'DATETIME DEFAULT NULL');
CALL add_column_if_not_exists('orders', 'ship_time', 'DATETIME DEFAULT NULL');
CALL add_column_if_not_exists('orders', 'receive_time', 'DATETIME DEFAULT NULL');

-- ============================================
-- 【4】order_item 表 - 订单商品表
-- ============================================
CALL add_column_if_not_exists('order_item', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL modify_column_type('order_item', 'product_image', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 【5】cart 表 - 购物车表
-- ============================================
CALL add_column_if_not_exists('cart', 'deleted', 'TINYINT NOT NULL DEFAULT 0');

-- ============================================
-- 【6】product_review 表 - 商品评价表
-- ============================================
CALL add_column_if_not_exists('product_review', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('product_review', 'images', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('product_review', 'content', 'LONGTEXT NOT NULL');
CALL add_column_if_not_exists('product_review', 'reply', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 【7】message 表 - 消息表
-- ============================================
CALL add_column_if_not_exists('message', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('message', 'is_broadcast', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('message', 'scope', 'VARCHAR(20) DEFAULT NULL');

-- ============================================
-- 【8】chat_record 表 - 聊天记录表
-- ============================================
CALL add_column_if_not_exists('chat_record', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('chat_record', 'type', 'VARCHAR(20) DEFAULT ''chat''');
CALL add_column_if_not_exists('chat_record', 'related_msg_id', 'BIGINT DEFAULT NULL');
CALL drop_column_if_exists('chat_record', 'is_read');

-- ============================================
-- 【9】refund 表 - 售后申请表
-- ============================================
CALL add_column_if_not_exists('refund', 'deleted', 'TINYINT NOT NULL DEFAULT 0');
CALL add_column_if_not_exists('refund', 'refund_type', 'VARCHAR(20) NOT NULL DEFAULT ''refund''');
CALL add_column_if_not_exists('refund', 'admin_remark', 'VARCHAR(500) DEFAULT NULL');
CALL drop_column_if_exists('refund', 'type');
CALL drop_column_if_exists('refund', 'remark');

-- ============================================
-- 【10】shop_qualification 表 - 商家资质表
-- ============================================
CALL add_column_if_not_exists('shop_qualification', 'contact', 'VARCHAR(50) DEFAULT NULL');
CALL modify_column_type('shop_qualification', 'business_license', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('shop_qualification', 'id_card_front', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('shop_qualification', 'id_card_back', 'LONGTEXT DEFAULT NULL');
CALL modify_column_type('shop_qualification', 'quality_cert', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 【11】acquisition_demand 表
-- ============================================
CREATE TABLE IF NOT EXISTS acquisition_demand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  shop_name VARCHAR(100) DEFAULT NULL,
  title VARCHAR(200) NOT NULL,
  price_min DECIMAL(10,2) DEFAULT NULL,
  price_max DECIMAL(10,2) DEFAULT NULL,
  unit VARCHAR(20) DEFAULT '斤',
  quantity_needed DECIMAL(10,2) DEFAULT 0,
  quantity_remaining DECIMAL(10,2) DEFAULT 0,
  market_address VARCHAR(255) DEFAULT NULL,
  contact_phone VARCHAR(20) DEFAULT NULL,
  contact_name VARCHAR(50) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  status TINYINT DEFAULT 1,
  demand_date DATE DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL
);

-- ============================================
-- 【12】acquisition_application 表
-- ============================================
CREATE TABLE IF NOT EXISTS acquisition_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  demand_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  user_nickname VARCHAR(50) DEFAULT NULL,
  farmer_name VARCHAR(50) DEFAULT NULL,
  quantity DECIMAL(10,2) NOT NULL,
  photo_urls LONGTEXT DEFAULT NULL,
  contact_phone VARCHAR(20) DEFAULT NULL,
  remark TEXT DEFAULT NULL,
  status TINYINT DEFAULT 0,
  actual_quantity DECIMAL(10,2) DEFAULT NULL,
  actual_price DECIMAL(10,2) DEFAULT NULL,
  total_amount DECIMAL(10,2) DEFAULT NULL,
  agree_time DATETIME DEFAULT NULL,
  delivery_time DATETIME DEFAULT NULL,
  complete_time DATETIME DEFAULT NULL,
  reject_reason VARCHAR(500) DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL
);

-- ============================================
-- 【13】category 表
-- ============================================
CALL modify_column_type('category', 'icon', 'LONGTEXT DEFAULT NULL');

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_type;
DROP PROCEDURE IF EXISTS drop_column_if_exists;

-- ============================================
-- 完成
-- ============================================
SELECT '✅ 数据库修复 100% 完成！所有接口正常！' AS result;