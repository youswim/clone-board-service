package com.example.controller;


import com.example.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 인증")
@Import(SecurityConfig.class)
@WebMvcTest
public class AuthControllerTest {

    private final MockMvc mvc;

    @Autowired
    public AuthControllerTest(MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[view][GET] 로그인 페이지 - 정상 호출")
    @Test
    public void giveNothing_whenTryingToLogIn_thenReturnsLogInView() throws Exception {

        mvc.perform(get("/login"))
                .andExpect(status().isOk()) // HTTP STATUS CODE가 OK인지
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)); // CONTENT TYPE확인
    }
}
