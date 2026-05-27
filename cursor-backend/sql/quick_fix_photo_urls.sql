-- ============================================
-- 快速修复 photo_urls 字段
-- 修复时间: 2026-04-15
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

-- ============================================
-- 修复 acquisition_application 表
-- ============================================

-- 1. 创建表（如果不存在）
CREATE TABLE IF NOT EXISTS `acquisition_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `demand_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `user_nickname` VARCHAR(50) DEFAULT NULL,
  `farmer_name` VARCHAR(50) DEFAULT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `remark` TEXT DEFAULT NULL,
  `photo_urls` LONGTEXT DEFAULT NULL COMMENT '茉莉花照片URLs(JSON数组)',
  `status` TINYINT DEFAULT 0,
  `actual_quantity` DECIMAL(10,2) DEFAULT NULL,
  `actual_price` DECIMAL(10,2) DEFAULT NULL,
  `total_amount` DECIMAL(10,2) DEFAULT NULL,
  `agree_time` DATETIME DEFAULT NULL,
  `delivery_time` DATETIME DEFAULT NULL,
  `complete_time` DATETIME DEFAULT NULL,
  `reject_reason` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 添加 photo_urls 字段（如果不存在）
CALL add_column_if_not_exists('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片URLs(JSON数组)''');

-- 3. 修改 photo_urls 为 LONGTEXT（如果存在但类型不对）
SET @sql = 'ALTER TABLE `acquisition_application` MODIFY COLUMN `photo_urls` LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片URLs(JSON数组)''';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 清理
-- ============================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ============================================
-- 验证
-- ============================================
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'moli_mall'
  AND TABLE_NAME = 'acquisition_application'
  AND COLUMN_NAME = 'photo_urls';

SELECT 'photo_urls 字段修复完成！' AS result;
