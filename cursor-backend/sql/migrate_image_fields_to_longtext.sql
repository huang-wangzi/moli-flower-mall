-- ============================================
-- 横州茉莉花商城 - 数据库迁移脚本（v2.0）
-- 选中 moli_mall 数据库后执行
-- 每次启动后端前执行一次（重复执行安全）
-- ============================================

USE moli_mall;

-- 【1】用户表 sys_user - shop_qualification_status
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS `shop_qualification_status` INT DEFAULT 0
  COMMENT '商家资质审核状态: 0-未提交 1-待审核 2-通过 3-拒绝'
  AFTER `shop_category`;

-- 【2】消息表 message - 广播消息支持
ALTER TABLE message ADD COLUMN IF NOT EXISTS `is_broadcast` TINYINT NOT NULL DEFAULT 0
  COMMENT '是否为广播消息: 0-普通消息 1-管理员广播'
  AFTER `is_read`;
ALTER TABLE message ADD COLUMN IF NOT EXISTS `scope` VARCHAR(20) DEFAULT NULL
  COMMENT '广播范围: all-全部 users-普通用户 shops-商家'
  AFTER `is_broadcast`;

-- 【3】聊天记录表 chat_record - 清理无用列
ALTER TABLE chat_record DROP COLUMN IF EXISTS `is_read`;
ALTER TABLE chat_record ADD COLUMN IF NOT EXISTS `type` VARCHAR(20) DEFAULT 'chat'
  COMMENT '消息类型: chat-普通聊天 broadcast_reply-广播回复'
  AFTER `product_id`;
ALTER TABLE chat_record ADD COLUMN IF NOT EXISTS `related_msg_id` BIGINT DEFAULT NULL
  COMMENT '关联的广播消息ID（用于广播回复场景）'
  AFTER `type`;

-- 【4】售后申请表 refund - 列重命名
ALTER TABLE refund ADD COLUMN IF NOT EXISTS `refund_type` VARCHAR(20) NOT NULL DEFAULT 'refund'
  COMMENT '类型: refund-仅退款 return-退货退款'
  AFTER `shop_id`;
UPDATE refund SET refund_type = type WHERE type IS NOT NULL AND type != '';
ALTER TABLE refund DROP COLUMN IF EXISTS `type`;
ALTER TABLE refund ADD COLUMN IF NOT EXISTS `admin_remark` VARCHAR(500) DEFAULT NULL
  COMMENT '管理员处理备注'
  AFTER `status`;
ALTER TABLE refund DROP COLUMN IF EXISTS `remark`;

-- 【5】商品表 product - 图片字段改为 LONGTEXT
ALTER TABLE product MODIFY COLUMN IF EXISTS `images` LONGTEXT DEFAULT NULL
  COMMENT '商品图片(JSON：URL或Base64)';
ALTER TABLE product MODIFY COLUMN IF EXISTS `description` LONGTEXT DEFAULT NULL
  COMMENT '商品描述';

-- 【6】订单商品表 order_item - 图片字段改为 LONGTEXT
ALTER TABLE order_item MODIFY COLUMN IF EXISTS `product_image` LONGTEXT DEFAULT NULL
  COMMENT '商品图片(JSON：URL或Base64)';

-- 【7】用户表 sys_user - 头像改为 LONGTEXT
ALTER TABLE sys_user MODIFY COLUMN IF EXISTS `avatar` LONGTEXT DEFAULT NULL
  COMMENT '头像(URL或Base64)';

-- 【8】商品评价表 product_review - 头像/图片改为 LONGTEXT
ALTER TABLE product_review MODIFY COLUMN IF EXISTS `avatar` LONGTEXT DEFAULT NULL
  COMMENT '用户头像';
ALTER TABLE product_review MODIFY COLUMN IF EXISTS `images` LONGTEXT DEFAULT NULL
  COMMENT '评价图片JSON';
ALTER TABLE product_review MODIFY COLUMN IF EXISTS `content` LONGTEXT NOT NULL
  COMMENT '评价内容';
ALTER TABLE product_review MODIFY COLUMN IF EXISTS `reply` LONGTEXT DEFAULT NULL
  COMMENT '商家回复';

-- 【9】分类表 category - 图标改为 LONGTEXT
ALTER TABLE category MODIFY COLUMN IF EXISTS `icon` LONGTEXT DEFAULT NULL
  COMMENT '分类图标';

-- 【10】商家资质表 shop_qualification
ALTER TABLE shop_qualification ADD COLUMN IF NOT EXISTS `contact` VARCHAR(50) DEFAULT NULL
  COMMENT '联系电话'
  AFTER `quality_cert`;
ALTER TABLE shop_qualification MODIFY COLUMN IF EXISTS `business_license` LONGTEXT DEFAULT NULL;
ALTER TABLE shop_qualification MODIFY COLUMN IF EXISTS `id_card_front` LONGTEXT DEFAULT NULL;
ALTER TABLE shop_qualification MODIFY COLUMN IF EXISTS `id_card_back` LONGTEXT DEFAULT NULL;
ALTER TABLE shop_qualification MODIFY COLUMN IF EXISTS `quality_cert` LONGTEXT DEFAULT NULL;

-- 完成
SELECT 'migrate_v2.0 执行完成，所有表结构已对齐实体类' AS result;
