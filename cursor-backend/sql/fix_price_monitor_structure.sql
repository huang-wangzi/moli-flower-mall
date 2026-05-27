-- ============================================
-- 价格监测表修复脚本 (MySQL 5.x 兼容版)
-- ============================================

-- 1. 先检查当前表结构
DESCRIBE `price_monitor`;

-- 2. 检查字段是否存在，如果不存在则添加
-- 检查 min_price 字段
SELECT COUNT(*) INTO @has_min_price 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'price_monitor' 
  AND COLUMN_NAME = 'min_price';

-- 检查 max_price 字段
SELECT COUNT(*) INTO @has_max_price 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'price_monitor' 
  AND COLUMN_NAME = 'max_price';

-- 如果 min_price 不存在，添加它
SET @sql = IF(@has_min_price = 0, 
    'ALTER TABLE `price_monitor` ADD COLUMN `min_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT ''最低价'' AFTER `unit`',
    'SELECT ''min_price already exists'' AS status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 max_price 不存在，添加它
SET @sql = IF(@has_max_price = 0, 
    'ALTER TABLE `price_monitor` ADD COLUMN `max_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT ''最高价'' AFTER `min_price`',
    'SELECT ''max_price already exists'' AS status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 更新旧数据：如果有 price 字段值，将其复制到 min_price 和 max_price
UPDATE `price_monitor` 
SET min_price = COALESCE(price, min_price),
    max_price = COALESCE(price, max_price)
WHERE price IS NOT NULL 
  AND (min_price = 0 OR min_price IS NULL OR max_price = 0 OR max_price IS NULL);

-- 4. 确保没有NULL值
UPDATE `price_monitor` 
SET min_price = COALESCE(min_price, 0),
    max_price = COALESCE(max_price, 0)
WHERE min_price IS NULL OR max_price IS NULL;

-- 5. 验证修复结果
SELECT '当前表结构:' AS info;
DESCRIBE `price_monitor`;

SELECT '分类1 (茉莉鲜花) 数据统计:' AS info;
SELECT `record_date` AS '日期', `market` AS '市场', `min_price` AS '最低价', `max_price` AS '最高价' 
FROM `price_monitor` 
WHERE `category` = 1 
ORDER BY `record_date` DESC, `market` 
LIMIT 20;

-- 查看总数据量
SELECT COUNT(*) AS 'price_monitor表总记录数' FROM `price_monitor`;
