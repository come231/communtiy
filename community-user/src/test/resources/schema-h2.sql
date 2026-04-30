-- H2（MODE=MySQL）下的最小表结构，用于 Mapper 切片测试
-- 物理表名使用 t_user，避免 H2 将 USER 当作关键字；逻辑表名仍为 user（见 UserMapperTestConfiguration 动态表名插件）
-- 字段尽量覆盖实体中的常用列；时间字段由 MetaObjectHandler 填充

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `user_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64),
    `nickname` VARCHAR(64),
    `avatar` VARCHAR(512),
    `background` VARCHAR(512),
    `phone` VARCHAR(32),
    `password` VARCHAR(255),
    `salt` VARCHAR(64),
    `gender` INT,
    `signature` VARCHAR(512),
    `birthday` DATE,
    `status` INT DEFAULT 1,
    `role` INT DEFAULT 0,
    `column_id` BIGINT,
    `exp` INT DEFAULT 0,
    `points` INT DEFAULT 0,
    `create_time` TIMESTAMP,
    `update_time` TIMESTAMP,
    `mute_end_time` TIMESTAMP,
    `post_count` INT DEFAULT 0,
    `collect_count` INT DEFAULT 0,
    `last_checkin` DATE,
    `continuous_checkin` INT DEFAULT 0
);
