-- 修复 acquisition_demand 表缺少 total_demand 字段的问题

-- 添加 total_demand 字段，设置默认值为 0
ALTER TABLE acquisition_demand ADD COLUMN total_demand INT DEFAULT 0 COMMENT '需求总申请量';
