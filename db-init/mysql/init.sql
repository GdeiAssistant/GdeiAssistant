-- =============================================================================
-- GdeiAssistant MySQL 初始化脚本（MySQL 8.0 兼容，三库融合版）
-- 融合旧版 gdeiassistant / gdeiassistant_data / gdeiassistant_log 结构；
-- 含完整黄页、公告/专题阅读、user/ershou/lostandfound/express/photograph 等 Mock 数据。
-- 字符集统一 utf8mb4，NOT NULL 与默认值已按 MySQL 8.0 严格模式校验。
-- 执行: mysql -u root -p < init.sql  或由 Docker /docker-entrypoint-initdb.d 自动执行
-- 旧版 SQL 融合说明见同目录 MERGE_LEGACY.md；日志表迁 MongoDB 建议见该文档。
-- =============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS gdeiassistant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS gdeiassistant_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS gdeiassistant_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gdeiassistant;

-- announcement 使用 gdeiassistant_data 库中的结构，避免在 app 库重复建表造成配置歧义。

-- ----------------------------
-- Table structure for authentication
-- ----------------------------
DROP TABLE IF EXISTS `authentication`;
CREATE TABLE `authentication` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `type` tinyint(1) NOT NULL,
  `number` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `gmt_create` datetime NOT NULL,
  `gmt_modified` datetime NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of authentication
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for cet
-- ----------------------------
DROP TABLE IF EXISTS `cet`;
CREATE TABLE `cet` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `number` bigint DEFAULT NULL COMMENT '四六级准考证号',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of cet
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for dating_pick
-- ----------------------------
DROP TABLE IF EXISTS `dating_pick`;
CREATE TABLE `dating_pick` (
  `pick_id` int NOT NULL AUTO_INCREMENT COMMENT '撩一下编号ID',
  `profile_id` int NOT NULL COMMENT '卖室友信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `content` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '撩一下内容',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`pick_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of dating_pick
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for dating_profile
-- ----------------------------
DROP TABLE IF EXISTS `dating_profile`;
CREATE TABLE `dating_profile` (
  `profile_id` int NOT NULL AUTO_INCREMENT COMMENT '卖室友信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `area` tinyint(1) NOT NULL COMMENT '地区',
  `grade` tinyint(1) NOT NULL COMMENT '年级',
  `faculty` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '院系',
  `hometown` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '家乡',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交友宣言',
  `qq` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'QQ号',
  `wechat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`profile_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of dating_profile
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for delivery_order
-- ----------------------------
DROP TABLE IF EXISTS `delivery_order`;
CREATE TABLE `delivery_order` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '下单者用户名',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '姓名',
  `number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '学号',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '手机号码',
  `price` float NOT NULL COMMENT '报酬',
  `company` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '快递公司',
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '地址',
  `state` tinyint(1) NOT NULL COMMENT '订单状态',
  `remarks` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of delivery_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for delivery_trade
-- ----------------------------
DROP TABLE IF EXISTS `delivery_trade`;
CREATE TABLE `delivery_trade` (
  `trade_id` int NOT NULL AUTO_INCREMENT COMMENT '交易主键ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `username` varchar(24) NOT NULL COMMENT '接单者用户名',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`trade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ----------------------------
-- Records of delivery_trade
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for email
-- ----------------------------
DROP TABLE IF EXISTS `email`;
CREATE TABLE `email` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `email` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电子邮件地址，使用AES加密时需要将varchar长度提升到32及以上',
  `gmt_create` datetime NOT NULL COMMENT '记录创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '记录更新时间',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of email
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for interaction_notification
-- ----------------------------
DROP TABLE IF EXISTS `interaction_notification`;
CREATE TABLE `interaction_notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '统一互动通知ID',
  `module` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '模块',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '通知类型',
  `receiver_username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收者用户名',
  `actor_username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '触发者用户名',
  `target_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '主目标ID',
  `target_sub_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '子目标ID',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '目标类型',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  PRIMARY KEY (`notification_id`),
  KEY `idx_interaction_notification_receiver_time` (`receiver_username`,`create_time`,`notification_id`),
  KEY `idx_interaction_notification_receiver_read` (`receiver_username`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of interaction_notification
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ershou
-- ----------------------------
DROP TABLE IF EXISTS `ershou`;
CREATE TABLE `ershou` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '二手交易信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品描述',
  `price` float NOT NULL COMMENT '商品价格',
  `location` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易地点',
  `type` tinyint NOT NULL COMMENT '商品类型',
  `qq` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'QQ号（选填，与手机至少填一种）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of ershou
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for express
-- ----------------------------
DROP TABLE IF EXISTS `express`;
CREATE TABLE `express` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表白信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者用户名',
  `nickname` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表白者昵称',
  `realname` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表白者真实姓名',
  `self_gender` tinyint(1) NOT NULL COMMENT '表白者性别',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '被表白者昵称',
  `content` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表白内容',
  `person_gender` tinyint(1) NOT NULL COMMENT '被表白者性别',
  `publish_time` datetime NOT NULL COMMENT '表白信息发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of express
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for express_comment
-- ----------------------------
DROP TABLE IF EXISTS `express_comment`;
CREATE TABLE `express_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表白评论信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论者用户名',
  `express_id` int NOT NULL COMMENT '表白信息ID',
  `comment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表白评论',
  `publish_time` datetime NOT NULL COMMENT '表白评论发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of express_comment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for express_guess
-- ----------------------------
DROP TABLE IF EXISTS `express_guess`;
CREATE TABLE `express_guess` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表白猜一下信息ID',
  `express_id` int NOT NULL COMMENT '表白信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '猜一下记录提交者用户名',
  `result` tinyint(1) NOT NULL COMMENT '猜一下结果，1为正确，0为错误',
  `create_time` datetime NOT NULL COMMENT '猜一下记录创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of express_guess
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for express_like
-- ----------------------------
DROP TABLE IF EXISTS `express_like`;
CREATE TABLE `express_like` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表白点赞ID',
  `express_id` int NOT NULL COMMENT '表白信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞者用户名',
  `create_time` datetime NOT NULL COMMENT '点赞记录创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of express_like
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for introduction
-- ----------------------------
DROP TABLE IF EXISTS `introduction`;
CREATE TABLE `introduction` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `introduction` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个人简介内容',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of introduction
-- ----------------------------
BEGIN;
INSERT INTO `introduction` (`username`, `introduction`) VALUES ('gdeiassistant', NULL);
COMMIT;

-- ----------------------------
-- Table structure for lostandfound
-- ----------------------------
DROP TABLE IF EXISTS `lostandfound`;
CREATE TABLE `lostandfound` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '失物招领信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品描述',
  `location` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '遗失地点',
  `item_type` tinyint NOT NULL COMMENT '物品类型',
  `lost_type` tinyint(1) NOT NULL COMMENT '寻主/寻物类型',
  `qq` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'QQ号',
  `wechat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信号',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of lostandfound
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for phone
-- ----------------------------
DROP TABLE IF EXISTS `phone`;
CREATE TABLE `phone` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `code` smallint NOT NULL COMMENT '国际区号',
  `phone` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号，使用AES加密时需要将varchar长度提升到32及以上',
  `gmt_create` datetime NOT NULL COMMENT '记录创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '记录更新时间',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of phone
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for photograph
-- ----------------------------
DROP TABLE IF EXISTS `photograph`;
CREATE TABLE `photograph` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '照片信息ID',
  `title` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '照片信息标题',
  `count` tinyint NOT NULL COMMENT '照片信息图片数量',
  `content` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '照片信息描述内容',
  `type` tinyint(1) NOT NULL COMMENT '照片信息类型',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者用户名',
  `create_time` datetime NOT NULL COMMENT '照片信息发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of photograph
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for photograph_comment
-- ----------------------------
DROP TABLE IF EXISTS `photograph_comment`;
CREATE TABLE `photograph_comment` (
  `comment_id` int NOT NULL AUTO_INCREMENT COMMENT '照片评论ID',
  `photo_id` int NOT NULL COMMENT '照片信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论者用户名',
  `comment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL COMMENT '评论信息发布时间',
  PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of photograph_comment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for photograph_like
-- ----------------------------
DROP TABLE IF EXISTS `photograph_like`;
CREATE TABLE `photograph_like` (
  `like_id` int NOT NULL AUTO_INCREMENT COMMENT '照片点赞记录ID',
  `photo_id` int NOT NULL COMMENT '照片信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞者用户名',
  `create_time` datetime NOT NULL COMMENT '点赞时间',
  PRIMARY KEY (`like_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of photograph_like
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for privacy
-- ----------------------------
DROP TABLE IF EXISTS `privacy`;
CREATE TABLE `privacy` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `is_location_open` tinyint(1) DEFAULT NULL COMMENT '公开国家/地区',
  `is_hometown_open` tinyint(1) DEFAULT NULL COMMENT '公开家乡',
  `is_introduction_open` tinyint(1) DEFAULT NULL COMMENT '公开个人简介',
  `is_faculty_open` tinyint(1) DEFAULT NULL COMMENT '公开院系',
  `is_major_open` tinyint(1) DEFAULT NULL COMMENT '公开专业',
  `is_enrollment_open` tinyint(1) DEFAULT NULL COMMENT '公开入学年份',
  `is_age_open` tinyint(1) DEFAULT NULL COMMENT '公开年龄',
  `is_degree_open` tinyint(1) DEFAULT NULL COMMENT '公开学历',
  `is_cache_allow` tinyint(1) DEFAULT NULL COMMENT '使用教务缓存',
  `is_robots_index_allow` tinyint(1) DEFAULT NULL COMMENT '允许搜索引擎收录',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of privacy
-- ----------------------------
BEGIN;
INSERT INTO `privacy` (`username`, `is_location_open`, `is_hometown_open`, `is_introduction_open`, `is_faculty_open`, `is_major_open`, `is_enrollment_open`, `is_age_open`, `is_degree_open`, `is_cache_allow`, `is_robots_index_allow`) VALUES
('gdeiassistant', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
COMMIT;

-- ----------------------------
-- Table structure for profile
-- ----------------------------
DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `degree` tinyint(1) DEFAULT NULL COMMENT '学历',
  `faculty` tinyint(1) DEFAULT NULL COMMENT '院系',
  `profession` tinyint(1) DEFAULT NULL COMMENT '职业',
  `major` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业',
  `location_region` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地国家/地区',
  `location_state` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地省份/州',
  `location_city` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地城市',
  `hometown_region` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '家乡国家/地区',
  `hometown_state` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '家乡省份/州',
  `hometown_city` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '家乡城市',
  `enrollment` int DEFAULT NULL COMMENT '入学年份',
  `primary_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小学',
  `junior_high_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '初中',
  `high_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高中/职中',
  `colleges` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '大专院校',
  `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像URL，开箱即用占位；正式由R2 avatar/{username}.jpg提供',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别，0男 1女 2其他',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of profile（测试账号：GDEI小助手 gdeiassistant）
-- ----------------------------
BEGIN;
INSERT INTO `profile` (`username`, `nickname`, `birthday`, `degree`, `faculty`, `profession`, `major`, `location_region`, `location_state`, `location_city`, `hometown_region`, `hometown_state`, `hometown_city`, `enrollment`, `primary_school`, `junior_high_school`, `high_school`, `colleges`, `avatar_url`, `gender`) VALUES
('gdeiassistant', 'GDEI小助手', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'https://picsum.photos/id/237/200/200', 0);
COMMIT;

-- ----------------------------
-- Table structure for secret_comment
-- ----------------------------
DROP TABLE IF EXISTS `secret_comment`;
CREATE TABLE `secret_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '校园树洞评论编号ID',
  `content_id` int unsigned NOT NULL COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `comment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校园树洞评论内容',
  `avatar_theme` tinyint NOT NULL COMMENT '随机头像编号',
  `publish_time` datetime NOT NULL COMMENT '回复时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of secret_comment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for secret_content
-- ----------------------------
DROP TABLE IF EXISTS `secret_content`;
CREATE TABLE `secret_content` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '校园树洞信息内容',
  `theme` tinyint(1) NOT NULL COMMENT '校园树洞信息主题',
  `type` tinyint(1) NOT NULL COMMENT '校园树洞信息类型，0为文字树洞信息，1为语音树洞信息',
  `timer` tinyint(1) NOT NULL COMMENT '校园树洞信息是否在24小时后删除，0为否，1为是',
  `state` tinyint(1) NOT NULL COMMENT '校园树洞信息状态',
  `publish_time` datetime NOT NULL COMMENT '校园树洞信息发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=226 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of secret_content
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for secret_like
-- ----------------------------
DROP TABLE IF EXISTS `secret_like`;
CREATE TABLE `secret_like` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '校园树洞点赞编号ID',
  `content_id` int unsigned NOT NULL COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of secret_like
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '话题信息主键ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题信息发布者用户名',
  `topic` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题信息关键词',
  `content` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '话题信息内容',
  `count` tinyint(1) NOT NULL COMMENT '话题信息图片数量',
  `publish_time` datetime NOT NULL COMMENT '话题信息发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of topic
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for topic_like
-- ----------------------------
DROP TABLE IF EXISTS `topic_like`;
CREATE TABLE `topic_like` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '话题信息点赞ID',
  `topic_id` int NOT NULL COMMENT '话题信息ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞者用户名',
  `create_time` datetime NOT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of topic_like
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码，使用AES加密时需要将varchar长度提升到32及以上',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of user（测试账号：gdeiassistant 密码同用户名；未启用 encrypt 时为明文）
-- ----------------------------
BEGIN;
INSERT INTO `user` (`username`, `password`) VALUES ('gdeiassistant', 'gdeiassistant');
COMMIT;

-- ----------------------------
-- Table structure for wechat_user
-- ----------------------------
DROP TABLE IF EXISTS `wechat_user`;
CREATE TABLE `wechat_user` (
  `wechat_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`wechat_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of wechat_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for yiban_user
-- ----------------------------
DROP TABLE IF EXISTS `yiban_user`;
CREATE TABLE `yiban_user` (
  `user_id` int NOT NULL COMMENT '易班唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of yiban_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `username` varchar(24) NOT NULL COMMENT '提交人用户名',
  `content` varchar(500) NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系方式（选填）',
  `type` varchar(50) DEFAULT NULL COMMENT '反馈类型',
  `create_time` datetime NOT NULL COMMENT '提交时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户帮助与反馈';

-- ----------------------------
-- Records of feedback
-- ----------------------------
BEGIN;
COMMIT;

-- =============================================================================
-- 开箱即用 Mock 数据（关联用户均为 gdeiassistant）
-- 图片说明：失物招领、二手、校园拍拍、话题的图片均存 R2，无独立 xxx_picture 子表。
-- 路径规则：lostandfound/{id}_{index}.jpg、ershou/{id}_{index}.jpg、
--          photograph/{id}_{index}.jpg、topic/{id}_{index}.jpg。
-- 前端若无 R2 可对无图项使用占位 https://picsum.photos/400/300
-- =============================================================================

-- 失物招领 lostandfound（item_type 0手机 1校园卡 2身份证 3银行卡 4书 5钥匙 6包包 7衣帽 8校园代步 9运动健身 10数码配件 11其他；lost_type 0寻主 1寻物；state 0寻主/寻物中 1确认寻回）
INSERT INTO `lostandfound` (`username`,`name`,`description`,`location`,`item_type`,`lost_type`,`qq`,`wechat`,`phone`,`state`,`publish_time`) VALUES
('gdeiassistant','黑色华为手机','华为Mate40 黑色 于图书馆三楼阅览室遗失，有蓝色手机壳','图书馆三楼阅览室',0,1,'123456789','wxid_abc','13800138000',0,NOW() - INTERVAL 2 DAY),
('gdeiassistant','校园一卡通','姓名李某某 学号2021开头 在食堂打饭时遗失','第一食堂窗口',1,0,'987654321',NULL,NULL,0,NOW() - INTERVAL 1 DAY),
('gdeiassistant','一串钥匙','共三把钥匙 含一个小熊钥匙扣 在田径场捡到','田径场看台',5,0,NULL,'wxid_xyz','13900139000',1,NOW() - INTERVAL 5 DAY),
('gdeiassistant','蓝色雨伞','折叠伞 伞柄有磨损 在综合楼B栋捡到','综合楼B栋二楼',7,0,'111222333',NULL,NULL,0,NOW() - INTERVAL 3 DAY),
('gdeiassistant','身份证','姓名张某某 在体育馆捡到','体育馆更衣室',2,0,NULL,NULL,'13700137000',0,NOW() - INTERVAL 4 DAY),
('gdeiassistant','英语四级准考证','2024年6月考场 姓名王某某 在自习室遗失','教学楼A栋302',11,1,'222333444',NULL,'13600136000',0,NOW() - INTERVAL 6 DAY),
('gdeiassistant','黑色双肩包','内有高数课本和笔记本 在食堂占座时被拿错','第二食堂',6,1,NULL,'wxid_back','13500135000',0,NOW() - INTERVAL 7 DAY),
('gdeiassistant','蓝牙耳机','白色 AirPods 收纳盒 在操场跑步后遗失','田径场跑道',10,1,'333444555',NULL,NULL,1,NOW() - INTERVAL 10 DAY),
('gdeiassistant','学生证','照片栏有钢印 在图书馆闸机处捡到','图书馆一楼闸机',11,0,NULL,NULL,'13400134000',0,NOW() - INTERVAL 1 DAY),
('gdeiassistant','钱包','棕色皮质 内有校园卡和少量现金 在体育馆更衣室','体育馆更衣室',3,0,'444555666','wxid_wallet',NULL,0,NOW() - INTERVAL 8 DAY);

-- 树洞 secret_content（theme 主题；type 0文本 1语音；timer 0否 1是24h删除；state 0已发布）
INSERT INTO `secret_content` (`username`,`content`,`theme`,`type`,`timer`,`state`,`publish_time`) VALUES
('gdeiassistant','今天高数考完了，感觉还行，希望别挂科～',1,0,0,0,NOW() - INTERVAL 1 DAY),
('gdeiassistant','图书馆占座现象能不能少一点，早上七点去都没位置了',2,0,0,0,NOW() - INTERVAL 2 DAY),
('gdeiassistant','有没有一起备考教资的同学，可以一起刷题互相监督',3,0,0,0,NOW() - INTERVAL 3 DAY),
('gdeiassistant','食堂新开的窗口味道不错，安利给各位',1,0,0,0,NOW() - INTERVAL 4 DAY),
('gdeiassistant','明天体测加油，大家早点休息',2,0,0,0,NOW() - INTERVAL 5 DAY);

-- 二手 ershou（type 0校园代步 1手机 2电脑 3数码配件 4数码 5电器 6运动健身 7衣物伞帽 8图书教材 9租赁 10生活娱乐 11其他；state 0下架 1待出售 2已出售）
INSERT INTO `ershou` (`username`,`name`,`description`,`price`,`location`,`type`,`qq`,`phone`,`state`,`publish_time`) VALUES
('gdeiassistant','九成新电动车','小牛U1 白色 续航约40km 带发票',1200,'海珠校区',0,'123456789','13800138000',1,NOW() - INTERVAL 2 DAY),
('gdeiassistant','高等数学第七版上下册','同济版 有少量笔记 无缺页',25,'花都校区',8,'987654321',NULL,1,NOW() - INTERVAL 1 DAY),
('gdeiassistant','机械键盘','雷柏V500 青轴 使用半年',80,'海珠校区',4,'111222333','13900139000',2,NOW() - INTERVAL 5 DAY),
('gdeiassistant','小功率电煮锅','宿舍可用 1.5L 九成新',35,'花都校区',5,NULL,NULL,1,NOW() - INTERVAL 3 DAY),
('gdeiassistant','自行车','凤凰牌 带锁 毕业出',150,'海珠校区',0,NULL,'13800138000',1,NOW() - INTERVAL 4 DAY),
('gdeiassistant','大学物理教材','张三慧版 两册 无勾画',30,'海珠校区',8,'555666777',NULL,1,NOW() - INTERVAL 6 DAY),
('gdeiassistant','瑜伽垫','加厚防滑 使用一学期',20,'花都校区',6,NULL,'13700137000',1,NOW() - INTERVAL 7 DAY),
('gdeiassistant','台灯','护眼LED 可调光 宿舍用',45,'海珠校区',5,'666777888','13600136000',1,NOW() - INTERVAL 8 DAY),
('gdeiassistant','有线耳机','入耳式 音质良好 闲置',15,'花都校区',4,NULL,NULL,2,NOW() - INTERVAL 9 DAY),
('gdeiassistant','冬季棉被','1.5米 厚实 毕业带不走',50,'海珠校区',7,'777888999','13500135000',1,NOW() - INTERVAL 10 DAY),
('gdeiassistant','线性代数辅导书','配套习题详解 几乎全新',18,'花都校区',8,NULL,'13400134000',1,NOW() - INTERVAL 11 DAY);

-- 表白墙 express（self_gender/person_gender 0男 1女 2其他或保密）
INSERT INTO `express` (`username`,`nickname`,`realname`,`self_gender`,`name`,`content`,`person_gender`,`publish_time`) VALUES
('gdeiassistant','小助手','',0,'某同学','在图书馆经常看到你在窗边看书，想认识一下，又怕打扰。如果能看到这条，可以回复我吗？',1,NOW() - INTERVAL 1 DAY),
('gdeiassistant','匿名','',1,'那个穿白衬衫的男生','周三下午在球场打球的你，笑容很好看。',0,NOW() - INTERVAL 2 DAY),
('gdeiassistant','路人甲',NULL,0,'全体考研人','祝大家一战成硕，加油！',2,NOW() - INTERVAL 3 DAY);

-- 校园拍拍 photograph（type 0生活照 1校园照 2毕业照；count 为图片张数，图片存 R2 photograph/{id}_{index}.jpg）
INSERT INTO `photograph` (`title`,`count`,`content`,`type`,`username`,`create_time`) VALUES
('校园秋色',2,'图书馆前的银杏黄了',1,'gdeiassistant',NOW() - INTERVAL 2 DAY),
('宿舍日常',1,'周末小聚',0,'gdeiassistant',NOW() - INTERVAL 1 DAY);

-- 话题 topic（count 为图片数量，图片存 R2 topic/{id}_{index}.jpg）
INSERT INTO `topic` (`username`,`topic`,`content`,`count`,`publish_time`) VALUES
('gdeiassistant','考研','今年考研的同学们加油，坚持到底就是胜利！',1,NOW() - INTERVAL 1 DAY),
('gdeiassistant','食堂','二饭新窗口麻辣烫不错',0,NOW() - INTERVAL 2 DAY);

-- =============================================================================
-- gdeiassistant_data 库（业务库 data：公告 / 电费 / 黄页 / 阅读）
-- 黄页功能跨库查询时请使用 gdeiassistant_data.yellow_page、gdeiassistant_data.yellow_page_type
-- =============================================================================
USE gdeiassistant_data;

DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '通知公告ID',
  `title` varchar(50) NOT NULL COMMENT '通知公告标题',
  `content` varchar(250) NOT NULL COMMENT '通知公告内容',
  `publish_time` datetime NOT NULL COMMENT '通知公告发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `electricfees`;
CREATE TABLE `electricfees` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '电费记录主键ID',
  `year` smallint NOT NULL COMMENT '年份',
  `building_number` varchar(5) NOT NULL COMMENT '大楼号',
  `room_number` smallint NOT NULL COMMENT '宿舍号',
  `people_number` tinyint(1) NOT NULL COMMENT '入住人数',
  `department` varchar(10) NOT NULL COMMENT '院系',
  `number` bigint NOT NULL COMMENT '学号',
  `name` varchar(10) NOT NULL COMMENT '姓名',
  `used_electric_amount` float NOT NULL COMMENT '用电数额',
  `free_electric_amount` float NOT NULL COMMENT '免费电额',
  `fee_based_electric_amount` float NOT NULL COMMENT '计费电数',
  `electric_price` float NOT NULL COMMENT '电价',
  `total_electric_bill` float NOT NULL COMMENT '总电费',
  `average_electric_bill` float NOT NULL COMMENT '平均电费',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `reading`;
CREATE TABLE `reading` (
  `id` varchar(43) NOT NULL COMMENT '专题阅读信息主键ID',
  `title` varchar(50) NOT NULL COMMENT '专题阅读信息标题',
  `description` varchar(150) NOT NULL COMMENT '专题阅读信息描述',
  `link` varchar(250) NOT NULL COMMENT '专题阅读信息链接',
  `create_time` datetime NOT NULL COMMENT '专题阅读信息创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `yellow_page`;
CREATE TABLE `yellow_page` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '黄页信息主键ID',
  `type_code` int NOT NULL COMMENT '类别',
  `section` varchar(25) NOT NULL COMMENT '单位',
  `campus` varchar(5) DEFAULT NULL COMMENT '校区',
  `major_phone` varchar(15) DEFAULT NULL COMMENT '主要电话',
  `minor_phone` varchar(15) DEFAULT NULL COMMENT '次要电话',
  `address` varchar(45) DEFAULT NULL COMMENT '地址',
  `email` varchar(35) DEFAULT NULL COMMENT '电子邮箱',
  `website` varchar(85) DEFAULT NULL COMMENT '网站',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园黄页信息';

DROP TABLE IF EXISTS `yellow_page_type`;
CREATE TABLE `yellow_page_type` (
  `type_code` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(15) NOT NULL,
  PRIMARY KEY (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (1, '倾听与倾诉'),(2, '故障保修'),(3, '网络'),(4, '党务'),(5, '医疗与救援'),(6, '生活与保障'),(7, '职能部门'),(8, '就业创业'),(9, '院系部门'),(10, '举报和申诉'),(11, '紧急求助');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES
(409, 11, '消防报警', '国家', '119', NULL, NULL, NULL, NULL),(410, 11, '医疗急救', '国家', '120', NULL, NULL, NULL, NULL),(411, 11, '交通事故报警', '国家', '122', NULL, NULL, NULL, NULL),(412, 11, '短信报警', '国家', '12110', NULL, NULL, NULL, NULL),(413, 10, '国家邮政局申诉', '国家', NULL, NULL, NULL, NULL, 'http://sswz.spb.gov.cn/'),(414, 10, '司法服务热线', '司法', '12368', NULL, NULL, NULL, NULL),(415, 10, '司法援助热线', '司法', '12348', NULL, NULL, NULL, 'http://www.12348.gov.cn/'),(416, 10, '国家工业和信息化部电信用户申诉', '国家', '12300', NULL, '北京市西城区月坛南街11号电信用户申诉受理中心', 'accept@chinatcc.gov.cn', 'http://www.chinatcc.gov.cn:8080/cms/shensus/'),(417, 10, '国家教育部统一监督举报', '国家', '010-66092315', NULL, NULL, '12391@moe.edu.cn', 'http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html'),(418, 10, '国家信访局投诉和建议', '国家', '010—68015310', NULL, '北京市西城区月坛南街8号', NULL, 'http://wsxf.gjxfj.gov.cn/zfp/webroot/index.html'),(419, 10, '国家工商总局消费者维权申诉', '国家', '12315', NULL, NULL, NULL, 'http://www.315.gov.cn'),(420, 10, '广州市人民政府服务', '市政', '12345', NULL, NULL, NULL, 'http://gz12345.gz.gov.cn/'),
(421, 1, '心理健康教育与辅导中心（海珠校区）', '海珠', '34114108', NULL, '田家炳大楼2楼', NULL, NULL),(422, 1, '心理健康教育与辅导中心（花都校区）', '花都', '36967712', NULL, '花都校区实验楼205', NULL, NULL),(423, 1, '心灵之约（海珠校区）', '海珠', '34113456', NULL, '学生宿舍4栋1楼，党员工作站内', NULL, NULL),(424, 1, '心灵之约（花都校区）', '花都', '34113987', NULL, '学生宿舍4栋1楼，党员工作站内', NULL, NULL),(425, 2, '宿舍管理科', '海珠', '34113723', NULL, '学生宿舍4栋007、008', NULL, NULL),(426, 2, '物业服务中心', '花都', '18011902709', NULL, '学生宿舍1栋1楼', NULL, NULL),(427, 3, '网络中心（海珠校区）', '海珠', '34113702', NULL, '综合楼B2203', NULL, 'http://web.gdei.edu.cn/nic/'),(428, 3, '网络中心（花都校区）', '花都', '36967722', NULL, '图书馆220', NULL, NULL),(429, 4, '党代表工作室（海珠校区）', '海珠', '34113023', NULL, '综合楼18楼1806', NULL, NULL),(430, 4, '党代表工作室（花都校区）', '花都', '36967719', NULL, '图书馆708', NULL, NULL),(431, 4, '党政办公室', '海珠', '34113736', NULL, '综合楼15楼', NULL, 'http://web.gdei.edu.cn/xyb/'),
(432, 5, '监控中心（紧急事务报告）', '花都', '36967733', NULL, NULL, NULL, NULL),(433, 5, '物业安保24小时值班', '花都', '36967733', NULL, NULL, NULL, NULL),(434, 5, '电梯应急救援（海珠校区）', '海珠', '96333', NULL, NULL, NULL, NULL),(435, 5, '电梯应急救援', '花都', '18837490602', '18837490597', NULL, NULL, NULL),(436, 5, '医务室（海珠校区）', '海珠', '34113271', NULL, '学生宿舍', NULL, NULL),(437, 5, '医务室（花都校区）', '花都', '36967710', NULL, '学生宿舍2栋1楼', NULL, NULL),(438, 5, '医保办', '海珠', '34113511', NULL, NULL, NULL, NULL),(439, 6, '水电中心值班处', '海珠', '34113363', NULL, NULL, NULL, NULL),(440, 6, '卡务中心', '花都', '36967715', NULL, '学生宿舍1栋1楼', NULL, NULL),(441, 6, '5栋宿舍楼送水', '海珠', '15920841618', NULL, '学生宿舍5栋', NULL, NULL),(442, 6, '培训楼招待处', '海珠', '34113500', NULL, '培训楼', NULL, NULL),(443, 6, '体育中心', '海珠', '34113643', '34113232', '体育中心', NULL, NULL),
(444, 7, '后勤基建处（海珠校区）', '海珠', '34113274', NULL, '综合楼9楼', NULL, 'http://web.gdei.edu.cn/hqc/'),(445, 7, '后勤基建处（花都校区）', '花都', '36967702', NULL, '图书馆705', NULL, 'http://web.gdei.edu.cn/hqc/'),(446, 7, '饭堂负责人（海珠校区）', '海珠', '13380052368', NULL, NULL, NULL, NULL),(447, 7, '饭堂负责人（花都校区第一饭堂）', '花都', '13533936083', NULL, NULL, NULL, NULL),(448, 7, '饭堂负责人（花都校区第二饭堂）', '花都', '15697632188', NULL, NULL, NULL, NULL),(449, 7, '饭堂负责人（花都校区第三饭堂）', '花都', '13725408927', NULL, NULL, NULL, NULL),(450, 7, '图书馆（海珠校区）', '海珠', '34113372', NULL, '图书馆大楼', NULL, 'http://lib.gdei.edu.cn/'),(451, 7, '图书馆（花都校区）', '花都', '39697731', NULL, '图书馆3楼大厅', NULL, 'http://lib.gdei.edu.cn/'),(452, 7, '教务处（海珠校区）', '海珠', '34113407', '34113249', '综合楼13楼', 'jwc@gdei.edu.cn', 'http://web.gdei.edu.cn/jwc/'),(453, 7, '教务处（花都校区）', '花都', '36967708', NULL, '图书馆703', NULL, 'http://web.gdei.edu.cn/jwc/'),(454, 7, '财务处（海珠校区）', '海珠', '34113275', NULL, '综合楼14楼', NULL, 'http://web.gdei.edu.cn/cwc/'),(455, 7, '财务处（花都校区）', '花都', '36967721', NULL, '学生宿舍3栋1楼', NULL, 'http://web.gdei.edu.cn/cwc/'),(456, 7, '学生工作部（处）（海珠校区）', '海珠', '34113360', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/xsc/'),(457, 7, '学生工作部（处）（花都校区）', '花都', '36967703', NULL, '图书馆702', NULL, 'http://web.gdei.edu.cn/xsc/'),(458, 7, '团委（海珠校区）', '海珠', '34113325', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/tw/'),(459, 7, '团委（花都校区）', '花都', '36967703', NULL, '图书馆702', NULL, 'http://web.gdei.edu.cn/tw/'),
(460, 8, '招生办公室', '海珠', '34113327', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/zsb/'),(461, 8, '纪检监察处', '海珠', '34113624', NULL, '综合楼18楼', 'jj@gdei.edu.cn', 'http://web.gdei.edu.cn/jwb/'),(462, 8, '就业指导中心', '海珠', '34114466', NULL, '综合楼12楼', NULL, 'http://210.38.64.162:9000/job'),(463, 9, '就业创业咨询预约（海珠校区）', '海珠', '34113716', NULL, '综合楼12楼1211室', 'jyzd@gdei.edu.cn', NULL),(464, 9, '就业创业咨询预约（花都校区）', '花都', '36967716', NULL, '图书馆2楼216室', 'jyzd@gdei.edu.cn', NULL),(465, 9, '教育学院', '海珠', '34113297', NULL, NULL, NULL, 'http://web.gdei.edu.cn/jyx/'),(466, 9, '物理与信息工程系', '海珠', '34113256', NULL, NULL, NULL, 'http://web.gdei.edu.cn/wlx/'),(467, 9, '生物与食品工程学院', '海珠', '34113257', NULL, NULL, NULL, 'http://web.gdei.edu.cn/swx/'),(468, 9, '体育学院', '海珠', '34113269', NULL, NULL, NULL, 'http://web.gdei.edu.cn/tyx/'),(469, 9, '中文系', '花都', '36967743', NULL, NULL, NULL, 'http://web.gdei.edu.cn/zwx/'),(470, 9, '政法系', '花都', '34113290', '34113397', NULL, NULL, 'http://web.gdei.edu.cn/zfx/'),(471, 9, '外语系', '花都', '36967750', '34113295', '实验楼509', NULL, 'http://web.gdei.edu.cn/wyx/'),(472, 9, '数学系', '花都', '34113296', '36967738', NULL, NULL, 'http://web.gdei.edu.cn/sxx/'),(473, 9, '化学系', '花都', '36967768', '34113456', NULL, NULL, 'http://web.gdei.edu.cn/hxx/'),(474, 9, '计算机科学系', '花都', '34115714', '36967761', NULL, NULL, 'http://web.gdei.edu.cn/jsjx/'),(475, 9, '音乐系', '花都', '36967776', '34114436', NULL, NULL, 'http://web.gdei.edu.cn/yyx/'),(476, 9, '美术学院', '花都', '36967771', '34113634', NULL, NULL, 'http://web.gdei.edu.cn/msx/');
INSERT INTO `announcement` (`title`, `content`, `publish_time`) VALUES
('校园助手系统上线通知', '广东二师助手已正式上线，欢迎同学们使用失物招领、二手交易、表白墙等功能。', NOW() - INTERVAL 7 DAY),
('图书馆开放时间调整', '即日起图书馆开放时间调整为 8:00—22:00，请同学们合理安排自习。', NOW() - INTERVAL 3 DAY),
('校园网缴费方式说明', '本学期校园网缴费请登录信息门户，支持微信、支付宝。欠费将影响选课与成绩查询。', NOW() - INTERVAL 10 DAY),
('体测补测安排', '体质健康测试补测时间为本周六上午 8:00—11:00，地点为田径场，请携带学生证。', NOW() - INTERVAL 4 DAY),
('宿舍热水供应时间', '海珠校区热水供应时间为 17:30—23:00，花都校区为 17:00—23:30，请同学们错峰使用。', NOW() - INTERVAL 6 DAY),
('四六级准考证打印', '请考生登录 CET 报名网站自行打印准考证，考试当日携带身份证、学生证、准考证。', NOW() - INTERVAL 8 DAY),
('教学楼空调开放通知', '即日起教学楼空调开放，请节约用电，离开时关闭空调与照明。', NOW() - INTERVAL 2 DAY),
('就业指导讲座预告', '本周三 14:30 综合楼 12 楼，就业指导中心举办简历与面试技巧讲座，欢迎参加。', NOW() - INTERVAL 1 DAY),
('校园卡补办流程', '校园卡遗失请先挂失，再持身份证到卡务中心补办，补办工本费 20 元。', NOW() - INTERVAL 5 DAY),
('寒假闭馆通知', '图书馆将于寒假期间闭馆，具体日期见官网。借阅到期日顺延至开馆后一周。', NOW() - INTERVAL 12 DAY);
INSERT INTO `reading` (`id`, `title`, `description`, `link`, `create_time`) VALUES
('sample-001', '开学季荐读：如何高效利用图书馆', '新学期图书馆资源使用指南与荐读书目。', 'https://lib.gdei.edu.cn/reading/guide', NOW() - INTERVAL 5 DAY),
('sample-002', '心理健康主题阅读', '情绪管理与压力应对专题文章合集。', 'https://lib.gdei.edu.cn/reading/mental', NOW() - INTERVAL 2 DAY),
('sample-003', '毕业论文写作规范', '本科毕业论文格式、引用与查重要求说明。', 'https://lib.gdei.edu.cn/reading/thesis', NOW() - INTERVAL 7 DAY),
('sample-004', '考研复试经验分享', '历年考研复试流程与常见问题汇总。', 'https://lib.gdei.edu.cn/reading/kaoyan', NOW() - INTERVAL 4 DAY),
('sample-005', '电子资源校外访问指南', 'VPN 与 CARSI 方式访问知网、万方等数据库。', 'https://lib.gdei.edu.cn/reading/remote', NOW() - INTERVAL 6 DAY),
('sample-006', '教资备考书目推荐', '教师资格证笔试、面试备考教材与真题推荐。', 'https://lib.gdei.edu.cn/reading/jiaozi', NOW() - INTERVAL 3 DAY),
('sample-007', '英语四六级备考专题', '听力、阅读、写作分项突破与真题解析。', 'https://lib.gdei.edu.cn/reading/cet', NOW() - INTERVAL 8 DAY),
('sample-008', '职业规划与就业指导', '简历撰写、面试技巧与实习渠道介绍。', 'https://lib.gdei.edu.cn/reading/career', NOW() - INTERVAL 1 DAY),
('sample-009', '校园安全与防诈提醒', '常见电信诈骗与校园贷防范知识。', 'https://lib.gdei.edu.cn/reading/safety', NOW() - INTERVAL 9 DAY),
('sample-010', '宿舍文明与卫生倡议', '文明宿舍评比标准与日常卫生注意事项。', 'https://lib.gdei.edu.cn/reading/dorm', NOW() - INTERVAL 10 DAY);

-- =============================================================================
-- gdeiassistant_log 库（充值 / 注销 / IP 日志）
-- =============================================================================
USE gdeiassistant_log;

DROP TABLE IF EXISTS `charge_log`;
CREATE TABLE `charge_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '校园卡充值记录编号ID',
  `username` varchar(24) NOT NULL COMMENT '用户名',
  `amount` int NOT NULL COMMENT '充值金额',
  `time` datetime NOT NULL COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `close_log`;
CREATE TABLE `close_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '注销账户记录编号ID',
  `username` varchar(24) NOT NULL COMMENT '用户原账户用户名',
  `resetname` varchar(24) NOT NULL COMMENT '用户注销后用户名',
  `time` datetime NOT NULL COMMENT '注销时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `ip_log`;
CREATE TABLE `ip_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'IP地址记录编号',
  `username` varchar(24) NOT NULL COMMENT '用户名',
  `type` tinyint(1) NOT NULL COMMENT 'IP地址记录类型',
  `ip` varchar(45) NOT NULL COMMENT 'IP地址',
  `network` varchar(15) NOT NULL COMMENT 'IP地址运营商类型',
  `country` varchar(15) DEFAULT NULL COMMENT 'IP地址所属国家',
  `province` varchar(15) DEFAULT NULL COMMENT 'IP地址所属省份',
  `city` varchar(15) DEFAULT NULL COMMENT 'IP所属城市',
  `time` datetime NOT NULL COMMENT 'IP地址记录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

SET FOREIGN_KEY_CHECKS = 1;
