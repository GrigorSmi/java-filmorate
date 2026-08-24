package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaRatingService mpaRatingService;
    private final GenreService genreService;

    public Film add(Film film) {
        // 1. ПРОВЕРКА СУЩЕСТВОВАНИЯ MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaRatingService.findById(film.getMpa().getId());
            // Если не найдет, метод findById сам выбросит NotFoundException (который даст 404)
        }

        // 2. ПРОВЕРКА СУЩЕСТВОВАНИЯ ВСЕХ ЖАНРОВ
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
                // Если не найдет, метод findById сам выбросит NotFoundException (который даст 404)
            }
        }

        log.info("Добавление фильма: {}", film.getName());
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        log.info("Обновление фильма с id={}: {}", film.getId(), film.getName());
        findById(film.getId()); // Проверяем, что сам фильм существует

        // 1. ПРОВЕРКА СУЩЕСТВОВАНИЯ MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaRatingService.findById(film.getMpa().getId());
        }

        // 2. ПРОВЕРКА СУЩЕСТВОВАНИЯ ВСЕХ ЖАНРОВ
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }

        return filmStorage.update(film);
    }

    public void delete(Long id) {
        log.info("Удаление фильма с id={}", id);
        // Проверяем существование перед удалением (опционально, но безопасно)
        findById(id);
        filmStorage.delete(id);
    }

    public Collection<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        log.info("Поиск фильма по id={}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка пользователем {} к фильму {}", userId, filmId);
        findById(filmId); // Проверяем существование фильма
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка пользователя {} у фильма {}", userId, filmId);
        findById(filmId); // Проверяем существование фильма
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count, Long genreId, Integer year) {
        log.info("Получение популярных фильмов. count={}, genreId={}, year={}", count, genreId, year);
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссера {}. Сортировка: {}", directorId, sortBy);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> search(String query, String by) {
        log.info("Поиск фильмов: query={}, by={}", query, by);
        return filmStorage.search(query, by);
    }
}