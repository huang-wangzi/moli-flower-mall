-- ============================================
-- 价格监测表完整修复脚本
-- 用于确保 price_monitor 表有正确的字段和初始数据
-- ============================================

-- 1. 检查表结构
DESCRIBE `price_monitor`;

-- 2. 如果 price 字段存在但 min_price 不存在，添加新字段
ALTER TABLE `price_monitor` 
ADD COLUMN IF NOT EXISTS `min_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低价' AFTER `unit`,
ADD COLUMN IF NOT EXISTS `max_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最高价' AFTER `min_price`;

-- 3. 如果旧数据有 price 但 min_price/max_price 为 0，更新它们
UPDATE `price_monitor` 
SET min_price = COALESCE(price, min_price),
    max_price = COALESCE(price, max_price)
WHERE (min_price = 0 OR min_price IS NULL) 
  AND (max_price = 0 OR max_price IS NULL)
  AND price IS NOT NULL;

-- 4. 确保所有记录都有 min_price 和 max_price
UPDATE `price_monitor` 
SET min_price = COALESCE(min_price, 0),
    max_price = COALESCE(max_price, 0)
WHERE min_price IS NULL OR max_price IS NULL;

-- 5. 删除所有旧数据（可选 - 如果需要重新初始化）
-- DELETE FROM `price_monitor`;

-- 6. 插入新的测试数据（茉莉鲜花，分类1）
INSERT INTO `price_monitor` (`category`, `market`, `unit`, `min_price`, `max_price`, `record_date`) VALUES
-- 横州茉莉花交易市场
(1, '横州茉莉花交易市场', '元/斤', 24.00, 26.50, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 24.50, 27.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 25.00, 27.50, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 24.80, 26.80, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 25.50, 28.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 26.00, 28.50, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, '横州茉莉花交易市场', '元/斤', 25.80, 28.20, CURDATE()),

-- 中华茉莉园
(1, '中华茉莉园', '元/斤', 23.50, 26.00, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '中华茉莉园', '元/斤', 24.00, 26.50, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '中华茉莉园', '元/斤', 23.80, 26.20, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '中华茉莉园', '元/斤', 24.50, 27.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '中华茉莉园', '元/斤', 25.00, 27.50, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '中华茉莉园', '元/斤', 24.80, 27.20, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, '中华茉莉园', '元/斤', 25.20, 27.80, CURDATE()),

-- 横县茉莉花市场
(1, '横县茉莉花市场', '元/斤', 24.20, 26.80, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '横县茉莉花市场', '元/斤', 24.80, 27.20, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '横县茉莉花市场', '元/斤', 25.20, 27.80, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '横县茉莉花市场', '元/斤', 24.50, 27.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '横县茉莉花市场', '元/斤', 25.80, 28.50, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '横县茉莉花市场', '元/斤', 26.20, 29.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, '横县茉莉花市场', '元/斤', 26.00, 28.80, CURDATE()),

-- 云表镇茉莉花市场
(1, '云表镇茉莉花市场', '元/斤', 23.00, 25.50, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 23.50, 26.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 23.20, 25.80, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 24.00, 26.50, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 24.50, 27.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 24.20, 26.80, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, '云表镇茉莉花市场', '元/斤', 24.80, 27.20, CURDATE()),

-- 南宁花卉交易市场
(1, '南宁花卉交易市场', '元/斤', 25.50, 28.00, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '南宁花卉交易市场', '元/斤', 26.00, 28.50, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '南宁花卉交易市场', '元/斤', 25.80, 28.20, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '南宁花卉交易市场', '元/斤', 26.50, 29.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '南宁花卉交易市场', '元/斤', 27.00, 29.50, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '南宁花卉交易市场', '元/斤', 26.80, 29.20, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, '南宁花卉交易市场', '元/斤', 27.20, 29.80, CURDATE()),

-- 茉莉花茶（分类2）
(2, '横州茉莉花交易市场', '元/斤', 80.00, 95.00, CURDATE()),
(2, '中华茉莉园', '元/斤', 75.00, 90.00, CURDATE()),

-- 茉莉盆栽（分类3）
(3, '横州茉莉花交易市场', '元/盆', 35.00, 55.00, CURDATE()),
(3, '中华茉莉园', '元/盆', 30.00, 50.00, CURDATE()),

-- 茉莉花苗（分类4）
(4, '横州茉莉花交易市场', '元/株', 10.00, 15.00, CURDATE()),
(4, '中华茉莉园', '元/株', 8.00, 12.00, CURDATE());

-- 7. 验证数据
SELECT COUNT(*) AS '总记录数' FROM `price_monitor`;
SELECT `category`, COUNT(*) AS '记录数' FROM `price_monitor` GROUP BY `category`;
SELECT `record_date`, COUNT(*) AS '记录数' FROM `price_monitor` GROUP BY `record_date` ORDER BY `record_date` DESC;
