package com.example.dto;

/**
 * A DTO for the {@link com.example.domain.Article} entity
 */
// 업데이트 기능에 필요한 데이터를 담고 움직이는 Dto
public record ArticleUpdateDto(String title,
                               String content,
                               String hashtag
) {
    public static ArticleUpdateDto of(String title, String content, String hashtag) {
        return new ArticleUpdateDto(title, content, hashtag);
    }
}