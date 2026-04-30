/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_content
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_content` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_content`;

/*Table structure for table `column_info` */

DROP TABLE IF EXISTS `column_info`;

CREATE TABLE `column_info` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '专栏ID',
  `user_id` bigint NOT NULL COMMENT '作者',
  `name` varchar(50) NOT NULL COMMENT '专栏名称',
  `cover` varchar(255) DEFAULT '' COMMENT '专栏封面',
  `description` varchar(255) DEFAULT '' COMMENT '专栏描述',
  `post_count` int DEFAULT '0' COMMENT '文章数量',
  `follow_count` int DEFAULT '0' COMMENT '关注数量',
  `is_verify` tinyint DEFAULT '0' COMMENT '是否认证',
  `status` tinyint DEFAULT '1' COMMENT '1正常0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`column_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_follow_count` (`follow_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='个人专栏';

/*Data for the table `column_info` */

/*Table structure for table `column_post` */

DROP TABLE IF EXISTS `column_post`;

CREATE TABLE `column_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `column_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_column_post` (`column_id`,`post_id`),
  KEY `idx_column_id` (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专栏文章关联';

/*Data for the table `column_post` */

/*Table structure for table `comment` */

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `parent_id` bigint DEFAULT '0' COMMENT '父评论',
  `reply_uid` bigint DEFAULT '0' COMMENT '回复用户',
  `like_count` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `floor` int DEFAULT '0' COMMENT '楼层号',
  PRIMARY KEY (`comment_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

/*Data for the table `comment` */

insert  into `comment`(`comment_id`,`post_id`,`user_id`,`content`,`parent_id`,`reply_uid`,`like_count`,`status`,`create_time`,`floor`) values (1,1,2,'写得很详细，学到了很多！',0,0,12,1,'2026-03-23 23:15:06',0),(2,1,3,'Spring Boot 3.0确实带来了很多改进',0,0,8,1,'2026-03-23 23:15:06',0),(3,1,4,'期待更多实战案例',1,1,3,1,'2026-03-23 23:15:06',0),(4,2,1,'并发特性确实很强大',0,0,15,1,'2026-03-23 23:15:06',0),(5,2,3,'有性能测试数据吗？',0,0,6,1,'2026-03-23 23:15:06',0),(6,2,4,'我分享了一些测试数据',5,2,4,1,'2026-03-23 23:15:06',0),(7,3,1,'微服务治理是个大问题',0,0,11,1,'2026-03-23 23:15:06',0),(8,3,2,'推荐看看Service Mesh',7,1,7,1,'2026-03-23 23:15:06',0),(9,4,2,'Composition API确实更好用',0,0,9,1,'2026-03-23 23:15:06',0),(10,5,1,'设计系统对团队协作很重要',0,0,13,1,'2026-03-23 23:15:06',0);

/*Table structure for table `post` */

DROP TABLE IF EXISTS `post`;

