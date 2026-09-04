package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbc;

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setName("name_" + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
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

    @Test
    void testAdd() {
        User user = createUser("login1", "user1@mail.com");
        User saved = userStorage.add(user);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindById() {
        User user = createUser("login2", "user2@mail.com");
        User saved = userStorage.add(user);
        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("login2");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> found = userStorage.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        jdbc.update("DELETE FROM users");
        userStorage.add(createUser("login_a", "a@mail.com"));
        userStorage.add(createUser("login_b", "b@mail.com"));
        assertThat(userStorage.findAll()).hasSize(2);
    }

    @Test
    void testUpdate() {
        User user = createUser("login3", "user3@mail.com");
        User saved = userStorage.add(user);
        saved.setEmail("updated@mail.com");
        User updated = userStorage.update(saved);
        assertThat(updated.getEmail()).isEqualTo("updated@mail.com");
    }

    @Test
    void testDelete() {
        User user = createUser("login4", "user4@mail.com");
        User saved = userStorage.add(user);
        userStorage.delete(saved.getId());
        assertThat(userStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void testClearAll() {
        userStorage.add(createUser("login_x", "x@mail.com"));
        userStorage.clearAll();
        assertThat(userStorage.findAll()).isEmpty();
    }

    @Test
    void testAddUserWithNullName() {
        User user = createUser("login5", "user5@mail.com");
        user.setName(null);
        User saved = userStorage.add(user);
        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isNull();
    }

    @Test
    void testGetRecommendations() {
        jdbc.update("DELETE FROM marks");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");

        User me = createUser("me", "me@mail.com");
        User meSaved = userStorage.add(me);
        User similar = createUser("similar", "similar@mail.com");
        User similarSaved = userStorage.add(similar);
        User distant = createUser("distant", "distant@mail.com");
        User distantSaved = userStorage.add(distant);

        Film f1 = filmStorage.add(createFilm("Shared1"));
        Film f2 = filmStorage.add(createFilm("RecFilm"));
        Film f3 = filmStorage.add(createFilm("NegativeFilm"));

        // "similar" ставит близкие оценки по f1, "distant" — далёкие; привязка к me идёт через f1
        filmStorage.addMark(f1.getId(), meSaved.getId(), 5);
        filmStorage.addMark(f1.getId(), similarSaved.getId(), 6);
        filmStorage.addMark(f1.getId(), distantSaved.getId(), 10);

        // similar положительно оценил f2 (рекомендуется), f3 не рекомендуется
        filmStorage.addMark(f2.getId(), similarSaved.getId(), 9);
        filmStorage.addMark(f3.getId(), similarSaved.getId(), 2);

        List<Film> recs = userStorage.getRecommendations(meSaved.getId());

        assertThat(recs).extracting(Film::getId).containsExactly(f2.getId());
    }

    @Test
    void testGetRecommendationsEmptyForUnknownUser() {
        List<Film> recs = userStorage.getRecommendations(9999L);
        assertThat(recs).isEmpty();
    }
}