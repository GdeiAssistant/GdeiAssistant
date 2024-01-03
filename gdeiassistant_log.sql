/*
 Navicat Premium Data Transfer

 Source Server         : GdeiAssistantLog
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : aws.connect.psdb.cloud:3306
 Source Schema         : gdeiassistant_log

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 02/10/2023 12:53:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for charge_log
-- ----------------------------
DROP TABLE IF EXISTS `charge_log`;
CREATE TABLE `charge_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '校园卡充值记录编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `amount` int NOT NULL COMMENT '充值金额',
  `time` datetime NOT NULL COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of charge_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for close_log
-- ----------------------------
DROP TABLE IF EXISTS `close_log`;
CREATE TABLE `close_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '注销账户记录编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户原账户用户名',
  `resetname` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户注销后用户名',
  `time` datetime NOT NULL COMMENT '注销时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of close_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ip_log
-- ----------------------------
DROP TABLE IF EXISTS `ip_log`;
CREATE TABLE `ip_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'IP地址记录编号',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `type` tinyint(1) NOT NULL COMMENT 'IP地址记录类型',
  `ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'IP地址',
  `network` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'IP地址运营商类型',
  `country` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'IP地址所属国家',
  `province` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'IP地址所属省份',
  `city` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'IP所属城市',
  `time` datetime NOT NULL COMMENT 'IP地址记录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of ip_log
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
