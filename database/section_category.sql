/*
板块分类功能扩展SQL
1. 创建 section_category 表存储板块分类
2. 修改 section 表添加 category_id 字段
*/

-- 创建板块分类表
CREATE TABLE IF NOT EXISTS `section_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '1启用0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `idx_name` (`name`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='板块分类';

-- 插入默认分类数据
INSERT INTO `section_category` (`name`, `sort`, `status`) VALUES
('技术交流', 1, 1),
('生活娱乐', 2, 1),
('学习成长', 3, 1),
('职场发展', 4, 1),
('兴趣爱好', 5, 1)
ON DUPLICATE KEY UPDATE status = 1;

-- 修改section表添加分类字段
ALTER TABLE `section` 
ADD COLUMN IF NOT EXISTS `category_id` bigint DEFAULT NULL COMMENT '板块分类ID',
ADD COLUMN IF NOT EXISTS `category_name` varchar(50) DEFAULT NULL COMMENT '板块分类名称（自定义分类时使用）',
ADD KEY IF NOT EXISTS `idx_category_id` (`category_id`);
