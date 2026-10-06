-- 私信图片元数据：可重复执行；旧消息保持 TEXT；保留原有唯一键。
-- 与 init.sql / upgrade-2026-10-05-social-messaging.sql 的 chat_message 最终结构一致。
USE gdeiassistant;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='type');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `type` varchar(16) NOT NULL DEFAULT ''TEXT'' AFTER `client_message_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_key');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_key` varchar(255) DEFAULT NULL AFTER `content`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_content_type');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_content_type` varchar(64) DEFAULT NULL AFTER `image_key`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_width');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_width` int DEFAULT NULL AFTER `image_content_type`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_height');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_height` int DEFAULT NULL AFTER `image_width`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_size');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_size` int DEFAULT NULL AFTER `image_height`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND COLUMN_NAME='image_sha256');
SET @sql := IF(@col=0, 'ALTER TABLE `chat_message` ADD COLUMN `image_sha256` char(64) DEFAULT NULL AFTER `image_size`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 确保旧行 type 不为空（若列已存在且曾被改成可空）
UPDATE `chat_message` SET `type`='TEXT' WHERE `type` IS NULL OR `type`='';
