package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Repository
@Qualifier("db")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        long mpaId = rs.getLong("mpa_rating_id");
        MpaRating mpa = new MpaRating();
        mpa.setId(mpaId);
        mpa.setName(getMpaName(mpaId));
        film.setMpa(mpa);
        return film;
    };

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private String getMpaName(Long id) {
        List<String> result = jdbc.queryForList("SELECT name FROM mpa_ratings WHERE id = ?", String.class, id);
        return result.isEmpty() ? null : result.get(0);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(
                "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                film.getId()
        );
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadLikes(Film film) {
        Set<Long> likes = new HashSet<>(
                jdbc.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId())
        );
        film.setLikes(likes);
    }

    private void enrichFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(getMpaName(film.getMpa().getId()));
        }
        loadGenres(film);
        loadLikes(film);
    }

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            if (film.getDescription() != null) {
                ps.setString(2, film.getDescription());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
            }
        }

        enrichFilm(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
            }
        }

        jdbc.update("DELETE FROM likes WHERE film_id = ?", film.getId());
        if (film.getLikes() != null) {
            for (Long userId : film.getLikes()) {
                jdbc.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film.getId(), userId);
            }
        }

        enrichFilm(film);
        return film;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(
                "SELECT id, name, description, release_date, duration, mpa_rating_id FROM films",
                filmRowMapper
        );
        for (Film film : films) {
            enrichFilm(film);
        }
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> result = jdbc.query(
                "SELECT id, name, description, release_date, duration, mpa_rating_id FROM films WHERE id = ?",
                filmRowMapper, id
        );
        if (result.isEmpty()) {
            return Optional.empty();
        }
        Film film = result.get(0);
        enrichFilm(film);
        return Optional.of(film);
    }

    @Override
    public void clearAll() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        List<Film> films = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "FROM films f LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC, f.id " +
                "LIMIT ?",
                filmRowMapper, count
        );
        for (Film film : films) {
            enrichFilm(film);
        }
        return films;
    }
}
