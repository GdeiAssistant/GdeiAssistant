-- Charge order status tracking.
-- Apply manually on existing deployments before enabling long-term charge status tracking.

USE gdeiassistant_log;

CREATE TABLE IF NOT EXISTS `charge_order` (
  `order_id` varchar(36) NOT NULL COMMENT '服务端生成的充值订单ID',
  `username` varchar(24) NOT NULL COMMENT '用户名',
  `amount` int NOT NULL COMMENT '充值金额',
  `status` varchar(32) NOT NULL COMMENT '订单状态',
  `idempotency_key_hash` varchar(64) DEFAULT NULL COMMENT '幂等键哈希',
  `payload_fingerprint` varchar(64) DEFAULT NULL COMMENT '请求参数指纹',
  `request_id` varchar(64) DEFAULT NULL COMMENT '请求链路ID',
  `device_id_hash` varchar(64) DEFAULT NULL COMMENT '设备标识哈希',
  `external_order_no` varchar(128) DEFAULT NULL COMMENT '外部订单号',
  `external_trade_no` varchar(128) DEFAULT NULL COMMENT '外部交易号',
  `school_trade_no` varchar(128) DEFAULT NULL COMMENT '学校侧流水号',
  `payment_url_hash` varchar(64) DEFAULT NULL COMMENT '支付URL哈希',
  `error_code` varchar(64) DEFAULT NULL COMMENT '脱敏错误码',
  `error_message_sanitized` varchar(255) DEFAULT NULL COMMENT '脱敏错误摘要',
  `unknown_reason` varchar(255) DEFAULT NULL COMMENT 'UNKNOWN原因摘要',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  `submitted_at` datetime DEFAULT NULL COMMENT '提交外部系统时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成当前阶段时间',
  `last_checked_at` datetime DEFAULT NULL COMMENT '最近补偿查询时间',
  `check_count` int NOT NULL DEFAULT 0 COMMENT '补偿查询次数',
  `manual_review_at` datetime DEFAULT NULL COMMENT '人工处理时间',
  `manual_review_note` varchar(255) DEFAULT NULL COMMENT '人工处理备注',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`order_id`),
  KEY `idx_charge_order_username_created` (`username`, `created_at`),
  KEY `idx_charge_order_idempotency_hash` (`idempotency_key_hash`),
  KEY `idx_charge_order_status_updated` (`status`, `updated_at`),
  KEY `idx_charge_order_external_order_no` (`external_order_no`),
  KEY `idx_charge_order_user_key_fingerprint` (`username`, `idempotency_key_hash`, `payload_fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='充值订单状态记录';
