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

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addMark(Long filmId, Long userId, double value) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.addMark(filmId, userId, value);
        feedEventService.addEvent(userId, FeedEventType.MARK, FeedEventOperation.ADD, filmId);
    }

    public void removeMark(Long filmId, Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.removeMark(filmId, userId);
        feedEventService.addEvent(userId, FeedEventType.MARK, FeedEventOperation.REMOVE, filmId);
    }

    public List<Film> getPopular(int count, Long genreId, Integer year) {
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void delete(Long id) {
        filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        filmStorage.delete(id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> search(String query, String by) {
        return filmStorage.search(query, by);
    }
}
