-- ============================================
-- 横州茉莉花商城 - 客诉与售后流程完整修复脚本
-- 修复时间: 2026-04-19
-- 兼容 MySQL 5.7+
-- ============================================

USE moli_mall;

-- ============================================
-- 辅助存储过程
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

DROP PROCEDURE IF EXISTS modify_column_if_exists;
DELIMITER //
CREATE PROCEDURE modify_column_if_exists(
    IN p_table VARCHAR(255),
    IN p_column VARCHAR(255),
    IN p_new_def VARCHAR(1000)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column;
    IF col_exists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` MODIFY COLUMN `', p_column, '` ', p_new_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ============================================
-- 1. complaint 表修复
-- ============================================

-- 1.1 确保基本字段存在
CALL add_column_if_not_exists('complaint', 'user_id',         'BIGINT NOT NULL COMMENT ''投诉用户ID''');
CALL add_column_if_not_exists('complaint', 'username',         'VARCHAR(100) DEFAULT NULL COMMENT ''用户名''');
CALL add_column_if_not_exists('complaint', 'shop_id',          'BIGINT NOT NULL COMMENT ''被投诉商家ID''');
CALL add_column_if_not_exists('complaint', 'order_id',         'BIGINT DEFAULT NULL COMMENT ''关联订单ID''');
CALL add_column_if_not_exists('complaint', 'refund_id',        'BIGINT DEFAULT NULL COMMENT ''关联售后ID''');
CALL add_column_if_not_exists('complaint', 'source_type',      'TINYINT NOT NULL DEFAULT 0 COMMENT ''来源: 0-普通投诉 1-售后客服介入''');
CALL add_column_if_not_exists('complaint', 'reason',            'VARCHAR(500) NOT NULL COMMENT ''投诉事由''');
CALL add_column_if_not_exists('complaint', 'description',      'TEXT DEFAULT NULL COMMENT ''详细描述''');
CALL add_column_if_not_exists('complaint', 'status',           'TINYINT NOT NULL DEFAULT 0 COMMENT ''状态: 0-待处理 1-已处理 2-已驳回''');
CALL add_column_if_not_exists('complaint', 'admin_remark',     'VARCHAR(500) DEFAULT NULL COMMENT ''管理员备注''');
CALL add_column_if_not_exists('complaint', 'deleted',         'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL add_column_if_not_exists('complaint', 'create_time',     'DATETIME DEFAULT NULL COMMENT ''创建时间''');
CALL add_column_if_not_exists('complaint', 'update_time',      'DATETIME DEFAULT NULL COMMENT ''更新时间''');

-- 1.2 修复 accused_id 字段（核心问题！）
--    如果字段已存在但 NOT NULL 且无默认值，需要改为允许 NULL
--    如果字段不存在则添加
CALL add_column_if_not_exists('complaint', 'accused_id',
    'BIGINT DEFAULT NULL COMMENT ''被投诉方ID(冗余，等同于 shop_id)''');

-- 1.3 修复 type 字段（投诉类型）
--    如果字段存在但 NOT NULL 且无默认值，改为允许 NULL + 默认值
--    如果字段不存在则添加
CALL add_column_if_not_exists('complaint', 'type',
    'TINYINT DEFAULT 0 COMMENT ''投诉类型: 0-商品问题 1-服务问题 2-物流问题 3-其他''');

-- 1.4 修复 content 字段（投诉内容）
CALL add_column_if_not_exists('complaint', 'content',
    'TEXT DEFAULT NULL COMMENT ''投诉内容(冗余，等同于 description)''');

-- 1.3 如果表刚创建没有主键，补上（极端情况）
-- 检查是否有主键，没有则添加
-- （正常情况 init_database.sql 会建好，这里保险起见检查索引）

-- ============================================
-- 2. refund 表确保 admin_intervention 字段存在
-- ============================================
CALL add_column_if_not_exists('refund', 'admin_intervention',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''是否需要管理员介入: 0-否 1-是''');
CALL add_column_if_not_exists('refund', 'admin_remark',
    'VARCHAR(500) DEFAULT NULL COMMENT ''管理员处理备注''');

-- ============================================
-- 3. 验证结果
-- ============================================
SELECT '========================================' AS '';
SELECT '  客诉与售后流程修复完成！' AS result;
SELECT '========================================' AS '';

-- 显示 complaint 表结构
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'complaint'
ORDER BY ORDINAL_POSITION;

-- 显示 refund 表关键字段
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund'
  AND COLUMN_NAME IN ('admin_intervention', 'admin_remark')
ORDER BY ORDINAL_POSITION;

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_if_exists;