CREATE TABLE `post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '作者',
  `section_id` bigint NOT NULL COMMENT '板块',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` mediumtext NOT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `video` varchar(255) DEFAULT '' COMMENT '视频',
  `vote_id` bigint DEFAULT NULL COMMENT '关联投票ID',
  `column_id` bigint DEFAULT NULL COMMENT '所属专栏ID',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名',
  `is_public` tinyint DEFAULT '1' COMMENT '是否公开: 1公开 0私密',
  `summary` varchar(255) DEFAULT '' COMMENT 'AI摘要',
  `keywords` varchar(255) DEFAULT '' COMMENT '关键词',
  `topic_ids` varchar(255) DEFAULT '' COMMENT '话题ID',
  `is_top` tinyint DEFAULT '0' COMMENT '置顶',
  `is_essence` tinyint DEFAULT '0' COMMENT '精华',
  `view_count` bigint DEFAULT '0' COMMENT '浏览',
  `like_count` int DEFAULT '0' COMMENT '点赞',
  `comment_count` int DEFAULT '0' COMMENT '评论',
  `collect_count` int DEFAULT '0' COMMENT '收藏',
  `status` tinyint DEFAULT '1' COMMENT '1正常0删除2审核中3打回',
  `reject_reason` varchar(1000) DEFAULT NULL COMMENT '审核打回原因',
  KEY `idx_is_public` (`is_public`),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `share_count` int DEFAULT '0' COMMENT '分享数',
  `tags` varchar(500) DEFAULT '' COMMENT '标签，逗号分隔',
  PRIMARY KEY (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_section_id` (`section_id`),
  KEY `idx_status_create` (`status`,`create_time`),
  KEY `idx_post_section_time` (`section_id`,`create_time` DESC),
  KEY `idx_post_hot` (`status`,`is_top`,`is_essence`,`like_count` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子表';

/*Data for the table `post` */

insert  into `post`(`post_id`,`user_id`,`section_id`,`title`,`content`,`images`,`video`,`vote_id`,`column_id`,`is_anonymous`,`summary`,`keywords`,`topic_ids`,`is_top`,`is_essence`,`view_count`,`like_count`,`comment_count`,`collect_count`,`status`,`create_time`,`update_time`,`share_count`,`tags`) values (1,1,1,'Spring Boot 3.0 新特性详解','Spring Boot 3.0带来了许多令人兴奋的新特性，包括...','[\"https://picsum.photos/800/400?random=11\"]','',NULL,NULL,0,'','','1,2',1,1,1251,89,23,45,1,'2026-03-23 23:15:06','2026-03-25 20:11:05',0,''),(2,2,2,'React 18 并发特性实践','React 18的并发特性让UI响应更加流畅...','[\"https://picsum.photos/800/400?random=12\"]','',NULL,NULL,0,'','','3,4',0,1,980,76,18,32,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(3,3,3,'微服务架构设计最佳实践','在设计微服务架构时，需要考虑服务拆分、通信机制...','[\"https://picsum.photos/800/400?random=13\"]','',NULL,NULL,0,'','','5,6',0,0,756,65,15,28,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(4,1,2,'Vue 3 Composition API 入门指南','Vue 3的Composition API提供了更灵活的代码组织方式...','[\"https://picsum.photos/800/400?random=14\"]','',NULL,NULL,0,'','','3,7',0,0,652,54,12,22,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(5,4,4,'设计系统构建方法论','构建一套完整的设计系统需要考虑色彩、字体、组件...','[\"https://picsum.photos/800/400?random=15\"]','',NULL,NULL,0,'','','8,9',0,1,543,48,10,19,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(6,5,5,'如何准备大厂技术面试','准备技术面试需要系统的复习和实战演练...','[\"https://picsum.photos/800/400?random=16\"]','',NULL,NULL,0,'','','10,11',0,0,892,71,21,35,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(7,2,1,'TypeScript 高级类型体操','TypeScript的类型系统非常强大，可以实现很多高级类型操作...','[\"https://picsum.photos/800/400?random=17\"]','',NULL,NULL,0,'','','2,12',0,0,445,39,8,15,1,'2026-03-23 23:15:06','2026-03-23 23:15:06',0,''),(8,3,3,'Docker 容器化部署实战','使用Docker进行应用容器化部署的完整流程...','[\"https://picsum.photos/800/400?random=18\"]','',NULL,NULL,0,'','','5,13',1,0,1106,82,26,41,1,'2026-03-23 23:15:06','2026-03-25 20:11:16',0,'');

/*Table structure for table `post_draft` */

DROP TABLE IF EXISTS `post_draft`;

CREATE TABLE `post_draft` (
  `draft_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `section_id` bigint DEFAULT NULL,
  `title` varchar(100) DEFAULT '',
  `content` mediumtext,
  `images` json DEFAULT NULL,
  `video` varchar(255) DEFAULT '',
  `topic_ids` varchar(255) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`draft_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子草稿箱';

/*Data for the table `post_draft` */

/*Table structure for table `post_report` */

DROP TABLE IF EXISTS `post_report`;

CREATE TABLE `post_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '被举报帖子ID',
  `user_id` bigint NOT NULL COMMENT '举报人ID',
  `reason` varchar(255) NOT NULL COMMENT '举报类型（可多选）',
  `description` varchar(500) DEFAULT NULL COMMENT '详细描述',
  `status` tinyint DEFAULT '0' COMMENT '0待处理 1已处理 2已忽略',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子举报';

/*Data for the table `post_report` */

/*Table structure for table `post_topic` */

DROP TABLE IF EXISTS `post_topic`;

CREATE TABLE `post_topic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `topic_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_post_topic` (`post_id`,`topic_id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子话题关联';

/*Data for the table `post_topic` */

/*Table structure for table `section` */

DROP TABLE IF EXISTS `section`;

CREATE TABLE `section` (
  `section_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '板块名',
  `cover` varchar(255) DEFAULT '' COMMENT '封面',
  `intro` varchar(255) DEFAULT '' COMMENT '介绍',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `parent_id` bigint DEFAULT '0' COMMENT '父板块ID，0表示一级板块',
  `post_count` int DEFAULT '0' COMMENT '帖子数（冗余统计）',
  `post_perm` tinyint DEFAULT '0' COMMENT '发帖权限：0所有人 1认证用户 2仅管理员',
  `moderator_id` bigint DEFAULT NULL COMMENT '版主用户ID',
  PRIMARY KEY (`section_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='板块';

/*Data for the table `section` */

insert  into `section`(`section_id`,`name`,`cover`,`intro`,`sort`,`status`,`create_time`,`parent_id`,`post_count`,`post_perm`,`moderator_id`) values (1,'技术分享','https://picsum.photos/400/300?random=1','分享最新的技术文章和开发经验',1,1,'2026-03-23 23:15:06',0,0,0,NULL),(2,'前端开发','https://picsum.photos/400/300?random=2','HTML、CSS、JavaScript等前端技术讨论',2,1,'2026-03-23 23:15:06',0,0,0,NULL),(3,'后端开发','https://picsum.photos/400/300?random=3','Java、Python、Go等后端技术交流',3,1,'2026-03-23 23:15:06',0,0,0,NULL),(4,'产品设计','https://picsum.photos/400/300?random=4','UI/UX设计、产品思维分享',4,1,'2026-03-23 23:15:06',0,0,0,NULL),(5,'职场心得','https://picsum.photos/400/300?random=5','职场经验、面试技巧分享',5,1,'2026-03-23 23:15:06',0,0,0,NULL);

/*Table structure for table `topic` */

DROP TABLE IF EXISTS `topic`;

CREATE TABLE `topic` (
  `topic_id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` varchar(50) NOT NULL COMMENT '话题名称',
  `cover` varchar(255) DEFAULT '' COMMENT '话题封面',
  `description` varchar(255) DEFAULT '' COMMENT '话题描述',
  `post_count` int DEFAULT '0' COMMENT '帖子数量',
  `follow_count` int DEFAULT '0' COMMENT '关注数量',
  `is_hot` tinyint DEFAULT '0' COMMENT '是否热门',
  `is_recommend` tinyint DEFAULT '0' COMMENT '是否推荐',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '1正常0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`topic_id`),
  UNIQUE KEY `idx_name` (`name`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='话题表';

/*Data for the table `topic` */

insert  into `topic`(`topic_id`,`name`,`cover`,`description`,`post_count`,`follow_count`,`is_hot`,`is_recommend`,`sort`,`status`,`create_time`) values (1,'热门讨论','','当前最热门的话题',0,0,1,1,1,1,'2026-03-30 22:40:45'),(2,'技术分享','','编程技术相关文章',0,0,1,1,2,1,'2026-03-30 22:40:45'),(3,'生活记录','','日常生活的点滴分享',0,0,1,0,3,1,'2026-03-30 22:40:45'),(4,'问答求助','','有问题来这里寻求帮助',0,0,1,1,4,1,'2026-03-30 22:40:45'),(5,'情感交流','','聊聊感情那些事',0,0,0,0,5,1,'2026-03-30 22:40:45'),(6,'美食推荐','','发现美食分享美食',0,0,0,0,6,1,'2026-03-30 22:40:45'),(7,'旅行日记','','用脚步丈量世界',0,0,0,0,7,1,'2026-03-30 22:40:45'),(8,'运动健身','','健康生活从运动开始',0,0,0,0,8,1,'2026-03-30 22:40:45');

/*Table structure for table `user_column` */

DROP TABLE IF EXISTS `user_column`;

CREATE TABLE `user_column` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `column_id` bigint NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1关注0取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_column` (`user_id`,`column_id`),
  KEY `idx_column_id` (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注专栏';

/*Data for the table `user_column` */

/*Table structure for table `user_topic` */

DROP TABLE IF EXISTS `user_topic`;

CREATE TABLE `user_topic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `topic_id` bigint NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1关注0取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_topic` (`user_id`,`topic_id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注话题';

/*Data for the table `user_topic` */

/*Table structure for table `vote` */

DROP TABLE IF EXISTS `vote`;

CREATE TABLE `vote` (
  `vote_id` bigint NOT NULL AUTO_INCREMENT COMMENT '投票ID',
  `user_id` bigint NOT NULL COMMENT '发起人',
  `post_id` bigint DEFAULT NULL COMMENT '关联帖子',
  `title` varchar(100) NOT NULL COMMENT '投票标题',
  `description` varchar(500) DEFAULT '' COMMENT '投票描述',
  `image` varchar(255) DEFAULT '' COMMENT '封面图',
  `vote_type` tinyint DEFAULT '0' COMMENT '0单选1多选',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名',
  `is_multi` tinyint DEFAULT '0' COMMENT '是否多人投票',
  `max_select` int DEFAULT '1' COMMENT '最大可选数',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `total_votes` int DEFAULT '0' COMMENT '总投票数',
  `view_count` int DEFAULT '0' COMMENT '浏览数',
  `status` tinyint DEFAULT '1' COMMENT '1进行中0已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vote_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票表';

/*Data for the table `vote` */

/*Table structure for table `vote_option` */

DROP TABLE IF EXISTS `vote_option`;

CREATE TABLE `vote_option` (
  `option_id` bigint NOT NULL AUTO_INCREMENT,
  `vote_id` bigint NOT NULL,
  `content` varchar(200) NOT NULL COMMENT '选项内容',
  `image` varchar(255) DEFAULT '' COMMENT '选项图片',
  `vote_count` int DEFAULT '0' COMMENT '票数',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`option_id`),
  KEY `idx_vote_id` (`vote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票选项';

/*Data for the table `vote_option` */

/*Table structure for table `vote_record` */

DROP TABLE IF EXISTS `vote_record`;

CREATE TABLE `vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vote_id` bigint NOT NULL,
  `option_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '投票用户',
  `ip` varchar(50) DEFAULT '' COMMENT 'IP地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_vote_user` (`vote_id`,`user_id`),
  KEY `idx_option_id` (`option_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票记录';

/*Data for the table `vote_record` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
