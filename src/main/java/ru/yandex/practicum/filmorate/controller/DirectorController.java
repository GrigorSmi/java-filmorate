package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    public Director create(@RequestBody Director director) {
        log.info("Запрос на создание режиссёра: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Запрос на обновление режиссёра: {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление режиссёра с id={}", id);
        directorService.delete(id);
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        log.info("Запрос режиссёра по id={}", id);
        return directorService.getById(id);
    }

    @GetMapping
    public List<Director> getAll() {
        log.info("Запрос списка всех режиссёров");
        return directorService.getAll();
    }
}