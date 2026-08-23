package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    };

    public ReviewDbStorage(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Review create(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO reviews (content, is_positive, film_id, user_id) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getFilmId());
            ps.setLong(4, review.getUserId());
            return ps;
        }, keyHolder);
        review.setReviewId(keyHolder.getKey().longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        jdbc.update("UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?", review.getContent(), review.getIsPositive(), review.getReviewId());
        return getById(review.getReviewId()).orElse(review);
    }

    @Override
    public void delete(Long reviewId) {
        jdbc.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
    }

    @Override
    public Optional<Review> getById(Long reviewId) {
        return jdbc.query("SELECT * FROM reviews WHERE review_id = ?", reviewRowMapper, reviewId).stream().findFirst();
    }

    @Override
    public List<Review> getByFilmId(Long filmId, Integer count) {
        String sql = filmId != null ? "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?" : "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbc.query(sql, reviewRowMapper, filmId != null ? new Object[]{filmId, count} : new Object[]{count});
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        List<Boolean> likes = jdbc.queryForList("SELECT is_like FROM reviews_likes WHERE review_id = ? AND user_id = ?", Boolean.class, reviewId, userId);
        if (likes.isEmpty()) {
            jdbc.update("INSERT INTO reviews_likes (review_id, user_id, is_like) VALUES (?, ?, true)", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);
        } else if (likes.get(0)) {
            jdbc.update("DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);
        } else {
            jdbc.update("UPDATE reviews_likes SET is_like = true WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 2 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        List<Boolean> likes = jdbc.queryForList("SELECT is_like FROM reviews_likes WHERE review_id = ? AND user_id = ?", Boolean.class, reviewId, userId);
        if (likes.isEmpty()) {
            jdbc.update("INSERT INTO reviews_likes (review_id, user_id, is_like) VALUES (?, ?, false)", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);
        } else if (!likes.get(0)) {
            jdbc.update("DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);
        } else {
            jdbc.update("UPDATE reviews_likes SET is_like = false WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful - 2 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    public void removeVote(Long reviewId, Long userId) {
        List<Boolean> likes = jdbc.queryForList("SELECT is_like FROM reviews_likes WHERE review_id = ? AND user_id = ?", Boolean.class, reviewId, userId);
        if (!likes.isEmpty()) {
            jdbc.update("DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
            jdbc.update("UPDATE reviews SET useful = useful " + (likes.get(0) ? "- 1" : "+ 1") + " WHERE review_id = ?", reviewId);
        }
    }
}