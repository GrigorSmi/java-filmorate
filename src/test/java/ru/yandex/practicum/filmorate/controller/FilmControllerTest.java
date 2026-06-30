package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    // создание фильма с описанием ровно 200 символов → успех
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

    // создание фильма с description = null → успех (поле необязательное)
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

    // создание фильма с датой релиза 27.12.1895 (раньше 28.12.1895) → ошибка валидации
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

    // создание фильма с датой релиза ровно 28.12.1895 (граничное значение) → успех
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

    // создание фильма с duration = 0 → ошибка валидации
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

    // создание фильма с duration = -1 → ошибка валидации
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

    // создание полностью валидного фильма → успех, присваивается id
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

    // последовательное создание двух фильмов → id увеличиваются: 1, 2
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

    // обновление фильма без id → ошибка валидации
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

        assertThrows(ResponseStatusException.class, () -> controller.update(update));
    }

    // обновление существующего фильма валидными данными → все поля меняются
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
