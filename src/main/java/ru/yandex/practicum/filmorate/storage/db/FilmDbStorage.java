package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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
<<<<<<< HEAD
        Set<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toSet());
        String inClause = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
        jdbc.query("SELECT fg.film_id, g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id IN (" + inClause + ") ORDER BY g.id",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    genresByFilm.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>()).add(genre);
                    return null;
                });

        Map<Long, Set<Director>> directorsByFilm = new HashMap<>();
        jdbc.query("SELECT fd.film_id, d.id, d.name FROM film_directors fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id IN (" + inClause + ") ORDER BY d.id",
                (rs, rowNum) -> {
                    Director director = new Director();
                    director.setId(rs.getLong("id"));
                    director.setName(rs.getString("name"));
                    directorsByFilm.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>()).add(director);
                    return null;
                });

        Map<Long, Set<Long>> likesByFilm = new HashMap<>();
        jdbc.query("SELECT film_id, user_id FROM likes WHERE film_id IN (" + inClause + ")",
                (rs, rowNum) -> {
                    likesByFilm.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>()).add(rs.getLong("user_id"));
                    return null;
                });
=======

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

        Map<Long, Set<Director>> directorsByFilm = new HashMap<>();
        jdbc.query(
                "SELECT fd.film_id, d.id, d.name FROM film_directors fd " +
                        "JOIN directors d ON fd.director_id = d.id " +
                        "WHERE fd.film_id IN (" + inClause + ") ORDER BY d.id",
                (rs, rowNum) -> {
                    long filmId = rs.getLong("film_id");
                    Director director = new Director();
                    director.setId(rs.getLong("id"));
                    director.setName(rs.getString("name"));
                    directorsByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
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
>>>>>>> develop

        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setDirectors(directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

<<<<<<< HEAD
    private String loadMpaName(Long mpaId) {
        List<String> names = jdbc.queryForList(
                "SELECT name FROM mpa_ratings WHERE id = ?",
                String.class,
                mpaId
        );
        return names.isEmpty() ? null : names.get(0);
    }

=======
>>>>>>> develop
    private void enrichFilm(Film film) {
        enrichFilms(List.of(film));
    }

<<<<<<< HEAD
=======
    private String loadMpaName(Long mpaId) {
        List<String> names = jdbc.queryForList(
                "SELECT name FROM mpa_ratings WHERE id = ?", String.class, mpaId);
        return names.isEmpty() ? null : names.get(0);
    }

>>>>>>> develop
    @Override
    @Transactional
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
<<<<<<< HEAD
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
=======
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
>>>>>>> develop
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
            }
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film.getId(), director.getId());
            }
        }

<<<<<<< HEAD
        // Загружаем имя MPA, если оно null
        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
        }

=======
        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
        }
>>>>>>> develop
        enrichFilm(film);
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
<<<<<<< HEAD
        findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        jdbc.update("UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?",
                film.getName(), film.getDescription(), java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());
=======
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
>>>>>>> develop

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
            }
        }

        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film.getId(), director.getId());
            }
        }

<<<<<<< HEAD
        // Загружаем имя MPA, если оно null
        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
        }

=======
        if (film.getMpa() != null && film.getMpa().getName() == null) {
            film.getMpa().setName(loadMpaName(film.getMpa().getId()));
        }
>>>>>>> develop
        enrichFilm(film);
        return film;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Collection<Film> findAll() {
<<<<<<< HEAD
        List<Film> films = jdbc.query("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id", filmRowMapper);
=======
        List<Film> films = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id",
                filmRowMapper
        );
>>>>>>> develop
        enrichFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
<<<<<<< HEAD
        List<Film> result = jdbc.query("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?", filmRowMapper, id);
        if (result.isEmpty()) return Optional.empty();
=======
        List<Film> result = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?",
                filmRowMapper, id
        );
        if (result.isEmpty()) {
            return Optional.empty();
        }
>>>>>>> develop
        enrichFilm(result.get(0));
        return Optional.of(result.get(0));
    }

    @Override
    public void clearAll() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
<<<<<<< HEAD
        jdbc.update("DELETE FROM directors");
=======
>>>>>>> develop
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

<<<<<<< HEAD
    @Override
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id LEFT JOIN likes l ON f.id = l.film_id ");
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.id = fg.film_id AND fg.genre_id = ? ");
            params.add(genreId);
        }
        if (year != null) {
            if (genreId == null) sql.append("WHERE EXTRACT(YEAR FROM f.release_date) = ? ");
            else sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }
        sql.append("GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name ORDER BY COUNT(l.user_id) DESC, f.id ASC LIMIT ?");
        params.add(count);

        List<Film> films = jdbc.query(sql.toString(), filmRowMapper, params.toArray());
=======
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
>>>>>>> develop
        enrichFilms(films);
        return films;
    }

<<<<<<< HEAD
    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String orderClause = "likes".equals(sortBy) ? "COUNT(l.user_id) DESC, f.id" : "f.release_date ASC, f.id";
        List<Film> films = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
=======
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String orderClause;
        if ("likes".equals(sortBy)) {
            orderClause = "COUNT(l.user_id) DESC, f.id";
        } else {
            orderClause = "f.release_date ASC, f.id";
        }

        List<Film> films = jdbc.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
>>>>>>> develop
                        "JOIN film_directors fd ON f.id = fd.film_id " +
                        "LEFT JOIN likes l ON f.id = l.film_id " +
                        "WHERE fd.director_id = ? " +
                        "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                        "ORDER BY " + orderClause,
<<<<<<< HEAD
                filmRowMapper, directorId);
=======
                filmRowMapper, directorId
        );
>>>>>>> develop
        enrichFilms(films);
        return films;
    }
}
