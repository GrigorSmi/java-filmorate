package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    @Autowired
    private FilmController controller;

    @Autowired
    private UserController userController;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Validator validator;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM marks");
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("test");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);
        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User createTestUser(String suffix) {
        User user = new User();
        user.setEmail("user" + suffix + "@mail.com");
        user.setLogin("login" + suffix);
        user.setName("name" + suffix);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void create_shouldSucceedWhenDescriptionIsExactly200() {
        Film film = createTestFilm();
        film.setDescription("a".repeat(200));

        Film result = controller.create(film);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void create_shouldSucceedWhenDescriptionIsNull() {
        Film film = createTestFilm();
        film.setDescription(null);

        Film result = controller.create(film);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldThrowWhenReleaseDateBeforeCinemaBirth() {
        Film film = createTestFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("Дата релиза не может быть раньше 28 декабря 1895 года")));
    }

    @Test
    void create_shouldSucceedWhenReleaseDateIsExactly18951228() {
        Film film = createTestFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film result = controller.create(film);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldThrowWhenDurationIsZero() {
        Film film = createTestFilm();
        film.setDuration(0L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("Продолжительность фильма должна быть положительной")));
    }

    @Test
    void create_shouldThrowWhenDurationIsNegative() {
        Film film = createTestFilm();
        film.setDuration(-1L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("Продолжительность фильма должна быть положительной")));
    }

    @Test
    void create_shouldSucceedWithValidFilm() {
        Film film = createTestFilm();
        film.setName("valid film");

        Film result = controller.create(film);
        assertNotNull(result.getId());
        assertEquals("valid film", result.getName());
    }

    @Test
    void delete_shouldRemoveFilm() {
        Film film = createTestFilm();
        Film created = controller.create(film);

        controller.delete(created.getId());

        assertThrows(NotFoundException.class, () -> controller.findById(created.getId()));
    }

    @Test
    void delete_shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> controller.delete(999L));
    }

    @Test
    void update_shouldThrowWhenIdIsNull() {
        Film update = new Film();
        update.setId(null);
        update.setName("name");

        assertThrows(ValidationException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldThrowWhenFilmNotFound() {
        Film update = createTestFilm();
        update.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldSucceedWithValidData() {
        Film film = createTestFilm();
        film.setName("original");
        film.setDescription("original desc");
        Film created = controller.create(film);

        Film update = createTestFilm();
        update.setId(created.getId());
        update.setName("updated");
        update.setDescription("updated desc");
        update.setReleaseDate(LocalDate.of(2000, 1, 1));
        update.setDuration(200L);

        Film result = controller.update(update);
        assertEquals("updated", result.getName());
        assertEquals("updated desc", result.getDescription());
        assertEquals(200L, result.getDuration());
    }

    @Test
    void getCommonFilms_shouldReturnCommonFilmsSortedByPopularity() {
        User user1 = userController.create(createTestUser("u1"));
        User user2 = userController.create(createTestUser("u2"));

        Film film1 = createTestFilm();
        film1.setName("common1");
        Film f1 = controller.create(film1);

        Film film2 = createTestFilm();
        film2.setName("common2");
        Film f2 = controller.create(film2);

        Film film3 = createTestFilm();
        film3.setName("onlyUser1");
        Film f3 = controller.create(film3);

        controller.addMark(f1.getId(), user1.getId(), 10d);
        controller.addMark(f1.getId(), user2.getId(), 10d);
        controller.addMark(f2.getId(), user1.getId(), 10d);
        controller.addMark(f2.getId(), user2.getId(), 10d);
        controller.addMark(f3.getId(), user1.getId(), 10d);

        List<Film> common = controller.getCommonFilms(user1.getId(), user2.getId());

        assertEquals(2, common.size());
        assertEquals("common1", common.get(0).getName());
        assertEquals("common2", common.get(1).getName());
    }

    @Test
    void getCommonFilms_shouldReturnEmptyListWhenNoCommonFilms() {
        User user1 = userController.create(createTestUser("u3"));
        User user2 = userController.create(createTestUser("u4"));

        Film film1 = createTestFilm();
        film1.setName("onlyUser1");
        Film f1 = controller.create(film1);

        Film film2 = createTestFilm();
        film2.setName("onlyUser2");
        Film f2 = controller.create(film2);

        controller.addMark(f1.getId(), user1.getId(), 10d);
        controller.addMark(f2.getId(), user2.getId(), 10d);

        List<Film> common = controller.getCommonFilms(user1.getId(), user2.getId());

        assertTrue(common.isEmpty());
    }

    @Test
    void getCommonFilms_shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> controller.getCommonFilms(999L, 1L));
    }

    @Test
    void getCommonFilms_shouldThrowWhenFriendNotFound() {
        User user1 = userController.create(createTestUser("u5"));

        assertThrows(NotFoundException.class,
                () -> controller.getCommonFilms(user1.getId(), 999L));
    }

    @Test
    void addLike_shouldAddMarkWithMaxValue() {
        Film film = createTestFilm();
        Film created = controller.create(film);
        User user = userController.create(createTestUser("l1"));

        controller.addLike(created.getId(), user.getId());

        Film result = controller.findById(created.getId());
        assertEquals(10.0, result.getRating());
    }

    @Test
    void addLikeWithValue_shouldAddMarkWithGivenValue() {
        Film film = createTestFilm();
        Film created = controller.create(film);
        User user = userController.create(createTestUser("l2"));

        controller.addLikeWithValue(created.getId(), user.getId(), 5.01);

        Film result = controller.findById(created.getId());
        assertEquals(5.01, result.getRating());
    }

    @Test
    void removeLike_shouldRemoveMark() {
        Film film = createTestFilm();
        Film created = controller.create(film);
        User user = userController.create(createTestUser("l3"));

        controller.addLike(created.getId(), user.getId());
        controller.removeLike(created.getId(), user.getId());

        Film result = controller.findById(created.getId());
        assertNull(result.getRating());
    }
}
