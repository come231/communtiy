package com.community.common.constant;

public class RedisKeyConstant {

    // 用户相关
    public static final String USER_TOKEN_PREFIX = "user:token:";
    public static final String USER_REFRESH_TOKEN_PREFIX = "user:refresh:";
    public static final String USER_BLACKLIST_PREFIX = "user:blacklist:";
    public static final String USER_INFO_PREFIX = "user:info:";
    public static final String USER_ONLINE_PREFIX = "user:online:";
    public static final String USER_CHECKIN_PREFIX = "user:checkin:";

    // 帖子相关
    public static final String POST_VIEW_PREFIX = "post:view:";
    public static final String POST_LIKE_COUNT = "post:like:count:";
    public static final String POST_COLLECT_COUNT = "post:collect:count:";
    public static final String POST_COMMENT_COUNT = "post:comment:count:";

    // 点赞相关
    public static final String USER_LIKE_POST_PREFIX = "user:like:post:";
    public static final String USER_LIKE_COMMENT_PREFIX = "user:like:comment:";
    public static final String USER_COLLECT_POST_PREFIX = "user:collect:post:";

    // 关注相关
    public static final String USER_FOLLOWING_PREFIX = "user:following:";
    public static final String USER_FOLLOWERS_PREFIX = "user:followers:";
    public static final String USER_FOLLOW_COUNT = "user:follow:count:";

    // 话题相关
    public static final String TOPIC_INFO_PREFIX = "topic:info:";
    public static final String USER_FOLLOW_TOPIC_PREFIX = "user:topic:";

    // 搜索相关
    public static final String SEARCH_HISTORY_PREFIX = "search:history:";
    public static final String SEARCH_HOT_KEYWORDS = "search:hot:keywords";

    // AI相关
    public static final String AI_CHAT_HISTORY_PREFIX = "ai:chat:";
    public static final String AI_RATE_LIMIT_PREFIX = "ai:rate:limit:";

    // 通知相关
    public static final String USER_UNREAD_NOTICE = "user:unread:notice:";
    public static final String USER_UNREAD_CHAT = "user:unread:chat:";

    // 限流相关
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    // 敏感词
    public static final String SENSITIVE_WORDS = "sensitive:words:all";

    // 热榜
    public static final String HOT_POST_LIST = "hot:post:list";
    public static final String POST_INFO_PREFIX = "post:info:";
}
