package ru.yandex.practicum.filmorate.storage.db;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("findOne query: {}", query);
            log.debug("findOne params: {}", Arrays.toString(params));
        }
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("findMany query: {}", query);
            log.debug("findMany params: {}", Arrays.toString(params));
        }
        return jdbc.query(query, mapper, params);
    }

    protected long insert(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Insert query: {}", query);
            log.debug("Insert params: {}", Arrays.toString(params));
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id новой строки
        if (id != null) {
            return id;
        } else {
            String msg = "Не удалось сохранить данные. Query: %s".formatted(query);
            log.debug(msg);
            throw new InternalServerException(msg);
        }
    }

    protected void insertWithCompositePrimaryKey(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Insert with composite PK query: {}", query);
            log.debug("Insert params: {}", Arrays.toString(params));
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();

        if (keys == null) {
            String msg = "Не удалось сохранить данные. Query: %s".formatted(query);
            log.debug(msg);
            throw new InternalServerException(msg);
        }
    }

    protected int update(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Update query: {}", query);
            log.debug("Update params: {}", Arrays.toString(params));
        }
        return jdbc.update(query, params);
    }

    protected boolean delete(String query, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Delete query: {}", query);
            log.debug("Delete params: {}", Arrays.toString(params));
        }
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }
}

