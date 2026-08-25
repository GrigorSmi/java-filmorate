package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
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
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedEventService feedEventService;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("db") FilmStorage filmStorage,
                         @Qualifier("db") UserStorage userStorage,
                         FeedEventService feedEventService) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.feedEventService = feedEventService;
    }

    public Review create(Review review) {
        log.info("Создание отзыва: {}", review);
        if (filmStorage.findById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }
        if (userStorage.findById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }
        Review created = reviewStorage.create(review);
        feedEventService.addEvent(review.getUserId(), FeedEventType.REVIEW, FeedEventOperation.ADD, created.getId());
        return created;
    }

    public Review update(Review review) {
        log.info("Обновление отзыва: {}", review);
        Review updated = reviewStorage.update(review);
        feedEventService.addEvent(review.getUserId(), FeedEventType.REVIEW, FeedEventOperation.UPDATE, review.getId());
        return updated;
    }

    public void delete(Long reviewId) {
        log.info("Удаление отзыва с id={}", reviewId);
        Review review = getById(reviewId);
        reviewStorage.delete(reviewId);
        feedEventService.addEvent(review.getUserId(), FeedEventType.REVIEW, FeedEventOperation.REMOVE, reviewId);
    }

    public Review getById(Long reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }

    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}
