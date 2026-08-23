package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Long reviewId);

    Optional<Review> getById(Long reviewId);

    List<Review> getByFilmId(Long filmId, Integer count);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeVote(Long reviewId, Long userId);
}