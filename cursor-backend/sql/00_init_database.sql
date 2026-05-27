-- ============================================
-- 横州茉莉花商城 - 完整数据库初始化脚本
-- 版本: v2.0（基于实体类精确对齐）
-- 更新时间: 2026-03-29
-- 说明: 以 Entity 实体类为唯一数据源，与 Java 代码严格一一对应
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS moli_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE moli_mall;

-- ============================================
-- 1. 用户表 sys_user
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` LONGTEXT DEFAULT NULL COMMENT '头像(URL或Base64)',
  `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-保密 1-男 2-女',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色: 1-普通用户 2-商家 3-园艺师 4-管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `member_level` VARCHAR(20) DEFAULT NULL COMMENT '会员等级',
  `member_points` INT DEFAULT 0 COMMENT '会员积分',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称(商家)',
  `shop_category` VARCHAR(50) DEFAULT NULL COMMENT '店铺类型(商家)',
  `shop_qualification_status` INT DEFAULT 0 COMMENT '商家资质审核状态: 0-未提交 1-待审核 2-通过 3-拒绝',
  `specialty` VARCHAR(100) DEFAULT NULL COMMENT '专业领域(园艺师)',
  `experience` INT DEFAULT 0 COMMENT '从业年限(园艺师)',
  `bio` TEXT DEFAULT NULL COMMENT '个人简介(园艺师)',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ============================================
-- 2. 商品分类表 category
-- ============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `icon` LONGTEXT DEFAULT NULL COMMENT '分类图标',
  `sort` INT DEFAULT 0 COMMENT '排序权重',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- ============================================
-- 3. 商品表 product
-- ============================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `images` LONGTEXT DEFAULT NULL COMMENT '商品图片(JSON：URL或Base64)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `specs` TEXT DEFAULT NULL COMMENT '商品规格(JSON)',
  `description` LONGTEXT DEFAULT NULL COMMENT '商品描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核 1-已审核/上架 2-审核拒绝',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- ============================================
-- 4. 购物车表 cart
-- ============================================
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `specs` VARCHAR(255) DEFAULT NULL COMMENT '规格',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- ============================================
-- 5. 订单表 orders
-- ============================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额',
  `freight` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
  `discount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款',
  `pay_type` TINYINT DEFAULT NULL COMMENT '支付方式: 1-微信 2-支付宝',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收货人地址',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

-- ============================================
-- 5b. 收货地址表 receiver_address
-- ============================================
DROP TABLE IF EXISTS `receiver_address`;
CREATE TABLE `receiver_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `province` VARCHAR(30) NOT NULL COMMENT '省份',
  `city` VARCHAR(30) NOT NULL COMMENT '城市',
  `district` VARCHAR(30) NOT NULL COMMENT '区县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `tag` VARCHAR(30) DEFAULT NULL COMMENT '标签：家/公司/学校',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0否 1是',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_addr_user` (`user_id`),
  KEY `idx_addr_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- ============================================
-- 6. 订单商品表 order_item
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单商品ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `product_image` LONGTEXT DEFAULT NULL COMMENT '商品图片(JSON：URL或Base64)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `specs` VARCHAR(255) DEFAULT NULL COMMENT '规格',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品表';

