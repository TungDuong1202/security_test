package com.example.user.utils;

import com.example.user.dto.response.ApiResponseEntity;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Utility class hỗ trợ chuyển đổi thông tin phân trang.
 * <p>
 * Class này giúp tách biệt đối tượng {@link Page} của Spring Data JPA
 * ra khỏi đối tượng phản hồi {@link ApiResponseEntity.PageInfo} trả về cho Client.
 */
public final class PageInfoMapper {
    private PageInfoMapper(){}

    /**
     * Chuyển đổi từ đối tượng Page (Spring Data) sang PageInfo (Custom DTO).
     *
     * @param page Đối tượng Page chứa dữ liệu và thông tin phân trang từ Database.
     * @return Đối tượng PageInfo đã được chuẩn hóa để trả về API hoặc null nếu page là null.
     */
    public static ApiResponseEntity.PageInfo from(Page<?> page){
        if(Objects.isNull(page)) return null;
        return ApiResponseEntity.PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
