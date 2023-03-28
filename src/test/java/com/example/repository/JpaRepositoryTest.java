package com.example.repository;


import com.example.domain.Article;
import com.example.domain.Hashtag;
import com.example.domain.UserAccount;
import org.assertj.core.api.Assertions;
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

         Assertions.assertThat(articles).isNotNull().hasSize(123);
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
        Assertions.assertThat(userAccountRepository.count()).isEqualTo(prevCount + 1);
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
        Assertions.assertThat(savedArticle.getHashtags())
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

        Assertions.assertThat(previousArticleCount).isEqualTo(articleRepository.count()+ 1);
        Assertions.assertThat(previousArticleCommentCount).isEqualTo(articleCommentRepository.count() + deletedCommentSize);
    }

    @DisplayName("[QueryDSL 전체 hashtag 리스트에서 이름만 조회하기")
    @Test
    void givenNothing_whenQueryingHashtags_thenReturnsHashtagNames(){
        // given

        // when
        List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

        // then
        Assertions.assertThat(hashtagNames).hasSize(19);
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
        Assertions.assertThat(articlePage.getContent()).hasSize(pageable.getPageSize());
        Assertions.assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Fusce posuere felis sed lacus.");
        Assertions.assertThat(articlePage.getContent().get(0).getHashtags())
                .extracting("hashtagName", String.class)
                .containsExactly("fuscia");
        Assertions.assertThat(articlePage.getTotalElements()).isEqualTo(17);
        Assertions.assertThat(articlePage.getTotalPages()).isEqualTo(4);

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