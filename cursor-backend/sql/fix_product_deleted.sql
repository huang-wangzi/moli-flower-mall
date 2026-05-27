-- ============================================
-- 修复 product 表缺失的 deleted 字段
-- 执行时间：2026-04-04
-- ============================================

USE moli_mall;

-- 辅助存储过程：添加列（如果不存在）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_def VARCHAR(500)
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
        SELECT CONCAT(p_column_name, ' 列已添加') AS result;
    ELSE
        SELECT CONCAT(p_column_name, ' 列已存在，跳过') AS result;
    END IF;
END//
DELIMITER ;

-- 执行修复
CALL add_column_if_not_exists('product', 'deleted',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除'' AFTER status');
CALL add_column_if_not_exists('orders', 'deleted',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除''');
CALL add_column_if_not_exists('order_item', 'deleted',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除''');
CALL add_column_if_not_exists('cart', 'deleted',
    'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除''');

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

SELECT 'deleted 字段修复完成' AS result;
