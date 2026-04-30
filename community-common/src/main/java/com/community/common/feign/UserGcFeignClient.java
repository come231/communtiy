package com.community.common.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "community-user", path = "/user", contextId = "userGcFeign")
public interface UserGcFeignClient {

    @GetMapping("/internal/gc/avatar-file-ids")
    Result<List<Long>> listAvatarReferencedFileIds();
}
