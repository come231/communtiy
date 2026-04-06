/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.28 : Database - community_admin
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`community_admin` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `community_admin`;

/*Table structure for table `sys_admin` */

DROP TABLE IF EXISTS `sys_admin`;

CREATE TABLE `sys_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` int DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `role` varchar(20) DEFAULT 'moderator' COMMENT '角色 admin-超级管理员 moderator-版主',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int DEFAULT '0' COMMENT '登录次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

/*Data for the table `sys_admin` */

insert  into `sys_admin`(`id`,`username`,`password`,`nickname`,`email`,`phone`,`avatar`,`status`,`role`,`last_login_time`,`last_login_ip`,`login_count`,`create_time`,`update_time`,`deleted`) values 
(1,'admin','e10adc3949ba59abbe56e057f20f883e','超级管理员','admin@community.com','13800138000','https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',1,'admin','2026-01-01 00:00:00','127.0.0.1',100,'2026-01-01 00:00:00','2026-01-01 00:00:00',0),
(2,'moderator','e10adc3949ba59abbe56e057f20f883e','版主','mod@community.com','13800138001','https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',1,'moderator','2026-01-01 00:00:00','127.0.0.1',50,'2026-01-01 00:00:00','2026-01-01 00:00:00',0);

/*Table structure for table `sys_admin_log` */

DROP TABLE IF EXISTS `sys_admin_log`;

CREATE TABLE `sys_admin_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '管理员名称',
  `operation` varchar(100) DEFAULT NULL COMMENT '操作类型',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `request_path` varchar(200) DEFAULT NULL COMMENT '请求路径',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `status` int DEFAULT NULL COMMENT '操作状态 0-失败 1-成功',
  `error_msg` text COMMENT '错误信息',
  `duration` bigint DEFAULT NULL COMMENT '执行时长(ms)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

/*Data for the table `sys_admin_log` */

/*Table structure for table `sys_config` */

DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` int DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

/*Data for the table `sys_config` */

insert  into `sys_config`(`id`,`config_key`,`config_value`,`config_name`,`remark`,`status`,`create_time`,`update_time`) values 
(1,'site_name','社区交流平台','网站名称','网站显示名称',1,'2026-01-01 00:00:00','2026-01-01 00:00:00'),
(2,'site_domain','http://localhost:3000','网站域名','网站访问域名',1,'2026-01-01 00:00:00','2026-01-01 00:00:00'),
(3,'allow_register','true','允许注册','是否允许用户注册',1,'2026-01-01 00:00:00','2026-01-01 00:00:00'),
(4,'ai_rate_limit','60','AI调用限制','每小时AI调用次数限制',1,'2026-01-01 00:00:00','2026-01-01 00:00:00'),
(5,'post_audit','false','帖子审核','发布帖子是否需要审核',1,'2026-01-01 00:00:00','2026-01-01 00:00:00');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
