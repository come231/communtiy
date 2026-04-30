package com.community.community.mapper;

import com.community.community.dto.PostFileFieldRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 与 {@code expand_post_comment_chat_media_and_export.sql} 中表名一致；未建表时由收集器 try/catch 忽略
 */
@Mapper
public interface FileReferenceScanMapper {

    @Select("SELECT pm.file_id FROM post_media pm INNER JOIN post p ON p.post_id = pm.post_id AND p.status = 1")
    List<Long> listPostMediaFileIds();

    @Select("SELECT cm.file_id FROM comment_media cm " +
            "INNER JOIN comment c ON c.comment_id = cm.comment_id AND c.status = 1 " +
            "INNER JOIN post p ON c.post_id = p.post_id AND p.status = 1")
    List<Long> listCommentMediaFileIds();

    @Select("SELECT file_id FROM post_document_export WHERE status = 1")
    List<Long> listPostExportFileIds();

    @Select("SELECT images, cover_image AS coverImage, video, content FROM post WHERE status = 1")
    List<PostFileFieldRow> listActivePostFileFields();

    @Select("SELECT cover_file_id FROM post WHERE status = 1 AND cover_file_id IS NOT NULL")
    List<Long> listCoverFileIdColumn();

    @Select("SELECT c.content FROM comment c INNER JOIN post p ON c.post_id = p.post_id AND p.status = 1 WHERE c.status = 1")
    List<String> listActiveCommentContents();
}
