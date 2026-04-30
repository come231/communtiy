package com.community.common.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 内置文本风险扫描：在 AI / 词库之外做底线拦截，避免模型误判或调用失败时直接放行。
 * <ul>
 *   <li>minCheckResult=2：明显违法/极端有害内容，对应 {@code checkResult=2} 拦截发帖</li>
 *   <li>minCheckResult=1：暴力教唆、斗殴技巧等，对应转人工审核</li>
 * </ul>
 */
public final class BuiltinContentRiskScanner {

    /** 严重违规：直接拦截 */
    private static final Pattern[] BLOCK_PATTERNS = new Pattern[]{
            Pattern.compile("冰毒|海洛因|可卡因"),
            Pattern.compile("自制\\s*炸药|炸弹.*教程|制作\\s*爆炸物"),
            Pattern.compile("自制\\s*枪支|枪支\\s*图纸"),
            Pattern.compile("拐卖\\s*儿童|儿童\\s*性侵"),
            Pattern.compile("强奸\\s*教程|迷奸"),
            // 教唆利用日常材料/器具制作危险装置、规避安全（公共安全类，直接拦截发帖）
            Pattern.compile("危险物品.{0,24}简易制作|简易制作.{0,20}思路讲解|日常.{0,8}危险物品"),
            Pattern.compile("危险材料.{0,20}组合使用|组合使用.{0,20}危险材料"),
            Pattern.compile("私自改造.{0,16}危险器具|危险器具.{0,12}私自改造"),
            Pattern.compile("违规操作.{0,16}高风险物品"),
            Pattern.compile("简单步骤.{0,28}危险装置|简易.{0,12}危险装置|做出危险装置|制成危险装置"),
            Pattern.compile("不考虑安全隐患.{0,32}(做出|制成|制作|就能)"),
    };

    /** 需人工复核：暴力教唆、斗殴技巧、校园霸凌/排挤教唆等 */
    private static final Pattern[] REVIEW_PATTERNS = new Pattern[]{
            Pattern.compile("街头\\s*打架|约架|聚众\\s*斗殴"),
            Pattern.compile("打架.*技巧|斗殴.*技巧|实战\\s*制敌|快速\\s*制敌|制敌\\s*办法"),
            Pattern.compile("击打\\s*要害|暴力\\s*反击|暴力\\s*解决"),
            Pattern.compile("雇凶|买凶"),
            Pattern.compile("自杀\\s*教程|教\\s*你\\s*自杀"),
            // 校园/社交霸凌、教唆压制他人（模型易误判为「正常」，必须内置兜底）
            Pattern.compile("欺负弱小|霸凌|校园霸凌|校园暴力|校暴"),
            Pattern.compile("肢体威慑|抱团施压|通过排挤"),
            Pattern.compile("强势压制|压制别人|压制他人"),
            Pattern.compile("不需要忍让[^。]{0,40}压制|相处不需要忍让"),
    };

    private BuiltinContentRiskScanner() {
    }

    public static final class ScanOutcome {
        /** 至少应达到的 checkResult：0 正常 1 转人工 2 拦截 */
        public final int minCheckResult;
        public final List<String> matchedRules;

        public ScanOutcome(int minCheckResult, List<String> matchedRules) {
            this.minCheckResult = minCheckResult;
            this.matchedRules = matchedRules;
        }
    }

    public static ScanOutcome scan(String text) {
        if (text == null || text.isBlank()) {
            return new ScanOutcome(0, List.of());
        }
        Set<String> hits = new LinkedHashSet<>();
        int min = 0;
        for (Pattern p : BLOCK_PATTERNS) {
            if (p.matcher(text).find()) {
                min = 2;
                hits.add("严重违规模式");
                break;
            }
        }
        if (min < 2) {
            for (Pattern p : REVIEW_PATTERNS) {
                if (p.matcher(text).find()) {
                    min = Math.max(min, 1);
                    hits.add(shortRuleLabel(p));
                }
            }
        }
        return new ScanOutcome(min, new ArrayList<>(hits));
    }

    private static String shortRuleLabel(Pattern p) {
        String r = p.pattern();
        if (r.length() > 36) {
            return r.substring(0, 33) + "...";
        }
        return r;
    }

    public static String formatHits(List<String> hits) {
        if (hits == null || hits.isEmpty()) {
            return "";
        }
        return String.join("；", hits);
    }
}
