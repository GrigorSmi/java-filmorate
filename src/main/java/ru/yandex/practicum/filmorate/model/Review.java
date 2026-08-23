package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull; // <-- Добавь этот импорт
import lombok.Data;

@Data
public class Review {
    private Long reviewId;

    private String content;
    private Boolean isPositive;

    @NotNull(message = "ID пользователя не может быть пустым") // <-- ДОБАВЬ ЭТО
    private Long userId;

    @NotNull(message = "ID фильма не может быть пустым")       // <-- ДОБАВЬ ЭТО
    private Long filmId;

    private Integer useful;
}