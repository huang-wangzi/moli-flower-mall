-- ========================================
-- 横州茉莉花商城 - 新功能数据库表
-- 创建时间: 2026-03-30
-- 功能: 天气监测、价格预警、市场简报
-- ========================================

-- 1. 天气数据表
CREATE TABLE IF NOT EXISTS `weather_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `city_name` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `city_code` VARCHAR(20) DEFAULT NULL COMMENT '城市代码',
  `weather` VARCHAR(50) DEFAULT NULL COMMENT '天气现象',
  `temp` DOUBLE DEFAULT NULL COMMENT '气温（摄氏度）',
  `feels_like` DOUBLE DEFAULT NULL COMMENT '体感温度',
  `temp_max` DOUBLE DEFAULT NULL COMMENT '最高气温',
  `temp_min` DOUBLE DEFAULT NULL COMMENT '最低气温',
  `humidity` INT DEFAULT NULL COMMENT '湿度（百分比）',
  `wind_speed` VARCHAR(20) DEFAULT NULL COMMENT '风力等级',
  `wind_dir` VARCHAR(20) DEFAULT NULL COMMENT '风向',
  `precipitation` DOUBLE DEFAULT 0 COMMENT '降雨量（mm）',
  `pressure` INT DEFAULT NULL COMMENT '气压（hPa）',
  `visibility` DOUBLE DEFAULT NULL COMMENT '能见度（km）',
  `uv_index` INT DEFAULT NULL COMMENT '紫外线指数',
  `aqi` INT DEFAULT NULL COMMENT '空气质量指数',
  `air_level` VARCHAR(20) DEFAULT NULL COMMENT '空气质量等级',
  `sunrise` VARCHAR(20) DEFAULT NULL COMMENT '日出时间',
  `sunset` VARCHAR(20) DEFAULT NULL COMMENT '日落时间',
  `report_time` VARCHAR(50) DEFAULT NULL COMMENT '预报时间',
  `source` VARCHAR(50) DEFAULT '和风天气' COMMENT '数据来源',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_city_name` (`city_name`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='天气数据表';

-- 2. 价格预警表
CREATE TABLE IF NOT EXISTS `price_alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_type` INT NOT NULL COMMENT '预警类型：1-价格上涨预警 2-价格下跌预警 3-天气影响预警 4-市场异常预警',
  `alert_level` INT NOT NULL DEFAULT 1 COMMENT '预警级别：1-提示 2-警告 3-紧急',
  `title` VARCHAR(200) NOT NULL COMMENT '预警标题',
  `content` TEXT COMMENT '预警内容',
  `category` INT DEFAULT NULL COMMENT '关联分类：1-茉莉鲜花 2-茉莉花茶 3-茉莉盆栽 4-茉莉花苗',
  `market` VARCHAR(100) DEFAULT NULL COMMENT '关联市场',
  `current_price` DECIMAL(10,2) DEFAULT NULL COMMENT '当前价格',
  `trigger_price` DECIMAL(10,2) DEFAULT NULL COMMENT '预警触发价格',
  `change_rate` DECIMAL(10,2) DEFAULT NULL COMMENT '涨跌幅（百分比）',
  `weather_id` BIGINT DEFAULT NULL COMMENT '关联天气数据ID',
  `weather_impact` TEXT COMMENT '天气影响描述',
  `analysis` TEXT COMMENT '影响分析',
  `suggestion` TEXT COMMENT '建议措施',
  `status` INT NOT NULL DEFAULT 0 COMMENT '预警状态：0-未读 1-已读 2-已处理',
  `alert_time` DATETIME DEFAULT NULL COMMENT '预警时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_by` BIGINT DEFAULT NULL COMMENT '处理人',
  `handle_remark` TEXT COMMENT '处理备注',
  `notified` INT DEFAULT 0 COMMENT '是否已发送通知：0-否 1-是',
  `notify_way` VARCHAR(50) DEFAULT NULL COMMENT '通知方式',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_level` (`alert_level`),
  KEY `idx_status` (`status`),
  KEY `idx_alert_time` (`alert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格预警表';

-- 3. 市场简报表
CREATE TABLE IF NOT EXISTS `market_briefing` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '简报标题',
  `briefing_type` INT NOT NULL DEFAULT 1 COMMENT '简报类型：1-日报 2-周报 3-月报 4-专题报告',
  `report_date` VARCHAR(20) NOT NULL COMMENT '简报日期',
  `summary` TEXT COMMENT '简报摘要',
  `content` TEXT COMMENT '简报内容（JSON格式）',
  `price_overview` TEXT COMMENT '价格概览JSON',
  `weather_info` TEXT COMMENT '天气情况JSON',
  `analysis` TEXT COMMENT '市场分析',
  `forecast` TEXT COMMENT '趋势预测',
  `suggestions` TEXT COMMENT '建议措施',
  `markets` VARCHAR(500) DEFAULT NULL COMMENT '包含的市场',
  `categories` VARCHAR(200) DEFAULT NULL COMMENT '包含的分类',
  `source` VARCHAR(100) DEFAULT NULL COMMENT '简报来源',
  `related_alerts` VARCHAR(500) DEFAULT NULL COMMENT '关联的价格预警IDs',
  `author` VARCHAR(100) DEFAULT NULL COMMENT '作者/生成人',
  `ai_generated` INT DEFAULT 0 COMMENT 'AI生成标记：0-手动生成 1-AI生成',
  `attachment` VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已归档',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_briefing_type` (`briefing_type`),
  KEY `idx_report_date` (`report_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='市场简报表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入模拟天气数据
INSERT INTO `weather_data` (`city_name`, `city_code`, `weather`, `temp`, `feels_like`, `humidity`, `wind_speed`, `wind_dir`, `precipitation`, `pressure`, `visibility`, `source`) VALUES
('横州', '101300401', '多云', 26.0, 28.0, 72, '2级', '东南风', 0.0, 1008, 15.0, '和风天气');

-- 插入示例价格预警
INSERT INTO `price_alert` (`alert_type`, `alert_level`, `title`, `content`, `category`, `market`, `current_price`, `trigger_price`, `change_rate`, `weather_impact`, `analysis`, `suggestion`, `status`, `alert_time`) VALUES
(1, 2, '价格上涨预警：茉莉鲜花', '茉莉鲜花在横州茉莉花交易市场的价格为28.5元/斤，较昨日上涨5.25%。当前天气多云，气温26℃，湿度72%，对采摘有一定影响。', 1, '横州茉莉花交易市场', 28.50, 27.08, 5.25, '天气条件适宜采摘，但需关注后续天气变化', '价格波动可能受市场供需和天气因素影响。建议密切关注天气预报，合理安排采摘和交易时间。', '1. 价格上涨，建议适时出售手中的存货；2. 可适当增加采购，但需关注价格回调风险；3. 天气条件良好，建议加快采摘进度；4. 请持续关注天气预报和市场动态。', 0, NOW()),
(3, 2, '天气影响预警：茉莉花种植条件一般', '当前天气条件对茉莉花生长有一定影响，需要加强管理措施。气温26℃，湿度72%，天气多云。', NULL, NULL, NULL, NULL, NULL, '气温26℃，湿度72%，天气多云。气温适宜，但需注意湿度变化对病虫害的影响。', '当前天气条件适合茉莉花生长，但高湿度环境易引发病虫害，需加强通风管理。', '1. 加强田间通风，降低湿度；2. 注意病虫害防治；3. 适时喷洒保护性杀菌剂；4. 关注后续天气预报。', 0, NOW());

-- 插入示例市场简报
INSERT INTO `market_briefing` (`title`, `briefing_type`, `report_date`, `summary`, `analysis`, `forecast`, `suggestions`, `ai_generated`, `status`, `publish_time`) VALUES
('横州茉莉花市场日报 2026-03-30', 1, '2026-03-30', '今日横州茉莉花市场整体平稳，主要交易品种包括茉莉鲜花、茉莉花茶、茉莉盆栽等。当前天气多云，对交易影响较小。建议各位商户根据实际情况合理安排交易。', '【今日市场概况】\n今日各品类价格稳定，茉莉鲜花均价28.5元/斤，较昨日上涨约5%；茉莉花茶价格平稳，均价约85元/斤。\n\n【天气影响】\n今日天气多云，气温26℃，湿度72%，对茉莉花采摘有一定影响，但整体交易正常进行。', '【短期趋势预测】\n根据近期价格走势和天气变化分析，茉莉鲜花价格短期内仍有小幅上涨空间。建议关注天气预报，合理安排采摘和交易。\n\n【天气预报】\n未来几天天气情况可能影响采摘进度，建议关注天气预报合理安排交易。', '【交易建议】\n1. 密切关注天气变化，合理安排采摘和交易时间；\n2. 价格波动期间，建议分批交易，降低风险；\n3. 注意储存条件，特别是高温天气下的保鲜措施；\n4. 建议与长期合作商户保持沟通，获取更稳定的销售渠道。', 1, 1, NOW());

-- ========================================
-- 查看创建的表
-- ========================================
-- SHOW TABLES;
-- SELECT COUNT(*) FROM weather_data;
-- SELECT COUNT(*) FROM price_alert;
-- SELECT COUNT(*) FROM market_briefing;
