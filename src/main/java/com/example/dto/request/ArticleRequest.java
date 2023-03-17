package com.example.dto.request;

import com.example.dto.ArticleDto;
import com.example.dto.UserAccountDto;

/**
 * A DTO for the {@link com.example.domain.Article} entity
 */
public record ArticleRequest(
        String title,
        String content,
        String hashtag) {

    public static ArticleRequest of(String title, String content, String hashtag) {
        return new ArticleRequest(title, content, hashtag);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto) {
        return ArticleDto.of(
                userAccountDto,
                title,
                content,
                hashtag
        );
    }
}