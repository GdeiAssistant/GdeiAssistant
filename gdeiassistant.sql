CREATE DATABASE  IF NOT EXISTS `gdeiassistantLogs` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `gdeiassistantLogs`;
-- MySQL dump 10.13  Distrib 5.7.19, for macos10.12 (x86_64)
--
-- Host: localhost    Database: gdeiassistantLogs
-- ------------------------------------------------------
-- Server version	5.7.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chargeLogs`
--

DROP TABLE IF EXISTS `chargeLogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chargeLogs` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(24) CHARACTER SET utf8mb4 NOT NULL,
  `amount` int(11) NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `closeLogs`
--

DROP TABLE IF EXISTS `closeLogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `closeLogs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(24) NOT NULL,
  `resetname` varchar(24) NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-09-27 22:04:22
CREATE DATABASE  IF NOT EXISTS `gdeiassistant` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `gdeiassistant`;
-- MySQL dump 10.13  Distrib 5.7.19, for macos10.12 (x86_64)
--
-- Host: localhost    Database: gdeiassistant
-- ------------------------------------------------------
-- Server version	5.7.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cet`
--

DROP TABLE IF EXISTS `cet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cet` (
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `number` bigint(15) DEFAULT NULL,
  PRIMARY KEY (`username`),
  CONSTRAINT `cetUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `datingMessage`
--

DROP TABLE IF EXISTS `datingMessage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datingMessage` (
  `messageId` int(11) NOT NULL AUTO_INCREMENT,
  `pickId` int(11) NOT NULL,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `type` tinyint(1) NOT NULL,
  `state` tinyint(1) NOT NULL,
  PRIMARY KEY (`messageId`),
  KEY `datingMessageUsername` (`username`),
  CONSTRAINT `datingMessageUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `datingPick`
--

DROP TABLE IF EXISTS `datingPick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datingPick` (
  `pickId` int(11) NOT NULL AUTO_INCREMENT,
  `profileId` int(11) NOT NULL,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `content` varchar(50) NOT NULL,
  `state` tinyint(1) NOT NULL,
  PRIMARY KEY (`pickId`),
  KEY `datingPickUsername` (`username`),
  CONSTRAINT `datingPickUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `datingProfile`
--

DROP TABLE IF EXISTS `datingProfile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datingProfile` (
  `profileId` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `kickname` varchar(15) NOT NULL,
  `area` tinyint(1) NOT NULL,
  `grade` tinyint(1) NOT NULL,
  `faculty` varchar(12) NOT NULL,
  `hometown` varchar(10) NOT NULL,
  `content` varchar(100) NOT NULL,
  `qq` varchar(15) DEFAULT NULL,
  `wechat` varchar(20) DEFAULT NULL,
  `state` tinyint(1) NOT NULL,
  PRIMARY KEY (`profileId`),
  KEY `datingProfileUsername` (`username`),
  CONSTRAINT `datingProfileUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ershou`
--

DROP TABLE IF EXISTS `ershou`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ershou` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `name` varchar(25) NOT NULL,
  `description` varchar(100) NOT NULL,
  `price` float NOT NULL,
  `location` varchar(30) NOT NULL,
  `type` tinyint(2) NOT NULL,
  `qq` varchar(20) CHARACTER SET utf8 NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8 DEFAULT NULL,
  `state` tinyint(1) NOT NULL,
  `publishTime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ershouUsername` (`username`),
  CONSTRAINT `ershouUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gender`
--

DROP TABLE IF EXISTS `gender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gender` (
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `customGender` varchar(50) NOT NULL,
  PRIMARY KEY (`username`),
  CONSTRAINT `genderUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `introduction`
--

DROP TABLE IF EXISTS `introduction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `introduction` (
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `introduction` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`username`),
  CONSTRAINT `introductionUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lostandfound`
--

DROP TABLE IF EXISTS `lostandfound`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lostandfound` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `name` varchar(25) NOT NULL,
  `description` varchar(100) NOT NULL,
  `location` varchar(30) NOT NULL,
  `itemType` tinyint(2) NOT NULL,
  `lostType` tinyint(1) NOT NULL,
  `qq` varchar(20) DEFAULT NULL,
  `wechat` varchar(20) DEFAULT NULL,
  `phone` varchar(11) DEFAULT NULL,
  `state` tinyint(1) NOT NULL,
  `publishTime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lostandfoundUsername` (`username`),
  CONSTRAINT `lostandfoundUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `privacy`
--

DROP TABLE IF EXISTS `privacy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `privacy` (
  `username` varchar(24) NOT NULL,
  `gender` tinyint(1) NOT NULL,
  `genderOrientation` tinyint(1) NOT NULL,
  `region` tinyint(1) NOT NULL,
  `introduction` tinyint(1) NOT NULL,
  `faculty` tinyint(1) NOT NULL,
  `major` tinyint(1) NOT NULL,
  `cache` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`),
  CONSTRAINT `privacyUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profile`
--

DROP TABLE IF EXISTS `profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile` (
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `kickname` varchar(24) NOT NULL,
  `realname` varchar(10) CHARACTER SET utf8 DEFAULT NULL,
  `gender` tinyint(1) DEFAULT NULL,
  `genderOrientation` tinyint(1) DEFAULT NULL,
  `faculty` tinyint(1) DEFAULT NULL,
  `major` varchar(20) DEFAULT NULL,
  `region` varchar(5) CHARACTER SET utf8 DEFAULT NULL,
  `state` varchar(5) CHARACTER SET utf8 DEFAULT NULL,
  `city` varchar(5) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`username`),
  CONSTRAINT `profileUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `secretComment`
--

DROP TABLE IF EXISTS `secretComment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `secretComment` (
  `id` int(10) unsigned NOT NULL,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `comment` varchar(50) NOT NULL,
  `avatarTheme` tinyint(2) NOT NULL,
  `publishTime` datetime NOT NULL,
  KEY `CommentId_idx` (`id`),
  KEY `secretCommentUsername` (`username`),
  CONSTRAINT `CommentId` FOREIGN KEY (`id`) REFERENCES `secretContent` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `secretCommentUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `secretContent`
--

DROP TABLE IF EXISTS `secretContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `secretContent` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(24) CHARACTER SET utf8 NOT NULL,
  `content` varchar(100) NOT NULL,
  `theme` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `secretContentUsername` (`username`),
  CONSTRAINT `secretContentUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `secretLike`
--

DROP TABLE IF EXISTS `secretLike`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `secretLike` (
  `id` int(10) unsigned NOT NULL,
  `username` varchar(24) NOT NULL,
  KEY `LikeId_idx` (`id`),
  KEY `secretLikeUsername` (`username`),
  CONSTRAINT `LikeId` FOREIGN KEY (`id`) REFERENCES `secretContent` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `secretLikeUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `username` varchar(24) NOT NULL,
  `password` varchar(24) DEFAULT NULL,
  `keycode` varchar(64) DEFAULT NULL,
  `number` varchar(24) DEFAULT NULL,
  `state` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wechatUser`
--

DROP TABLE IF EXISTS `wechatUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wechatUser` (
  `wechatId` varchar(30) NOT NULL,
  `username` varchar(24) NOT NULL,
  PRIMARY KEY (`wechatId`),
  KEY `wechatUserUsername` (`username`),
  CONSTRAINT `wechatUserUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `yibanUser`
--

DROP TABLE IF EXISTS `yibanUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yibanUser` (
  `userid` int(11) NOT NULL,
  `username` varchar(24) NOT NULL,
  PRIMARY KEY (`userid`),
  KEY `yibanUserUsername` (`username`),
  CONSTRAINT `yibanUserUsername` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-09-27 22:04:22
