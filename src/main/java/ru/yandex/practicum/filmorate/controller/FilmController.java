package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на создание фильма: {}", film);
        Film created = filmService.add(film);
        log.info("Добавлен фильм: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Ошибка: id фильма не указан");
            throw new ValidationException("id фильма не указан");
        }
        Film updated = filmService.update(newFilm);
        log.info("Обновлён фильм: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление фильма: id={}", id);
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на лайк фильма {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на снятие лайка фильма {} пользователем {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос популярных фильмов: count={}", count);
        return filmService.getPopular(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                          @RequestParam(defaultValue = "year") String sortBy) {
        log.info("Запрос фильмов режиссёра: directorId={}, sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}
