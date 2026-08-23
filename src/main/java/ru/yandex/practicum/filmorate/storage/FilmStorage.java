package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void delete(Long id);

    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    void clearAll();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);


    List<Film> getPopular(int count, Long genreId, Integer year);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);
}