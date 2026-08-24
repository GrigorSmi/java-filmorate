package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> findAll();

    Optional<Director> findById(Long id);

    Director add(Director director);

    Director update(Director director);

    void delete(Long id);
}
