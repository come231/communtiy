package com.community.user.controller;

import com.community.common.result.Result;
import com.community.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 供各业务侧 GC 聚合适引用的 file_record.id；须网络内访问（部署侧限制路由）
 */
@RestController
@RequestMapping("/user/internal/gc")
@RequiredArgsConstructor
public class UserGcController {

    private final UserMapper userMapper;

    @GetMapping("/avatar-file-ids")
    public Result<List<Long>> listAvatarFileIds() {
        List<String> s = userMapper.listAvatarFileIdStrings();
        List<Long> out = new ArrayList<>();
        if (s == null) {
            return Result.success(out);
        }
        for (String a : s) {
            if (a == null || a.isEmpty()) {
                continue;
            }
            try {
                out.add(Long.parseLong(a.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return Result.success(out);
    }
}
