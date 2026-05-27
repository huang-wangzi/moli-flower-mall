-- ============================================
-- 横州茉莉花商城 - 完整数据库修复脚本
-- 执行时间：2026-04-04
-- 修复所有表结构与后端实体类不匹配的问题
-- ============================================

USE moli_mall;

-- ============================================
-- 辅助存储过程（兼容MySQL 5.7+）
-- ============================================

DROP PROCEDURE IF EXISTS fix_column;
DELIMITER //
CREATE PROCEDURE fix_column(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_def VARCHAR(1000))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS modify_col;
DELIMITER //
CREATE PROCEDURE modify_col(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_new_def VARCHAR(1000))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table, ' MODIFY COLUMN ', p_column, ' ', p_new_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS create_table_if_not_exists;
DELIMITER //
CREATE PROCEDURE create_table_if_not_exists(
    IN p_table VARCHAR(64), 
    IN p_create_sql VARCHAR(8000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table
    ) THEN
        SET @sql = p_create_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ============================================
-- 【1】sys_user 表 - 用户表
-- ============================================
CALL fix_column('sys_user', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('sys_user', 'member_level', 'VARCHAR(50) DEFAULT NULL COMMENT ''会员等级''');
CALL fix_column('sys_user', 'member_points', 'INT DEFAULT 0 COMMENT ''会员积分''');
CALL fix_column('sys_user', 'balance', 'DECIMAL(10,2) DEFAULT 0.00 COMMENT ''账户余额''');
CALL fix_column('sys_user', 'shop_name', 'VARCHAR(100) DEFAULT NULL COMMENT ''店铺名称''');
CALL fix_column('sys_user', 'shop_category', 'VARCHAR(100) DEFAULT NULL COMMENT ''店铺类型''');
CALL fix_column('sys_user', 'merchant_name', 'VARCHAR(100) DEFAULT NULL COMMENT ''收购商名称''');
CALL fix_column('sys_user', 'shop_qualification_status', 'INT DEFAULT 0 COMMENT ''资质审核状态''');
CALL fix_column('sys_user', 'specialty', 'VARCHAR(100) DEFAULT NULL COMMENT ''专业领域''');
CALL fix_column('sys_user', 'experience', 'INT DEFAULT NULL COMMENT ''从业年限''');
CALL fix_column('sys_user', 'bio', 'TEXT DEFAULT NULL COMMENT ''个人简介''');
CALL modify_col('sys_user', 'avatar', 'LONGTEXT DEFAULT NULL COMMENT ''头像''');

-- ============================================
-- 【2】product 表 - 商品表
-- ============================================
CALL fix_column('product', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL modify_col('product', 'images', 'LONGTEXT DEFAULT NULL COMMENT ''商品图片''');
CALL modify_col('product', 'description', 'LONGTEXT DEFAULT NULL COMMENT ''商品描述''');

-- ============================================
-- 【3】orders 表 - 订单表
-- ============================================
CALL fix_column('orders', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('orders', 'freight', 'DECIMAL(10,2) DEFAULT 0.00 COMMENT ''运费''');
CALL fix_column('orders', 'discount', 'DECIMAL(10,2) DEFAULT 0.00 COMMENT ''优惠金额''');
CALL fix_column('orders', 'actual_amount', 'DECIMAL(10,2) DEFAULT 0.00 COMMENT ''实付金额''');
CALL fix_column('orders', 'pay_time', 'DATETIME DEFAULT NULL COMMENT ''支付时间''');
CALL fix_column('orders', 'ship_time', 'DATETIME DEFAULT NULL COMMENT ''发货时间''');
CALL fix_column('orders', 'receive_time', 'DATETIME DEFAULT NULL COMMENT ''收货时间''');

-- ============================================
-- 【4】order_item 表 - 订单商品表
-- ============================================
CALL fix_column('order_item', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL modify_col('order_item', 'product_image', 'LONGTEXT DEFAULT NULL COMMENT ''商品图片''');

-- ============================================
-- 【5】cart 表 - 购物车表
-- ============================================
CALL fix_column('cart', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');

-- ============================================
-- 【6】product_review 表 - 商品评价表
-- ============================================
CALL fix_column('product_review', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL modify_col('product_review', 'avatar', 'LONGTEXT DEFAULT NULL COMMENT ''用户头像''');
CALL modify_col('product_review', 'images', 'LONGTEXT DEFAULT NULL COMMENT ''评价图片''');
CALL modify_col('product_review', 'content', 'LONGTEXT NOT NULL COMMENT ''评价内容''');
CALL modify_col('product_review', 'reply', 'LONGTEXT DEFAULT NULL COMMENT ''商家回复''');

-- ============================================
-- 【7】message 表 - 消息表
-- ============================================
CALL fix_column('message', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('message', 'is_broadcast', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''广播消息''');
CALL fix_column('message', 'scope', 'VARCHAR(20) DEFAULT NULL COMMENT ''广播范围''');
CALL fix_column('message', 'sender_id', 'BIGINT DEFAULT NULL COMMENT ''发送者ID''');

-- ============================================
-- 【8】chat_record 表 - 聊天记录表
-- ============================================
CALL fix_column('chat_record', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('chat_record', 'type', 'VARCHAR(20) DEFAULT ''chat'' COMMENT ''消息类型''');
CALL fix_column('chat_record', 'related_msg_id', 'BIGINT DEFAULT NULL COMMENT ''关联消息ID''');

-- ============================================
-- 【9】refund 表 - 退款表
-- ============================================
CALL fix_column('refund', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('refund', 'refund_type', 'VARCHAR(20) NOT NULL DEFAULT ''refund'' COMMENT ''类型''');
CALL fix_column('refund', 'admin_remark', 'VARCHAR(500) DEFAULT NULL COMMENT ''管理员备注''');

-- ============================================
-- 【10】complaint 表 - 投诉表
-- ============================================
CALL fix_column('complaint', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('complaint', 'source_type', 'TINYINT DEFAULT 0 COMMENT ''来源类型''');
CALL fix_column('complaint', 'admin_remark', 'VARCHAR(500) DEFAULT NULL COMMENT ''管理员备注''');

-- ============================================
-- 【11】shop_qualification 表 - 商家资质表
-- ============================================
CALL fix_column('shop_qualification', 'contact', 'VARCHAR(50) DEFAULT NULL COMMENT ''联系电话''');
CALL modify_col('shop_qualification', 'business_license', 'LONGTEXT DEFAULT NULL COMMENT ''营业执照''');
CALL modify_col('shop_qualification', 'id_card_front', 'LONGTEXT DEFAULT NULL COMMENT ''身份证正面''');
CALL modify_col('shop_qualification', 'id_card_back', 'LONGTEXT DEFAULT NULL COMMENT ''身份证背面''');
CALL modify_col('shop_qualification', 'quality_cert', 'LONGTEXT DEFAULT NULL COMMENT ''品质认证''');

-- ============================================
-- 【12】acquisition_demand 表 - 收购需求表
-- ============================================
CALL create_table_if_not_exists('acquisition_demand', 
'CREATE TABLE `acquisition_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT ''需求ID'',
  `shop_id` BIGINT NOT NULL COMMENT ''收购商ID'',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT ''收购商名称'',
  `title` VARCHAR(200) NOT NULL COMMENT ''需求标题'',
  `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT ''最低价'',
  `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT ''最高价'',
  `unit` VARCHAR(20) DEFAULT ''斤'' COMMENT ''单位'',
  `quantity_needed` DECIMAL(10,2) DEFAULT 0 COMMENT ''计划收购量'',
  `quantity_remaining` DECIMAL(10,2) DEFAULT 0 COMMENT ''剩余收购量'',
  `market_address` VARCHAR(255) DEFAULT NULL COMMENT ''市场地址'',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT ''联系电话'',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT ''联系人'',
  `description` TEXT DEFAULT NULL COMMENT ''备注说明'',
  `status` TINYINT DEFAULT 1 COMMENT ''状态'',
  `demand_date` DATE DEFAULT NULL COMMENT ''收购日期'',
  `created_at` DATETIME DEFAULT NULL COMMENT ''创建时间'',
  `updated_at` DATETIME DEFAULT NULL COMMENT ''更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''收购需求表''');

-- 添加字段（如果表已存在）
CALL fix_column('acquisition_demand', 'shop_name', 'VARCHAR(100) DEFAULT NULL COMMENT ''收购商名称''');
CALL fix_column('acquisition_demand', 'market_address', 'VARCHAR(255) DEFAULT NULL COMMENT ''市场地址''');
CALL fix_column('acquisition_demand', 'contact_phone', 'VARCHAR(20) DEFAULT NULL COMMENT ''联系电话''');
CALL fix_column('acquisition_demand', 'contact_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''联系人''');
CALL fix_column('acquisition_demand', 'description', 'TEXT DEFAULT NULL COMMENT ''备注说明''');
CALL fix_column('acquisition_demand', 'unit', 'VARCHAR(20) DEFAULT ''斤'' COMMENT ''单位''');
CALL fix_column('acquisition_demand', 'quantity_needed', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''计划收购量''');
CALL fix_column('acquisition_demand', 'quantity_remaining', 'DECIMAL(10,2) DEFAULT 0 COMMENT ''剩余收购量''');
CALL fix_column('acquisition_demand', 'created_at', 'DATETIME DEFAULT NULL COMMENT ''创建时间''');
CALL fix_column('acquisition_demand', 'updated_at', 'DATETIME DEFAULT NULL COMMENT ''更新时间''');

-- ============================================
-- 【13】acquisition_application 表 - 收购申请表
-- ============================================
CALL create_table_if_not_exists('acquisition_application', 
'CREATE TABLE `acquisition_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT ''申请ID'',
  `demand_id` BIGINT NOT NULL COMMENT ''需求ID'',
  `user_id` BIGINT NOT NULL COMMENT ''用户ID'',
  `user_nickname` VARCHAR(50) DEFAULT NULL COMMENT ''用户昵称'',
  `farmer_name` VARCHAR(50) DEFAULT NULL COMMENT ''花农姓名'',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT ''申请斤数'',
  `photo_urls` LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片'',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT ''联系电话'',
  `remark` TEXT DEFAULT NULL COMMENT ''备注'',
  `status` TINYINT DEFAULT 0 COMMENT ''状态'',
  `actual_quantity` DECIMAL(10,2) DEFAULT NULL COMMENT ''实际斤数'',
  `actual_price` DECIMAL(10,2) DEFAULT NULL COMMENT ''成交单价'',
  `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT ''总金额'',
  `agree_time` DATETIME DEFAULT NULL COMMENT ''同意时间'',
  `delivery_time` DATETIME DEFAULT NULL COMMENT ''交付时间'',
  `complete_time` DATETIME DEFAULT NULL COMMENT ''完成时间'',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT ''拒绝原因'',
  `created_at` DATETIME DEFAULT NULL COMMENT ''创建时间'',
  `updated_at` DATETIME DEFAULT NULL COMMENT ''更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''收购申请表''');

-- 添加字段
CALL fix_column('acquisition_application', 'user_nickname', 'VARCHAR(50) DEFAULT NULL COMMENT ''用户昵称''');
CALL fix_column('acquisition_application', 'farmer_name', 'VARCHAR(50) DEFAULT NULL COMMENT ''花农姓名''');
CALL fix_column('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片''');
CALL fix_column('acquisition_application', 'remark', 'TEXT DEFAULT NULL COMMENT ''备注''');
CALL fix_column('acquisition_application', 'actual_quantity', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''实际斤数''');
CALL fix_column('acquisition_application', 'actual_price', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''成交单价''');
CALL fix_column('acquisition_application', 'total_amount', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''总金额''');
CALL fix_column('acquisition_application', 'agree_time', 'DATETIME DEFAULT NULL COMMENT ''同意时间''');
CALL fix_column('acquisition_application', 'delivery_time', 'DATETIME DEFAULT NULL COMMENT ''交付时间''');
CALL fix_column('acquisition_application', 'complete_time', 'DATETIME DEFAULT NULL COMMENT ''完成时间''');
CALL fix_column('acquisition_application', 'reject_reason', 'VARCHAR(500) DEFAULT NULL COMMENT ''拒绝原因''');
CALL fix_column('acquisition_application', 'created_at', 'DATETIME DEFAULT NULL COMMENT ''创建时间''');
CALL fix_column('acquisition_application', 'updated_at', 'DATETIME DEFAULT NULL COMMENT ''更新时间''');
CALL modify_col('acquisition_application', 'photo_urls', 'LONGTEXT DEFAULT NULL COMMENT ''茉莉花照片''');

-- ============================================
-- 【14】receiver_address 表 - 收货地址表
-- ============================================
CALL fix_column('receiver_address', 'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标记''');
CALL fix_column('receiver_address', 'province', 'VARCHAR(50) DEFAULT NULL COMMENT ''省份''');
CALL fix_column('receiver_address', 'city', 'VARCHAR(50) DEFAULT NULL COMMENT ''城市''');
CALL fix_column('receiver_address', 'district', 'VARCHAR(50) DEFAULT NULL COMMENT ''区县''');
CALL fix_column('receiver_address', 'detail', 'VARCHAR(255) DEFAULT NULL COMMENT ''详细地址''');
CALL fix_column('receiver_address', 'tag', 'VARCHAR(50) DEFAULT NULL COMMENT ''地址标签''');
CALL fix_column('receiver_address', 'is_default', 'TINYINT DEFAULT 0 COMMENT ''是否默认''');

-- ============================================
-- 【15】coupon 表 - 优惠券表
-- ============================================
CALL fix_column('coupon', 'valid_from', 'DATETIME DEFAULT NULL COMMENT ''有效期开始''');
CALL fix_column('coupon', 'valid_to', 'DATETIME DEFAULT NULL COMMENT ''有效期截止''');
CALL fix_column('coupon', 'total_count', 'INT DEFAULT 0 COMMENT ''发放总量''');
CALL fix_column('coupon', 'receive_count', 'INT DEFAULT 0 COMMENT ''已领取''');
CALL fix_column('coupon', 'use_count', 'INT DEFAULT 0 COMMENT ''已使用''');

-- ============================================
-- 【16】user_coupon 表 - 用户优惠券表
-- ============================================
CALL fix_column('user_coupon', 'order_id', 'BIGINT DEFAULT NULL COMMENT ''订单ID''');
CALL fix_column('user_coupon', 'use_time', 'DATETIME DEFAULT NULL COMMENT ''使用时间''');

-- ============================================
-- 【17】category 表 - 商品分类表
-- ============================================
CALL modify_col('category', 'icon', 'LONGTEXT DEFAULT NULL COMMENT ''分类图标''');

-- ============================================
-- 清理存储过程
-- ============================================
DROP PROCEDURE IF EXISTS fix_column;
DROP PROCEDURE IF EXISTS modify_col;
DROP PROCEDURE IF EXISTS create_table_if_not_exists;

-- ============================================
-- 完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT '  数据库修复完成！所有表结构已对齐' AS result;
SELECT '========================================' AS '';
