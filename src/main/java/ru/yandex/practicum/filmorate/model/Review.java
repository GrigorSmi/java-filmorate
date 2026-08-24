package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long id;
    private String content;
    private Boolean isPositive; // Lombok сгенерирует getIsPositive() для этого поля
    private Long userId;
    private Long filmId;
    private Integer useful;
}