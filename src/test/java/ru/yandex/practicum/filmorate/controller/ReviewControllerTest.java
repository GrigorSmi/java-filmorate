package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateReviewWithEmptyContent() throws Exception {
        // Пытаемся создать отзыв с пустым content
        String reviewJson = "{\"content\": \"\", \"isPositive\": true, \"userId\": 1, \"filmId\": 1}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isBadRequest()); // Ожидаем 400 ошибку
    }

    @Test
    public void testCreateReviewWithNullUserId() throws Exception {
        // Пытаемся создать отзыв без userId
        String reviewJson = "{\"content\": \"Хороший фильм\", \"isPositive\": true, \"userId\": null, \"filmId\": 1}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateReviewWithNullFilmId() throws Exception {
        // Пытаемся создать отзыв без filmId
        String reviewJson = "{\"content\": \"Хороший фильм\", \"isPositive\": true, \"userId\": 1, \"filmId\": null}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateReviewWithNullIsPositive() throws Exception {
        // Пытаемся создать отзыв без isPositive
        String reviewJson = "{\"content\": \"Хороший фильм\", \"isPositive\": null, \"userId\": 1, \"filmId\": 1}";

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isBadRequest());
    }
}