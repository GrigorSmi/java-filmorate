package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    void delete(Long id);

    Collection<User> findAll();

    Optional<User> findById(Long id);

    void clearAll();

    List<Film> getRecommendations(Long userId);
}