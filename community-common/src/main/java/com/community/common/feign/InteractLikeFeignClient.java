package com.community.common.feign;

import com.community.common.dto.UserLikedPostItemDTO;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 互动服务：点赞相关内部查询（用户资料「喜欢的帖子」等）
 */
@FeignClient(name = "community-interact", contextId = "interactLikeClient")
public interface InteractLikeFeignClient {

    @GetMapping("/likes/internal/user-post-likes")
    Result<PageResult<UserLikedPostItemDTO>> pageUserPostLikes(
            @RequestParam("userId") Long userId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize);
}
