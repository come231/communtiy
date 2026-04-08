/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_message
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_message` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_message`;

/*Table structure for table `chat_msg` */

DROP TABLE IF EXISTS `chat_msg`;

CREATE TABLE `chat_msg` (
  `msg_id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint DEFAULT '0' COMMENT '0未读 1已读',
  PRIMARY KEY (`msg_id`),
  KEY `idx_from_to` (`from_user_id`,`to_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私信表';

/*Data for the table `chat_msg` */

insert  into `chat_msg`(`msg_id`,`from_user_id`,`to_user_id`,`content`,`status`,`create_time`,`is_read`) values (1,1,2,'你好，看了你的React文章，有个问题想请教',1,'2026-03-23 23:15:53',0),(2,2,1,'你好！请问有什么问题？',1,'2026-03-23 23:15:53',0),(3,1,2,'关于并发渲染的部分，能详细解释一下吗？',1,'2026-03-23 23:15:53',0),(4,2,1,'没问题，并发渲染是React 18的重要特性...',1,'2026-03-23 23:15:53',0),(5,3,4,'你的设计文章写得很好，想请教一些设计系统的问题',1,'2026-03-23 23:15:53',0),(6,4,3,'谢谢！设计系统确实是个很有意思的话题',1,'2026-03-23 23:15:53',0),(7,1,3,'大佬，能请教一些微服务的问题吗？',1,'2026-03-23 23:15:53',0),(8,3,1,'当然可以，有什么问题尽管问',1,'2026-03-23 23:15:53',0),(9,5,1,'你的面试文章很有帮助，感谢分享！',1,'2026-03-23 23:15:53',0),(10,1,5,'不客气，希望对你有帮助',1,'2026-03-23 23:15:53',0);

/*Table structure for table `notice` */

DROP TABLE IF EXISTS `notice`;

CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1点赞2评论3关注',
  `content` varchar(255) NOT NULL,
  `target_id` bigint DEFAULT '0',
  `is_read` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `from_uid` bigint DEFAULT '0' COMMENT '触发通知的用户ID，0表示系统',
  `extra_data` varchar(500) DEFAULT '' COMMENT '附加数据JSON，如帖子标题等',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

/*Data for the table `notice` */

insert  into `notice`(`id`,`user_id`,`type`,`content`,`target_id`,`is_read`,`create_time`,`from_uid`,`extra_data`) values (1,1,1,'小红点赞了你的帖子《Spring Boot 3.0 新特性详解》',2,0,'2026-03-23 23:15:53',0,''),(2,1,1,'技术达人点赞了你的帖子《Spring Boot 3.0 新特性详解》',3,0,'2026-03-23 23:15:53',0,''),(3,1,2,'小红评论了你的帖子：写得很详细，学到了很多！',2,0,'2026-03-23 23:15:53',0,''),(4,1,2,'技术达人评论了你的帖子：Spring Boot 3.0确实带来了很多改进',3,0,'2026-03-23 23:15:53',0,''),(5,1,3,'小红关注了你',2,0,'2026-03-23 23:15:53',0,''),(6,1,3,'技术达人关注了你',3,0,'2026-03-23 23:15:53',0,''),(7,2,1,'小明点赞了你的帖子《React 18 并发特性实践》',1,0,'2026-03-23 23:15:53',0,''),(8,2,1,'技术达人点赞了你的帖子《React 18 并发特性实践》',3,0,'2026-03-23 23:15:53',0,''),(9,2,2,'小明评论了你的帖子：并发特性确实很强大',1,0,'2026-03-23 23:15:53',0,''),(10,2,3,'小明关注了你',1,0,'2026-03-23 23:15:53',0,''),(11,3,1,'小明点赞了你的帖子《微服务架构设计最佳实践》',1,0,'2026-03-23 23:15:53',0,''),(12,3,1,'小红点赞了你的帖子《微服务架构设计最佳实践》',2,0,'2026-03-23 23:15:53',0,''),(13,3,2,'小明评论了你的帖子：微服务治理是个大问题',1,0,'2026-03-23 23:15:53',0,''),(14,3,3,'小明关注了你',1,0,'2026-03-23 23:15:53',0,''),(15,3,3,'小红关注了你',2,0,'2026-03-23 23:15:53',0,''),(16,3,3,'设计师小美关注了你',4,0,'2026-03-23 23:15:53',0,''),(17,4,1,'技术达人点赞了你的帖子《Vue 3 Composition API 入门指南》',3,0,'2026-03-23 23:15:53',0,''),(18,4,2,'小红评论了你的帖子：Composition API确实更好用',2,0,'2026-03-23 23:15:53',0,''),(19,5,1,'小明点赞了你的帖子《如何准备大厂技术面试》',1,0,'2026-03-23 23:15:53',0,''),(20,5,1,'技术达人点赞了你的帖子《如何准备大厂技术面试》',3,0,'2026-03-23 23:15:53',0,'');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
