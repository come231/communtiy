package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.entity.SensitiveWord;
import com.community.admin.service.SensitiveWordService;
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
    public Result<IPage<SensitiveWord>> getSensitiveWords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(sensitiveWordService.getSensitiveWords(page, size, keyword));
    }

    @PostMapping("/sensitive-word")
    @Operation(summary = "添加敏感词", description = "添加新的敏感词")
    public Result<SensitiveWord> addSensitiveWord(@RequestBody Map<String, Object> params) {
        String word = (String) params.get("word");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : 1;
        SensitiveWord result = sensitiveWordService.addSensitiveWord(word, level);
        return Result.success(result);
    }

    @PutMapping("/sensitive-word/{id}")
    @Operation(summary = "更新敏感词", description = "更新敏感词信息")
    public Result<Void> updateSensitiveWord(@PathVariable Long id,
                                            @RequestBody Map<String, Object> params) {
        String word = (String) params.get("word");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : null;
        sensitiveWordService.updateSensitiveWord(id, word, level);
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
}
