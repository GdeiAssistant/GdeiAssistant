/*
 Navicat Premium Data Transfer

 Source Server         : GdeiAssistantData
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : aws.connect.psdb.cloud:3306
 Source Schema         : gdeiassistant_data

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 02/10/2023 12:52:49
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
-- Table structure for electricfees
-- ----------------------------
DROP TABLE IF EXISTS `electricfees`;
CREATE TABLE `electricfees` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '电费记录主键ID',
  `year` smallint NOT NULL COMMENT '年份',
  `building_number` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '大楼号',
  `room_number` smallint NOT NULL COMMENT '宿舍号',
  `people_number` tinyint(1) NOT NULL COMMENT '入住人数',
  `department` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '院系',
  `number` bigint NOT NULL COMMENT '学号',
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `used_electric_amount` float NOT NULL COMMENT '用电数额',
  `free_electric_amount` float NOT NULL COMMENT '免费电额',
  `fee_based_electric_amount` float NOT NULL COMMENT '计费电数',
  `electric_price` float NOT NULL COMMENT '电价',
  `total_electric_bill` float NOT NULL COMMENT '总电费',
  `average_electric_bill` float NOT NULL COMMENT '平均电费',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31972 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of electricfees
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for reading
-- ----------------------------
DROP TABLE IF EXISTS `reading`;
CREATE TABLE `reading` (
  `id` varchar(43) NOT NULL COMMENT '专题阅读信息主键ID',
  `title` varchar(50) NOT NULL COMMENT '专题阅读信息标题',
  `description` varchar(150) NOT NULL COMMENT '专题阅读信息描述',
  `link` varchar(250) NOT NULL COMMENT '专题阅读信息链接',
  `create_time` datetime NOT NULL COMMENT '专题阅读信息创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of reading
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for yellow_page
-- ----------------------------
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
) ENGINE=InnoDB AUTO_INCREMENT=477 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园黄页信息';

-- ----------------------------
-- Records of yellow_page
-- ----------------------------
BEGIN;
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (409, 11, '消防报警', '国家', '119', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (410, 11, '医疗急救', '国家', '120', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (411, 11, '交通事故报警', '国家', '122', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (412, 11, '短信报警', '国家', '12110', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (413, 10, '国家邮政局申诉', '国家', NULL, NULL, NULL, NULL, 'http://sswz.spb.gov.cn/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (414, 10, '司法服务热线', '司法', '12368', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (415, 10, '司法援助热线', '司法', '12348', NULL, NULL, NULL, 'http://www.12348.gov.cn/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (416, 10, '国家工业和信息化部电信用户申诉', '国家', '12300', NULL, '北京市西城区月坛南街11号电信用户申诉受理中心', 'accept@chinatcc.gov.cn', 'http://www.chinatcc.gov.cn:8080/cms/shensus/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (417, 10, '国家教育部统一监督举报', '国家', '010-66092315', NULL, NULL, '12391@moe.edu.cn', 'http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (418, 10, '国家信访局投诉和建议', '国家', '010—68015310', NULL, '北京市西城区月坛南街8号', NULL, 'http://wsxf.gjxfj.gov.cn/zfp/webroot/index.html');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (419, 10, '国家工商总局消费者维权申诉', '国家', '12315', NULL, NULL, NULL, 'http://www.315.gov.cn');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (420, 10, '广州市人民政府服务', '市政', '12345', NULL, NULL, NULL, 'http://gz12345.gz.gov.cn/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (421, 1, '心理健康教育与辅导中心（海珠校区）', '海珠', '34114108', NULL, '田家炳大楼2楼', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (422, 1, '心理健康教育与辅导中心（花都校区）', '花都', '36967712', NULL, '花都校区实验楼205', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (423, 1, '心灵之约（海珠校区）', '海珠', '34113456', NULL, '学生宿舍4栋1楼，党员工作站内', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (424, 1, '心灵之约（花都校区）', '花都', '34113987', NULL, '学生宿舍4栋1楼，党员工作站内', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (425, 2, '宿舍管理科', '海珠', '34113723', NULL, '学生宿舍4栋007、008', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (426, 2, '物业服务中心', '花都', '18011902709', NULL, '学生宿舍1栋1楼', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (427, 3, '网络中心（海珠校区）', '海珠', '34113702', NULL, '综合楼B2203', NULL, 'http://web.gdei.edu.cn/nic/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (428, 3, '网络中心（花都校区）', '花都', '36967722', NULL, '图书馆220', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (429, 4, '党代表工作室（海珠校区）', '海珠', '34113023', NULL, '综合楼18楼1806', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (430, 4, '党代表工作室（花都校区）', '花都', '36967719', NULL, '图书馆708', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (431, 4, '党政办公室', '海珠', '34113736', NULL, '综合楼15楼', NULL, 'http://web.gdei.edu.cn/xyb/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (432, 5, '监控中心（紧急事务报告）', '花都', '36967733', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (433, 5, '物业安保24小时值班', '花都', '36967733', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (434, 5, '电梯应急救援（海珠校区）', '海珠', '96333', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (435, 5, '电梯应急救援', '花都', '18837490602', '18837490597', NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (436, 5, '医务室（海珠校区）', '海珠', '34113271', NULL, '学生宿舍', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (437, 5, '医务室（花都校区）', '花都', '36967710', NULL, '学生宿舍2栋1楼', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (438, 5, '医保办', '海珠', '34113511', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (439, 6, '水电中心值班处', '海珠', '34113363', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (440, 6, '卡务中心', '花都', '36967715', NULL, '学生宿舍1栋1楼', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (441, 6, '5栋宿舍楼送水', '海珠', '15920841618', NULL, '学生宿舍5栋', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (442, 6, '培训楼招待处', '海珠', '34113500', NULL, '培训楼', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (443, 6, '体育中心', '海珠', '34113643', '34113232', '体育中心', NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (444, 7, '后勤基建处（海珠校区）', '海珠', '34113274', NULL, '综合楼9楼', NULL, 'http://web.gdei.edu.cn/hqc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (445, 7, '后勤基建处（花都校区）', '花都', '36967702', NULL, '图书馆705', NULL, 'http://web.gdei.edu.cn/hqc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (446, 7, '饭堂负责人（海珠校区）', '海珠', '13380052368', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (447, 7, '饭堂负责人（花都校区第一饭堂）', '花都', '13533936083', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (448, 7, '饭堂负责人（花都校区第二饭堂）', '花都', '15697632188', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (449, 7, '饭堂负责人（花都校区第三饭堂）', '花都', '13725408927', NULL, NULL, NULL, NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (450, 7, '图书馆（海珠校区）', '海珠', '34113372', NULL, '图书馆大楼', NULL, 'http://lib.gdei.edu.cn/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (451, 7, '图书馆（花都校区）', '花都', '39697731', NULL, '图书馆3楼大厅', NULL, 'http://lib.gdei.edu.cn/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (452, 7, '教务处（海珠校区）', '海珠', '34113407', '34113249', '综合楼13楼', 'jwc@gdei.edu.cn', 'http://web.gdei.edu.cn/jwc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (453, 7, '教务处（花都校区）', '花都', '36967708', NULL, '图书馆703', NULL, 'http://web.gdei.edu.cn/jwc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (454, 7, '财务处（海珠校区）', '海珠', '34113275', NULL, '综合楼14楼', NULL, 'http://web.gdei.edu.cn/cwc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (455, 7, '财务处（花都校区）', '花都', '36967721', NULL, '学生宿舍3栋1楼', NULL, 'http://web.gdei.edu.cn/cwc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (456, 7, '学生工作部（处）（海珠校区）', '海珠', '34113360', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/xsc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (457, 7, '学生工作部（处）（花都校区）', '花都', '36967703', NULL, '图书馆702', NULL, 'http://web.gdei.edu.cn/xsc/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (458, 7, '团委（海珠校区）', '海珠', '34113325', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/tw/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (459, 7, '团委（花都校区）', '花都', '36967703', NULL, '图书馆702', NULL, 'http://web.gdei.edu.cn/tw/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (460, 8, '招生办公室', '海珠', '34113327', NULL, '综合楼12楼', NULL, 'http://web.gdei.edu.cn/zsb/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (461, 8, '纪检监察处', '海珠', '34113624', NULL, '综合楼18楼', 'jj@gdei.edu.cn', 'http://web.gdei.edu.cn/jwb/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (462, 8, '就业指导中心', '海珠', '34114466', NULL, '综合楼12楼', NULL, 'http://210.38.64.162:9000/job');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (463, 9, '就业创业咨询预约（海珠校区）', '海珠', '34113716', NULL, '综合楼12楼1211室', 'jyzd@gdei.edu.cn', NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (464, 9, '就业创业咨询预约（花都校区）', '花都', '36967716', NULL, '图书馆2楼216室', 'jyzd@gdei.edu.cn', NULL);
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (465, 9, '教育学院', '海珠', '34113297', NULL, NULL, NULL, 'http://web.gdei.edu.cn/jyx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (466, 9, '物理与信息工程系', '海珠', '34113256', NULL, NULL, NULL, 'http://web.gdei.edu.cn/wlx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (467, 9, '生物与食品工程学院', '海珠', '34113257', NULL, NULL, NULL, 'http://web.gdei.edu.cn/swx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (468, 9, '体育学院', '海珠', '34113269', NULL, NULL, NULL, 'http://web.gdei.edu.cn/tyx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (469, 9, '中文系', '花都', '36967743', NULL, NULL, NULL, 'http://web.gdei.edu.cn/zwx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (470, 9, '政法系', '花都', '34113290', '34113397', NULL, NULL, 'http://web.gdei.edu.cn/zfx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (471, 9, '外语系', '花都', '36967750', '34113295', '实验楼509', NULL, 'http://web.gdei.edu.cn/wyx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (472, 9, '数学系', '花都', '34113296', '36967738', NULL, NULL, 'http://web.gdei.edu.cn/sxx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (473, 9, '化学系', '花都', '36967768', '34113456', NULL, NULL, 'http://web.gdei.edu.cn/hxx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (474, 9, '计算机科学系', '花都', '34115714', '36967761', NULL, NULL, 'http://web.gdei.edu.cn/jsjx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (475, 9, '音乐系', '花都', '36967776', '34114436', NULL, NULL, 'http://web.gdei.edu.cn/yyx/');
INSERT INTO `yellow_page` (`id`, `type_code`, `section`, `campus`, `major_phone`, `minor_phone`, `address`, `email`, `website`) VALUES (476, 9, '美术学院', '花都', '36967771', '34113634', NULL, NULL, 'http://web.gdei.edu.cn/msx/');
COMMIT;

-- ----------------------------
-- Table structure for yellow_page_type
-- ----------------------------
DROP TABLE IF EXISTS `yellow_page_type`;
CREATE TABLE `yellow_page_type` (
  `type_code` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of yellow_page_type
-- ----------------------------
BEGIN;
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (1, '倾听与倾诉');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (2, '故障保修');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (3, '网络');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (4, '党务');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (5, '医疗与救援');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (6, '生活与保障');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (7, '职能部门');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (8, '就业创业');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (9, '院系部门');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (10, '举报和申诉');
INSERT INTO `yellow_page_type` (`type_code`, `type_name`) VALUES (11, '紧急求助');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
