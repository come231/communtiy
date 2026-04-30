-- =============================================================================
-- 将 community_admin_init.sql 中的 sys_config（主键 id 为 VARCHAR 配置键、无 config_key）
-- 迁移为与 community_admin.sql / Java 实体 SysConfig 一致的结构（bigint id + config_key）
--
-- 执行前请先备份：
--   mysqldump -uroot -p community_admin sys_config > sys_config_backup.sql
--
-- 若已存在列 config_key，请勿执行（已是标准结构）。可先执行：
--   SHOW COLUMNS FROM sys_config LIKE 'config_key';
-- =============================================================================

USE community_admin;

-- 新表（临时名）
DROP TABLE IF EXISTS sys_config_std_new;
CREATE TABLE `sys_config_std_new` (
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

-- 旧表 id（varchar）写入新表 config_key
INSERT INTO sys_config_std_new (config_key, config_value, config_name, remark, status, create_time, update_time)
SELECT
  `id`,
  `config_value`,
  `config_name`,
  `remark`,
  1,
  `create_time`,
  `update_time`
FROM `sys_config`;

DROP TABLE `sys_config`;

RENAME TABLE sys_config_std_new TO sys_config;
