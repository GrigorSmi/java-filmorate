package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;

@Data
@Builder
public class FeedEvent {
    private Long timestamp;
    private Long userId;
    private FeedEventType eventType;
    private FeedEventOperation operation;
    private Long eventId;
    private Long entityId;
}
