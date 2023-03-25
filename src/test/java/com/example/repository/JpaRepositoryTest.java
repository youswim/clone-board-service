package com.example.repository;


import com.example.domain.Article;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;


@ActiveProfiles("testdb")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;

    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    JpaRepositoryTest(ArticleRepository articleRepository, ArticleCommentRepository articleCommentRepository, UserAccountRepository userAccountRepository) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.userAccountRepository = userAccountRepository;
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

        // when
        userAccountRepository.save(UserAccount.of("atsdt", "123", "hel@g.c", "spongebobo", "daepa"));

        // then
        Assertions.assertThat(userAccountRepository.count()).isEqualTo(prevCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    void updateTest() {

        Article article = articleRepository.findById(1L).orElseThrow();
        String updatedHashtag = "#springboot";
        article.setHashtag(updatedHashtag);

        Article savedArticle = articleRepository.saveAndFlush(article);

        Assertions.assertThat(savedArticle).hasFieldOrPropertyWithValue("hashtag", updatedHashtag);

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

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("uno");
        }
    }
}