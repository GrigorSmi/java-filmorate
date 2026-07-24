package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    };

    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Genre> findAll() {
        return jdbc.query("SELECT id, name FROM genres ORDER BY id", genreRowMapper);
    }

    public Optional<Genre> findById(Long id) {
        List<Genre> result = jdbc.query("SELECT id, name FROM genres WHERE id = ?", genreRowMapper, id);
        return result.stream().findFirst();
    }
}
