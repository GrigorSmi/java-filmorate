package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("db") UserStorage userStorage,
                         @Qualifier("db") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review create(Review review) {
        // 1. Явная проверка на null (вернет 400 Bad Request)
        if (review.getUserId() == null) {
            throw new ValidationException("ID пользователя не может быть пустым");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("ID фильма не может быть пустым");
        }

        // 2. Дальше твои обычные проверки, что пользователь и фильм существуют (вернут 404, если их нет в БД)
        userStorage.findById(review.getUserId()); // или userStorage.findById(...)
        filmStorage.findById(review.getFilmId()); // или filmStorage.findById(...)

        log.info("Создание отзыва: {}", review);
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        reviewStorage.getById(review.getReviewId()).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        return reviewStorage.update(review);
    }

    public void delete(Long reviewId) {
        reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        reviewStorage.delete(reviewId);
    }

    public Review getById(Long reviewId) {
        return reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public List<Review> getByFilmId(Long filmId, Integer count) {
        return reviewStorage.getByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeVote(Long reviewId, Long userId) {
        reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        reviewStorage.removeVote(reviewId, userId);
    }
}