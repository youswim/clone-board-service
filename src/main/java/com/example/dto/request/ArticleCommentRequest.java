package com.example.dto.request;

import com.example.dto.ArticleCommentDto;
import com.example.dto.UserAccountDto;

/**
 * A DTO for the {@link com.example.domain.ArticleComment} entity
 */
public record ArticleCommentRequest(Long articleId, String content) {

    public static ArticleCommentRequest of(Long articleId, String content) {
        return new ArticleCommentRequest(articleId, content);
    }

    public ArticleCommentDto toDto(UserAccountDto userAccountDto) {
        return ArticleCommentDto.of(
                articleId,
                userAccountDto,
                content
        );
    }
}