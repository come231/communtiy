/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_user
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_user` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_user`;

/*Table structure for table `hot_post` */

DROP TABLE IF EXISTS `hot_post`;

CREATE TABLE `hot_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `section_id` bigint DEFAULT NULL COMMENT '板块ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `hot_score` decimal(10,2) DEFAULT '0.00' COMMENT '热度值',
  `view_count` int DEFAULT '0' COMMENT '浏览',
  `like_count` int DEFAULT '0' COMMENT '点赞',
  `comment_count` int DEFAULT '0' COMMENT '评论',
  `share_count` int DEFAULT '0' COMMENT '分享',
  `rank` int DEFAULT '0' COMMENT '排名',
  `period` varchar(20) NOT NULL COMMENT '周期(小时/日/周)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_post_period` (`post_id`,`period`),
  KEY `idx_rank` (`rank`,`period`),
  KEY `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='热榜缓存';

/*Data for the table `hot_post` */

/*Table structure for table `online_user` */

DROP TABLE IF EXISTS `online_user`;

CREATE TABLE `online_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_id` varchar(64) NOT NULL,
  `ip` varchar(50) DEFAULT '',
  `device` varchar(50) DEFAULT '',
  `last_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1' COMMENT '1在线0离线',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在线用户';

/*Data for the table `online_user` */

/*Table structure for table `points_record` */

DROP TABLE IF EXISTS `points_record`;

CREATE TABLE `points_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1签到2发帖3评论4点赞5分享6被关注',
  `points` int NOT NULL COMMENT '积分数',
  `description` varchar(100) DEFAULT '',
  `target_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分记录';

/*Data for the table `points_record` */

/*Table structure for table `system_notice` */

DROP TABLE IF EXISTS `system_notice`;

CREATE TABLE `system_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` tinyint DEFAULT '0' COMMENT '0普通公告 1紧急公告',
  `status` tinyint DEFAULT '1' COMMENT '1发布 0下线',
  `admin_id` bigint NOT NULL COMMENT '发布管理员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告';

/*Data for the table `system_notice` */

insert  into `system_notice`(`id`,`title`,`content`,`type`,`status`,`admin_id`,`create_time`,`update_time`) values (1,'欢迎来到社区平台','感谢您加入我们的社区！请遵守社区规范，文明交流。',0,1,1,'2026-03-25 23:22:46','2026-03-25 23:22:46');

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `nickname` varchar(50) NOT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT '' COMMENT '头像',
  `background` varchar(255) DEFAULT '' COMMENT '主页背景',
  `phone` varchar(20) DEFAULT '' COMMENT '手机号',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `salt` varchar(32) DEFAULT '' COMMENT '盐值',
  `gender` tinyint DEFAULT '0' COMMENT '0未知1男2女',
  `signature` varchar(255) DEFAULT '' COMMENT '个性签名',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint DEFAULT '1' COMMENT '1正常0禁用',
  `role` tinyint DEFAULT '0' COMMENT '0普通1版主2管理员',
  `column_id` bigint DEFAULT NULL COMMENT '个人专栏ID',
  `exp` int DEFAULT '0' COMMENT '经验值',
  `points` int DEFAULT '0' COMMENT '积分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mute_end_time` datetime DEFAULT NULL COMMENT '禁言截止时间，NULL表示未禁言',
  `post_count` int DEFAULT '0' COMMENT '发帖数（冗余统计）',
  `collect_count` int DEFAULT '0' COMMENT '收藏数（冗余统计）',
  `last_checkin` date DEFAULT NULL COMMENT '上次签到日期',
  `continuous_checkin` int DEFAULT '0' COMMENT '连续签到天数',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_phone` (`phone`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

/*Data for the table `user` */

insert  into `user`(`user_id`,`username`,`nickname`,`avatar`,`background`,`phone`,`password`,`salt`,`gender`,`signature`,`birthday`,`status`,`role`,`column_id`,`exp`,`points`,`create_time`,`update_time`,`mute_end_time`,`post_count`,`collect_count`,`last_checkin`,`continuous_checkin`) values (1,'user001','小明','https://api.dicebear.com/7.x/avataaars/svg?seed=user001','','13800138001','e10adc3949ba59abbe56e057f20f883e','',1,'热爱分享技术',NULL,1,0,NULL,100,50,'2026-03-23 23:16:08','2026-03-23 23:16:08',NULL,0,0,NULL,0),(2,'user002','小红','https://api.dicebear.com/7.x/avataaars/svg?seed=user002','','13800138002','e10adc3949ba59abbe56e057f20f883e','',2,'前端开发爱好者',NULL,1,0,NULL,150,80,'2026-03-23 23:16:08','2026-03-23 23:16:08',NULL,0,0,NULL,0),(3,'user003','技术达人','https://api.dicebear.com/7.x/avataaars/svg?seed=user003','','13800138003','e10adc3949ba59abbe56e057f20f883e','',1,'全栈工程师',NULL,1,1,NULL,500,200,'2026-03-23 23:16:08','2026-03-23 23:16:08',NULL,0,0,NULL,0),(4,'user004','设计师小美','https://api.dicebear.com/7.x/avataaars/svg?seed=user004','','13800138004','e10adc3949ba59abbe56e057f20f883e','',2,'UI/UX设计师',NULL,1,0,NULL,200,100,'2026-03-23 23:16:08','2026-03-23 23:16:08',NULL,0,0,NULL,0),(5,'user005','产品经理','https://api.dicebear.com/7.x/avataaars/svg?seed=user005','','13800138005','e10adc3949ba59abbe56e057f20f883e','',1,'专注用户体验',NULL,1,0,NULL,300,150,'2026-03-23 23:16:08','2026-03-23 23:16:08',NULL,0,0,NULL,0),(7,'admin','超级管理员','','','','0192023a7bbd73250516f069df18b500','',0,'',NULL,1,2,NULL,9999,9999,'2026-03-25 23:22:46','2026-03-25 23:22:46',NULL,0,0,NULL,0);

/*Table structure for table `user_block` */

DROP TABLE IF EXISTS `user_block`;

CREATE TABLE `user_block` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `block_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_block` (`user_id`,`block_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拉黑表';

