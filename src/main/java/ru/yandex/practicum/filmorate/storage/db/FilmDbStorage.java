package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

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
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getLong("mpa_rating_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);
        return film;
    };

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private void enrichFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
        jdbc.query(
                "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.id " +
                        "WHERE fg.film_id IN (" + inClause + ") ORDER BY g.id",
                (rs, rowNum) -> {
                    long filmId = rs.getLong("film_id");
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    genresByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
                    return null;
                }
        );

        Map<Long, Set<Long>> likesByFilm = new HashMap<>();
        jdbc.query(
                "SELECT film_id, user_id FROM likes WHERE film_id IN (" + inClause + ")",
                (rs, rowNum) -> {
                    long filmId = rs.getLong("film_id");
                    likesByFilm.computeIfAbsent(filmId, k -> new HashSet<>()).add(rs.getLong("user_id"));
                    return null;
                }
        );

        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private void enrichFilm(Film film) {
        enrichFilms(List.of(film));
    }

    private String loadMpaName(Long mpaId) {
        List<String> names = jdbc.queryForList(
                "SELECT name FROM mpa_ratings WHERE id = ?", String.class, mpaId);
        return names.isEmpty() ? null : names.get(0);
    }

    @Override
    @Transactional
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

        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
        }
        enrichFilm(film);
        return film;
    }

    @Override
    @Transactional
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

        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
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
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id",
                filmRowMapper
        );
        enrichFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> result = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?",
                filmRowMapper, id
        );
        if (result.isEmpty()) {
            return Optional.empty();
        }
        enrichFilm(result.get(0));
        return Optional.of(result.get(0));
    }

    @Override
    public void clearAll() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        List<Film> films = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                        "LEFT JOIN likes l ON f.id = l.film_id " +
                        "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                        "ORDER BY COUNT(l.user_id) DESC, f.id " +
                        "LIMIT ?",
                filmRowMapper, count
        );
        enrichFilms(films);
        return films;
    }
}
