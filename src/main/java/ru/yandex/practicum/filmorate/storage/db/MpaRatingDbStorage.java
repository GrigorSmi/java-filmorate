package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<MpaRating> mpaRowMapper = (rs, rowNum) -> {
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };

    public MpaRatingDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<MpaRating> findAll() {
        return jdbc.query("SELECT id, name FROM mpa_ratings ORDER BY id", mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        List<MpaRating> result = jdbc.query("SELECT id, name FROM mpa_ratings WHERE id = ?", mpaRowMapper, id);
        return result.stream().findFirst();
    }
}
