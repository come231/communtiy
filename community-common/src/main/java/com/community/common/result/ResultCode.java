package com.community.common.result;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),
    ACCOUNT_MUTED(1005, "账号已被禁言"),
    TOKEN_EXPIRED(1006, "令牌已过期"),
    TOKEN_INVALID(1007, "令牌无效"),

    POST_NOT_FOUND(2001, "帖子不存在"),
    POST_DELETED(2002, "帖子已删除"),
    COMMENT_NOT_FOUND(2003, "评论不存在"),
    SECTION_NOT_FOUND(2004, "板块不存在"),
    TOPIC_NOT_FOUND(2005, "话题不存在"),
    VOTE_NOT_FOUND(2006, "投票不存在"),
    VOTE_EXPIRED(2007, "投票已过期"),
    ALREADY_LIKED(2008, "已点赞"),
    ALREADY_COLLECTED(2009, "已收藏"),
    ALREADY_FOLLOWED(2010, "已关注"),
    ALREADY_VOTED(2011, "已投票"),

    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_TYPE_NOT_ALLOWED(3003, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(3004, "文件大小超限"),

    AI_SERVICE_ERROR(4001, "AI服务异常"),
    AI_RATE_LIMIT(4002, "AI调用次数已达上限"),

    SENSITIVE_WORD_FOUND(5001, "内容包含敏感词"),
    CONTENT_REVIEW_FAILED(5002, "内容审核未通过");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
