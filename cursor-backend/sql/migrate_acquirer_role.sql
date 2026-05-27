-- 茉莉花收购商功能迁移
-- 添加商家类型字段，支持区分普通商家和收购商
-- 创建时间: 2026-04-02

-- 添加商家类型字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS shop_type TINYINT DEFAULT 1 COMMENT '商家类型: 1-普通商家(商品销售) 2-收购商(茉莉花收购)' AFTER role;

-- 为现有商家设置默认值（普通商家）
UPDATE sys_user SET shop_type = 1 WHERE role = 2 AND shop_type IS NULL;

-- 为收购商设置shop_type = 2（如果已有收购商数据）
-- UPDATE sys_user SET shop_type = 2 WHERE role = 2 AND [收购商条件];
