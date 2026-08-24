package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component
@Qualifier("memory")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (id == null || !users.containsKey(id)) {
            log.warn("Пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        users.put(id, user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void clearAll() {
        users.clear();
        nextId = 1;
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        return Collections.emptyList();
    }
}
