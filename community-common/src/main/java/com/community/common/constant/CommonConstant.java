package com.community.common.constant;

public class CommonConstant {

    // 默认分页
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    // 用户状态
    public static final int USER_STATUS_NORMAL = 1;
    public static final int USER_STATUS_DISABLED = 0;

    // 帖子状态
    public static final int POST_STATUS_NORMAL = 1;
    public static final int POST_STATUS_DELETED = 0;
    public static final int POST_STATUS_REVIEWING = 2;

    // 评论状态
    public static final int COMMENT_STATUS_NORMAL = 1;
    public static final int COMMENT_STATUS_DELETED = 0;

    // 关注状态
    public static final int FOLLOW_STATUS_FOLLOW = 1;
    public static final int FOLLOW_STATUS_UNFOLLOW = 0;

    // 点赞类型
    public static final int LIKE_TYPE_POST = 0;
    public static final int LIKE_TYPE_COMMENT = 1;

    // 通知类型
    public static final int NOTICE_TYPE_LIKE = 1;
    public static final int NOTICE_TYPE_COMMENT = 2;
    public static final int NOTICE_TYPE_FOLLOW = 3;
    public static final int NOTICE_TYPE_COLLECT = 4;
    public static final int NOTICE_TYPE_SYSTEM = 5;
    public static final int NOTICE_TYPE_REPLY = 6;

    // 举报类型
    public static final int REPORT_TARGET_USER = 0;
    public static final int REPORT_TARGET_POST = 1;
    public static final int REPORT_TARGET_COMMENT = 2;
    public static final int REPORT_TARGET_CHAT = 3;

    // 举报状态
    public static final int REPORT_STATUS_PENDING = 0;
    public static final int REPORT_STATUS_PROCESSED = 1;
    public static final int REPORT_STATUS_IGNORED = 2;

    // 敏感词类型
    public static final int SENSITIVE_TYPE_VIOLATION = 0;
    public static final int SENSITIVE_TYPE_AD = 1;

    // AI任务类型
    public static final String AI_TASK_WRITE = "write";
    public static final String AI_TASK_SUMMARY = "summary";
    public static final String AI_TASK_POLISH = "polish";

    // 请求头
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_TOKEN = "Authorization";
}
