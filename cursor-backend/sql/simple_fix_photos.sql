-- 修复茉莉花照片字段问题
USE moli_mall;

-- 1. 检查表结构
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'moli_mall' 
  AND TABLE_NAME = 'acquisition_application' 
  AND COLUMN_NAME IN ('photo_urls', 'jasmine_photos');

-- 2. 查看当前数据
SELECT id, demand_id, quantity, status, photo_urls, jasmine_photos, created_at 
FROM acquisition_application 
ORDER BY id DESC 
LIMIT 5;

-- 3. 添加 photo_urls 字段（如果不存在）
ALTER TABLE acquisition_application 
ADD COLUMN photo_urls LONGTEXT DEFAULT NULL COMMENT '茉莉花照片URLs(JSON数组)';

-- 4. 同步数据（从 jasmine_photos 到 photo_urls）
UPDATE acquisition_application 
SET photo_urls = jasmine_photos 
WHERE (photo_urls IS NULL OR photo_urls = '') 
  AND jasmine_photos IS NOT NULL;

-- 5. 同步数据（从 photo_urls 到 jasmine_photos）双向同步
UPDATE acquisition_application 
SET jasmine_photos = photo_urls 
WHERE (jasmine_photos IS NULL OR jasmine_photos = '') 
  AND photo_urls IS NOT NULL;

-- 6. 查看修复后的结果
SELECT id, photo_urls, jasmine_photos, quantity, status 
FROM acquisition_application 
ORDER BY id DESC 
LIMIT 5;

-- 7. 统计有图片的记录数
SELECT 
    COUNT(*) AS total,
    SUM(CASE WHEN photo_urls IS NOT NULL AND photo_urls != '' THEN 1 ELSE 0 END) AS has_photo_urls,
    SUM(CASE WHEN jasmine_photos IS NOT NULL AND jasmine_photos != '' THEN 1 ELSE 0 END) AS has_jasmine_photos
FROM acquisition_application;
