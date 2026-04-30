package com.community.community.dto;

import lombok.Data;

@Data
public class PostMediaVO {
    private String fileId;
    /** 1=image 2=video */
    private Integer mediaType;
    private Integer sortOrder;
    private Long sizeBytes;
    private String downloadPath;
}
