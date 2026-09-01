package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam String query,
                             @RequestParam(defaultValue = "title") String by) {
        log.info("Запрос поиска: query={}, by={}", query, by);
        return filmService.search(query, by);
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

    @PutMapping("/{id}/marks/{userId}")
    public void addMark(@PathVariable Long id,
                        @PathVariable Long userId,
                        @RequestParam Double value) {
        log.info("Запрос на оценку {} от пользователя {} со значением {}", id, userId, value);
        filmService.addMark(id, userId, value);
    }

    @DeleteMapping("/{id}/marks/{userId}")
    public void removeMark(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление оценки фильма {} пользователем {}", id, userId);
        filmService.removeMark(id, userId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на лайк фильма {} от пользователя {} (совместимость, оценка 10)", id, userId);
        filmService.addMark(id, userId, 10);
    }

    @PutMapping("/{id}/like/{userId}/{value}")
    public void addLikeWithValue(@PathVariable Long id,
                                 @PathVariable Long userId,
                                 @PathVariable Double value) {
        log.info("Запрос на лайк фильма {} от пользователя {} со значением {} (совместимость)", id, userId, value);
        filmService.addMark(id, userId, value);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка фильма {} пользователем {} (совместимость)", id, userId);
        filmService.removeMark(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        log.info("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                          @RequestParam(defaultValue = "year") String sortBy) {
        log.info("Запрос фильмов режиссёра: directorId={}, sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Long userId,
                                     @RequestParam Long friendId) {
        log.info("Запрос общих фильмов: userId={}, friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
