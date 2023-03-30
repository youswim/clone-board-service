package com.example.service;

import com.example.domain.Article;
import com.example.domain.Hashtag;
import com.example.repository.HashtagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 해시태그")
@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService sut;

    @Mock
    private HashtagRepository hashtagRepository;

    @DisplayName("본문을 파싱하면, 해시태그 이름들을 중복 없이 반환한다.")
    @MethodSource
    @ParameterizedTest(name = "[{index}] \"{0}\" => {1}")
    void givenContent_whenParsing_thenReturnsUniqueHashtagNames(String input, Set<String> expected){
        // given

        // when
        Set<String> actual = sut.parseHashtagNames(input);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        then(hashtagRepository).shouldHaveNoInteractions();
    }

    static Stream<Arguments> givenContent_whenParsing_thenReturnsUniqueHashtagNames() {
        return Stream.of(
                arguments(null, Set.of()),
                arguments("", Set.of()),
                arguments("   ", Set.of()),
                arguments("#", Set.of()),
                arguments("  #", Set.of()),
                arguments("#   ", Set.of()),
                arguments("java", Set.of()),
                arguments("java#", Set.of()),
                arguments("ja#va", Set.of("va")),
                arguments("#java", Set.of("java")),
                arguments("#java_spring", Set.of("java_spring")),
                arguments("#java-spring", Set.of("java")),
                arguments("#_java_spring", Set.of("_java_spring")),
                arguments("#-java-spring", Set.of()),
                arguments("#_java_spring__", Set.of("_java_spring__")),
                arguments("#java#spring", Set.of("java", "spring")),
                arguments("#java #spring", Set.of("java", "spring")),
                arguments("#java  #spring", Set.of("java", "spring")),
                arguments("#java   #spring", Set.of("java", "spring")),
                arguments("#java     #spring", Set.of("java", "spring")),
                arguments("  #java     #spring ", Set.of("java", "spring")),
                arguments("   #java     #spring   ", Set.of("java", "spring")),
                arguments("#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring #부트", Set.of("java", "spring", "부트")),
                arguments("#java,#spring,#부트", Set.of("java", "spring", "부트")),
                arguments("#java.#spring;#부트", Set.of("java", "spring", "부트")),
                arguments("#java|#spring:#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring  #부트", Set.of("java", "spring", "부트")),
                arguments("   #java,? #spring  ...  #부트 ", Set.of("java", "spring", "부트")),
                arguments("#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring#java#부트#java", Set.of("java", "spring", "부트")),
                arguments("#java#스프링 아주 긴 글~~~~~~~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~~~~~~~~~~~~~~~~#java#스프링", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java#스프링~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java~~~~~~~#스프링~~~~~~~~", Set.of("java", "스프링"))
        );
    }

    @DisplayName("해시태그 이름을 입력하면, 저장된 해시태그 중 이름에 매칭하는 것들을 중복 없이 반환한다.")
    @Test
    void given_when_then(){
        // given
        Set<String> hashtagNames = Set.of("java", "spring", "boots");
        given(hashtagRepository.findByHashtagNameIn(hashtagNames))
                .willReturn(List.of(Hashtag.of("java"), Hashtag.of("spring")));

        // when
        Set<Hashtag> hashtags = sut.findHashtagsByNames(hashtagNames);

        // then
        assertThat(hashtags).hasSize(2);
        then(hashtagRepository).should().findByHashtagNameIn(hashtagNames);
    }

    @DisplayName("해시태그의 ID를 입력했을 때, 해당 해시태그를 갖는 게시물이 없다면 해시태그 삭제")
    @Test
    void givenHashtagId_whenHashtagNotHaveArticle_thenDeleteHashtag(){
        // given
        Long hashtagId = 1L;
        Hashtag hashtag = createHashtag();
        ReflectionTestUtils.setField(hashtag, "articles", Set.of());
        given(hashtagRepository.findById(hashtagId)).willReturn(Optional.of(hashtag));
//        willDoNothing().given(hashtagRepository).delete(hashtag);

        // when
        sut.deleteHashtagWithoutArticles(hashtagId);


        // then
        then(hashtagRepository).should().findById(hashtagId);
        then(hashtagRepository).should().delete(hashtag);

    }

    @DisplayName("해시태그의 ID를 입력했을 때, 해당 해시태그를 갖는 게시물이 있다면 삭제하지 않음")
    @Test
    void givenHashtagId_whenHashtagHaveArticle_thenNotDeleteHashtag(){
        // given
        Long hashtagId = 1L;
        Hashtag hashtag = createHashtag();
        ReflectionTestUtils.setField(hashtag, "articles", Set.of(createArticle()));
        given(hashtagRepository.findById(hashtagId)).willReturn(Optional.of(hashtag));
//        willDoNothing().given(hashtagRepository).delete(hashtag);

        // when
        sut.deleteHashtagWithoutArticles(hashtagId);


        // then
        then(hashtagRepository).should().findById(hashtagId);
    }

    private Hashtag createHashtag() {
        return Hashtag.of("test");
    }

    private Article createArticle() {
        return Article.of(null, null, null);
    }

}