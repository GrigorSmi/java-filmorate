package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final FeedEventService feedEventService;

    public ReviewService(ReviewStorage reviewStorage,
                         FilmService filmService,
                         UserService userService,
                         FeedEventService feedEventService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
        this.feedEventService = feedEventService;
    }

    public Review create(Review review) {
        log.info("Создание отзыва: {}", review);
        filmService.findById(review.getFilmId());
        userService.findById(review.getUserId());
        Review created = reviewStorage.create(review);
        feedEventService.addEvent(review.getUserId(), FeedEventType.REVIEW, FeedEventOperation.ADD, created.getId());
        return created;
    }

    public Review update(Review review) {
        log.info("Обновление отзыва: {}", review);
        Review existing = getById(review.getId());
        Review updated = reviewStorage.update(review);
        feedEventService.addEvent(existing.getUserId(), FeedEventType.REVIEW, FeedEventOperation.UPDATE, review.getId());
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
        getById(reviewId);
        userService.findById(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        getById(reviewId);
        userService.findById(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        getById(reviewId);
        userService.findById(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        getById(reviewId);
        userService.findById(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }
}
