package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

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

    public FilmService(@Qualifier("db") FilmStorage filmStorage,
                       @Qualifier("db") UserStorage userStorage,
                       MpaRatingService mpaService,
                       GenreService genreService,
                       DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
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

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        filmStorage.removeLike(filmId, userId);
    }

    // Универсальный метод, который делегирует вызов в конкретную реализацию хранилища
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        // Если используется реализация для БД, вызываем её оптимизированный метод
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getPopular(count, genreId, year);
        }
        // Иначе вызываем метод из интерфейса (например, для InMemory реализации)
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId);
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getFilmsByDirector(directorId, sortBy);
        }
        return List.of();
    }
}
