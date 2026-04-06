package com.community.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    /**
     * 文件类型: 1-图片, 2-文档, 3-视频, 4-音频, 5-其他
     */
    private Integer fileType;

    private Long uploaderId;

    private LocalDateTime createTime;
}
