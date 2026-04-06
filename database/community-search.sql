-- community_search 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS `community_search` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `community_search`;

-- 热搜关键词表
DROP TABLE IF EXISTS `search_hot_keyword`;
CREATE TABLE `search_hot_keyword` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(100) NOT NULL COMMENT '关键词',
    `search_count` INT NOT NULL DEFAULT 0 COMMENT '搜索次数',
    `rank` INT DEFAULT 1 COMMENT '排名',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜关键词表';

-- Elasticsearch 索引模板 (post_index)
-- 在ES中执行以下命令创建索引：
/*
PUT /post_index
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "analysis": {
      "analyzer": {
        "ik_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "title": {
        "type": "text",
        "analyzer": "ik_max_word",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "summary": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "authorId": { "type": "long" },
      "authorName": { "type": "keyword" },
      "sectionId": { "type": "long" },
      "sectionName": { "type": "keyword" },
      "status": { "type": "integer" },
      "createTime": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis" },
      "viewCount": { "type": "integer" },
      "likeCount": { "type": "integer" }
    }
  }
}
*/
