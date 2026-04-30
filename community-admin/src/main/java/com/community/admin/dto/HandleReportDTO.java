package com.community.admin.dto;

import lombok.Data;

@Data
public class HandleReportDTO {

    private Long reportId;
    private String handleResult;
    /** 处理状态：1 已处理 2 已驳回；不传则默认 1 */
    private Integer status;
}
