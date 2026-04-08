/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_security
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_security` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_security`;

/*Table structure for table `ip_blacklist` */

DROP TABLE IF EXISTS `ip_blacklist`;

CREATE TABLE `ip_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT 'IP地址',
  `reason` varchar(100) DEFAULT '' COMMENT '封禁原因',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='IP黑名单';

/*Data for the table `ip_blacklist` */

/*Table structure for table `operation_log` */

DROP TABLE IF EXISTS `operation_log`;

CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(50) DEFAULT '',
  `module` varchar(30) NOT NULL COMMENT '模块(用户/帖子/评论/权限)',
  `operation` varchar(30) NOT NULL COMMENT '操作类型',
  `method` varchar(100) DEFAULT '' COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `result` varchar(50) DEFAULT '' COMMENT '返回结果',
  `ip` varchar(50) DEFAULT '',
  `user_agent` varchar(255) DEFAULT '',
  `cost_time` int DEFAULT '0' COMMENT '耗时(ms)',
  `status` tinyint DEFAULT '1' COMMENT '1成功0失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';

/*Data for the table `operation_log` */

/*Table structure for table `rate_limit_record` */

DROP TABLE IF EXISTS `rate_limit_record`;

CREATE TABLE `rate_limit_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `ip` varchar(50) NOT NULL COMMENT 'IP地址',
  `api` varchar(100) NOT NULL COMMENT 'API路径',
  `count` int DEFAULT '1' COMMENT '请求次数',
  `window_start` datetime NOT NULL COMMENT '窗口开始时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_window` (`user_id`,`window_start`),
  KEY `idx_ip_window` (`ip`,`window_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='限流记录';

/*Data for the table `rate_limit_record` */

/*Table structure for table `report` */

DROP TABLE IF EXISTS `report`;

CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL COMMENT '举报人',
  `reported_id` bigint DEFAULT NULL COMMENT '被举报用户',
  `target_type` tinyint NOT NULL COMMENT '0用户1帖子2评论3私信',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `reason` tinyint NOT NULL COMMENT '0垃圾广告1色情低俗2暴力血腥3虚假信息4人身攻击5其他',
  `description` varchar(500) DEFAULT '' COMMENT '详细描述',
  `images` json DEFAULT NULL COMMENT '截图',
  `status` tinyint DEFAULT '0' COMMENT '0待处理1已处理2已忽略',
  `handle_result` varchar(200) DEFAULT '' COMMENT '处理结果',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='举报表';

/*Data for the table `report` */

/*Table structure for table `sensitive_word` */

DROP TABLE IF EXISTS `sensitive_word`;

CREATE TABLE `sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(50) NOT NULL COMMENT '敏感词',
  `status` tinyint DEFAULT '1' COMMENT '0禁用1正常',
  `type` tinyint DEFAULT '0' COMMENT '0违规1广告2政治3暴力4色情',
  `level` tinyint DEFAULT '1' COMMENT '1一般2严重3绝密',
  `replace_word` varchar(20) DEFAULT '***' COMMENT '替换词',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_word` (`word`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='敏感词表';

/*Data for the table `sensitive_word` */

insert  into `sensitive_word`(`id`,`word`,`type`,`level`,`replace_word`,`create_time`) values (1,'垃圾',1,1,'垃圾','2026-03-30 22:40:45'),(2,'作弊',1,1,'作弊','2026-03-30 22:40:45'),(3,'作弊软件',1,2,'作弊软件','2026-03-30 22:40:45'),(4,'假证',1,2,'假证','2026-03-30 22:40:45'),(5,'发票',1,1,'发票','2026-03-30 22:40:45'),(6,'枪支',2,3,'枪支','2026-03-30 22:40:45'),(7,'毒品',2,3,'毒品','2026-03-30 22:40:45'),(8,'赌博',2,2,'赌博','2026-03-30 22:40:45'),(9,'银行卡',3,2,'银行卡','2026-03-30 22:40:45'),(10,'身份证',3,2,'身份证','2026-03-30 22:40:45'),(11,'犀利哥',3,3,'犀利哥','2026-03-30 22:40:45'),(12,'妈祖',3,3,'妈祖','2026-03-30 22:40:45'),(13,'苍井空',4,1,'苍井空','2026-03-30 22:40:45'),(14,'av',4,1,'av','2026-03-30 22:40:45'),(15,'黄色',4,2,'黄色','2026-03-30 22:40:45'),(16,'性感',4,1,'性感','2026-03-30 22:40:45');

/*Table structure for table `sys_config` */

DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) NOT NULL COMMENT '配置值',
    `status` int DEFAULT 1,
  `config_type` varchar(20) DEFAULT 'string' COMMENT '类型',
  `description` varchar(100) DEFAULT '' COMMENT '描述',
    `group_key` varchar(50) DEFAULT '' COMMENT '分组键',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

/*Data for the table `sys_config` */

insert  into `sys_config`(`id`,`config_key`,`config_value`,`config_type`,`description`,`create_time`,`update_time`) values (1,'hot_post_period','24','int','热榜计算周期(小时)','2026-03-30 22:40:45','2026-03-30 22:40:45'),(2,'hot_post_limit','50','int','热榜展示数量','2026-03-30 22:40:45','2026-03-30 22:40:45'),(3,'hot_score_view','1','double','浏览量热度权重','2026-03-30 22:40:45','2026-03-30 22:40:45'),(4,'hot_score_like','5','double','点赞量热度权重','2026-03-30 22:40:45','2026-03-30 22:40:45'),(5,'hot_score_comment','8','double','评论量热度权重','2026-03-30 22:40:45','2026-03-30 22:40:45'),(6,'hot_score_share','10','double','分享量热度权重','2026-03-30 22:40:45','2026-03-30 22:40:45'),(7,'rate_limit_post','10','int','发帖限流次数','2026-03-30 22:40:45','2026-03-30 22:40:45'),(8,'rate_limit_comment','30','int','评论限流次数','2026-03-30 22:40:45','2026-03-30 22:40:45'),(9,'rate_limit_interval','60','int','限流时间窗口(秒)','2026-03-30 22:40:45','2026-03-30 22:40:45'),(10,'checkin_points','5','int','签到获得积分','2026-03-30 22:40:45','2026-03-30 22:40:45'),(11,'post_points','10','int','发帖获得积分','2026-03-30 22:40:45','2026-03-30 22:40:45'),(12,'comment_points','2','int','评论获得积分','2026-03-30 22:40:45','2026-03-30 22:40:45'),(13,'like_points','1','int','点赞获得积分','2026-03-30 22:40:45','2026-03-30 22:40:45'),(14,'follow_points','5','int','被关注获得积分','2026-03-30 22:40:45','2026-03-30 22:40:45'),(15,'ai_summary_enabled','true','boolean','是否启用AI摘要','2026-03-30 22:40:45','2026-03-30 22:40:45'),(16,'ai_review_enabled','true','boolean','是否启用AI审核','2026-03-30 22:40:45','2026-03-30 22:40:45'),(17,'ai_recommend_enabled','true','boolean','是否启用AI推荐','2026-03-30 22:40:45','2026-03-30 22:40:45'),(18,'sensitive_word_filter','true','boolean','是否启用敏感词过滤','2026-03-30 22:40:45','2026-03-30 22:40:45'),(19,'post_review_enabled','false','boolean','发帖是否需要审核','2026-03-30 22:40:45','2026-03-30 22:40:45'),(20,'comment_review_enabled','false','boolean','评论是否需要审核','2026-03-30 22:40:45','2026-03-30 22:40:45');

/*Table structure for table `text_review` */

DROP TABLE IF EXISTS `text_review`;

CREATE TABLE `text_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `type` tinyint NOT NULL COMMENT '0帖子1评论2私信3头像4简介',
  `target_id` bigint DEFAULT NULL,
  `content` text COMMENT '审核内容',
  `result` tinyint NOT NULL COMMENT '0通过1违规2疑似需人工',
  `reject_reason` varchar(100) DEFAULT '',
  `ai_score` decimal(5,2) DEFAULT NULL COMMENT 'AI置信度',
  `keywords` varchar(255) DEFAULT '' COMMENT '命中的敏感词',
  `ip` varchar(50) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_target` (`type`,`target_id`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文本审核记录';

/*Data for the table `text_review` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
