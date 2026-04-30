package com.community.community.service;

import com.community.common.feign.MessageGcFeignClient;
import com.community.common.feign.UserGcFeignClient;
import com.community.common.result.Result;
import com.community.community.dto.PostFileFieldRow;
import com.community.community.mapper.FileReferenceScanMapper;
import com.community.community.util.FileIdExtractUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 从帖子/评论/用户头像/聊天气泡等可解析来源汇聚「仍被引用的 file_record.id」，供 file 服务清理无引用对象。
 * <p>业务约定：编辑仅更新 URL 列表/正文中的引用，不主动删 OSS；本收集器不假设「删帖即清引用」以外的补偿逻辑。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReferencedFileIdCollector {

    private final FileReferenceScanMapper fileReferenceScanMapper;
    private final UserGcFeignClient userGcFeignClient;
    private final MessageGcFeignClient messageGcFeignClient;

    public Set<Long> collectAllReferencedFileIds() {
        Set<Long> s = new HashSet<>();
        addAllSafe("postMedia", fileReferenceScanMapper::listPostMediaFileIds, s);
        addAllSafe("commentMedia", fileReferenceScanMapper::listCommentMediaFileIds, s);
        addAllSafe("postExport", fileReferenceScanMapper::listPostExportFileIds, s);
        addAllSafe("coverFileIdColumn", fileReferenceScanMapper::listCoverFileIdColumn, s);
        for (PostFileFieldRow row : listPostRows()) {
            FileIdExtractUtil.collectFromJsonArrayImages(row.getImages(), s);
            if (row.getCoverImage() != null) {
                FileIdExtractUtil.collectFromText(row.getCoverImage(), s);
                FileIdExtractUtil.tryParsePlainLong(row.getCoverImage(), s);
            }
            if (row.getVideo() != null) {
                FileIdExtractUtil.collectFromText(row.getVideo(), s);
                FileIdExtractUtil.tryParsePlainLong(row.getVideo(), s);
            }
            FileIdExtractUtil.collectFromText(row.getContent(), s);
        }
        for (String c : listCommentContents()) {
            FileIdExtractUtil.collectFromText(c, s);
        }
        mergeUserAvatars(s);
        mergeChatAttachments(s);
        return s;
    }

    private void mergeUserAvatars(Set<Long> s) {
        try {
            Result<List<Long>> r = userGcFeignClient.listAvatarReferencedFileIds();
            if (r != null && r.isSuccess() && !CollectionUtils.isEmpty(r.getData())) {
                s.addAll(r.getData());
            }
        } catch (Exception e) {
            log.warn("GC: 拉取用户头像 fileId 失败: {}", e.getMessage());
        }
    }

    private void mergeChatAttachments(Set<Long> s) {
        try {
            Result<List<Long>> r = messageGcFeignClient.listChatAttachmentFileIds();
            if (r != null && r.isSuccess() && !CollectionUtils.isEmpty(r.getData())) {
                s.addAll(r.getData());
            }
        } catch (Exception e) {
            log.warn("GC: 拉取聊天附件 fileId 失败: {}", e.getMessage());
        }
    }

    private List<PostFileFieldRow> listPostRows() {
        try {
            List<PostFileFieldRow> rows = fileReferenceScanMapper.listActivePostFileFields();
            return rows != null ? rows : Collections.emptyList();
        } catch (Exception e) {
            log.warn("GC: listActivePostFileFields 忽略(缺表/列?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> listCommentContents() {
        try {
            List<String> rows = fileReferenceScanMapper.listActiveCommentContents();
            return rows != null ? rows : Collections.emptyList();
        } catch (Exception e) {
            log.warn("GC: listActiveCommentContents 忽略: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void addAllSafe(String name, java.util.function.Supplier<List<Long>> sup, Set<Long> out) {
        try {
            List<Long> list = sup.get();
            if (list != null) {
                for (Long x : list) {
                    FileIdExtractUtil.addIfValid(x, out);
                }
            }
        } catch (Exception e) {
            log.warn("GC[{}] 忽略: {}", name, e.getMessage());
        }
    }
}
