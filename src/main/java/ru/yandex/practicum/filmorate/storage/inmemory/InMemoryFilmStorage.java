package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("memory")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Map<Long, Double>> marksByFilm = new HashMap<>();
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
        marksByFilm.clear();
        nextId = 1;
    }

    @Override
    public void addMark(Long filmId, Long userId, double value) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        Map<Long, Double> marks = marksByFilm.computeIfAbsent(filmId, k -> new HashMap<>());
        marks.put(userId, value);
        recalcRating(film);
    }

    @Override
    public void removeMark(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        Map<Long, Double> marks = marksByFilm.get(filmId);
        if (marks != null) {
            marks.remove(userId);
        }
        recalcRating(film);
    }

    private void recalcRating(Film film) {
        Map<Long, Double> marks = marksByFilm.get(film.getId());
        if (marks == null || marks.isEmpty()) {
            film.setRating(null);
        } else {
            double avg = marks.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            film.setRating(avg);
        }
    }

    @Override
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        return films.values().stream()
                .filter(film -> genreId == null || (film.getGenres() != null && film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId))))
                .filter(film -> year == null || (film.getReleaseDate() != null && film.getReleaseDate().getYear() == year))
                .sorted((a, b) -> Double.compare(
                        b.getRating() == null ? 0.0 : b.getRating(),
                        a.getRating() == null ? 0.0 : a.getRating()))
                .limit(count)
                .toList();
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        return List.of();
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return films.values().stream()
                .filter(f -> marksByFilm.containsKey(f.getId())
                        && marksByFilm.get(f.getId()).containsKey(userId)
                        && marksByFilm.get(f.getId()).containsKey(friendId))
                .sorted((a, b) -> {
                    int cmp = Double.compare(
                            b.getRating() == null ? 0.0 : b.getRating(),
                            a.getRating() == null ? 0.0 : a.getRating());
                    return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
                })
                .toList();
    }

    @Override
    public List<Film> search(String query, String by) {
        String lowerQuery = query.toLowerCase();
        boolean searchTitle = by.contains("title");
        boolean searchDirector = by.contains("director");

        return films.values().stream()
                .filter(film -> {
                    boolean matchTitle = searchTitle && film.getName().toLowerCase().contains(lowerQuery);
                    boolean matchDirector = searchDirector && film.getDirectors() != null &&
                            film.getDirectors().stream()
                                    .anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery));
                    return matchTitle || matchDirector;
                })
                .sorted(Comparator.comparingDouble((Film f) -> f.getRating() == null ? 0.0 : f.getRating()).reversed()
                        .thenComparing(Film::getId))
                .collect(Collectors.toList());
    }
}
