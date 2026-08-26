package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;

    private final RowMapper<Review> reviewRowMapper = (ResultSet rs, int rowNum) -> {
        Review review = new Review();
        review.setId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    };

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, film_id, user_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getFilmId());
            ps.setLong(4, review.getUserId());
            ps.setInt(5, review.getUseful() != null ? review.getUseful() : 0);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            review.setId(keyHolder.getKey().longValue());
        }

        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbc.update(sql, review.getContent(), review.getIsPositive(), review.getId());
        return findById(review.getId()).orElse(review);
    }

    @Override
    public void delete(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(sql, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbc.query(sql, reviewRowMapper, reviewId);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        if (filmId != null) {
            String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbc.query(sql, reviewRowMapper, filmId, count);
        } else {
            String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbc.query(sql, reviewRowMapper, count);
        }
    }

    @Override
    @Transactional
    public void addLike(Long reviewId, Long userId) {
        Boolean current = getCurrentReaction(reviewId, userId);

        if (Boolean.TRUE.equals(current)) {
            return;
        }

        if (current == null) {
            jdbc.update("INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, true)",
                    reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);
        } else {
            jdbc.update("UPDATE review_likes SET is_positive = true WHERE review_id = ? AND user_id = ?",
                    reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 2 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    @Transactional
    public void removeLike(Long reviewId, Long userId) {
        Boolean current = getCurrentReaction(reviewId, userId);

        if (Boolean.TRUE.equals(current)) {
            jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        Boolean current = getCurrentReaction(reviewId, userId);

        if (Boolean.FALSE.equals(current)) {
            return;
        }

        if (current == null) {
            jdbc.update("INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, false)",
                    reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);
        } else {
            jdbc.update("UPDATE review_likes SET is_positive = false WHERE review_id = ? AND user_id = ?",
                    reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 2 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    @Transactional
    public void removeDislike(Long reviewId, Long userId) {
        Boolean current = getCurrentReaction(reviewId, userId);

        if (Boolean.FALSE.equals(current)) {
            jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);
        }
    }

    private Boolean getCurrentReaction(Long reviewId, Long userId) {
        List<Boolean> results = jdbc.query(
                "SELECT is_positive FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_positive"),
                reviewId, userId);
        return results.isEmpty() ? null : results.get(0);
    }
}