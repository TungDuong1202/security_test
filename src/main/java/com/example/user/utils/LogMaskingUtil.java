package com.example.user.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích hỗ trợ che giấu (Masking) dữ liệu nhạy cảm trong log.
 * <p>
 * Class này sử dụng Regular Expression để quét và thay thế các giá trị nhạy cảm
 * bằng dấu hỏi {@code ?} trước khi ghi xuống file log hoặc console.
 * Mục đích: Đảm bảo an toàn thông tin (PII) và tuân thủ các nguyên tắc bảo mật.
 * </p>
 */
public final class LogMaskingUtil {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)((?:transactionId|account|inDebt|have|amount|\\btime\\b)\\s*[:=]\\s*)(\"[^\"]*\"|[^,\\s\\]}]+)"
    );

    private LogMaskingUtil(){}

    /**
     * Thực hiện che giấu thông tin nhạy cảm trong chuỗi đầu vào.
     *
     * @param input Chuỗi log gốc
     * @return Chuỗi log đã được che giá trị nhạy cảm bằng dấu {@code ?}.
     * Trả về chuỗi "null" nếu input là null.
     *
     * <p><b>Ví dụ:</b></p>
     * <pre>
     * Input:  "User[account=123456789, amount=50000]"
     * Output: "User[account=?, amount=?]"
     *
     * Input:  {"transactionId": "TX123", "time": "12:00"}
     * Output: {"transactionId": ?, "time": ?}
     * </pre>
     */
    public static String mask(String input) {
        if (Objects.isNull(input)) return "null";
        return SENSITIVE_PATTERN.matcher(input).replaceAll("$1?");
    }
}