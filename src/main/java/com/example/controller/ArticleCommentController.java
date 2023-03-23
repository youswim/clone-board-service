package com.example.controller;

import com.example.dto.request.ArticleCommentRequest;
import com.example.dto.security.BoardPrincipal;
import com.example.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {
    private final ArticleCommentService articleCommentService;

    @PostMapping("/new")
    public String postNewArticleComments(
            ArticleCommentRequest articleCommentRequest,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        articleCommentService.saveArticleComment(articleCommentRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/articles/" + articleCommentRequest.articleId();
    }

    @PostMapping("/{commentId}/delete")
    public String postNewArticleComments(
            @PathVariable Long commentId,
            Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        articleCommentService.deleteArticleComment(commentId, boardPrincipal.getUsername());

        return "redirect:/articles/" + articleId;
    }

}
