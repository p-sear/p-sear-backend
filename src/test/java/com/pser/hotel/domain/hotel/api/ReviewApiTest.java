package com.pser.hotel.domain.hotel.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 Controller 테스트")
class ReviewApiTest {
    @InjectMocks
    ReviewApi reviewApi;

    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewApi)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("리뷰 리스트 조회 테스트")
    public void reviewList() throws Exception {
        mockMvc.perform(get("/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 생성 테스트")
    public void createReview() throws Exception {
        String reviewJson = "{\"content\":\"Great hotel!\", \"rating\":\"5\"}"; // 임시 데이터로 채워진 JSON
        mockMvc.perform(post("/reviews")
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 상세 조회 테스트")
    public void reviewDetails() throws Exception {
        mockMvc.perform(get("/reviews/{reviewId}", 1L))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("리뷰 업데이트 테스트")
    public void updateReview() throws Exception {
        String reviewJson = "{\"content\":\"Updated review\", \"rating\":\"4\"}"; // 임시 데이터
        mockMvc.perform(patch("/reviews/{reviewId}", 1L)
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 삭제 테스트")
    public void deleteReview() throws Exception {
        mockMvc.perform(delete("/reviews/{reviewId}", 1L))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
