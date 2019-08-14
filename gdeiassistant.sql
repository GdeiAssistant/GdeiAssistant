-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
--
-- Host: localhost    Database: gdeiassistant_log
-- ------------------------------------------------------
-- Server version	8.0.16

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `gdeiassistant_log`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gdeiassistant_log` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `gdeiassistant_log`;

--
-- Table structure for table `charge_log`
--

DROP TABLE IF EXISTS `charge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `charge_log` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '校园卡充值记录编号ID',
  `username` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `amount` int(11) NOT NULL COMMENT '充值金额',
  `time` datetime NOT NULL COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `charge_log`
--

LOCK TABLES `charge_log` WRITE;
/*!40000 ALTER TABLE `charge_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `charge_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `close_log`
--

DROP TABLE IF EXISTS `close_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `close_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '注销账户记录编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户原账户用户名',
  `resetname` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户注销后用户名',
  `time` datetime NOT NULL COMMENT '注销时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `close_log`
--

LOCK TABLES `close_log` WRITE;
/*!40000 ALTER TABLE `close_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `close_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `gdeiassistant_data`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gdeiassistant_data` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `gdeiassistant_data`;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `announcement` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '通知公告ID',
  `title` varchar(50) NOT NULL COMMENT '通知公告标题',
  `content` varchar(250) NOT NULL COMMENT '通知公告内容',
  `publish_time` datetime NOT NULL COMMENT '通知公告发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `electricfees`
--

DROP TABLE IF EXISTS `electricfees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `electricfees` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '电费记录主键ID',
  `year` smallint(4) NOT NULL COMMENT '年份',
  `building_number` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '大楼号',
  `room_number` smallint(4) NOT NULL COMMENT '宿舍号',
  `people_number` tinyint(1) NOT NULL COMMENT '入住人数',
  `department` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '院系',
  `number` bigint(11) NOT NULL COMMENT '学号',
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `used_electric_amount` float NOT NULL COMMENT '用电数额',
  `free_electric_amount` float NOT NULL COMMENT '免费电额',
  `fee_based_electric_amount` float NOT NULL COMMENT '计费电数',
  `electric_price` float NOT NULL COMMENT '电价',
  `total_electric_bill` float NOT NULL COMMENT '总电费',
  `average_electric_bill` float NOT NULL COMMENT '平均电费',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31972 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `electricfees`
--

LOCK TABLES `electricfees` WRITE;
/*!40000 ALTER TABLE `electricfees` DISABLE KEYS */;
/*!40000 ALTER TABLE `electricfees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reading`
--

DROP TABLE IF EXISTS `reading`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `reading` (
  `id` varchar(43) NOT NULL COMMENT '专题阅读信息主键ID',
  `title` varchar(50) NOT NULL COMMENT '专题阅读信息标题',
  `description` varchar(150) NOT NULL COMMENT '专题阅读信息描述',
  `link` varchar(250) NOT NULL COMMENT '专题阅读信息链接',
  `create_time` datetime NOT NULL COMMENT '专题阅读信息创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reading`
--

LOCK TABLES `reading` WRITE;
/*!40000 ALTER TABLE `reading` DISABLE KEYS */;
/*!40000 ALTER TABLE `reading` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yellow_page`
--

DROP TABLE IF EXISTS `yellow_page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `yellow_page` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '黄页信息主键ID',
  `type_code` int(11) NOT NULL COMMENT '类别',
  `section` varchar(25) NOT NULL COMMENT '单位',
  `campus` varchar(5) DEFAULT NULL COMMENT '校区',
  `major_phone` varchar(15) DEFAULT NULL COMMENT '主要电话',
  `minor_phone` varchar(15) DEFAULT NULL COMMENT '次要电话',
  `address` varchar(45) DEFAULT NULL COMMENT '地址',
  `email` varchar(35) DEFAULT NULL COMMENT '电子邮箱',
  `website` varchar(85) DEFAULT NULL COMMENT '网站',
  PRIMARY KEY (`id`),
  KEY `yellowPageTypeForeignKey_idx` (`type_code`),
  CONSTRAINT `yellowPageTypeForeignKey` FOREIGN KEY (`type_code`) REFERENCES `yellow_page_type` (`type_code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=409 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园黄页信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yellow_page`
--

LOCK TABLES `yellow_page` WRITE;
/*!40000 ALTER TABLE `yellow_page` DISABLE KEYS */;
INSERT INTO `yellow_page` VALUES (341,11,'消防报警','国家','119',NULL,NULL,NULL,NULL),(342,11,'医疗急救','国家','120',NULL,NULL,NULL,NULL),(343,11,'交通事故报警','国家','122',NULL,NULL,NULL,NULL),(344,11,'短信报警','国家','12110',NULL,NULL,NULL,NULL),(345,10,'中共中央纪律检查委员会纪检监察机关举报','党政','12388',NULL,'北京市西城区永定门内西街甲2号',NULL,'http://www.12388.gov.cn/'),(346,10,'中共中央组织部用人选拔问题举报','党政','12380',NULL,NULL,NULL,'http://www.12380.gov.cn/'),(347,10,'国家邮政局申诉','国家',NULL,NULL,NULL,NULL,'http://sswz.spb.gov.cn/'),(348,10,'国家工业和信息化部电信用户申诉','国家','12300',NULL,'北京市西城区月坛南街11号电信用户申诉受理中心','accept@chinatcc.gov.cn','http://www.chinatcc.gov.cn:8080/cms/shensus/'),(349,10,'国家教育部统一监督举报','国家','010-66092315',NULL,NULL,'12391@moe.edu.cn','http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html'),(350,10,'国家信访局投诉和建议','国家','010—68015310',NULL,'北京市西城区月坛南街8号',NULL,'http://wsxf.gjxfj.gov.cn/zfp/webroot/index.html'),(351,10,'国家工商总局消费者维权申诉','国家','12315',NULL,NULL,NULL,'http://www.315.gov.cn'),(352,10,'广州市人民政府服务','市政','12345',NULL,NULL,NULL,'http://gz12345.gz.gov.cn/'),(353,1,'心理健康教育与辅导中心（海珠校区）','海珠','34114108',NULL,'田家炳大楼2楼',NULL,NULL),(354,1,'心理健康教育与辅导中心（花都校区）','花都','36967712',NULL,'花都校区实验楼205',NULL,NULL),(355,1,'心灵之约（海珠校区）','海珠','34113456',NULL,'学生宿舍4栋1楼，党员工作站内',NULL,NULL),(356,1,'心灵之约（花都校区）','花都','34113987',NULL,'学生宿舍4栋1楼，党员工作站内',NULL,NULL),(357,2,'宿舍管理科','海珠','34113723',NULL,'学生宿舍4栋007、008',NULL,NULL),(358,2,'物业服务中心','花都','18011902709',NULL,'学生宿舍1栋1楼',NULL,NULL),(359,3,'网络中心（海珠校区）','海珠','34113702',NULL,'综合楼B2203',NULL,'http://web.gdei.edu.cn/nic/'),(360,3,'网络中心（花都校区）','花都','36967722',NULL,'图书馆220',NULL,NULL),(361,4,'党代表工作室（海珠校区）','海珠','34113023',NULL,'综合楼18楼1806',NULL,NULL),(362,4,'党代表工作室（花都校区）','花都','36967719',NULL,'图书馆708',NULL,NULL),(363,4,'党政办公室','海珠','34113736',NULL,'综合楼15楼',NULL,'http://web.gdei.edu.cn/xyb/'),(364,5,'监控中心（紧急事务报告）','花都','36967733',NULL,NULL,NULL,NULL),(365,5,'物业安保24小时值班','花都','36967733',NULL,NULL,NULL,NULL),(366,5,'电梯应急救援（海珠校区）','海珠','96333',NULL,NULL,NULL,NULL),(367,5,'电梯应急救援','花都','18837490602','18837490597',NULL,NULL,NULL),(368,5,'医务室（海珠校区）','海珠','34113271',NULL,'学生宿舍',NULL,NULL),(369,5,'医务室（花都校区）','花都','36967710',NULL,'学生宿舍2栋1楼',NULL,NULL),(370,5,'医保办','海珠','34113511',NULL,NULL,NULL,NULL),(371,6,'水电中心值班处','海珠','34113363',NULL,NULL,NULL,NULL),(372,6,'卡务中心','花都','36967715',NULL,'学生宿舍1栋1楼',NULL,NULL),(373,6,'5栋宿舍楼送水','海珠','15920841618',NULL,'学生宿舍5栋',NULL,NULL),(374,6,'培训楼招待处','海珠','34113500',NULL,'培训楼',NULL,NULL),(375,6,'体育中心','海珠','34113643','34113232','体育中心',NULL,NULL),(376,7,'后勤基建处（海珠校区）','海珠','34113274',NULL,'综合楼9楼',NULL,'http://web.gdei.edu.cn/hqc/'),(377,7,'后勤基建处（花都校区）','花都','36967702',NULL,'图书馆705',NULL,'http://web.gdei.edu.cn/hqc/'),(378,7,'饭堂负责人（海珠校区）','海珠','13380052368',NULL,NULL,NULL,NULL),(379,7,'饭堂负责人（花都校区第一饭堂）','花都','13533936083',NULL,NULL,NULL,NULL),(380,7,'饭堂负责人（花都校区第二饭堂）','花都','15697632188',NULL,NULL,NULL,NULL),(381,7,'饭堂负责人（花都校区第三饭堂）','花都','13725408927',NULL,NULL,NULL,NULL),(382,7,'图书馆（海珠校区）','海珠','34113372',NULL,'图书馆大楼',NULL,'http://lib.gdei.edu.cn/'),(383,7,'图书馆（花都校区）','花都','39697731',NULL,'图书馆3楼大厅',NULL,'http://lib.gdei.edu.cn/'),(384,7,'教务处（海珠校区）','海珠','34113407','34113249','综合楼13楼','jwc@gdei.edu.cn','http://web.gdei.edu.cn/jwc/'),(385,7,'教务处（花都校区）','花都','36967708',NULL,'图书馆703',NULL,'http://web.gdei.edu.cn/jwc/'),(386,7,'财务处（海珠校区）','海珠','34113275',NULL,'综合楼14楼',NULL,'http://web.gdei.edu.cn/cwc/'),(387,7,'财务处（花都校区）','花都','36967721',NULL,'学生宿舍3栋1楼',NULL,'http://web.gdei.edu.cn/cwc/'),(388,7,'学生工作部（处）（海珠校区）','海珠','34113360',NULL,'综合楼12楼',NULL,'http://web.gdei.edu.cn/xsc/'),(389,7,'学生工作部（处）（花都校区）','花都','36967703',NULL,'图书馆702',NULL,'http://web.gdei.edu.cn/xsc/'),(390,7,'团委（海珠校区）','海珠','34113325',NULL,'综合楼12楼',NULL,'http://web.gdei.edu.cn/tw/'),(391,7,'团委（花都校区）','花都','36967703',NULL,'图书馆702',NULL,'http://web.gdei.edu.cn/tw/'),(392,8,'招生办公室','海珠','34113327',NULL,'综合楼12楼',NULL,'http://web.gdei.edu.cn/zsb/'),(393,8,'纪检监察处','海珠','34113624',NULL,'综合楼18楼','jj@gdei.edu.cn','http://web.gdei.edu.cn/jwb/'),(394,8,'就业指导中心','海珠','34114466',NULL,'综合楼12楼',NULL,'http://210.38.64.162:9000/job'),(395,9,'就业创业咨询预约（海珠校区）','海珠','34113716',NULL,'综合楼12楼1211室','jyzd@gdei.edu.cn',NULL),(396,9,'就业创业咨询预约（花都校区）','花都','36967716',NULL,'图书馆2楼216室','jyzd@gdei.edu.cn',NULL),(397,9,'教育学院','海珠','34113297',NULL,NULL,NULL,'http://web.gdei.edu.cn/jyx/'),(398,9,'物理与信息工程系','海珠','34113256',NULL,NULL,NULL,'http://web.gdei.edu.cn/wlx/'),(399,9,'生物与食品工程学院','海珠','34113257',NULL,NULL,NULL,'http://web.gdei.edu.cn/swx/'),(400,9,'体育学院','海珠','34113269',NULL,NULL,NULL,'http://web.gdei.edu.cn/tyx/'),(401,9,'中文系','花都','36967743',NULL,NULL,NULL,'http://web.gdei.edu.cn/zwx/'),(402,9,'政法系','花都','34113290','34113397',NULL,NULL,'http://web.gdei.edu.cn/zfx/'),(403,9,'外语系','花都','36967750','34113295','实验楼509',NULL,'http://web.gdei.edu.cn/wyx/'),(404,9,'数学系','花都','34113296','36967738',NULL,NULL,'http://web.gdei.edu.cn/sxx/'),(405,9,'化学系','花都','36967768','34113456',NULL,NULL,'http://web.gdei.edu.cn/hxx/'),(406,9,'计算机科学系','花都','34115714','36967761',NULL,NULL,'http://web.gdei.edu.cn/jsjx/'),(407,9,'音乐系','花都','36967776','34114436',NULL,NULL,'http://web.gdei.edu.cn/yyx/'),(408,9,'美术学院','花都','36967771','34113634',NULL,NULL,'http://web.gdei.edu.cn/msx/');
/*!40000 ALTER TABLE `yellow_page` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yellow_page_type`
--

DROP TABLE IF EXISTS `yellow_page_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `yellow_page_type` (
  `type_code` int(11) NOT NULL AUTO_INCREMENT,
  `type_name` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yellow_page_type`
--

LOCK TABLES `yellow_page_type` WRITE;
/*!40000 ALTER TABLE `yellow_page_type` DISABLE KEYS */;
INSERT INTO `yellow_page_type` VALUES (1,'倾听与倾诉'),(2,'故障保修'),(3,'网络'),(4,'党务'),(5,'医疗与救援'),(6,'生活与保障'),(7,'职能部门'),(8,'就业创业'),(9,'院系部门'),(10,'举报和申诉'),(11,'紧急求助');
/*!40000 ALTER TABLE `yellow_page_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `gdeiassistant`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gdeiassistant` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `gdeiassistant`;

--
-- Table structure for table `access`
--

DROP TABLE IF EXISTS `access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `access` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限表ID',
  `user_group` tinyint(2) NOT NULL COMMENT '用户组ID',
  `name` varchar(15) NOT NULL COMMENT '功能名称',
  PRIMARY KEY (`id`),
  KEY `accessUserGroup_idx` (`user_group`),
  CONSTRAINT `accessUserGroup` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access`
--

LOCK TABLES `access` WRITE;
/*!40000 ALTER TABLE `access` DISABLE KEYS */;
INSERT INTO `access` VALUES (28,1,'grade'),(29,1,'schedule'),(30,1,'cet'),(31,1,'collection'),(32,1,'book'),(33,1,'bill'),(34,1,'card'),(35,1,'evaluate'),(36,1,'spare'),(37,1,'kaoyan'),(38,1,'tice'),(39,1,'news'),(40,1,'data'),(41,1,'ershou'),(42,1,'lostandfound'),(43,1,'secret'),(44,1,'delivery'),(45,1,'calendar'),(46,1,'government'),(47,1,'volunteer'),(48,1,'wechat'),(49,1,'yiban'),(52,2,'grade'),(53,2,'schedule'),(54,2,'cet'),(55,2,'collection'),(56,2,'book'),(57,2,'bill'),(58,2,'card'),(59,2,'evaluate'),(60,2,'spare'),(61,2,'kaoyan'),(62,2,'tice'),(63,2,'news'),(64,2,'data'),(65,2,'ershou'),(66,2,'lostandfound'),(67,2,'secret'),(68,2,'delivery'),(69,2,'calendar'),(70,2,'government'),(71,2,'volunteer'),(72,2,'wechat'),(73,2,'yiban'),(76,3,'grade'),(77,3,'schedule'),(79,3,'collection'),(80,3,'book'),(83,3,'evaluate'),(84,3,'spare'),(85,3,'kaoyan'),(86,3,'tice'),(87,3,'news'),(88,3,'data'),(89,3,'ershou'),(90,3,'lostandfound'),(91,3,'secret'),(92,3,'delivery'),(93,3,'calendar'),(94,3,'government'),(95,3,'volunteer'),(96,3,'wechat'),(97,3,'yiban'),(102,6,'cet'),(103,6,'collection'),(109,6,'kaoyan'),(111,6,'news'),(112,6,'data'),(113,6,'ershou'),(114,6,'lostandfound'),(115,6,'secret'),(116,6,'delivery'),(117,6,'calendar'),(118,6,'government'),(119,6,'volunteer'),(120,6,'wechat'),(121,6,'yiban'),(124,1,'lost'),(125,2,'lost'),(126,1,'charge'),(127,2,'charge'),(128,7,'grade'),(129,7,'schedule'),(130,7,'cet'),(131,7,'collection'),(132,7,'book'),(133,7,'bill'),(134,7,'card'),(135,7,'evaluate'),(136,7,'spare'),(137,7,'kaoyan'),(138,7,'tice'),(139,7,'news'),(140,7,'data'),(141,7,'ershou'),(142,7,'lostandfound'),(143,7,'secret'),(144,7,'delivery'),(145,7,'calendar'),(146,7,'government'),(147,7,'volunteer'),(148,7,'wechat'),(149,7,'yiban'),(150,1,'security'),(151,2,'security'),(152,3,'security'),(153,6,'security'),(154,7,'security');
/*!40000 ALTER TABLE `access` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `authentication`
--

DROP TABLE IF EXISTS `authentication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `authentication` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '实名认证记录编号ID',
  `identity_code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '实名人唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `realname` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `identity_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证号',
  `school_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学号',
  `gmt_create` datetime DEFAULT NULL COMMENT '记录创建时间',
  `gmt_modified` datetime DEFAULT NULL COMMENT '记录更新时间',
  `method` tinyint(1) DEFAULT NULL COMMENT '实名认证方法',
  `is_deleted` tinyint(1) DEFAULT NULL COMMENT '记录标记删除状态',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `authenticationUsername_idx` (`username`),
  CONSTRAINT `authenticationUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `authentication`
--

LOCK TABLES `authentication` WRITE;
/*!40000 ALTER TABLE `authentication` DISABLE KEYS */;
INSERT INTO `authentication` VALUES (1,'6376696892374389d5106ad04a7915ab76a68fb0','gdeiassistant','李*懿','440***********2826','14*******12','2019-06-06 05:41:16','2019-06-06 05:47:27',0,0);
/*!40000 ALTER TABLE `authentication` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cet`
--

DROP TABLE IF EXISTS `cet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `cet` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `number` bigint(15) DEFAULT NULL COMMENT '四六级准考证号',
  PRIMARY KEY (`username`),
  CONSTRAINT `cetUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cet`
--

LOCK TABLES `cet` WRITE;
/*!40000 ALTER TABLE `cet` DISABLE KEYS */;
/*!40000 ALTER TABLE `cet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_message`
--

DROP TABLE IF EXISTS `dating_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `dating_message` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息编号ID',
  `pick_id` int(11) NOT NULL COMMENT '撩一下编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `type` tinyint(1) NOT NULL COMMENT '消息类型',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `datingMessageUsername` (`username`),
  CONSTRAINT `datingMessageUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_message`
--

LOCK TABLES `dating_message` WRITE;
/*!40000 ALTER TABLE `dating_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_pick`
--

DROP TABLE IF EXISTS `dating_pick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `dating_pick` (
  `pick_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '撩一下编号ID',
  `profile_id` int(11) NOT NULL COMMENT '卖室友信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `content` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '撩一下内容',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`pick_id`) USING BTREE,
  KEY `datingPickUsername` (`username`),
  CONSTRAINT `datingPickUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_pick`
--

LOCK TABLES `dating_pick` WRITE;
/*!40000 ALTER TABLE `dating_pick` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_pick` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_profile`
--

DROP TABLE IF EXISTS `dating_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `dating_profile` (
  `profile_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '卖室友信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `kickname` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `area` tinyint(1) NOT NULL COMMENT '地区',
  `grade` tinyint(1) NOT NULL COMMENT '年级',
  `faculty` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '院系',
  `hometown` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '家乡',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交友宣言',
  `qq` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'QQ号',
  `wechat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`profile_id`) USING BTREE,
  KEY `datingProfileUsername` (`username`),
  CONSTRAINT `datingProfileUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_profile`
--

LOCK TABLES `dating_profile` WRITE;
/*!40000 ALTER TABLE `dating_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_order`
--

DROP TABLE IF EXISTS `delivery_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `delivery_order` (
  `order_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_order`
--

LOCK TABLES `delivery_order` WRITE;
/*!40000 ALTER TABLE `delivery_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `delivery_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_trade`
--

DROP TABLE IF EXISTS `delivery_trade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `delivery_trade` (
  `trade_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '交易主键ID',
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `username` varchar(24) NOT NULL COMMENT '接单者用户名',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  PRIMARY KEY (`trade_id`),
  KEY `deliveryOrderId_idx` (`order_id`),
  CONSTRAINT `deliveryOrderId` FOREIGN KEY (`order_id`) REFERENCES `delivery_order` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_trade`
--

LOCK TABLES `delivery_trade` WRITE;
/*!40000 ALTER TABLE `delivery_trade` DISABLE KEYS */;
/*!40000 ALTER TABLE `delivery_trade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ershou`
--

DROP TABLE IF EXISTS `ershou`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ershou` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '二手交易信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品描述',
  `price` float NOT NULL COMMENT '商品价格',
  `location` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易地点',
  `type` tinyint(2) NOT NULL COMMENT '商品类型',
  `qq` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'QQ号',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '手机号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `ershouUsername` (`username`),
  CONSTRAINT `ershouUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ershou`
--

LOCK TABLES `ershou` WRITE;
/*!40000 ALTER TABLE `ershou` DISABLE KEYS */;
/*!40000 ALTER TABLE `ershou` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gender`
--

DROP TABLE IF EXISTS `gender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `gender` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `gender` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '自定义性别',
  PRIMARY KEY (`username`),
  CONSTRAINT `genderUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gender`
--

LOCK TABLES `gender` WRITE;
/*!40000 ALTER TABLE `gender` DISABLE KEYS */;
/*!40000 ALTER TABLE `gender` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `graduation`
--

DROP TABLE IF EXISTS `graduation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `graduation` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `program` tinyint(1) DEFAULT NULL COMMENT '毕业用户账号处理方案',
  PRIMARY KEY (`username`),
  CONSTRAINT `graduationUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `graduation`
--

LOCK TABLES `graduation` WRITE;
/*!40000 ALTER TABLE `graduation` DISABLE KEYS */;
/*!40000 ALTER TABLE `graduation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `introduction`
--

DROP TABLE IF EXISTS `introduction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `introduction` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `introduction` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个人简介内容',
  PRIMARY KEY (`username`),
  CONSTRAINT `introductionUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `introduction`
--

LOCK TABLES `introduction` WRITE;
/*!40000 ALTER TABLE `introduction` DISABLE KEYS */;
INSERT INTO `introduction` VALUES ('gdeiassistant',NULL);
/*!40000 ALTER TABLE `introduction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lostandfound`
--

DROP TABLE IF EXISTS `lostandfound`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `lostandfound` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '失物招领信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品描述',
  `location` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '遗失地点',
  `item_type` tinyint(2) NOT NULL COMMENT '物品类型',
  `lost_type` tinyint(1) NOT NULL COMMENT '寻主/寻物类型',
  `qq` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'QQ号',
  `wechat` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '微信号',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `state` tinyint(1) NOT NULL COMMENT '状态',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `lostandfoundUsername` (`username`),
  CONSTRAINT `lostandfoundUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lostandfound`
--

LOCK TABLES `lostandfound` WRITE;
/*!40000 ALTER TABLE `lostandfound` DISABLE KEYS */;
/*!40000 ALTER TABLE `lostandfound` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `privacy`
--

DROP TABLE IF EXISTS `privacy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `privacy` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `is_gender_open` tinyint(1) NOT NULL COMMENT '公开性别',
  `is_region_open` tinyint(1) NOT NULL COMMENT '公开国家/地区',
  `is_introduction_open` tinyint(1) NOT NULL COMMENT '公开个人简介',
  `is_faculty_open` tinyint(1) NOT NULL COMMENT '公开院系',
  `is_major_open` tinyint(1) NOT NULL COMMENT '公开专业',
  `is_enrollment_open` tinyint(1) NOT NULL COMMENT '公开入学年份',
  `is_profession_open` tinyint(1) NOT NULL COMMENT '公开职业',
  `is_primary_school_open` tinyint(1) NOT NULL COMMENT '公开小学',
  `is_junior_high_school_open` tinyint(1) NOT NULL COMMENT '公开初中',
  `is_high_school_open` tinyint(1) NOT NULL COMMENT '公开高中/职中',
  `is_cache_allow` tinyint(1) NOT NULL COMMENT '使用教务缓存',
  `is_robots_index_allow` tinyint(1) NOT NULL COMMENT '允许搜索引擎收录',
  PRIMARY KEY (`username`) USING BTREE,
  CONSTRAINT `privacyUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `privacy`
--

LOCK TABLES `privacy` WRITE;
/*!40000 ALTER TABLE `privacy` DISABLE KEYS */;
INSERT INTO `privacy` VALUES ('gdeiassistant',1,1,1,1,1,1,1,1,1,1,0,1);
/*!40000 ALTER TABLE `privacy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile`
--

DROP TABLE IF EXISTS `profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `profile` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `kickname` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `degree` tinyint(1) DEFAULT NULL COMMENT '学历',
  `faculty` tinyint(1) DEFAULT NULL COMMENT '院系',
  `profession` tinyint(1) DEFAULT NULL COMMENT '职业',
  `major` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业',
  `region` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家/地区',
  `state` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份/州',
  `city` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `enrollment` int(4) DEFAULT NULL COMMENT '入学年份',
  `primary_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小学',
  `junior_high_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '初中',
  `high_school` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高中/职中',
  PRIMARY KEY (`username`),
  UNIQUE KEY `kickname_UNIQUE` (`kickname`),
  CONSTRAINT `profileUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile`
--

LOCK TABLES `profile` WRITE;
/*!40000 ALTER TABLE `profile` DISABLE KEYS */;
INSERT INTO `profile` VALUES ('gdeiassistant','gdeiassistant',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secret_comment`
--

DROP TABLE IF EXISTS `secret_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `secret_comment` (
  `id` int(10) unsigned NOT NULL COMMENT '校园树洞评论编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `comment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校园树洞评论内容',
  `avatar_theme` tinyint(2) NOT NULL COMMENT '随机头像编号',
  `publish_time` datetime NOT NULL COMMENT '回复时间',
  KEY `CommentId_idx` (`id`),
  KEY `secretCommentUsername` (`username`),
  CONSTRAINT `CommentId` FOREIGN KEY (`id`) REFERENCES `secret_content` (`id`),
  CONSTRAINT `secretCommentUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secret_comment`
--

LOCK TABLES `secret_comment` WRITE;
/*!40000 ALTER TABLE `secret_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `secret_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secret_content`
--

DROP TABLE IF EXISTS `secret_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `secret_content` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '校园树洞信息内容',
  `theme` tinyint(1) NOT NULL COMMENT '校园树洞信息主题',
  `type` tinyint(1) NOT NULL COMMENT '校园树洞信息类型，0为文字树洞信息，1为语音树洞信息',
  `timer` tinyint(1) NOT NULL COMMENT '校园树洞信息是否在24小时后删除，0为否，1为是',
  `state` tinyint(1) NOT NULL COMMENT '校园树洞信息状态',
  `publish_time` datetime NOT NULL COMMENT '校园树洞信息发布时间',
  PRIMARY KEY (`id`),
  KEY `secretContentUsername` (`username`),
  CONSTRAINT `secretContentUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=226 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secret_content`
--

LOCK TABLES `secret_content` WRITE;
/*!40000 ALTER TABLE `secret_content` DISABLE KEYS */;
/*!40000 ALTER TABLE `secret_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secret_like`
--

DROP TABLE IF EXISTS `secret_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `secret_like` (
  `id` int(10) unsigned NOT NULL COMMENT '校园树洞信息编号ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  KEY `LikeId_idx` (`id`),
  KEY `secretLikeUsername` (`username`),
  CONSTRAINT `LikeId` FOREIGN KEY (`id`) REFERENCES `secret_content` (`id`),
  CONSTRAINT `secretLikeUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secret_like`
--

LOCK TABLES `secret_like` WRITE;
/*!40000 ALTER TABLE `secret_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `secret_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '密码',
  `state` tinyint(1) NOT NULL COMMENT '账户状态',
  `user_group` tinyint(2) NOT NULL COMMENT '账户所属的用户组',
  PRIMARY KEY (`username`),
  KEY `userUserGroup_idx` (`user_group`),
  CONSTRAINT `userUserGroup` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('gdeiassistant','gdeiassistant',1,7);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_group`
--

DROP TABLE IF EXISTS `user_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_group` (
  `id` tinyint(2) NOT NULL AUTO_INCREMENT COMMENT '用户组ID',
  `description` varchar(10) NOT NULL COMMENT '用户组描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_group`
--

LOCK TABLES `user_group` WRITE;
/*!40000 ALTER TABLE `user_group` DISABLE KEYS */;
INSERT INTO `user_group` VALUES (1,'管理员'),(2,'学生用户'),(3,'测试用户'),(4,'教师用户'),(5,'客服人员'),(6,'毕业学生'),(7,'体验用户');
/*!40000 ALTER TABLE `user_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wechat_user`
--

DROP TABLE IF EXISTS `wechat_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `wechat_user` (
  `wechat_id` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '微信唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`wechat_id`) USING BTREE,
  KEY `wechatUserUsername` (`username`),
  CONSTRAINT `wechatUserUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wechat_user`
--

LOCK TABLES `wechat_user` WRITE;
/*!40000 ALTER TABLE `wechat_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yiban_user`
--

DROP TABLE IF EXISTS `yiban_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `yiban_user` (
  `user_id` int(11) NOT NULL COMMENT '易班唯一标识ID',
  `username` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `yibanUserUsername` (`username`),
  CONSTRAINT `yibanUserUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yiban_user`
--

LOCK TABLES `yiban_user` WRITE;
/*!40000 ALTER TABLE `yiban_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `yiban_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-07-23  4:03:38
