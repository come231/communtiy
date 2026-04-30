package com.community.community.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从正文/URL/图片 JSON 中解析 file_record.id，与上传返回的 <code>/api/file/download/{id}</code> 形式一致
 */
public final class FileIdExtractUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern FILE_DOWNLOAD = Pattern.compile("(?i)(?:/api)?/file/download/(\\d+)");

    private FileIdExtractUtil() {
    }

    public static void collectFromText(String text, Set<Long> out) {
        if (text == null || text.isEmpty() || out == null) {
            return;
        }
        Matcher m = FILE_DOWNLOAD.matcher(text);
        while (m.find()) {
            out.add(Long.parseLong(m.group(1)));
        }
    }

    public static void collectFromJsonArrayImages(String imagesJson, Set<Long> out) {
        if (imagesJson == null || imagesJson.isEmpty() || out == null) {
            return;
        }
        String s = imagesJson.trim();
        if (s.isEmpty() || s.charAt(0) != '[') {
            collectFromText(imagesJson, out);
            return;
        }
        try {
            for (String item : MAPPER.readValue(s, new TypeReference<java.util.List<String>>() { })) {
                if (item == null) {
                    continue;
                }
                collectFromText(item, out);
                tryParsePlainLong(item, out);
            }
        } catch (Exception e) {
            collectFromText(imagesJson, out);
        }
    }

    /**
     * 纯数字 fileId 串（3～18 位，避免与短楼层号混淆）
     */
    public static void tryParsePlainLong(String s, Set<Long> out) {
        if (s == null || out == null) {
            return;
        }
        String t = s.trim();
        if (t.isEmpty() || t.contains("/") || t.toLowerCase().contains("http") || t.contains("oss")) {
            return;
        }
        if (!t.matches("^[0-9]{3,18}$")) {
            return;
        }
        try {
            long v = Long.parseLong(t);
            if (v > 0) {
                out.add(v);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public static void addIfValid(Long fileId, Set<Long> out) {
        if (fileId == null || fileId <= 0 || out == null) {
            return;
        }
        out.add(fileId);
    }
}
