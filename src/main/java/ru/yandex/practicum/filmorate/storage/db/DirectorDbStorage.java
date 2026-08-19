package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Director> directorRowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));
        return director;
    };

    public DirectorDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Director> findAll() {
        return jdbc.query("SELECT id, name FROM directors ORDER BY id", directorRowMapper);
    }

    public Optional<Director> findById(Long id) {
        List<Director> result = jdbc.query("SELECT id, name FROM directors WHERE id = ?", directorRowMapper, id);
        return result.stream().findFirst();
    }

    public Director add(Director director) {
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

    public Director update(Director director) {
        jdbc.update("UPDATE directors SET name = ? WHERE id = ?", director.getName(), director.getId());
        return director;
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM directors WHERE id = ?", id);
    }
}
