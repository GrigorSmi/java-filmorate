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

    public List<Film> getPopular(int count) {
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getPopular(count);
        }
        return filmStorage.findAll().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getLikes().size(), a.getLikes().size());
                    return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
                })
                .limit(count)
                .toList();
    }

    public void delete(Long id) {
        filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        filmStorage.delete(id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId);
        if (filmStorage instanceof FilmDbStorage dbStorage) {
            return dbStorage.getFilmsByDirector(directorId, sortBy);
        }
        return List.of();
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        return filmStorage.getCommonFilms(userId, friendId);
    }
    
    public List<Film> search(String query, String by) {
        return filmStorage.search(query, by);
    }
}
