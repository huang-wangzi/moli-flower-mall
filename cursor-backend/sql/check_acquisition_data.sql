-- 检查并创建收购相关数据库表和字段

-- ============================================
-- 1. 检查 acquisition_demand 表是否存在
-- ============================================
SELECT '检查 acquisition_demand 表结构' AS info;
DESCRIBE acquisition_demand;

-- ============================================
-- 2. 检查 acquisition_application 表
-- ============================================
SELECT '检查 acquisition_application 表结构' AS info;
DESCRIBE acquisition_application;

-- ============================================
-- 3. 检查 chat_record 表
-- ============================================
SELECT '检查 chat_record 表结构' AS info;
DESCRIBE chat_record;

-- ============================================
-- 4. 检查 message 表
-- ============================================
SELECT '检查 message 表结构' AS info;
DESCRIBE message;

-- ============================================
-- 5. 检查是否有测试数据
-- ============================================
SELECT '收购需求数量' AS info, COUNT(*) AS count FROM acquisition_demand;
SELECT '收购申请数量' AS info, COUNT(*) AS count FROM acquisition_application;
SELECT '聊天记录数量' AS info, COUNT(*) AS count FROM chat_record;
SELECT '消息数量' AS info, COUNT(*) AS count FROM message;