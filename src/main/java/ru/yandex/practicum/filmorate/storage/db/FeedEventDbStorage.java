package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.FeedEventStorage;

import java.util.List;


@Repository
public class FeedEventDbStorage extends BaseDbStorage<FeedEvent> implements FeedEventStorage {
    private static final String INSERT_QUERY = """
            INSERT INTO events (user_id, event_type, operation, entity_id, timestamp)
            VALUES (?, ?, ?, ?, ?);""";
    private static final String FIND_BY_USER_ID_QUERY = """
            SELECT id, user_id, event_type, operation, entity_id, timestamp
            FROM events
            WHERE user_id = ?
            ORDER BY timestamp DESC;""";
    private static final String DELETE_BY_TYPE_AND_ENTITY_ID_QUERY = """
            DELETE
            FROM events
            WHERE event_type = ? and entity_id = ?;""";
    private static final String DELETE_BY_ID_QUERY = """
            DELETE
            FROM events
            WHERE id = ?;""";

    public FeedEventDbStorage(JdbcTemplate jdbc, RowMapper<FeedEvent> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public FeedEvent add(FeedEvent feedEvent) {
        Long id = insert(INSERT_QUERY, feedEvent.getUserId(),
                feedEvent.getEventType().name(),
                feedEvent.getOperation().name(),
                feedEvent.getEntityId(),
                feedEvent.getTimestamp());
        feedEvent.setEventId(id);
        return feedEvent;
    }

    @Override
    public List<FeedEvent> findByUserId(Long userId) {
        return findMany(FIND_BY_USER_ID_QUERY, userId);
    }

    @Override
    public boolean deleteByTypeAndEntityId(FeedEventType eventType, Long entityId) {
        return delete(DELETE_BY_TYPE_AND_ENTITY_ID_QUERY, eventType.name(), entityId);
    }

    @Override
    public boolean deleteById(Long id) {
        return delete(DELETE_BY_ID_QUERY, id);
    }
}
