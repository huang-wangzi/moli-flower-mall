-- 诊断和修复茉莉花收购申请图片字段
-- 执行时间: 2026-04-15

USE moli_mall;

-- ============================================
-- 1. 检查表结构
-- ============================================
SELECT '检查 acquisition_application 表结构:' AS info;
DESCRIBE acquisition_application;

-- ============================================
-- 2. 检查图片字段是否存在和数据
-- ============================================
SELECT '检查图片字段数据:' AS info;
SELECT 
    id,
    demand_id,
    quantity,
    status,
    photo_urls,
    jasmine_photos,
    LEFT(created_at, 19) as created_time
FROM acquisition_application 
ORDER BY id DESC 
LIMIT 10;

-- ============================================
-- 3. 统计有图片和无图片的记录数
-- ============================================
SELECT '统计图片字段:' AS info;
SELECT 
    COUNT(*) AS total_records,
    SUM(CASE WHEN photo_urls IS NOT NULL AND photo_urls != '' THEN 1 ELSE 0 END) AS has_photo_urls,
    SUM(CASE WHEN jasmine_photos IS NOT NULL AND jasmine_photos != '' THEN 1 ELSE 0 END) AS has_jasmine_photos,
    SUM(CASE WHEN (photo_urls IS NULL OR photo_urls = '') AND (jasmine_photos IS NULL OR jasmine_photos = '') THEN 1 ELSE 0 END) AS no_photos
FROM acquisition_application;

-- ============================================
-- 4. 如果 jasmine_photos 有数据但 photo_urls 没有，同步数据
-- ============================================
SELECT '同步 jasmine_photos 到 photo_urls:' AS info;

UPDATE acquisition_application 
SET photo_urls = jasmine_photos 
WHERE (photo_urls IS NULL OR photo_urls = '') 
  AND jasmine_photos IS NOT NULL 
  AND jasmine_photos != '';

SELECT CONCAT('已同步 ', ROW_COUNT(), ' 条记录') AS sync_result;

-- ============================================
-- 5. 确保 photo_urls 字段存在且类型正确
-- ============================================
SELECT '检查并修复 photo_urls 字段类型:' AS info;

-- 检查字段类型
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'moli_mall'
  AND TABLE_NAME = 'acquisition_application'
  AND COLUMN_NAME IN ('photo_urls', 'jasmine_photos');

-- ============================================
-- 6. 查看最近提交的申请的图片数据（如果有的话）
-- ============================================
SELECT '最近5条申请的图片URL预览:' AS info;
SELECT 
    id,
    status,
    CASE 
        WHEN photo_urls IS NOT NULL AND photo_urls != '' THEN LEFT(photo_urls, 100)
        WHEN jasmine_photos IS NOT NULL AND jasmine_photos != '' THEN LEFT(jasmine_photos, 100)
        ELSE '无图片'
    END AS photo_preview
FROM acquisition_application 
ORDER BY id DESC 
LIMIT 5;

-- ============================================
-- 7. 完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT '  图片字段诊断完成！' AS result;
SELECT '========================================' AS '';
