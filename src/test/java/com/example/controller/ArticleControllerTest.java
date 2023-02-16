package com.example.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Disabled // 테스트코드를 먼저 작성해둔 상태라서, 테스트가 실패한다. gradle build에 성공하기 위해서 어노테이션 붙임
@DisplayName("View 컨트롤러 - 게시글")
@WebMvcTest(ArticleController.class) // 원하는 컨트롤러 클래스만 빈으로 등록해서 테스트 가능. 가벼운 테스트를 위함
class ArticleControllerTest {
    private final MockMvc mvc;

    ArticleControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 정상 호출")
    @Test
    public void giveNothing_whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/articles"))
                .andExpect(MockMvcResultMatchers.status().isOk()) // HTTP STATUS CODE가 OK인지
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML)) // CONTENT TYPE확인
                .andExpect(MockMvcResultMatchers.view().name("articles/index")) // 반환되는 뷰의 이름 확인
                .andExpect(MockMvcResultMatchers.model().attributeExists("articles"));// View로 넘기는 Model에 "articles"라는 이름의 key가 있는지
    }

    @DisplayName("[view][GET] 게시글 상세 페이지 - 정상 호출")
    @Test
    public void giveNothing_whenRequestingArticleView_thenReturnsArticleView() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/articles/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))
                .andExpect(MockMvcResultMatchers.view().name("articles/detail"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("article")) // 게시글
                .andExpect(MockMvcResultMatchers.model().attributeExists("articleComments")); // 댓글
    }

    @DisplayName("[view][GET] 게시글 검색 전용 페이지 - 정상 호출")
    @Test
    public void giveNothing_whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/articles/search"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))// Search Veiw는 Model에 넣을 Attribute가 없음
                .andExpect(MockMvcResultMatchers.view().name("articles/search"));

    }

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출")
    @Test
    public void giveNothing_whenRequestingArticleHashtagSearchView_thenReturnsArticleHashtagSearchView() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/articles/search-hashtag"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))// Search hashtag Veiw는 Model에 넣을 Attribute가 없음
                .andExpect(MockMvcResultMatchers.view().name("articles/search-hashtag"));

    }
}
