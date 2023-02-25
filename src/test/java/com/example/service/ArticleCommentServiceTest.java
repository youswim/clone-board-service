package com.example.service;

import com.example.domain.Article;
import com.example.dto.ArticleCommentDto;
import com.example.repository.ArticleCommentRepository;
import com.example.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @InjectMocks
    private ArticleCommentService sut;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleCommentRepository articleCommentRepository;

    @DisplayName("게시글 ID 로 조회화면, 해당하는 댓글 리스트를 반환한다")
    @Test
    void givenArticleId_whenSearchingComments_thenReturnsComments(){
        // given
        Long articleId = 1L;
        BDDMockito.given(articleRepository.findById(articleId)).willReturn(
                Optional.of(Article.of("title", "content", "#java"))
        );
        // when
        List<ArticleCommentDto> articleComments = sut.searchArticleComment(articleId);

        // then
        assertThat(articleComments).isNotNull();
        BDDMockito.then(articleRepository).should().findById(articleId);
    }
    
    // 댓글 생성, 수정, 삭제 기능 테스트 코드 추가
    

}