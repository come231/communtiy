-- 若现有库仍使用旧列名 msg_id，而应用已映射为 id，执行本脚本一次。
-- 若表已经是 id 主键，请勿执行。

USE `community_message`;

ALTER TABLE `chat_msg` CHANGE COLUMN `msg_id` `id` bigint NOT NULL AUTO_INCREMENT;
