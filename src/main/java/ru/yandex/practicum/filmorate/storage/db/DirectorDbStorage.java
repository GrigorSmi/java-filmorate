package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Director> directorRowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));
        return director;
    };

    public DirectorDbStorage(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        jdbc.update("UPDATE directors SET name = ? WHERE id = ?", director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM directors WHERE id = ?", id);
    }

    @Override
    public Optional<Director> getById(Long id) {
        return jdbc.query("SELECT * FROM directors WHERE id = ?", directorRowMapper, id).stream().findFirst();
    }

    @Override
    public List<Director> getAll() {
        return jdbc.query("SELECT * FROM directors ORDER BY id", directorRowMapper);
    }
}