-- ============================================
-- 6b. 聊天记录表 chat_record
-- ============================================
DROP TABLE IF EXISTS `chat_record`;
CREATE TABLE `chat_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
  `to_user_id` BIGINT DEFAULT NULL COMMENT '接收者用户ID（广播回复时为NULL）',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
  `type` VARCHAR(20) DEFAULT 'chat' COMMENT '消息类型: chat-普通聊天 broadcast_reply-广播回复',
  `related_msg_id` BIGINT DEFAULT NULL COMMENT '关联的广播消息ID（用于广播回复场景）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pair_time` (`from_user_id`, `to_user_id`, `create_time`),
  KEY `idx_related_msg` (`related_msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-商家聊天记录';

-- ============================================
-- 6c. 售后申请表 refund
-- ============================================
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家用户ID',
  `refund_type` VARCHAR(20) NOT NULL COMMENT '类型: refund-仅退款 return-退货退款',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '退款原因摘要',
  `description` TEXT COMMENT '详细说明',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已同意 3-已拒绝 4-已完成',
  `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_refund_order` (`order_id`),
  KEY `idx_refund_shop` (`shop_id`),
  KEY `idx_refund_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='售后/退款申请';

-- ============================================
-- 6d. 客诉表 complaint
-- ============================================
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客诉ID',
  `user_id` BIGINT NOT NULL COMMENT '投诉用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '被投诉商家用户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
  `refund_id` BIGINT DEFAULT NULL COMMENT '关联售后ID',
  `source_type` TINYINT NOT NULL DEFAULT 0 COMMENT '来源: 0-普通投诉 1-售后客服介入',
  `reason` VARCHAR(200) NOT NULL COMMENT '投诉事由',
  `description` TEXT COMMENT '详细描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理 2-已驳回',
  `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_complaint_user` (`user_id`),
  KEY `idx_complaint_shop` (`shop_id`),
  KEY `idx_complaint_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易客诉表';

-- ============================================
-- 7. 消息表 message
-- ============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID(系统消息为null)',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-系统通知 2-订单通知 3-评价提醒 4-审核通知 5-聊天消息',
  `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `is_broadcast` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为广播消息: 0-普通消息 1-管理员广播',
  `scope` VARCHAR(20) DEFAULT NULL COMMENT '广播范围: all-全部 users-普通用户 shops-商家',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息表';

-- ============================================
-- 8. 商品评价表 product_review
-- ============================================
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `avatar` LONGTEXT DEFAULT NULL COMMENT '用户头像',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
  `content` LONGTEXT NOT NULL COMMENT '评价内容',
  `images` LONGTEXT DEFAULT NULL COMMENT '评价图片(JSON数组)',
  `reply` LONGTEXT DEFAULT NULL COMMENT '商家回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏 1-显示',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';

-- ============================================
-- 9. 商家资质表 shop_qualification
-- ============================================
DROP TABLE IF EXISTS `shop_qualification`;
CREATE TABLE `shop_qualification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资质ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `business_license` LONGTEXT DEFAULT NULL COMMENT '营业执照(URL或Base64)',
  `id_card_front` LONGTEXT DEFAULT NULL COMMENT '身份证正面(URL或Base64)',
  `id_card_back` LONGTEXT DEFAULT NULL COMMENT '身份证背面(URL或Base64)',
  `quality_cert` LONGTEXT DEFAULT NULL COMMENT '品质认证(URL或Base64)',
  `contact` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核 1-通过 2-拒绝',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家资质表';

-- ============================================
-- 10. 优惠券模板表 coupon
-- ============================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` VARCHAR(50) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型: 1-满减 2-立减',
  `value` DECIMAL(10,2) NOT NULL COMMENT '优惠值',
  `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费门槛',
  `valid_from` DATETIME NOT NULL COMMENT '有效期开始',
  `valid_to` DATETIME NOT NULL COMMENT '有效期截止',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '发放总量',
  `receive_count` INT NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `use_count` INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券模板表';

-- ============================================
-- 10b. 用户优惠券表 user_coupon
-- ============================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券模板ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已过期',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间(领取时间)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';

-- ============================================
-- 11. 市场信息表 price_market
-- ============================================
DROP TABLE IF EXISTS `price_market`;
CREATE TABLE `price_market` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '市场ID',
  `name` VARCHAR(100) NOT NULL COMMENT '市场名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '市场地址',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `description` TEXT DEFAULT NULL COMMENT '市场描述',
  `contact` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `sort` INT DEFAULT 0 COMMENT '排序权重',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_province` (`province`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场信息表';

-- ============================================
-- 12. 价格监测表 price_monitor
-- ============================================
DROP TABLE IF EXISTS `price_monitor`;
CREATE TABLE `price_monitor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `category` TINYINT NOT NULL COMMENT '分类: 1-茉莉鲜花 2-茉莉花茶 3-茉莉盆栽 4-茉莉花苗',
  `market` VARCHAR(100) NOT NULL COMMENT '市场名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位: 元/斤 元/盆 元/株',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `create_by` BIGINT DEFAULT NULL COMMENT '录入人ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_market` (`market`),
  KEY `idx_category` (`category`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='价格监测表';

-- ============================================
-- 初始数据
-- ============================================

-- 测试账号（BCrypt 哈希，对应明文: admin123 / 123456）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `role`, `status`) VALUES
('admin',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@moli.com', 4, 1);

INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `role`, `status`, `member_points`) VALUES
('testuser', '$2a$10$8K1p/a0dL1LXMIoMQgKK4O3B0F0.7qVc0h3h8aNdO3K0xqK5B3xGe', '测试用户', '13900139000', 'test@moli.com', 1, 1, 100);

INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `role`, `status`, `shop_name`, `shop_category`) VALUES
('testshop', '$2a$10$8K1p/a0dL1LXMIoMQgKK4O3B0F0.7qVc0h3h8aNdO3K0xqK5B3xGe', '测试商家', '13700137000', 'shop@moli.com', 2, 1, '茉莉花旗舰店', 'fresh');

-- 商品分类
INSERT INTO `category` (`name`, `description`, `icon`, `sort`) VALUES
('茉莉鲜花', '新鲜茉莉花束、花篮等', 'flower', 1),
('茉莉花茶', '茉莉花茶叶、茉莉茶饮等', 'tea', 2),
('茉莉盆栽', '茉莉花盆栽、盆景等', 'plant', 3),
('茉莉花苗', '茉莉花种苗、种子等', 'seedling', 4);

-- 市场数据
INSERT INTO `price_market` (`name`, `address`, `province`, `city`, `description`, `contact`, `phone`, `status`, `sort`) VALUES
('横州茉莉花交易市场', '广西横州市横州镇茉莉花大道1号', '广西', '横州市', '全国最大的茉莉花交易市场', '市场管理处', '0771-7218888', 1, 100),
('南宁花卉交易市场', '广西南宁市江南区五一西路28号', '广西', '南宁市', '广西最大的综合性花卉批发交易市场', '招商部', '0771-4815666', 1, 90),
('柳州花木大世界', '广西柳州市柳南区瑞龙路98号', '广西', '柳州市', '柳州地区最大的花卉苗木批发市场', '管理办公室', '0772-3699888', 1, 80),
('桂林花卉批发市场', '广西桂林市象山区环城南二路165号', '广西', '桂林市', '桂林地区重要的花卉交易集散地', '市场部', '0773-3861888', 1, 70),
('玉林花卉交易中心', '广西玉林市玉州区二环北路568号', '广西', '玉林市', '桂东南地区最大的花卉绿植批发中心', '客服中心', '0775-2339888', 1, 60),
('广州岭南花卉市场', '广东省广州市荔湾区芳村大道28号', '广东', '广州市', '华南地区最大的花卉批发市场', '招商部', '020-81416888', 1, 50),
('昆明斗南花卉市场', '云南省昆明市呈贡区斗南镇', '云南', '昆明市', '亚洲最大的鲜切花交易市场', '信息中心', '0871-67494888', 1, 40);

-- 测试商品 (商家ID=3)
INSERT INTO `product` (`shop_id`, `category_id`, `name`, `images`, `price`, `original_price`, `stock`, `sales`, `description`, `status`) VALUES
(3, 1, '新鲜茉莉花束',   '[]', 29.90, 39.90, 100,  50, '精选横县茉莉鲜花束，香味浓郁，适合送礼或家居装饰', 1),
(3, 1, '茉莉花篮',       '[]', 68.00, 88.00,  50,  30, '精美茉莉花篮，手工编织，节日送礼首选', 1),
(3, 2, '茉莉花茶礼盒',   '[]', 168.00, 198.00, 80,  25, '正宗横县茉莉花茶，精美礼盒装，送礼佳品', 1),
(3, 2, '散装茉莉花茶',   '[]', 68.00, 78.00, 200, 100, '优质散装茉莉花茶，经济实惠', 1),
(3, 3, '茉莉花盆栽',     '[]', 45.00, 58.00,  80, 120, '四季开花茉莉花盆栽，易养护，适合家居种植', 1),
(3, 3, '双色茉莉盆栽',   '[]', 88.00, 108.00, 30,  45, '进口双色茉莉，观赏价值高', 1),
(3, 4, '茉莉花苗',       '[]', 15.00, 18.00, 500, 300, '优质茉莉花种苗，当年开花，成活率高', 1),
(3, 4, '茉莉花种子套餐', '[]', 25.00, 32.00, 200, 150, '茉莉花种子+营养土+花肥套餐', 1);

-- 价格监测数据（最近7天）
INSERT INTO `price_monitor` (`category`, `market`, `price`, `unit`, `record_date`) VALUES
(1, '横州茉莉花交易市场', 25.50, '元/斤', DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
(1, '横州茉莉花交易市场', 26.00, '元/斤', DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, '横州茉莉花交易市场', 25.80, '元/斤', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, '横州茉莉花交易市场', 26.50, '元/斤', DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, '横州茉莉花交易市场', 26.20, '元/斤', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, '横州茉莉花交易市场', 27.00, '元/斤', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, '横州茉莉花交易市场', 26.80, '元/斤', CURDATE()),
(1, '南宁花卉交易市场',   24.80, '元/斤', CURDATE()),
(1, '昆明斗南花卉市场',   23.50, '元/斤', CURDATE()),
(2, '横州茉莉花交易市场', 85.00, '元/斤', CURDATE()),
(3, '横州茉莉花交易市场', 35.00, '元/盆', CURDATE()),
(4, '横州茉莉花交易市场', 12.00, '元/株', CURDATE());

-- ============================================
-- 完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT '  横州茉莉花商城数据库初始化完成！' AS '恭喜';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '测试账号（v2.0）:' AS '账号信息';
SELECT '  管理员: admin / admin123' AS '';
SELECT '  用户:   testuser / 123456' AS '';
SELECT '  商家:   testshop / 123456' AS '';
SELECT '' AS '';
