-- Campus credential consent persistence and quick-auth controls
-- Apply manually on existing deployments before enabling the new backend endpoints.

CREATE TABLE IF NOT EXISTS `campus_credential_consent` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校园凭证授权记录ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `consent_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '授权类型',
  `policy_date` date DEFAULT NULL COMMENT '政策版本日期',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `scene` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权场景',
  `consented_at` datetime NOT NULL COMMENT '授权时间',
  `revoked_at` datetime DEFAULT NULL COMMENT '撤回时间',
  `revoked_reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '撤回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ccc_username_type_revoked` (`username`,`consent_type`,`revoked_at`),
  KEY `idx_ccc_username_type_consented` (`username`,`consent_type`,`consented_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='校园凭证授权留痕';

ALTER TABLE `privacy`
  ADD COLUMN IF NOT EXISTS `is_quick_auth_allow` tinyint(1) DEFAULT NULL COMMENT '允许快速认证复用校园凭证' AFTER `is_cache_allow`;

UPDATE `privacy`
SET `is_quick_auth_allow` = 0
WHERE `is_quick_auth_allow` IS NULL;
