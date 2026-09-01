package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;


public interface FeedEventStorage {
    FeedEvent add(FeedEvent feedEvent);

    /* TODO Возвращать во всех интерфейсах List, вместо Collection */
    List<FeedEvent> findByUserId(Long userId);

    boolean deleteByTypeAndEntityId(FeedEventType eventType, Long entityId);

    boolean deleteById(Long id);
}
