/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_interact
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_interact` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_interact`;

/*Table structure for table `collect` */

DROP TABLE IF EXISTS `collect`;

CREATE TABLE `collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `folder_id` bigint DEFAULT NULL COMMENT '所属收藏夹ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_post` (`user_id`,`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

/*Data for the table `collect` */

insert  into `collect`(`id`,`user_id`,`post_id`,`create_time`,`folder_id`) values (1,1,2,'2026-03-23 23:15:27',NULL),(2,1,3,'2026-03-23 23:15:27',NULL),(3,1,5,'2026-03-23 23:15:27',NULL),(4,2,1,'2026-03-23 23:15:27',NULL),(5,2,3,'2026-03-23 23:15:27',NULL),(6,2,4,'2026-03-23 23:15:27',NULL),(7,2,6,'2026-03-23 23:15:27',NULL),(8,3,1,'2026-03-23 23:15:27',NULL),(9,3,2,'2026-03-23 23:15:27',NULL),(10,3,5,'2026-03-23 23:15:27',NULL),(11,3,7,'2026-03-23 23:15:27',NULL),(12,3,8,'2026-03-23 23:15:27',NULL),(13,4,1,'2026-03-23 23:15:27',NULL),(14,4,2,'2026-03-23 23:15:27',NULL),(15,4,3,'2026-03-23 23:15:27',NULL),(16,5,1,'2026-03-23 23:15:27',NULL),(17,5,2,'2026-03-23 23:15:27',NULL),(18,5,3,'2026-03-23 23:15:27',NULL),(19,5,4,'2026-03-23 23:15:27',NULL),(20,5,5,'2026-03-23 23:15:27',NULL);

/*Table structure for table `collect_folder` */

DROP TABLE IF EXISTS `collect_folder`;

CREATE TABLE `collect_folder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(50) NOT NULL DEFAULT '默认收藏夹',
  `is_default` tinyint DEFAULT '0' COMMENT '1默认收藏夹',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏夹';

/*Data for the table `collect_folder` */

/*Table structure for table `like_record` */

DROP TABLE IF EXISTS `like_record`;

CREATE TABLE `like_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '0帖子1评论',
  `target_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_type_target` (`user_id`,`type`,`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录';

/*Data for the table `like_record` */

insert  into `like_record`(`id`,`user_id`,`type`,`target_id`,`create_time`) values (1,1,0,2,'2026-03-23 23:15:27'),(2,1,0,3,'2026-03-23 23:15:27'),(3,1,0,5,'2026-03-23 23:15:27'),(4,2,0,1,'2026-03-23 23:15:27'),(5,2,0,3,'2026-03-23 23:15:27'),(6,2,0,4,'2026-03-23 23:15:27'),(7,3,0,1,'2026-03-23 23:15:27'),(8,3,0,2,'2026-03-23 23:15:27'),(9,3,0,5,'2026-03-23 23:15:27'),(10,4,0,1,'2026-03-23 23:15:27'),(11,4,0,2,'2026-03-23 23:15:27'),(12,4,0,3,'2026-03-23 23:15:27'),(13,5,0,1,'2026-03-23 23:15:27'),(14,5,0,2,'2026-03-23 23:15:27'),(15,5,0,3,'2026-03-23 23:15:27'),(16,5,0,4,'2026-03-23 23:15:27'),(17,1,1,1,'2026-03-23 23:15:27'),(18,1,1,2,'2026-03-23 23:15:27'),(19,2,1,1,'2026-03-23 23:15:27'),(20,2,1,3,'2026-03-23 23:15:27'),(21,3,1,4,'2026-03-23 23:15:27'),(22,3,1,5,'2026-03-23 23:15:27'),(23,4,1,6,'2026-03-23 23:15:27'),(24,5,1,7,'2026-03-23 23:15:27'),(25,5,1,8,'2026-03-23 23:15:27');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
