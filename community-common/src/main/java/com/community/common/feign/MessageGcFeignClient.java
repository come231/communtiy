package com.community.common.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "community-message", path = "/chat", contextId = "messageGcFeign")
public interface MessageGcFeignClient {

    @GetMapping("/internal/gc/attachment-file-ids")
    Result<List<Long>> listChatAttachmentFileIds();
}
