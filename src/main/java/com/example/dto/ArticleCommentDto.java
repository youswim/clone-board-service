package com.example.dto;

import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.example.domain.ArticleComment} entity
 */

// article 저장에 필요한 dto
public record ArticleCommentDto(LocalDateTime createdAt,
                                String createdBy,
                                LocalDateTime modifiedAt,
                                String modifiedBy,
                                String content
) {
    public static ArticleCommentDto of(LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String content) {
        return new ArticleCommentDto(createdAt, createdBy, modifiedAt, modifiedBy, content);
    }
}