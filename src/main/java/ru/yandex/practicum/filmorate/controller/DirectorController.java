package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Long id) {
        return directorService.findById(id);
    }

    @PostMapping
    public Director add(@Valid @RequestBody Director director) {
        log.info("Запрос на создание режиссёра: {}", director);
        Director created = directorService.add(director);
        log.info("Добавлен режиссёр: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Запрос на обновление режиссёра: {}", director);
        if (director.getId() == null) {
            log.warn("Ошибка: id режиссёра не указан");
            throw new ValidationException("id режиссёра не указан");
        }
        Director updated = directorService.update(director);
        log.info("Обновлён режиссёр: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление режиссёра: id={}", id);
        directorService.delete(id);
    }
}
