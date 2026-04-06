package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.MQConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.community.dto.VoteCreateDTO;
import com.community.community.dto.VoteVO;
import com.community.community.entity.Post;
import com.community.community.entity.Vote;
import com.community.community.entity.VoteOption;
import com.community.community.entity.VoteRecord;
import com.community.community.mapper.PostMapper;
import com.community.community.mapper.VoteMapper;
import com.community.community.mapper.VoteOptionMapper;
import com.community.community.mapper.VoteRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VoteService {

    private final VoteMapper voteMapper;
    private final VoteOptionMapper voteOptionMapper;
    private final VoteRecordMapper voteRecordMapper;
    private final PostMapper postMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @OperationLog(module = "投票", operation = "创建投票")
    public Vote createVote(Long userId, VoteCreateDTO dto) {
        Vote vote = new Vote();
        vote.setUserId(userId);
        vote.setPostId(dto.getPostId());
        vote.setTitle(dto.getTitle());
        vote.setDescription(dto.getDescription());
        vote.setImage(dto.getImage());
        vote.setVoteType(dto.getVoteType());
        vote.setIsAnonymous(dto.getIsAnonymous());
        vote.setIsMulti(dto.getIsMulti());
        vote.setMaxSelect(dto.getMaxSelect());
        vote.setExpireTime(dto.getExpireTime());
        vote.setTotalVotes(0);
        vote.setViewCount(0);
        vote.setStatus(1);
        voteMapper.insert(vote);

        int sort = 1;
        for (String optionContent : dto.getOptions()) {
            VoteOption option = new VoteOption();
            option.setVoteId(vote.getVoteId());
            option.setContent(optionContent);
            option.setImage("");
            option.setVoteCount(0);
            option.setSort(sort++);
            voteOptionMapper.insert(option);
        }

        // 关联到帖子（如果有）
        if (dto.getPostId() != null) {
            Post post = postMapper.selectById(dto.getPostId());
            if (post != null) {
                post.setVoteId(vote.getVoteId());
                postMapper.updateById(post);
            }
        }
        return vote;
    }

    public VoteVO getVoteDetail(Long voteId) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) throw new BusinessException(ResultCode.VOTE_NOT_FOUND);

        // 检查是否过期
        if (vote.getStatus() == 1 && vote.getExpireTime() != null &&
            LocalDateTime.now().isAfter(vote.getExpireTime())) {
            vote.setStatus(0);
            voteMapper.updateById(vote);
        }

        List<VoteOption> options = voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, voteId)
                        .orderByAsc(VoteOption::getSort));

        // 检查当前用户是否已投票
        Long currentUserId = ContextHolder.getUserId();
        boolean hasVoted = false;
        if (currentUserId != null) {
            hasVoted = voteRecordMapper.selectCount(
                    new LambdaQueryWrapper<VoteRecord>()
                            .eq(VoteRecord::getVoteId, voteId)
                            .eq(VoteRecord::getUserId, currentUserId)) > 0;
        }

        VoteVO vo = new VoteVO();
        vo.setVoteId(vote.getVoteId());
        vo.setTitle(vote.getTitle());
        vo.setDescription(vote.getDescription());
        vo.setVoteType(vote.getVoteType());
        vo.setIsAnonymous(vote.getIsAnonymous());
        vo.setIsMulti(vote.getIsMulti());
        vo.setMaxSelect(vote.getMaxSelect());
        vo.setExpireTime(vote.getExpireTime());
        vo.setTotalVotes(vote.getTotalVotes());
        vo.setViewCount(vote.getViewCount());
        vo.setStatus(vote.getStatus());
        vo.setOptions(options);
        vo.setHasVoted(hasVoted);
        return vo;
    }

    @Transactional
    @OperationLog(module = "投票", operation = "参与投票")
    public Map<String, Object> castVote(Long userId, Long voteId, List<Long> optionIds) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) throw new BusinessException(ResultCode.VOTE_NOT_FOUND);
        if (vote.getStatus() != 1) throw new BusinessException(ResultCode.VOTE_EXPIRED);
        if (vote.getExpireTime() != null && LocalDateTime.now().isAfter(vote.getExpireTime())) {
            throw new BusinessException(ResultCode.VOTE_EXPIRED);
        }

        // 检查是否已投票
        long votedCount = voteRecordMapper.selectCount(
                new LambdaQueryWrapper<VoteRecord>()
                        .eq(VoteRecord::getVoteId, voteId)
                        .eq(VoteRecord::getUserId, userId));
        if (votedCount > 0) throw new BusinessException(ResultCode.ALREADY_VOTED);

        // 验证选项数量
        if (vote.getIsMulti() == null || vote.getIsMulti() != 1 && optionIds.size() > 1) {
            throw new BusinessException("该投票为单选");
        }
        if (optionIds.size() > vote.getMaxSelect()) {
            throw new BusinessException("最多选择" + vote.getMaxSelect() + "项");
        }

        for (Long optionId : optionIds) {
            // 插入投票记录
            VoteRecord record = new VoteRecord();
            record.setVoteId(voteId);
            record.setOptionId(optionId);
            record.setUserId(userId);
            record.setIp("");
            voteRecordMapper.insert(record);

            // 更新选项票数
            VoteOption option = voteOptionMapper.selectById(optionId);
            if (option != null && option.getVoteId().equals(voteId)) {
                option.setVoteCount(option.getVoteCount() + 1);
                voteOptionMapper.updateById(option);
            }
        }

        // 更新总投票数
        vote.setTotalVotes(vote.getTotalVotes() + optionIds.size());
        voteMapper.updateById(vote);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalVotes", vote.getTotalVotes());
        return result;
    }
}
