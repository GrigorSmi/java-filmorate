package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaRatingService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final FeedEventService feedEventService;

    public FilmService(@Qualifier("db") FilmStorage filmStorage,
                       @Qualifier("db") UserStorage userStorage,
                       MpaRatingService mpaService,
                       GenreService genreService,
                       DirectorService directorService,
                       FeedEventService feedEventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
        this.feedEventService = feedEventService;
    }

    private void validateFilmReferences(Film film) {
        if (film.getMpa() != null) {
            mpaService.findById(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }
        if (film.getDirectors() != null) {
            for (var director : film.getDirectors()) {
                directorService.findById(director.getId());
            }
        }
    }

    public Film add(Film film) {
        validateFilmReferences(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateFilmReferences(film);
        return filmStorage.update(film);
    }

    public void delete(Long id) {
        log.info("Удаление фильма с id={}", id);
        findById(id);
        filmStorage.delete(id);

        if (!feedEventService.deleteByEntityId(FeedEventType.LIKE, id)) {
            log.warn("При удалении фильма id={} не было удаления событий из ленты!", id);
        }
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
        findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.addLike(filmId, userId);

        feedEventService.addEvent(userId, FeedEventType.LIKE, FeedEventOperation.ADD, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка пользователя {} у фильма {}", userId, filmId);
        findById(filmId);
        filmStorage.removeLike(filmId, userId);

        feedEventService.addEvent(userId, FeedEventType.LIKE, FeedEventOperation.REMOVE, filmId);
    }

    public List<Film> getPopular(int count, Long genreId, Integer year) {
        log.info("Получение популярных фильмов. count={}, genreId={}, year={}", count, genreId, year);
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссера {}. Сортировка: {}", directorId, sortBy);
        directorService.findById(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение общих фильмов: userId={}, friendId={}", userId, friendId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> search(String query, String by) {
        log.info("Поиск фильмов: query={}, by={}", query, by);
        return filmStorage.search(query, by);
    }
}
