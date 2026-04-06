package com.community.common.constant;

public class MQConstant {

    // Exchange
    public static final String COMMUNITY_EXCHANGE = "community.exchange";
    public static final String AI_EXCHANGE = "ai.exchange";

    // 互动事件 Queue
    public static final String LIKE_QUEUE = "community.like.queue";
    public static final String COLLECT_QUEUE = "community.collect.queue";
    public static final String FOLLOW_QUEUE = "community.follow.queue";

    // 通知 Queue
    public static final String NOTICE_QUEUE = "community.notice.queue";

    // 搜索同步 Queue
    public static final String SEARCH_SYNC_QUEUE = "community.search.sync.queue";

    // AI任务 Queue
    public static final String AI_TASK_QUEUE = "ai.task.queue";
    public static final String AI_SUMMARY_QUEUE = "ai.summary.queue";
    public static final String AI_REVIEW_QUEUE = "ai.review.queue";

    // Routing Key
    public static final String LIKE_ROUTING_KEY = "community.like";
    public static final String COLLECT_ROUTING_KEY = "community.collect";
    public static final String FOLLOW_ROUTING_KEY = "community.follow";
    public static final String NOTICE_ROUTING_KEY = "community.notice";
    public static final String SEARCH_SYNC_ROUTING_KEY = "community.search.sync";
    public static final String AI_TASK_ROUTING_KEY = "ai.task";
    public static final String AI_SUMMARY_ROUTING_KEY = "ai.summary";
    public static final String AI_REVIEW_ROUTING_KEY = "ai.review";
}
