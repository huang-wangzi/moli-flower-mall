-- 为 acquisition_application 表添加 apply_weight 字段
ALTER TABLE acquisition_application ADD COLUMN apply_weight DECIMAL(10,2) DEFAULT NULL COMMENT '实际交付重量';
