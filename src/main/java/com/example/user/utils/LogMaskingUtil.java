package com.example.user.utils;

import java.util.Objects;
import java.util.regex.Pattern;

import java.util.regex.Matcher;

public class LogMaskingUtil {
    // Regex tìm các cặp key=value nhạy cảm (không phân biệt hoa thường)
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(transactionId|account|inDebt|have|time)\\s*[:=]\\s*([^\\s,\\]\\}]+)"
    );

    public static String mask(String input) {
        if (Objects.isNull(input)) return "null";
        Matcher matcher = SENSITIVE_PATTERN.matcher(input);
        // Thay thế giá trị tìm được bằng dấu ?
        return matcher.replaceAll("$1$2?");
    }
}
