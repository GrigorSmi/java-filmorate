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
<<<<<<< HEAD
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;
=======
>>>>>>> develop

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
<<<<<<< HEAD
    private final MpaRatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(@Qualifier("db") FilmStorage filmStorage,
                       @Qualifier("db") UserStorage userStorage,
                       MpaRatingDbStorage mpaStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
=======
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
>>>>>>> develop
    }

    private void validateFilmReferences(Film film) {
        if (film.getMpa() != null) {
<<<<<<< HEAD
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + genre.getId() + " не найден"));
=======
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
>>>>>>> develop
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

<<<<<<< HEAD
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getPopular(count, genreId, year);
        }
        return filmStorage.findAll().stream()
                .filter(film -> genreId == null || (film.getGenres() != null && film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId))))
                .filter(film -> year == null || (film.getReleaseDate() != null && film.getReleaseDate().getYear() == year))
=======
    public List<Film> getPopular(int count) {
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getPopular(count);
        }
        return filmStorage.findAll().stream()
>>>>>>> develop
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getLikes().size(), a.getLikes().size());
                    return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
                })
                .limit(count)
                .toList();
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
<<<<<<< HEAD
        // Проверка существования режиссера делегируется в storage или сервис режиссеров
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getFilmsByDirector(directorId, sortBy);
        }
        return java.util.Collections.emptyList();
=======
        directorService.findById(directorId);
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getFilmsByDirector(directorId, sortBy);
        }
        return List.of();
>>>>>>> develop
    }
}
