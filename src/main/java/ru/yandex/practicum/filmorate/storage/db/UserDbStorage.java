package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@Qualifier("db")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

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

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            if (user.getName() != null) {
                ps.setString(3, user.getName());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить id нового пользователя");
        }
        user.setId(key.longValue());
        return user;
    }

    @Override
    public User update(User user) {
        findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query("SELECT id, email, login, name, birthday FROM users", userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        var result = jdbc.query("SELECT id, email, login, name, birthday FROM users WHERE id = ?", userRowMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public void clearAll() {
        jdbc.update("DELETE FROM users");
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        String sql = "WITH CommonLikes AS (" +
                "    SELECT l1.user_id AS other_user_id, COUNT(*) AS common_count " +
                "    FROM likes l1 " +
                "    JOIN likes l2 ON l1.film_id = l2.film_id " +
                "    WHERE l2.user_id = ? AND l1.user_id != ? " +
                "    GROUP BY l1.user_id " +
                "), " +
                "MaxCount AS (" +
                "    SELECT MAX(common_count) AS max_count FROM CommonLikes " +
                ") " +
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "JOIN likes l ON f.id = l.film_id " +
                "JOIN CommonLikes cl ON l.user_id = cl.other_user_id " +
                "JOIN MaxCount mc ON cl.common_count = mc.max_count " +
                "WHERE f.id NOT IN (SELECT film_id FROM likes WHERE user_id = ?) " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                "ORDER BY COUNT(l.user_id) DESC, f.id ASC";

        List<Film> films = jdbc.query(sql, filmRowMapper, userId, userId, userId);

        if (!films.isEmpty()) {
            Set<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toSet());
            String inClause = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));

            Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
            jdbc.query(
                    "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                            "JOIN genres g ON fg.genre_id = g.id " +
                            "WHERE fg.film_id IN (" + inClause + ")",
                    (rs, rowNum) -> {
                        long fId = rs.getLong("film_id");
                        Genre genre = new Genre();
                        genre.setId(rs.getLong("id"));
                        genre.setName(rs.getString("name"));
                        genresByFilm.computeIfAbsent(fId, k -> new LinkedHashSet<>()).add(genre);
                        return null;
                    }
            );

            Map<Long, Set<Director>> directorsByFilm = new HashMap<>();
            jdbc.query(
                    "SELECT fd.film_id, d.id, d.name FROM film_directors fd " +
                            "JOIN directors d ON fd.director_id = d.id " +
                            "WHERE fd.film_id IN (" + inClause + ")",
                    (rs, rowNum) -> {
                        long fId = rs.getLong("film_id");
                        Director d = new Director();
                        d.setId(rs.getLong("id"));
                        d.setName(rs.getString("name"));
                        directorsByFilm.computeIfAbsent(fId, k -> new LinkedHashSet<>()).add(d);
                        return null;
                    }
            );

            Map<Long, Set<Long>> likesByFilm = new HashMap<>();
            jdbc.query(
                    "SELECT film_id, user_id FROM likes WHERE film_id IN (" + inClause + ")",
                    (rs, rowNum) -> {
                        long fId = rs.getLong("film_id");
                        likesByFilm.computeIfAbsent(fId, k -> new HashSet<>()).add(rs.getLong("user_id"));
                        return null;
                    }
            );

            for (Film film : films) {
                film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
                film.setDirectors(directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
                film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
            }
        }
        return films;
    }
}