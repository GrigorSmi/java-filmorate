package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedEventService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FeedEventService feedEventService;

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable @Positive Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        User created = userService.add(user);
        log.info("Добавлен пользователь: id={}, email={}", created.getId(), created.getEmail());
        return created;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Запрос на обновление пользователя: {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Ошибка: id пользователя не указан");
            throw new ValidationException("id пользователя не указан");
        }
        User updated = userService.update(newUser);
        log.info("Обновлён пользователь: id={}, email={}", updated.getId(), updated.getEmail());
        return updated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя: id={}", id);
        userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive Long id, @PathVariable @Positive Long friendId) {
        log.info("Запрос на добавление друга: userId={}, friendId={}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Positive Long id, @PathVariable @Positive Long friendId) {
        log.info("Запрос на удаление друга: userId={}, friendId={}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable @Positive Long id) {
        log.info("Запрос списка друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive Long id, @PathVariable @Positive Long otherId) {
        log.info("Запрос общих друзей: userId={}, otherId={}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<FeedEvent> getUserFeed(@PathVariable @Positive Long id) {
        log.info("Запрос ленты событий: userId={}", id);
        return feedEventService.findByUserId(id);
    }
}
