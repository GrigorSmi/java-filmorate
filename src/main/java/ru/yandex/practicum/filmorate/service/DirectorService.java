package ru.yandex.practicum.filmorate.service;

<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
=======
>>>>>>> develop
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

<<<<<<< HEAD
@Slf4j
=======
>>>>>>> develop
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

<<<<<<< HEAD
    public Director create(Director director) {
        log.info("Создание режиссёра: {}", director);
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        log.info("Обновление режиссёра: {}", director);
        directorStorage.getById(director.getId())
=======
    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public Director add(Director director) {
        return directorStorage.add(director);
    }

    public Director update(Director director) {
        directorStorage.findById(director.getId())
>>>>>>> develop
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + director.getId() + " не найден"));
        return directorStorage.update(director);
    }

    public void delete(Long id) {
<<<<<<< HEAD
        log.info("Удаление режиссёра с id={}", id);
        directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
        directorStorage.delete(id);
    }

    public Director getById(Long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public List<Director> getAll() {
        return directorStorage.getAll();
    }
}
=======
        directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
        directorStorage.delete(id);
    }
}
>>>>>>> develop
