/*
 Navicat Premium Data Transfer

 Source Server         : GdeiAssistant
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : aws.connect.psdb.cloud:3306
 Source Schema         : gdeiassistant

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 02/10/2023 12:51:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '通知公告ID',
  `title` varchar(50) NOT NULL COMMENT '通知公告标题',
  `content` varchar(250) NOT NULL COMMENT '通知公告内容',
  `publish_time` datetime NOT NULL COMMENT '通知公告发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of announcement
-- ----------------------------
BEGIN;
COMMIT;

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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `number` bigint DEFAULT NULL COMMENT '四六级准考证号',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of cet
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for dating_message
-- ----------------------------
DROP TABLE IF EXISTS `dating_message`;
CREATE TABLE `dating_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息编号ID',
  `pick_id` int NOT NULL COMMENT '撩一下编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `type` tinyint(1) NOT NULL COMMENT '消息类型',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of dating_message
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `email` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '电子邮件地址，使用AES加密时需要将varchar长度提升到32及以上',
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
-- Table structure for ershou
-- ----------------------------
DROP TABLE IF EXISTS `ershou`;
CREATE TABLE `ershou` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '二手交易信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品描述',
  `price` float NOT NULL COMMENT '商品价格',
  `location` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易地点',
  `type` tinyint NOT NULL COMMENT '商品类型',
  `qq` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'QQ号',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '手机号',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发布者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '猜一下记录提交者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '点赞者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `code` smallint NOT NULL COMMENT '国际区号',
  `phone` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '手机号，使用AES加密时需要将varchar长度提升到32及以上',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发布者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '点赞者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `is_gender_open` tinyint(1) DEFAULT NULL COMMENT '公开性别',
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
INSERT INTO `privacy` (`username`, `is_gender_open`, `is_location_open`, `is_hometown_open`, `is_introduction_open`, `is_faculty_open`, `is_major_open`, `is_enrollment_open`, `is_age_open`, `is_degree_open`, `is_cache_allow`, `is_robots_index_allow`) VALUES ('gdeiassistant', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
COMMIT;

-- ----------------------------
-- Table structure for profile
-- ----------------------------
DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别',
  `custom_gender` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '自定义性别',
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
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of profile
-- ----------------------------
BEGIN;
INSERT INTO `profile` (`username`, `nickname`, `gender`, `custom_gender`, `birthday`, `degree`, `faculty`, `profession`, `major`, `location_region`, `location_state`, `location_city`, `hometown_region`, `hometown_state`, `hometown_city`, `enrollment`, `primary_school`, `junior_high_school`, `high_school`, `colleges`) VALUES ('gdeiassistant', 'gdeiassistant', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for secret_comment
-- ----------------------------
DROP TABLE IF EXISTS `secret_comment`;
CREATE TABLE `secret_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '校园树洞评论编号ID',
  `content_id` int unsigned NOT NULL COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '话题信息发布者用户名',
  `topic` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '话题信息关键词',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '点赞者用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '密码，使用AES加密时需要将varchar长度提升到32及以上',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`username`, `password`) VALUES ('gdeiassistant', 'gdeiassistant');
COMMIT;

-- ----------------------------
-- Table structure for wechat_user
-- ----------------------------
DROP TABLE IF EXISTS `wechat_user`;
CREATE TABLE `wechat_user` (
  `wechat_id` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '微信唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
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
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of yiban_user
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
