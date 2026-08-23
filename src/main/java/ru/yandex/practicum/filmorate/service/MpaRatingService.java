package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.List;

@Service
public class MpaRatingService {
    private final MpaRatingStorage mpaStorage;

    public MpaRatingService(MpaRatingStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<MpaRating> findAll() {
        return mpaStorage.findAll();
    }

    public MpaRating findById(Long id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + id + " не найден"));
    }
}