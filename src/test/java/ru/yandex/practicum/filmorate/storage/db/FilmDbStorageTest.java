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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    void testAddWithGenres() {
        Film film = createFilm("WithGenres");
        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        film.setGenres(new HashSet<>(List.of(genre1, genre2)));

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGenres()).hasSize(2);
        assertThat(saved.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testFindById() {
        Film film = createFilm("FindMe");
        Film saved = filmStorage.add(film);

        Optional<Film> found = filmStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName()).isEqualTo("FindMe");
        assertThat(found.get().getMpa()).isNotNull();
        assertThat(found.get().getMpa().getName()).isEqualTo("G");
        assertThat(found.get().getGenres()).isEmpty();
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Film> found = filmStorage.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        filmStorage.add(createFilm("Film1"));
        filmStorage.add(createFilm("Film2"));
        filmStorage.add(createFilm("Film3"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);
    }

    @Test
    void testUpdate() {
        Film film = createFilm("Original");
        Film saved = filmStorage.add(film);

        saved.setName("Updated");
        saved.setDescription("Updated desc");
        saved.setDuration(200L);
        Film updated = filmStorage.update(saved);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getDuration()).isEqualTo(200L);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
    }

    @Test
    void testUpdateGenres() {
        Film film = createFilm("WithGenres");
        Genre g1 = new Genre();
        g1.setId(1L);
        film.setGenres(new HashSet<>(List.of(g1)));
        Film saved = filmStorage.add(film);

        Genre g2 = new Genre();
        g2.setId(2L);
        saved.setGenres(new HashSet<>(List.of(g2)));
        filmStorage.update(saved);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getGenres()).extracting(Genre::getId).containsExactly(2L);
    }

    @Test
    void testDelete() {
        Film film = createFilm("ToDelete");
        Film saved = filmStorage.add(film);

        filmStorage.delete(saved.getId());

        assertThat(filmStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void testClearAll() {
        filmStorage.add(createFilm("F1"));
        filmStorage.add(createFilm("F2"));

        filmStorage.clearAll();

        assertThat(filmStorage.findAll()).isEmpty();
    }

    @Test
    void testAddLike() {
        Film film = createFilm("Liked");
        Film saved = filmStorage.add(film);
        User user = createUser("user1");

        filmStorage.addLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).containsExactly(user.getId());
    }

    @Test
    void testRemoveLike() {
        Film film = createFilm("Unliked");
        Film saved = filmStorage.add(film);
        User user = createUser("user2");

        filmStorage.addLike(saved.getId(), user.getId());
        filmStorage.removeLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
    void testGetPopular() {
        Film film1 = createFilm("Popular");
        Film saved1 = filmStorage.add(film1);
        Film film2 = createFilm("NotPopular");
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");

        filmStorage.addLike(saved1.getId(), u1.getId());
        filmStorage.addLike(saved1.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u3.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(popular.get(0).getLikes()).hasSize(2);
        assertThat(popular.get(1).getId()).isEqualTo(saved2.getId());
        assertThat(popular.get(1).getLikes()).hasSize(1);
    }

    @Test
    void testGetPopularLimit() {
        for (int i = 0; i < 5; i++) {
            Film film = createFilm("Film" + i);
            Film saved = filmStorage.add(film);
            User user = createUser("u" + i);
            filmStorage.addLike(saved.getId(), user.getId());
        }

        List<Film> popular = filmStorage.getPopular(3);

        assertThat(popular).hasSize(3);
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
}
