package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.entity.SensitiveWord;
import com.community.admin.service.SensitiveWordService;
import com.community.admin.vo.SensitiveFilterRecordVO;
import com.community.admin.vo.SensitiveWordStatsVO;
import com.community.admin.vo.SensitiveWordVO;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "敏感词管理", description = "敏感词管理接口")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping("/sensitive-words")
    @Operation(summary = "敏感词列表", description = "分页获取敏感词列表")
    public Result<IPage<SensitiveWordVO>> getSensitiveWords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "级别") @RequestParam(required = false) Integer level,
            @Parameter(description = "分类") @RequestParam(required = false) Integer type) {
        return Result.success(sensitiveWordService.getSensitiveWords(page, size, keyword, level, type));
    }

    @PostMapping("/sensitive-word")
    @Operation(summary = "添加敏感词", description = "添加新的敏感词")
    public Result<SensitiveWord> addSensitiveWord(@RequestBody Map<String, Object> params) {
        String word = (String) params.get("word");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : 1;
        Integer type = params.get("type") != null ? ((Number) params.get("type")).intValue() : 0;
        String replacement = (String) params.get("replacement");
        SensitiveWord result = sensitiveWordService.addSensitiveWord(word, level, type, replacement);
        return Result.success(result);
    }

    @PutMapping("/sensitive-word/{id}")
    @Operation(summary = "更新敏感词", description = "更新敏感词信息")
    public Result<Void> updateSensitiveWord(@PathVariable Long id,
                                            @RequestBody Map<String, Object> params) {
        String word = (String) params.get("word");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : null;
        Integer type = params.get("type") != null ? ((Number) params.get("type")).intValue() : null;
        Integer status = params.get("status") != null ? ((Number) params.get("status")).intValue() : null;
        String replacement = (String) params.get("replacement");
        sensitiveWordService.updateSensitiveWord(id, word, level, type, replacement, status);
        return Result.success();
    }

    @DeleteMapping("/sensitive-word/{id}")
    @Operation(summary = "删除敏感词", description = "删除敏感词")
    public Result<Void> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return Result.success();
    }

    @PostMapping("/sensitive-words/import")
    @Operation(summary = "批量导入", description = "批量导入敏感词")
    public Result<Void> importSensitiveWords(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) params.get("words");
        int count = sensitiveWordService.importSensitiveWords(words);
        return Result.success("成功导入" + count + "个敏感词", null);
    }

    @GetMapping("/sensitive-words/stats")
    @Operation(summary = "敏感词统计", description = "查询敏感词数量与拦截次数统计")
    public Result<SensitiveWordStatsVO> stats() {
        return Result.success(sensitiveWordService.getStats());
    }

    @GetMapping("/sensitive-words/records")
    @Operation(summary = "过滤记录表", description = "分页查询敏感词过滤记录")
    public Result<PageResult<SensitiveFilterRecordVO>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return Result.success(sensitiveWordService.getFilterRecords(page, size, keyword, userId));
    }
}
