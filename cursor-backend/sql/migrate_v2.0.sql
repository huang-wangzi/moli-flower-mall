-- ============================================
-- 横州茉莉花商城 - 数据库迁移脚本（v2.0 最终修复版）
-- 完全基于你原版，只修错误，不改逻辑
-- ============================================

USE moli_mall;

-- 辅助存储过程：添加列（如果不存在）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64),
    IN column_def VARCHAR(500)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;
    IF col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD COLUMN ', column_name, ' ', column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 辅助存储过程：修改列（如果存在）
DROP PROCEDURE IF EXISTS modify_column_if_exists;
DELIMITER //
CREATE PROCEDURE modify_column_if_exists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64),
    IN column_def VARCHAR(500)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' MODIFY COLUMN ', column_name, ' ', column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 辅助存储过程：删除列（如果存在）
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' DROP COLUMN ', column_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 【1】用户表
CALL add_column_if_not_exists('sys_user', 'shop_qualification_status',
    'INT DEFAULT 0 COMMENT ''商家资质审核状态: 0-未提交 1-待审核 2-通过 3-拒绝'' AFTER shop_category');

-- 【2】消息表
CALL add_column_if_not_exists('message', 'is_broadcast',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''是否为广播消息: 0-普通消息 1-管理员广播'' AFTER is_read');
CALL add_column_if_not_exists('message', 'scope',
    'VARCHAR(20) DEFAULT NULL COMMENT ''广播范围: all-全部 users-普通用户 shops-商家'' AFTER is_broadcast');

-- 【3】聊天记录表
CALL add_column_if_not_exists('chat_record', 'type',
    'VARCHAR(20) DEFAULT ''chat'' COMMENT ''消息类型: chat-普通聊天 broadcast_reply-广播回复'' AFTER product_id');
CALL add_column_if_not_exists('chat_record', 'related_msg_id',
    'BIGINT DEFAULT NULL COMMENT ''关联的广播消息ID（用于广播回复场景）'' AFTER type');

-- 【4】售后表
CALL add_column_if_not_exists('refund', 'refund_type',
    'VARCHAR(20) NOT NULL DEFAULT ''refund'' COMMENT ''类型: refund-仅退款 return-退货退款'' AFTER shop_id');
CALL add_column_if_not_exists('refund', 'admin_remark',
    'VARCHAR(500) DEFAULT NULL COMMENT ''管理员处理备注'' AFTER status');

-- 【5】商品表
CALL modify_column_if_exists('product', 'images',
    'LONGTEXT DEFAULT NULL COMMENT ''商品图片(JSON：URL或Base64)''');
CALL modify_column_if_exists('product', 'description',
    'LONGTEXT DEFAULT NULL COMMENT ''商品描述''');

-- 【6】订单商品表
CALL modify_column_if_exists('order_item', 'product_image',
    'LONGTEXT DEFAULT NULL COMMENT ''商品图片(JSON：URL或Base64)''');

-- 【7】用户表头像
CALL modify_column_if_exists('sys_user', 'avatar',
    'LONGTEXT DEFAULT NULL COMMENT ''头像(URL或Base64)''');

-- 【8】评价表（修复：不存在的列不修改！）
CALL add_column_if_not_exists('product_review', 'images', 'LONGTEXT DEFAULT NULL COMMENT ''评价图片JSON''');
CALL add_column_if_not_exists('product_review', 'content', 'LONGTEXT NOT NULL COMMENT ''评价内容''');
CALL add_column_if_not_exists('product_review', 'reply', 'LONGTEXT DEFAULT NULL COMMENT ''商家回复''');

-- 【9】分类表
CALL modify_column_if_exists('category', 'icon',
    'LONGTEXT DEFAULT NULL COMMENT ''分类图标''');

-- 【10】商家资质表
CALL add_column_if_not_exists('shop_qualification', 'contact',
    'VARCHAR(50) DEFAULT NULL COMMENT ''联系电话'' AFTER quality_cert');
CALL modify_column_if_exists('shop_qualification', 'business_license', 'LONGTEXT DEFAULT NULL');
CALL modify_column_if_exists('shop_qualification', 'id_card_front', 'LONGTEXT DEFAULT NULL');
CALL modify_column_if_exists('shop_qualification', 'id_card_back', 'LONGTEXT DEFAULT NULL');
CALL modify_column_if_exists('shop_qualification', 'quality_cert', 'LONGTEXT DEFAULT NULL');

-- 清理
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_if_exists;
DROP PROCEDURE IF EXISTS drop_column_if_exists;

SELECT 'migrate_v2.0 执行完成，所有表结构已对齐实体类' AS result;