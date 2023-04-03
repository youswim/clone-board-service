package com.example.repository;


import com.example.domain.Article;
import com.example.domain.ArticleComment;
import com.example.domain.Hashtag;
import com.example.domain.UserAccount;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("testdb")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;

    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;
    private final HashtagRepository hashtagRepository;

    @Autowired
    JpaRepositoryTest(ArticleRepository articleRepository, ArticleCommentRepository articleCommentRepository, UserAccountRepository userAccountRepository, HashtagRepository hashtagRepository) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.userAccountRepository = userAccountRepository;
        this.hashtagRepository = hashtagRepository;
    }

    @DisplayName("select 테스트")
    @Test
    void selectTest() {

        List<Article> articles = articleRepository.findAll();

         assertThat(articles).isNotNull().hasSize(123);
    }

    @DisplayName("insert 테스트")
    @Test
    void insertTest() {

        // given
        long prevCount = userAccountRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("atsdt", "123", "hel@g.c", "spongebobo", "daepa"));
        Article article = Article.of(userAccount, "new article", "new content");
        article.addHashtags(Set.of(Hashtag.of("spring")));

        // when
        articleRepository.save(article);

        // then
        assertThat(userAccountRepository.count()).isEqualTo(prevCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    void updateTest() {
        // Given
        Article article = articleRepository.findById(1L).orElseThrow();
        Hashtag updateHashtag = Hashtag.of("springboot");
        article.clearHashtags();
        article.addHashtags(Set.of(updateHashtag));

        // Then
        Article savedArticle = articleRepository.saveAndFlush(article);

        // When
        assertThat(savedArticle.getHashtags())
                .hasSize(1)
                .extracting("hashtagName", String.class)
                .containsExactly(updateHashtag.getHashtagName());
    }

    @DisplayName("delete 테스트")
    @Test
    void deleteTest() { // 게시물 pk를 fk로 갖는 댓글이 cascade로 삭제되는지 확인

        //given
        long previousArticleCount = articleRepository.count();
        long previousArticleCommentCount = articleCommentRepository.count();

        Article article = articleRepository.findById(1L).orElseThrow();
        int deletedCommentSize = article.getArticleComments().size();
        //when

        articleRepository.delete(article);
        //then

        assertThat(previousArticleCount).isEqualTo(articleRepository.count()+ 1);
        assertThat(previousArticleCommentCount).isEqualTo(articleCommentRepository.count() + deletedCommentSize);
    }

    @DisplayName("대댓글 조회 테스트")
    @Test
    void givenParentCommentId_whenSelectiong_thenReturnsChildComments(){
        // given

        // when
        Optional<ArticleComment> parentComment = articleCommentRepository.findById(1L);

        // then
        assertThat(parentComment).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(4);
    }

    @DisplayName("댓글에 대댓글 삽입 테스트")
    @Test
    void givenParentComment_whenSaving_thenInsertsChildComment(){
        // given
        ArticleComment parentComment = articleCommentRepository.getReferenceById(1L);
        ArticleComment childComment = ArticleComment.of(
                parentComment.getArticle(),
                parentComment.getUserAccount(),
                "대댓글 내용"); // 이거 가져오느라 SELECT쿼리 한번

        // when
        parentComment.addChildComment(childComment); // 여기서 getChildComments 하느라 SELECT 쿼리 한번
        articleCommentRepository.flush();

        // then
        assertThat(articleCommentRepository.findById(1L)).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(5);
    }

    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트")
    @Test
    void givenArticleCommentHavingChildComments_whenDeletingParentComment_thenDeletesEveryComment(){
        // given
        ArticleComment parentComment = articleCommentRepository.getReferenceById(1L);
        long previousArticleCommentCount = articleCommentRepository.count();

        // when
        articleCommentRepository.delete(parentComment);

        // then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - 5);
    }

    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트 - 댓글 ID + 유저 ID")
    @Test
    void givenArticleCommentIdHavingChildCommentsAndUserId_whenDeletingParentComment_thenDeletesEveryComment(){
        // given
        long previousArticleCommentCount = articleCommentRepository.count();

        // when
        articleCommentRepository.deleteByIdAndUserAccount_UserId(1L, "uno");

        // then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - 5);
    }



    @DisplayName("[QueryDSL 전체 hashtag 리스트에서 이름만 조회하기")
    @Test
    void givenNothing_whenQueryingHashtags_thenReturnsHashtagNames(){
        // given

        // when
        List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

        // then
        assertThat(hashtagNames).hasSize(19);
    }

    @DisplayName("[QueryDSL] hashtag로 페이징된 게시글 검색하기")
    @Test
    void givenHashtagNamesAndPageable_whenQueryingArticles_thenReturnsArticlePage(){
        // given
        List<String> hashtagNames = List.of("blue", "crimson", "fuscia");
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(
                Sort.Order.desc("hashtags.hashtagName"),
                Sort.Order.asc("title")));

        // when
        Page<Article> articlePage = articleRepository.findByHashtagNames(hashtagNames, pageable);

        // then
        assertThat(articlePage.getContent()).hasSize(pageable.getPageSize());
        assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Fusce posuere felis sed lacus.");
        assertThat(articlePage.getContent().get(0).getHashtags())
                .extracting("hashtagName", String.class)
                .containsExactly("fuscia");
        assertThat(articlePage.getTotalElements()).isEqualTo(17);
        assertThat(articlePage.getTotalPages()).isEqualTo(4);

    }

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("uno");
        }
    }
}