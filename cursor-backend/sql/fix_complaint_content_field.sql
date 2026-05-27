-- 修复 complaint 表缺少 content 字段的问题
-- 执行时间: 2026-04-19

-- 检查 content 字段是否存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'complaint' 
    AND COLUMN_NAME = 'content'
);

-- 如果字段不存在，添加它
SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE complaint ADD COLUMN content TEXT DEFAULT NULL COMMENT "投诉内容（冗余，等同于 description）"',
    'SELECT "content 字段已存在，无需添加" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证修复结果
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'complaint' 
AND COLUMN_NAME IN ('content', 'description', 'reason');
