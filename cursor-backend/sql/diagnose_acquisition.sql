-- 诊断 SQL：检查收购申请数据流

-- 1. 查看所有用户信息
SELECT id, username, role, shop_type, status FROM sys_user WHERE deleted = 0;

-- 2. 查看所有收购需求
SELECT id, shop_id, title, status, remaining_demand FROM acquisition_demand WHERE deleted = 0;

-- 3. 查看所有收购申请
SELECT 
  a.id,
  a.demand_id,
  a.user_id,
  a.status,
  a.contact_phone,
  a.apply_weight,
  a.created_at,
  d.shop_id as demand_shop_id
FROM acquisition_application a
LEFT JOIN acquisition_demand d ON a.demand_id = d.id
WHERE a.deleted = 0;

-- 4. 测试特定查询：按 demand_id 查询申请
SELECT * FROM acquisition_application WHERE demand_id = 1 AND deleted = 0;

-- 5. 测试按 shop_id 查询需求
SELECT * FROM acquisition_demand WHERE shop_id = 10 AND deleted = 0;