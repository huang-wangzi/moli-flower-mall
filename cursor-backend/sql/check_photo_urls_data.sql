-- 检查数据库中 photo_urls 字段的数据情况
USE moli_mall;

-- 查看 acquisition_application 表结构
DESCRIBE acquisition_application;

-- 查看最近10条申请记录的图片字段
SELECT 
    id,
    demand_id,
    user_id,
    quantity,
    status,
    photo_urls,
    jasmine_photos,
    created_at
FROM acquisition_application 
ORDER BY id DESC 
LIMIT 10;

-- 统计 photo_urls 字段有值和无值的记录数
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN photo_urls IS NOT NULL AND photo_urls != '' THEN 1 ELSE 0 END) as has_photo_urls,
    SUM(CASE WHEN jasmine_photos IS NOT NULL AND jasmine_photos != '' THEN 1 ELSE 0 END) as has_jasmine_photos
FROM acquisition_application;