package com.example.controller;

import com.example.config.SecurityConfig;
import com.example.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Import(SecurityConfig.class)
@WebMvcTest(MainController.class)
class MainControllerTest {

    private final MockMvc mvc;

    @MockBean
    private UserAccountRepository userAccountRepository;

    @Autowired
    public MainControllerTest(MockMvc mvc) {
        this.mvc = mvc;
    }


    @Test
    void givenNothing_whenRequestingRootPage_thenRedirectToArticlesPage() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }
}