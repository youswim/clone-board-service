package com.example.dto;

import java.time.LocalDateTime;
/**
 * A DTO for the {@link com.example.domain.Article} entity
 */
// 게시물 저장 기능에 필요한 dto
public record ArticleDto(LocalDateTime createdAt,
                         String createdBy,
                         String title,
                         String content,
                         String hashtag
) {
    public static ArticleDto of(LocalDateTime createdAt, String createdBy, String title, String content, String hashtag) {
        return new ArticleDto(createdAt, createdBy, title, content, hashtag);
    }
}