package com.example.dto.request;

import com.example.dto.ArticleDto;
import com.example.dto.HashtagDto;
import com.example.dto.UserAccountDto;

import java.util.Set;

/**
 * A DTO for the {@link com.example.domain.Article} entity
 */
public record ArticleRequest(
        String title,
        String content) {

    public static ArticleRequest of(String title, String content) {
        return new ArticleRequest(title, content);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto) {
        return toDto(userAccountDto, null);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto, Set<HashtagDto> hashtagDtos) {
        return ArticleDto.of(
                userAccountDto,
                title,
                content,
                hashtagDtos
        );
    }
}