package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на создание фильма: {}", film);
        if (film.getReleaseDate() != null && film.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: дата релиза {}, раньше 28.12.1895", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: продолжительность фильма {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Ошибка: id фильма не указан");
            throw new ValidationException("id фильма не указан");
        }
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: дата релиза {}, раньше 28.12.1895", newFilm.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (newFilm.getDuration() <= 0) {
            log.warn("Ошибка валидации: продолжительность фильма {}", newFilm.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id={} не найден", newFilm.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм с id=" + newFilm.getId() + " не найден");
        }
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        log.info("Обновлён фильм: id={}, name={}", oldFilm.getId(), oldFilm.getName());
        return oldFilm;
    }

    private long nextId = 1;

    private long getNextId() {
        return nextId++;
    }
}
