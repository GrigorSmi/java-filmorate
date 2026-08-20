package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description of " + name);
        film.setReleaseDate(LocalDate.of(2000, 6, 15));
        film.setDuration(120L);
        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.com");
        user.setName("name_" + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.add(user);
    }

    @Test
    void testAdd() {
        Film film = createFilm("TestFilm");
        Film saved = filmStorage.add(film);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TestFilm");
        assertThat(saved.getMpa()).isNotNull();
        assertThat(saved.getMpa().getName()).isEqualTo("G");
        assertThat(saved.getGenres()).isEmpty();
        assertThat(saved.getLikes()).isEmpty();
    }

    @Test
    void testFindByIdWithGenresAndLikes() {
        Film film = createFilm("FullFilm");
        Genre g = new Genre();
        g.setId(1L);
        film.setGenres(new HashSet<>(Set.of(g)));
        Film saved = filmStorage.add(film);

        User user = createUser("liker");
        filmStorage.addLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getLikes()).hasSize(1);
    }

    @Test
    void testGetPopularFilteredByGenreAndYear() {
        // Создаем фильмы разных жанров и годов
        Film film1 = createFilm("Old Comedy");
        film1.setReleaseDate(LocalDate.of(1990, 5, 1));
        film1.setGenres(Set.of(new Genre(1L, "Комедия")));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("New Comedy");
        film2.setReleaseDate(LocalDate.of(2020, 5, 1));
        film2.setGenres(Set.of(new Genre(1L, "Комедия")));
        Film saved2 = filmStorage.add(film2);

        Film film3 = createFilm("New Drama");
        film3.setReleaseDate(LocalDate.of(2020, 6, 1));
        film3.setGenres(Set.of(new Genre(2L, "Драма")));
        Film saved3 = filmStorage.add(film3);

        // Добавляем лайки: New Drama (3), New Comedy (2), Old Comedy (1)
        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");

        filmStorage.addLike(saved3.getId(), u1.getId());
        filmStorage.addLike(saved3.getId(), u2.getId());
        filmStorage.addLike(saved3.getId(), u3.getId());

        filmStorage.addLike(saved2.getId(), u1.getId());
        filmStorage.addLike(saved2.getId(), u2.getId());

        filmStorage.addLike(saved1.getId(), u1.getId());

        // Фильтр: только Комедия (genreId=1)
        List<Film> popularComedies = filmStorage.getPopular(10, 1L, null);
        assertThat(popularComedies).hasSize(2);
        assertThat(popularComedies.get(0).getId()).isEqualTo(saved2.getId()); // New Comedy популярнее

        // Фильтр: только 2020 год
        List<Film> popular2020 = filmStorage.getPopular(10, null, 2020);
        assertThat(popular2020).hasSize(2);
        assertThat(popular2020.get(0).getId()).isEqualTo(saved3.getId()); // New Drama популярнее

        // Фильтр: Комедия И 2020 год
        List<Film> popularComedy2020 = filmStorage.getPopular(10, 1L, 2020);
        assertThat(popularComedy2020).hasSize(1);
        assertThat(popularComedy2020.get(0).getId()).isEqualTo(saved2.getId());
    }

    // Остальные твои тесты (testAddWithGenres, testFindById, testUpdate и т.д.)
    // можно оставить как есть, они не мешают.
}
