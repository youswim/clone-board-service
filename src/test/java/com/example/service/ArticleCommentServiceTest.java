package com.example.service;

import com.example.domain.Article;
import com.example.domain.ArticleComment;
import com.example.domain.Hashtag;
import com.example.domain.UserAccount;
import com.example.dto.ArticleCommentDto;
import com.example.dto.UserAccountDto;
import com.example.repository.ArticleCommentRepository;
import com.example.repository.ArticleRepository;
import com.example.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @InjectMocks
    private ArticleCommentService sut;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleCommentRepository articleCommentRepository;

    @Mock
    private UserAccountRepository userAccountRepository;


    @DisplayName("게시글 ID 로 조회화면, 해당하는 댓글 리스트를 반환한다")
    @Test
    void givenArticleId_whenSearchingArticleComments_thenReturnsArticleComments() {
        // given
        Long articleId = 1L;
        ArticleComment expectedParentComment = createArticleComment(1L, "parent content");
        ArticleComment expectedChildComment = createArticleComment(2L, "child content");
        expectedChildComment.setParentCommentId(expectedParentComment.getId());
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(
                List.of(expectedParentComment, expectedChildComment)
        );

        // when
        List<ArticleCommentDto> actual = sut.searchArticleComment(articleId);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting("id", "articleId", "parentCommentId", "content")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, null, "parent content"),
                        tuple(2L, 1L, 1L, "child content")
                );
    }

    @DisplayName("댓글 정보를 입력하면, 댓글을 저장한다.")
    @Test
    void givenArticleCommentInfo_whenSavingArticleComment_thenSaveArticleComment() {
        // given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        // when
        sut.saveArticleComment(dto);

        // then
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(articleCommentRepository).should(never()).getReferenceById(anyLong());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @DisplayName("댓글 저장을 시도했는데 해당하는 게시글이 없다면, 경고 로그를 찍고 아무것도 안한다.")
    @Test
    void givenNonexistentArticle_whenSavingArticleComment_thenLogSituationAndDoesNothing() {
        // given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

        // when
        sut.saveArticleComment(dto);

        // then
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(articleCommentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("부모 댓글 ID와 댓글 정보를 입력하면, 대댓글을 저장한다.")
    @Test
    void givenParentCommentIdAndArticleCommentInfo_whenSaving_thenSavesChildComment() {
        // given
        Long parentCommentId = 1L;
        ArticleComment parent = createArticleComment(parentCommentId, "댓글");
        ArticleCommentDto child = createArticleCommentDto(parentCommentId, "대댓글");
        given(articleRepository.getReferenceById(child.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(child.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

        // when
        sut.saveArticleComment(child);

        // then
        assertThat(child.parentCommentId()).isNotNull();
        then(articleRepository).should().getReferenceById(child.articleId());
        then(userAccountRepository).should().getReferenceById(child.userAccountDto().userId());
        then(articleCommentRepository).should(never()).save(any(ArticleComment.class));
    }

    @DisplayName("댓글 정보를 입력하면, 댓글을 수정한다.")
    @Test
    void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
        // given
        String oldContent = "content";
        String updatedContent = "댓글";
        ArticleComment articleComment = createArticleComment(1L, oldContent);
        ArticleCommentDto dto = createArticleCommentDto(updatedContent);
        given(articleCommentRepository.getReferenceById(dto.id())).willReturn(articleComment);

        // when
        sut.updateArticleComment(dto);

        // then
        assertThat(articleComment.getContent())
                .isNotEqualTo(oldContent)
                .isEqualTo(updatedContent);
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
    @Test
    void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
        // Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleCommentRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        // When
        sut.updateArticleComment(dto);

        // Then
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("댓글 ID를 입력하면, 댓글을 삭제한다.")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        // Given
        Long articleCommentId = 1L;
        String userId = "uno";
        willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(articleCommentId, userId);

        // When
        sut.deleteArticleComment(articleCommentId, userId);

        // Then
        then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }

    private ArticleCommentDto createArticleCommentDto(String content) { // root 댓글 만들 때 사용
        return createArticleCommentDto(null, content);
    }

    private ArticleCommentDto createArticleCommentDto(Long parentCommentId, String content) { // 자식 댓글 만들기 가능
        return createArticleCommentDto(1L, parentCommentId, content);
    }

    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, String content) { // 자식 댓글, 댓글 id 지정
        return ArticleCommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                content,
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
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

    private ArticleComment createArticleComment(Long id, String content) {
        ArticleComment articleComment = ArticleComment.of(
                createArticle(),
                createUserAccount(),
                content
        );
        ReflectionTestUtils.setField(articleComment, "id", id);

        return articleComment;
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
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );
        ReflectionTestUtils.setField(article, "id", 1L);
        article.addHashtags(Set.of(createHashtag(article)));

        return article;
    }

    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }

}