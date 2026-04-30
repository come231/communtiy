package com.community.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 无引用文件清理：由业务侧汇聚「仍被引用的 file_record.id」后调用 file 服务执行删除（OSS + 元数据）。
 */
@Data
public class GcOrphansRequest {

    /**
     * 全站/跨库仍被引用的 file_record.id
     */
    private List<Long> referencedFileIds = new ArrayList<>();

    /**
     * 仅处理早于「当前时间 - minAgeHours」的 file_record，避免与刚上传未落库引用的竞态
     */
    private Integer minAgeHours = 24;
}
