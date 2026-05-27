-- ============================================
-- 修复 product_review 表缺失列（通用版）
-- ============================================

USE moli_mall;

-- 1. 添加 update_time
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'update_time');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间''', 'SELECT ''update_time 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 添加 create_time
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'create_time');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT ''创建时间''', 'SELECT ''create_time 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 添加 avatar
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'avatar');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT ''用户头像''', 'SELECT ''avatar 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 添加 username
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'username');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN username VARCHAR(50) DEFAULT NULL COMMENT ''用户名''', 'SELECT ''username 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. 添加 order_id
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'order_id');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN order_id BIGINT DEFAULT NULL COMMENT ''订单ID''', 'SELECT ''order_id 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. 添加 deleted
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'deleted');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''', 'SELECT ''deleted 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7. 添加 reply
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'reply');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN reply LONGTEXT DEFAULT NULL COMMENT ''商家回复''', 'SELECT ''reply 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 8. 添加 reply_time
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'reply_time');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN reply_time DATETIME DEFAULT NULL COMMENT ''回复时间''', 'SELECT ''reply_time 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 9. 添加 status
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review' AND COLUMN_NAME = 'status');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE product_review ADD COLUMN status TINYINT DEFAULT 1 COMMENT ''状态: 0-隐藏 1-显示''', 'SELECT ''status 已存在''');
PREPARE stmt FROM @sqlstmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 验证
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'moli_mall' AND TABLE_NAME = 'product_review';
