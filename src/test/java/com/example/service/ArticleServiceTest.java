package com.example.service;

import com.example.domain.Article;
import com.example.domain.UserAccount;
import com.example.domain.type.SearchType;
import com.example.dto.ArticleDto;
import com.example.dto.ArticleWithCommentsDto;
import com.example.dto.UserAccountDto;
import com.example.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    ArticleService sut;

    @Mock
    ArticleRepository articleRepository;

    @DisplayName("검색어 없이 게시물을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenNoSearchParameters_whenSearchingArticles_thenReturnsArticlePage(){
        // given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        // when
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);

        // then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("검색어와 함게 게시글을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticleList() {
        // given
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        // when
        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);

        // then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);

    }

    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticles_thenReturnsArticle() {
        // given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // when
        ArticleWithCommentsDto dto = sut.getArticle(articleId);

        // then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("없는 게시글을 조회하면, 예외를 던진다")
    @Test
    void givenNonExistArticleId_whenSearchingArticle_thenThrowsException(){
        // given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // when
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));

        // then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }



    @DisplayName("게시글 정보를 입력하면, 게시글을 생성한다.")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        // given
        ArticleDto dto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        // when
        sut.saveArticle(dto);

        // then
        then(articleRepository).should().save(any(Article.class));
        // save가 호출 되었는지 확인
    }

    @DisplayName("게시글의 수정 정보를 입력하면, 게시글을 수정한다.")
    @Test
    void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
        // given
        Article article = createArticle();
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willReturn(article);

        // when
        sut.updateArticle(dto);

        // then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", dto.title())
                .hasFieldOrPropertyWithValue("content", dto.content())
                .hasFieldOrPropertyWithValue("hashtag", dto.hashtag());
        then(articleRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing(){
        // given
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        // when
        sut.updateArticle(dto);

        // then
        then(articleRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("게시글 ID를 입력하면, 게시글을 삭제한다.")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeleteArticles() {
        // given
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);
        // 이 코드는 현재 테스트에 아무런 영향을 미치지 않는다.

        // when
        sut.deleteArticle(1L);

        // then
        then(articleRepository).should().deleteById(articleId);
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "#java"
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "Uno",
                LocalDateTime.now(),
                "Uno");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                1L,
                "uno",
                "password",
                "uno@mail.com",
                "Uno",
                "This is memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }

}
