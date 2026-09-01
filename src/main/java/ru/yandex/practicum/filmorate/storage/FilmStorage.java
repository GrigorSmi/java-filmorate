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

    void addMark(Long filmId, Long userId, int value);

    void removeMark(Long filmId, Long userId);

    List<Film> getPopular(int count, Long genreId, Integer year);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> search(String query, String by);
}
