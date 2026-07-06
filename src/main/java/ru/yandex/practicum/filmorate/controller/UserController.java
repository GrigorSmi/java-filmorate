package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
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
        if (newUser.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: логин содержит пробелы: {}", newUser.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.debug("Имя пользователя пустое, будет использован логин: {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id={} не найден", newUser.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id=" + newUser.getId() + " не найден");
        }
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Обновлён пользователь: id={}, email={}", oldUser.getId(), oldUser.getEmail());
        return oldUser;
    }

    private long nextId = 1;

    private long getNextId() {
        return nextId++;
    }
}
