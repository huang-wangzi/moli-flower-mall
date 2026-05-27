-- ============================================
-- 修复收购申请和聊天功能
-- 请在 MySQL 中执行以下 SQL
-- ============================================

-- 1. 确保 acquisition_application 表有正确字段
-- 执行前请确认表结构，如果没有 apply_weight 字段，执行以下 SQL
ALTER TABLE acquisition_application ADD COLUMN IF NOT EXISTS `apply_weight` DECIMAL(10,2) DEFAULT NULL COMMENT '申请重量(斤)';

-- 2. 确保 acquisition_demand 表有 shop_id 字段
ALTER TABLE acquisition_demand ADD COLUMN IF NOT EXISTS `shop_id` BIGINT DEFAULT NULL COMMENT '收购商ID';

-- 3. 添加索引以优化查询性能
ALTER TABLE acquisition_application ADD INDEX `idx_demand_id` (`demand_id`);
ALTER TABLE acquisition_application ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE acquisition_demand ADD INDEX `idx_shop_id` (`shop_id`);
ALTER TABLE chat_record ADD INDEX `idx_pair_time` (`from_user_id`, `to_user_id`, `create_time`);

-- 4. 查看表结构确认
DESCRIBE acquisition_application;
DESCRIBE acquisition_demand;
DESCRIBE chat_record;

-- 5. 查看当前数据
SELECT '===== 测试数据 =====' AS info;
SELECT '收购需求数量:' AS info, COUNT(*) AS count FROM acquisition_demand;
SELECT '收购申请数量:' AS info, COUNT(*) AS count FROM acquisition_application;
SELECT '聊天记录数量:' AS info, COUNT(*) AS count FROM chat_record;