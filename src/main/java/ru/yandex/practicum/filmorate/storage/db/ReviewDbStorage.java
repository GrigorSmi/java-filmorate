package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        return review;
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
    public void addLike(Long reviewId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM reviews_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE reviews_likes SET is_like = true WHERE review_id = ? AND user_id = ?";
            jdbc.update(updateSql, reviewId, userId);
        } else {
            String insertSql = "INSERT INTO reviews_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbc.update(insertSql, reviewId, userId);
        }

        String updateUsefulSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbc.update(updateUsefulSql, reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbc.update(deleteSql, reviewId, userId);

        String updateUsefulSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbc.update(updateUsefulSql, reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM reviews_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE reviews_likes SET is_like = false WHERE review_id = ? AND user_id = ?";
            jdbc.update(updateSql, reviewId, userId);
        } else {
            String insertSql = "INSERT INTO reviews_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbc.update(insertSql, reviewId, userId);
        }

        String updateUsefulSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbc.update(updateUsefulSql, reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbc.update(deleteSql, reviewId, userId);

        String updateUsefulSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbc.update(updateUsefulSql, reviewId);
    }
}