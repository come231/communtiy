package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_task")
public class AiTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 任务类型: 1写作 2润色 3翻译 4总结 */
    private Integer taskType;

    private String inputContent;

    private String outputContent;

    /** 状态: 0进行中 1完成 2失败 */
    private Integer status;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
