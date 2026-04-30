package com.community.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 与 database/community_file.sql 中 file_record 表一致：
 * user_id, original_name, stored_name, url, size, mime_type
 * <p>
 * stored_name 存磁盘相对路径（如 uploads/2026/0417/xxx.jpg），供本机读取；url 为对外访问地址。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String originalName;

    @TableField("stored_name")
    private String storedName;

    private String url;

    private Long size;

    private String mimeType;

    private LocalDateTime createTime;
}
