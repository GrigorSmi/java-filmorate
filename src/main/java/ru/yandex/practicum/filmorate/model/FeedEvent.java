package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedEvent {
    private LocalDateTime timestamp;
    private Long userId;
    private FeedEventType eventType;
    private FeedEventOperation operation;
    private Long eventId; // primary key
    /* идентификатор сущности, с которой произошло событие:
    * LIKE - filmId, которому поставили лайк
    * REVIEW - reviewId отзыва
    * FRIEND - userId друга
    * */
    private Long entityId;
}
