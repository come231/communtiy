package com.community.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.util.ContextHolder;
import com.community.common.result.PageResult;
import com.community.search.dto.PostSearchDTO;
import com.community.search.entity.SearchHotKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final HotKeywordService hotKeywordService;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String POST_INDEX = "post_index";
    private static final int HISTORY_EXPIRE_DAYS = 30;

    public PageResult<PostSearchDTO> searchPosts(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, size, page);
        }
        keyword = keyword.trim();

        // 先尝试ES搜索
        try {
            PageResult<PostSearchDTO> esResult = searchFromEs(keyword, page, size);
            log.warn("ES搜索成功: {}");
            if (esResult != null) {
                return esResult;
            }
        } catch (Exception e) {
            log.warn("ES搜索失败，回退到MySQL模糊查询: {}", e.getMessage());
        }

        // ES不可用时fallback到MySQL
        log.info("使用MySQL fallback搜索: {}", keyword);
        return searchFromMysql(keyword, page, size);
    }

    private PageResult<PostSearchDTO> searchFromEs(String keyword, int pageNum, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest(POST_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "title", "content", "summary")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .fuzziness(Fuzziness.AUTO));

        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<em>").postTags("</em>");
        highlightBuilder.field("content").preTags("<em>").postTags("</em>");
        highlightBuilder.numOfFragments(3);
        highlightBuilder.fragmentSize(150);
        sourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        long totalHits = response.getHits().getTotalHits().value;
        List<PostSearchDTO> results = Arrays.stream(response.getHits().getHits())
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());

        return new PageResult<>(results, totalHits, pageSize, pageNum);
    }

    private PostSearchDTO convertToPostDTO(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();
        PostSearchDTO dto = new PostSearchDTO();
        dto.setId(((Number) source.get("id")).longValue());
        dto.setTitle((String) source.get("title"));
        dto.setContent((String) source.get("content"));
        dto.setSummary((String) source.get("summary"));
        if (source.get("authorId") != null) {
            dto.setAuthorId(((Number) source.get("authorId")).longValue());
        }
        dto.setAuthorName((String) source.get("authorName"));
        if (source.get("sectionId") != null) {
            dto.setSectionId(((Number) source.get("sectionId")).longValue());
        }
        dto.setSectionName((String) source.get("sectionName"));
        if (source.get("status") != null) {
            dto.setStatus(((Number) source.get("status")).intValue());
        }
        if (source.get("viewCount") != null) {
            dto.setViewCount(((Number) source.get("viewCount")).intValue());
        }
        if (source.get("likeCount") != null) {
            dto.setLikeCount(((Number) source.get("likeCount")).intValue());
        }

        // 处理高亮
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        if (highlightFields.containsKey("title")) {
            org.elasticsearch.common.text.Text[] fragments = highlightFields.get("title").getFragments();
            if (fragments.length > 0) {
                dto.setTitle(fragments[0].string());
            }
        }
        if (highlightFields.containsKey("content")) {
            org.elasticsearch.common.text.Text[] fragments = highlightFields.get("content").getFragments();
            if (fragments.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (org.elasticsearch.common.text.Text fragment : fragments) {
                    sb.append(fragment.string()).append("...");
                }
                dto.setContent(sb.toString());
            }
        }
        return dto;
    }

    @SuppressWarnings("unchecked")
    private PageResult<PostSearchDTO> searchFromMysql(String keyword, int pageNum, int pageSize) {
        // MySQL fallback: 返回空结果（实际可扩展为DB查询）
        return new PageResult<>(Collections.emptyList(), 0, pageSize, pageNum);
    }

    public List<SearchHotKeyword> getHotKeywords() {
        Set<ZSetOperations.TypedTuple<String>> redisResults =
                redisTemplate.opsForZSet().reverseRangeWithScores(RedisKeyConstant.SEARCH_HOT_KEYWORDS, 0, 9);

        if (redisResults != null && !redisResults.isEmpty()) {
            List<SearchHotKeyword> keywords = new ArrayList<>();
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : redisResults) {
                SearchHotKeyword kw = new SearchHotKeyword();
                kw.setKeyword(tuple.getValue());
                kw.setSearchCount(tuple.getScore().intValue());
                kw.setRank(rank++);
                keywords.add(kw);
            }
            return keywords;
        }

        // Redis无数据时从数据库获取
        return hotKeywordService.getTopKeywords(10);
    }

    public void saveSearchHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        keyword = keyword.trim();

        String key = RedisKeyConstant.SEARCH_HISTORY_PREFIX + userId;
        double score = System.currentTimeMillis() / 1000.0;
        redisTemplate.opsForZSet().add(key, keyword, score);
        redisTemplate.expire(key, HISTORY_EXPIRE_DAYS, TimeUnit.DAYS);

        hotKeywordService.incrementCount(keyword);
    }

    public List<String> getSearchHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        String key = RedisKeyConstant.SEARCH_HISTORY_PREFIX + userId;
        Set<String> history = redisTemplate.opsForZSet().reverseRange(key, 0, 19);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    public void clearSearchHistory(Long userId) {
        if (userId == null) {
            return;
        }
        String key = RedisKeyConstant.SEARCH_HISTORY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void syncPostToEs(Map<String, Object> postData) {
        try {
            IndexRequest request = new IndexRequest(POST_INDEX);
            request.id(String.valueOf(postData.get("id")));
            request.source(postData);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            log.info("同步帖子到ES成功: postId={}", postData.get("id"));
        } catch (Exception e) {
            log.error("同步帖子到ES失败: postId={}", postData.get("id"), e);
        }
    }

    public void deletePostFromEs(Long postId) {
        try {
            DeleteRequest request = new DeleteRequest(POST_INDEX, String.valueOf(postId));
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            log.info("从ES删除帖子成功: postId={}", postId);
        } catch (Exception e) {
            log.error("从ES删除帖子失败: postId={}", postId, e);
        }
    }
}
