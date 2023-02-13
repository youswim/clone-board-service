package com.example.repository;


import com.example.config.JpaConfig;
import com.example.domain.Article;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;


@ActiveProfiles("testdb")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JPA 연결 테스트")
@Import(JpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;

    private final ArticleCommentRepository articleCommentRepository;

    @Autowired
    JpaRepositoryTest(ArticleRepository articleRepository, ArticleCommentRepository articleCommentRepository) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
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

        long prevCount = articleRepository.count();

        Article save = articleRepository.save(Article.of("hello", "cont", "#test"));
        System.out.println(save.getId());

        Assertions.assertThat(articleRepository.count()).isEqualTo(prevCount + 1);

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

        Assertions.assertThat(previousArticleCount).isEqualTo(articleRepository.count() + 1);
        Assertions.assertThat(previousArticleCommentCount).isEqualTo(articleCommentRepository.count() + deletedCommentSize);

    }
}