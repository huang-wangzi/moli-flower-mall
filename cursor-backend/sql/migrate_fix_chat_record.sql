-- 修复 chat_record 表结构，允许 to_user_id 为 NULL（用于广播回复场景）
ALTER TABLE `chat_record` MODIFY COLUMN `to_user_id` BIGINT DEFAULT NULL COMMENT '接收者用户ID（广播回复时为NULL）';

-- 添加索引以加速广播回复查询
ALTER TABLE `chat_record` ADD INDEX `idx_related_msg` (`related_msg_id`);
