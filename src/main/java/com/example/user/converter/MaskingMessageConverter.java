package com.example.user.converter;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.example.user.utils.LogMaskingUtil;

/**
 * Bộ chuyển đổi log tùy chỉnh (Custom Logback Converter) dùng để tự động che giấu dữ liệu nhạy cảm.
 * <p>
 * Class này mở rộng {@link MessageConverter} của Logback để can thiệp vào nội dung log
 * ngay tại thời điểm ghi (runtime). Nó hoạt động như một lớp lọc (Filter Layer) cuối cùng.
 * </p>
 */
public class MaskingMessageConverter extends MessageConverter {

    /**
     * Phương thức chuyển đổi chính của Logback.
     * <p>
     * Phương thức này được gọi tự động mỗi khi có một dòng log được ghi nhận.
     * </p>
     *
     * @param event Sự kiện log hiện tại (chứa thông tin về message, thread, logger, level...).
     * @return Chuỗi nội dung message sau khi đã được xử lý che giấu thông tin (Sanitized Message).
     */
    @Override
    public String convert(ILoggingEvent event) {
        return LogMaskingUtil.mask(event.getFormattedMessage());
    }
}
