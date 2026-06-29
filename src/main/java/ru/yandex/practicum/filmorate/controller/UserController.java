package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Запрос на обновление пользователя: {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Ошибка: id пользователя не указан");
            return null;
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.debug("Имя пользователя пустое, будет использован логин: {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id={} не найден", newUser.getId());
            return null;
        }
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Обновлён пользователь: id={}, email={}", oldUser.getId(), oldUser.getEmail());
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации: {}", message);
        throw new ValidationException(message);
    }
}