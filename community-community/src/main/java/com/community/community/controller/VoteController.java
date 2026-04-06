package com.community.community.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.community.dto.VoteCreateDTO;
import com.community.community.dto.VoteVO;
import com.community.community.entity.Vote;
import com.community.community.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/votes")
@RequiredArgsConstructor
@Tag(name = "投票管理", description = "帖子投票接口")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    @OperationLog(module = "投票", operation = "创建投票")
    @Operation(summary = "创建投票", description = "在帖子中创建投票")
    public Result<Vote> createVote(@Valid @RequestBody VoteCreateDTO dto) {
        return Result.success(voteService.createVote(ContextHolder.getUserId(), dto));
    }

    @GetMapping("/{voteId}")
    @Operation(summary = "投票详情", description = "获取投票的详细信息和选项")
    public Result<VoteVO> getVoteDetail(@PathVariable Long voteId) {
        return Result.success(voteService.getVoteDetail(voteId));
    }

    @PostMapping("/{voteId}/cast")
    @OperationLog(module = "投票", operation = "参与投票")
    @Operation(summary = "参与投票", description = "对投票进行投票")
    public Result<Map<String, Object>> castVote(
            @PathVariable Long voteId, @RequestBody Map<String, List<Long>> body) {
        return Result.success(voteService.castVote(
                ContextHolder.getUserId(), voteId, body.get("optionIds")));
    }
}
