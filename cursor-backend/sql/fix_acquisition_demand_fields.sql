-- 修复 acquisition_demand 表字段问题

-- 检查并添加 total_demand 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acquisition_demand' AND COLUMN_NAME = 'total_demand');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE acquisition_demand ADD COLUMN total_demand INT DEFAULT 0 COMMENT ''需求总申请量''', 'SELECT ''total_demand already exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 remaining_demand 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acquisition_demand' AND COLUMN_NAME = 'remaining_demand');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE acquisition_demand ADD COLUMN remaining_demand INT DEFAULT 0 COMMENT ''剩余需求数''', 'SELECT ''remaining_demand already exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
