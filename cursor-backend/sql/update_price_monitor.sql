-- 修改价格监测表：将 price 字段改为 min_price 和 max_price
-- 注意：执行前请先备份数据库

-- 1. 如果 price 字段存在，先修改它为可选
ALTER TABLE `price_monitor` MODIFY COLUMN `price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '已废弃';

-- 2. 添加新的最低价和最高价字段
ALTER TABLE `price_monitor` 
ADD COLUMN `min_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低价' AFTER `unit`,
ADD COLUMN `max_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最高价' AFTER `min_price`;

-- 3. 如果有旧数据，将 price 值复制到 min_price 和 max_price
-- 注意：只更新 price 字段不为 NULL 且 min_price/max_price 为 0 的记录
-- UPDATE `price_monitor` SET min_price = price, max_price = price WHERE price IS NOT NULL AND min_price = 0 AND max_price = 0;
