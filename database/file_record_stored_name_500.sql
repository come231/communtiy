-- 若已有库中 stored_name 仍为 varchar(255) 且路径超长失败，可执行：
ALTER TABLE `file_record`
  MODIFY COLUMN `stored_name` varchar(500) NOT NULL COMMENT '磁盘相对路径 uploads/yyyy/MMdd/文件名';
