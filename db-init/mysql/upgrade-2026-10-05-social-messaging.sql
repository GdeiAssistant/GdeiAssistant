-- 社交与身份升级：与 init.sql 最终结构对齐（演示库升级路径，不执行破坏性数据删除）
-- 三种路径：新建后跑整目录脚本、旧演示库升级、重复升级 —— 均应得到同一最终结构。
-- 本文件不 DROP 业务数据；重复升级通过 IF NOT EXISTS / information_schema 跳过。
USE gdeiassistant;

-- ---------------------------------------------------------------------------
-- privacy.dm_policy
-- ---------------------------------------------------------------------------
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'privacy' AND COLUMN_NAME = 'dm_policy'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `privacy` ADD COLUMN `dm_policy` varchar(16) NOT NULL DEFAULT ''MUTUAL'' COMMENT ''私信接收策略 ALL/FOLLOWING/MUTUAL/NONE'' AFTER `is_robots_index_allow`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- like 表唯一约束：先报告重复（不删除），无重复才加索引
-- 审查重复：SELECT topic_id,username,COUNT(*) c FROM topic_like GROUP BY topic_id,username HAVING c>1;
-- ---------------------------------------------------------------------------
SET @dup := (
  SELECT COUNT(*) FROM (
    SELECT 1 FROM topic_like GROUP BY topic_id, username HAVING COUNT(*) > 1
  ) d
);
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'topic_like' AND INDEX_NAME = 'uk_topic_like_topic_user'
);
SET @sql := IF(@dup = 0 AND @idx_exists = 0,
  'ALTER TABLE `topic_like` ADD UNIQUE KEY `uk_topic_like_topic_user` (`topic_id`,`username`)',
  IF(@dup > 0, 'SELECT ''REVIEW_REQUIRED: topic_like has duplicate (topic_id,username) rows; unique key not added'' AS warning', 'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @dup := (
  SELECT COUNT(*) FROM (
    SELECT 1 FROM express_like GROUP BY express_id, username HAVING COUNT(*) > 1
  ) d
);
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'express_like' AND INDEX_NAME = 'uk_express_like_express_user'
);
SET @sql := IF(@dup = 0 AND @idx_exists = 0,
  'ALTER TABLE `express_like` ADD UNIQUE KEY `uk_express_like_express_user` (`express_id`,`username`)',
  IF(@dup > 0, 'SELECT ''REVIEW_REQUIRED: express_like has duplicate (express_id,username) rows; unique key not added'' AS warning', 'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @dup := (
  SELECT COUNT(*) FROM (
    SELECT 1 FROM photograph_like GROUP BY photo_id, username HAVING COUNT(*) > 1
  ) d
);
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'photograph_like' AND INDEX_NAME = 'uk_photograph_like_photo_user'
);
SET @sql := IF(@dup = 0 AND @idx_exists = 0,
  'ALTER TABLE `photograph_like` ADD UNIQUE KEY `uk_photograph_like_photo_user` (`photo_id`,`username`)',
  IF(@dup > 0, 'SELECT ''REVIEW_REQUIRED: photograph_like has duplicate (photo_id,username) rows; unique key not added'' AS warning', 'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @dup := (
  SELECT COUNT(*) FROM (
    SELECT 1 FROM secret_like GROUP BY content_id, username HAVING COUNT(*) > 1
  ) d
);
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'secret_like' AND INDEX_NAME = 'uk_secret_like_content_user'
);
SET @sql := IF(@dup = 0 AND @idx_exists = 0,
  'ALTER TABLE `secret_like` ADD UNIQUE KEY `uk_secret_like_content_user` (`content_id`,`username`)',
  IF(@dup > 0, 'SELECT ''REVIEW_REQUIRED: secret_like has duplicate (content_id,username) rows; unique key not added'' AS warning', 'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- app_user / campus_credential
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` char(36) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_public_id` (`public_id`),
  KEY `idx_app_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `campus_credential` (
  `user_id` bigint NOT NULL,
  `campus_username` varchar(24) NOT NULL,
  `password` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_campus_credential_username` (`campus_username`),
  CONSTRAINT `fk_campus_credential_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- 旧 demo 账号仅迁移身份；旧演示密码置 NULL。
-- 临时映射用随机 UUID，不能通过公开 ID 猜测校园用户名。既有绑定不重复迁移。
DROP TEMPORARY TABLE IF EXISTS `social_legacy_identity_map`;
CREATE TEMPORARY TABLE `social_legacy_identity_map` (
  `campus_username` varchar(24) NOT NULL PRIMARY KEY,
  `public_id` char(36) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
-- MySQL TEMPORARY DDL 不提交事务；仅将旧账号映射与两张身份表写入作为一个事务。
START TRANSACTION;
SET @has_legacy_user := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'
);
SET @sql := IF(@has_legacy_user > 0,
  'INSERT INTO `social_legacy_identity_map` (`campus_username`,`public_id`)
   SELECT u.username, LOWER(CONCAT(
     HEX(RANDOM_BYTES(4)), ''-'', HEX(RANDOM_BYTES(2)), ''-4'',
     SUBSTR(HEX(RANDOM_BYTES(2)), 2, 3), ''-8'',
     SUBSTR(HEX(RANDOM_BYTES(2)), 2, 3), ''-'', HEX(RANDOM_BYTES(6))))
   FROM `user` u
   WHERE NOT EXISTS (SELECT 1 FROM `campus_credential` c WHERE c.campus_username = u.username)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
INSERT INTO `app_user` (`public_id`,`status`,`created_at`,`updated_at`)
SELECT m.public_id, 'ACTIVE', NOW(), NOW() FROM `social_legacy_identity_map` m;
INSERT INTO `campus_credential` (`user_id`,`campus_username`,`password`)
SELECT au.id, m.campus_username, NULL
FROM `social_legacy_identity_map` m JOIN `app_user` au ON au.public_id = m.public_id;
COMMIT;
DROP TEMPORARY TABLE `social_legacy_identity_map`;

-- ---------------------------------------------------------------------------
-- 社交表（结构与 init 对齐；随后补 FK/CHECK）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_follow` (
  `follower_id` bigint NOT NULL,
  `followee_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`,`followee_id`),
  KEY `idx_user_follow_followee` (`followee_id`,`created_at`,`follower_id`),
  KEY `idx_user_follow_follower` (`follower_id`,`created_at`,`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `user_block` (
  `blocker_id` bigint NOT NULL,
  `blocked_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`blocker_id`,`blocked_id`),
  KEY `idx_user_block_blocked` (`blocked_id`,`created_at`,`blocker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_low_id` bigint NOT NULL,
  `user_high_id` bigint NOT NULL,
  `last_seq` bigint NOT NULL DEFAULT 0,
  `last_message_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_pair` (`user_low_id`,`user_high_id`),
  KEY `idx_conversation_updated` (`last_message_at`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `conversation_member` (
  `conversation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `last_read_seq` bigint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_id`,`user_id`),
  KEY `idx_conversation_member_user` (`user_id`,`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `seq` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `client_message_id` char(36) NOT NULL,
  `type` varchar(16) NOT NULL DEFAULT 'TEXT',
  `content` varchar(4000) NOT NULL,
  `image_key` varchar(255) DEFAULT NULL,
  `image_content_type` varchar(64) DEFAULT NULL,
  `image_width` int DEFAULT NULL,
  `image_height` int DEFAULT NULL,
  `image_size` int DEFAULT NULL,
  `image_sha256` char(64) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_message_seq` (`conversation_id`,`seq`),
  UNIQUE KEY `uk_chat_message_client` (`conversation_id`,`sender_id`,`client_message_id`),
  KEY `idx_chat_message_created` (`conversation_id`,`created_at`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- FK / CHECK（若已存在则跳过；重复升级安全）
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_follow' AND CONSTRAINT_NAME='fk_user_follow_follower');
SET @sql := IF(@fk=0, 'ALTER TABLE `user_follow` ADD CONSTRAINT `fk_user_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_follow' AND CONSTRAINT_NAME='fk_user_follow_followee');
SET @sql := IF(@fk=0, 'ALTER TABLE `user_follow` ADD CONSTRAINT `fk_user_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- TiDB 的 TABLE_CONSTRAINTS 不列出 CHECK；CHECK_CONSTRAINTS 在 MySQL / TiDB 均可用。
SET @chk := (SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME='chk_user_follow_not_self');
SET @sql := IF(@chk=0, 'ALTER TABLE `user_follow` ADD CONSTRAINT `chk_user_follow_not_self` CHECK (`follower_id` <> `followee_id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_block' AND CONSTRAINT_NAME='fk_user_block_blocker');
SET @sql := IF(@fk=0, 'ALTER TABLE `user_block` ADD CONSTRAINT `fk_user_block_blocker` FOREIGN KEY (`blocker_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user_block' AND CONSTRAINT_NAME='fk_user_block_blocked');
SET @sql := IF(@fk=0, 'ALTER TABLE `user_block` ADD CONSTRAINT `fk_user_block_blocked` FOREIGN KEY (`blocked_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @chk := (SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME='chk_user_block_not_self');
SET @sql := IF(@chk=0, 'ALTER TABLE `user_block` ADD CONSTRAINT `chk_user_block_not_self` CHECK (`blocker_id` <> `blocked_id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='conversation' AND CONSTRAINT_NAME='fk_conversation_low');
SET @sql := IF(@fk=0, 'ALTER TABLE `conversation` ADD CONSTRAINT `fk_conversation_low` FOREIGN KEY (`user_low_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='conversation' AND CONSTRAINT_NAME='fk_conversation_high');
SET @sql := IF(@fk=0, 'ALTER TABLE `conversation` ADD CONSTRAINT `fk_conversation_high` FOREIGN KEY (`user_high_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @chk := (SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME='chk_conversation_ordered');
SET @sql := IF(@chk=0, 'ALTER TABLE `conversation` ADD CONSTRAINT `chk_conversation_ordered` CHECK (`user_low_id` < `user_high_id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='conversation_member' AND CONSTRAINT_NAME='fk_conversation_member_conv');
SET @sql := IF(@fk=0, 'ALTER TABLE `conversation_member` ADD CONSTRAINT `fk_conversation_member_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='conversation_member' AND CONSTRAINT_NAME='fk_conversation_member_user');
SET @sql := IF(@fk=0, 'ALTER TABLE `conversation_member` ADD CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND CONSTRAINT_NAME='fk_chat_message_conv');
SET @sql := IF(@fk=0, 'ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='chat_message' AND CONSTRAINT_NAME='fk_chat_message_sender');
SET @sql := IF(@fk=0, 'ALTER TABLE `chat_message` ADD CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `app_user` (`id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
