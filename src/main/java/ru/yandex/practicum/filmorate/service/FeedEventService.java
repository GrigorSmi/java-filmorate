package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.exception.FeedEventException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import ru.yandex.practicum.filmorate.storage.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FeedEventService {
    private final FeedEventStorage feedEventStorage;
    private final UserStorage userStorage;

    public FeedEventService(FeedEventStorage feedEventStorage,
                            @Qualifier("db") UserStorage userStorage) {
        this.feedEventStorage = feedEventStorage;
        this.userStorage = userStorage;
    }

    /**
     * Создает событие в ленте активности
     *
     * @param userId    идентификатор пользователя (не null)
     * @param eventType тип события (не null)
     * @param operation операция (не null)
     * @param entityId  идентификатор сущности (не null)
     * @throws IllegalArgumentException если любой параметр null
     * @throws FeedEventException       если пользователь не найден
     */
    public void addEvent(Long userId, FeedEventType eventType, FeedEventOperation operation, Long entityId) {
        // Проверка обязательных параметров
        if (userId == null || eventType == null || operation == null || entityId == null) {
            log.error("Попытка создать событие с null параметрами: userId={}, eventType={}, operation={}, entityId={}",
                    userId, eventType, operation, entityId);
            throw new IllegalArgumentException("Параметры события не могут быть null");
        }

        userStorage.findById(userId)
                .orElseThrow(() -> new FeedEventException("Нельзя создать событие " +
                        "для несуществующего пользователя: " + userId)
                );

        FeedEvent event = feedEventStorage.add(FeedEvent.builder()
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build());
        log.info("Создано событие: {}", event);
    }

    public List<FeedEvent> findByUserId(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return feedEventStorage.findByUserId(userId);
    }

    /**
     * Удаляем события из ленты при удалении сущностей, на которые ссылается entityId:
     * - LIKE: filmId
     * - FRIEND: userId друга (friendId)
     * При удалении пользователя по userId, события ленты удалятся на уровне БД (ON DELETE CASCADE)
     * Параметры: entityId могут совпасть для разных типов сущностей, необходимо учитывать eventType
     *
     * @param eventType тип сущности
     * @param entityId  id сущности
     * @return Было ли удалено хотя бы одно событие
     */
    public boolean deleteByEntityId(FeedEventType eventType, Long entityId) {
        // Проверка обязательных параметров
        if (eventType == null || entityId == null) {
            log.error("Попытка удалить событие с null параметрами: eventType={}, entityId={}",
                    eventType, entityId);
            throw new IllegalArgumentException(("Параметры события " +
                    "eventType=%s, entityId=%s для удаления не могут быть null").formatted(eventType, entityId));
        }

        log.info("Удаляем событие с параметрами: eventType={}, entityId={}", eventType, entityId);
        if (feedEventStorage.deleteByTypeAndEntityId(eventType, entityId)) {
            log.info("Как минимум одно событие с параметрами eventType={}, entityId={} удалено", eventType, entityId);
            return true;
        }

        log.info("Ошибка удаления: событий с параметрами eventType={}, entityId={} не найдено", eventType, entityId);
        return false;
    }

    public boolean delete(Long eventId) {
        return feedEventStorage.deleteById(eventId);
    }
}
