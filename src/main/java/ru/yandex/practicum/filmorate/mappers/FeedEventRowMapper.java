package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedEventRowMapper implements RowMapper<FeedEvent> {
    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FeedEvent.builder()
                .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
                .userId(rs.getLong("user_id"))
                .eventType(FeedEventType.valueOf(rs.getString("event_type")))
                .operation(FeedEventOperation.valueOf(rs.getString("operation")))
                .eventId(rs.getLong("id"))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
