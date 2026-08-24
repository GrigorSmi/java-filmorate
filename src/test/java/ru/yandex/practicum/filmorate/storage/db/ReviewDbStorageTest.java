package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(ReviewDbStorage.class)
public class ReviewDbStorageTest {

    @Autowired
    private ReviewDbStorage reviewStorage;

    @Autowired
    private JdbcTemplate jdbc;

    private Long testUserId;
    private Long testFilmId;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы перед каждым тестом (важен порядок из-за внешних ключей)
        jdbc.update("DELETE FROM reviews");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");

        // 1. Создаем тестового пользователя
        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "test@test.com", "testlogin", "Test User", LocalDate.of(1990, 1, 1));

        // Получаем его реальный ID из базы
        testUserId = jdbc.queryForObject("SELECT id FROM users WHERE email = 'test@test.com'", Long.class);

        // 2. Создаем тестовый фильм
        jdbc.update("INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)",
                "Test Film", "Test Description", LocalDate.of(2020, 1, 1), 120, 1);

        // Получаем его реальный ID из базы
        testFilmId = jdbc.queryForObject("SELECT id FROM films WHERE name = 'Test Film'", Long.class);
    }

    @Test
    void testCreate() {
        Review review = new Review();
        review.setContent("Отличный фильм!");
        review.setIsPositive(true);
        review.setUserId(testUserId);
        review.setFilmId(testFilmId);
        review.setUseful(0);

        Review created = reviewStorage.create(review);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getContent()).isEqualTo("Отличный фильм!");
        assertThat(created.getUserId()).isEqualTo(testUserId);
        assertThat(created.getFilmId()).isEqualTo(testFilmId);
    }

    @Test
    void testFindById() {
        // Сначала создаем отзыв
        Review review = new Review();
        review.setContent("Хороший фильм");
        review.setIsPositive(true);
        review.setUserId(testUserId);
        review.setFilmId(testFilmId);
        review.setUseful(0);

        Review created = reviewStorage.create(review);

        // Ищем его по ID
        Review found = reviewStorage.findById(created.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getContent()).isEqualTo("Хороший фильм");
    }

    @Test
    void testGetReviewsByFilmId() {
        // Создаем два отзыва для одного фильма с разной полезностью
        Review review1 = new Review();
        review1.setContent("Отзыв 1");
        review1.setIsPositive(true);
        review1.setUserId(testUserId);
        review1.setFilmId(testFilmId);
        review1.setUseful(10);
        reviewStorage.create(review1);

        Review review2 = new Review();
        review2.setContent("Отзыв 2");
        review2.setIsPositive(false);
        review2.setUserId(testUserId);
        review2.setFilmId(testFilmId);
        review2.setUseful(5);
        reviewStorage.create(review2);

        // Получаем отзывы и проверяем, что их 2
        List<Review> reviews = reviewStorage.getReviewsByFilmId(testFilmId, 10);

        assertThat(reviews).hasSize(2);
        // Проверяем, что они отсортированы по useful DESC (по убыванию)
        assertThat(reviews.get(0).getUseful()).isEqualTo(10);
        assertThat(reviews.get(1).getUseful()).isEqualTo(5);
    }
}