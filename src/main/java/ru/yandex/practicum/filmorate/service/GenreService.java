package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
<<<<<<< HEAD
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
=======
import ru.yandex.practicum.filmorate.storage.GenreStorage;
>>>>>>> develop

import java.util.List;

@Service
public class GenreService {
<<<<<<< HEAD
    private final GenreDbStorage genreStorage;

    public GenreService(GenreDbStorage genreStorage) {
=======
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
>>>>>>> develop
        this.genreStorage = genreStorage;
    }

    public List<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}
