package com.community.community.job;

import com.community.common.dto.GcOrphansRequest;
import com.community.common.dto.GcOrphansResult;
import com.community.common.feign.FileFeignClient;
import com.community.common.result.Result;
import com.community.community.service.ReferencedFileIdCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * 每天凌晨将「全站仍引用的 fileId」与 file 库比对，删无引用且足够旧的 OSS+元数据
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "community.file-gc", name = "enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileGcScheduler {

    private final ReferencedFileIdCollector referencedFileIdCollector;
    private final FileFeignClient fileFeignClient;

    @Value("${community.file-gc.internal-token:}")
    private String internalToken;

    @Value("${community.file-gc.min-age-hours:24}")
    private int minAgeHours;

    @Scheduled(cron = "${community.file-gc.cron:0 0 0 * * ?}")
    public void cleanOrphanFiles() {
        if (!StringUtils.hasText(internalToken)) {
            log.debug("未配置 community.file-gc.internal-token，跳过 file GC");
            return;
        }
        GcOrphansRequest req = new GcOrphansRequest();
        req.setMinAgeHours(minAgeHours);
        Set<Long> refs = referencedFileIdCollector.collectAllReferencedFileIds();
        req.setReferencedFileIds(new ArrayList<>(refs));
        try {
            Result<GcOrphansResult> r = fileFeignClient.garbageCollectOrphans(internalToken, req);
            if (r != null && r.isSuccess() && r.getData() != null) {
                log.info("无引用文件 GC 完成，删除条数(含 OSS/本地): {}", r.getData().getDeletedCount());
            } else {
                log.warn("无引用文件 GC 调用失败: {}", r != null ? r.getMessage() : "null");
            }
        } catch (Exception e) {
            log.error("无引用 file GC 异常", e);
        }
    }
}
