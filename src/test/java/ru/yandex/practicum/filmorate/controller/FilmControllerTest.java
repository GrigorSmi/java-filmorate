package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    @Autowired
    private FilmController controller;

    @Autowired
    private InMemoryFilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage.clearAll();
    }

    @Test
    void create_shouldSucceedWhenDescriptionIsExactly200() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);

        Film result = controller.create(film);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void create_shouldSucceedWhenDescriptionIsNull() {
        Film film = new Film();
        film.setName("name");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);

        Film result = controller.create(film);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldThrowWhenReleaseDateBeforeCinemaBirth() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100L);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", e.getMessage());
    }

    @Test
    void create_shouldSucceedWhenReleaseDateIsExactly18951228() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(100L);

        Film result = controller.create(film);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldThrowWhenDurationIsZero() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(0L);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
    }

    @Test
    void create_shouldThrowWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-1L);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", e.getMessage());
    }

    @Test
    void create_shouldSucceedWithValidFilm() {
        Film film = new Film();
        film.setName("valid film");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);

        Film result = controller.create(film);
        assertNotNull(result.getId());
        assertEquals("valid film", result.getName());
    }

    @Test
    void create_shouldGenerateIncrementingIds() {
        Film first = new Film();
        first.setName("first");
        first.setDescription("desc");
        first.setReleaseDate(LocalDate.now());
        first.setDuration(100L);

        Film second = new Film();
        second.setName("second");
        second.setDescription("desc");
        second.setReleaseDate(LocalDate.now());
        second.setDuration(100L);

        Film r1 = controller.create(first);
        Film r2 = controller.create(second);

        assertEquals(1L, r1.getId());
        assertEquals(2L, r2.getId());
    }

    @Test
    void delete_shouldRemoveFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);
        Film created = controller.create(film);

        filmStorage.delete(created.getId());

        assertTrue(filmStorage.findById(created.getId()).isEmpty());
    }

    @Test
    void update_shouldThrowWhenIdIsNull() {
        Film update = new Film();
        update.setId(null);
        update.setName("name");

        assertThrows(ValidationException.class, () -> controller.update(update));
    }

    // обновление несуществующего фильма (id=999) → ошибка 404
    @Test
    void update_shouldThrowWhenFilmNotFound() {
        Film update = new Film();
        update.setId(999L);
        update.setName("name");
        update.setDescription("desc");
        update.setReleaseDate(LocalDate.now());
        update.setDuration(100L);

        assertThrows(NotFoundException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldSucceedWithValidData() {
        Film film = new Film();
        film.setName("original");
        film.setDescription("original desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100L);
        Film created = controller.create(film);

        Film update = new Film();
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
}
