package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
@Qualifier("memory")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film add(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        if (id == null || !films.containsKey(id)) {
            log.warn("Фильм с id={} не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        films.put(id, film);
        return film;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void clearAll() {
        films.clear();
        nextId = 1;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (!film.getLikes().add(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
        } else {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (!film.getLikes().remove(userId)) {
            log.warn("Лайк пользователя {} у фильма {} не найден", userId, filmId);
        } else {
            log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        return films.values().stream()
                .filter(film -> genreId == null || (film.getGenres() != null && film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId))))
                .filter(film -> year == null || (film.getReleaseDate() != null && film.getReleaseDate().getYear() == year))
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .toList();
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        return java.util.Collections.emptyList(); // Для InMemory можно оставить пустым
    }
}