/*Data for the table `user_block` */

insert  into `user_block`(`id`,`user_id`,`block_id`,`create_time`) values (1,1,5,'2026-03-23 23:16:09');

/*Table structure for table `user_checkin` */

DROP TABLE IF EXISTS `user_checkin`;

CREATE TABLE `user_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `date` date NOT NULL COMMENT '签到日期',
  `points` int DEFAULT '0' COMMENT '获得积分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_date` (`user_id`,`date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签到记录';

/*Data for the table `user_checkin` */

/*Table structure for table `user_device` */

DROP TABLE IF EXISTS `user_device`;

CREATE TABLE `user_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `device_id` varchar(64) NOT NULL COMMENT '设备唯一ID',
  `device_type` varchar(20) DEFAULT '' COMMENT '设备类型',
  `device_name` varchar(50) DEFAULT '' COMMENT '设备名称',
  `os` varchar(20) DEFAULT '' COMMENT '操作系统',
  `browser` varchar(30) DEFAULT '' COMMENT '浏览器',
  `ip` varchar(50) DEFAULT '',
  `last_login` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1' COMMENT '1启用0禁用',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户设备';

/*Data for the table `user_device` */

/*Table structure for table `user_follow` */

DROP TABLE IF EXISTS `user_follow`;

CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户',
  `follow_id` bigint NOT NULL COMMENT '关注的人',
  `status` tinyint DEFAULT '1' COMMENT '1关注0取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_follow` (`user_id`,`follow_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注';

/*Data for the table `user_follow` */

insert  into `user_follow`(`id`,`user_id`,`follow_id`,`status`,`create_time`) values (1,1,2,1,'2026-03-23 23:16:09'),(2,1,3,1,'2026-03-23 23:16:09'),(3,2,1,1,'2026-03-23 23:16:09'),(4,2,3,1,'2026-03-23 23:16:09'),(5,3,1,1,'2026-03-23 23:16:09'),(6,3,2,1,'2026-03-23 23:16:09'),(7,3,4,1,'2026-03-23 23:16:09'),(8,4,3,1,'2026-03-23 23:16:09'),(9,5,1,1,'2026-03-23 23:16:09'),(10,5,2,1,'2026-03-23 23:16:09'),(11,5,3,1,'2026-03-23 23:16:09');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
