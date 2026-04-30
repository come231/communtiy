package com.community.common.feign;

import com.community.common.dto.FileInfoDTO;
import com.community.common.dto.GcOrphansRequest;
import com.community.common.dto.GcOrphansResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "community-file", path = "/file", contextId = "fileFeign")
public interface FileFeignClient {

    @PostMapping("/infos")
    Result<List<FileInfoDTO>> listInfos(@RequestBody List<Long> fileIds);

    @org.springframework.web.bind.annotation.GetMapping("/info/{id}")
    Result<FileInfoDTO> getInfo(@PathVariable("id") Long id);

    /**
     * 与 {@link com.community.common.feign.UserGcFeignClient} 等汇聚的已引用 fileId 配合，按创建时间删无引用且足够旧的文件
     */
    @PostMapping("/internal/gc/orphans")
    Result<GcOrphansResult> garbageCollectOrphans(
            @RequestHeader("X-Internal-Token") String token,
            @RequestBody GcOrphansRequest request);
}
