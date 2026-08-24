package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FeedEventService feedEventService;
    private final JdbcTemplate jdbc;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    public UserService(@Qualifier("db") UserStorage userStorage,
                       FeedEventService feedEventService,
                       JdbcTemplate jdbc) {
        this.userStorage = userStorage;
        this.feedEventService = feedEventService;
        this.jdbc = jdbc;
    }

    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
        }
        return userStorage.add(user);
    }

    public User update(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
        }
        return userStorage.update(user);
    }

    public List<User> findAll() {
        return userStorage.findAll().stream().toList();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void delete(Long id) {
        log.info("Удаление пользователя с id={}", id);
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        userStorage.delete(id);

        if (!feedEventService.deleteByEntityId(FeedEventType.FRIEND, id)) {
            log.warn("При удалении пользователя id={} не было удаления событий из ленты!", id);
        }
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        List<Long> existing = jdbc.queryForList(
                "SELECT id FROM friendships WHERE user_id = ? AND friend_id = ?",
                Long.class, userId, friendId
        );
        if (!existing.isEmpty()) {
            log.info("Запрос на дружбу уже существует: {} -> {}", userId, friendId);
            return;
        }

        jdbc.update("INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)",
                userId, friendId, FriendshipStatus.UNCONFIRMED.name());
        log.info("Пользователь {} отправил запрос на дружбу пользователю {}", userId, friendId);

        feedEventService.addEvent(userId, FeedEventType.FRIEND, FeedEventOperation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
        log.info("Дружба между {} и {} удалена", userId, friendId);

        feedEventService.addEvent(userId, FeedEventType.FRIEND, FeedEventOperation.REMOVE, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        return jdbc.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM users u JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? ORDER BY u.id",
                userRowMapper, userId
        );
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + otherId + " не найден"));

        return jdbc.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ? " +
                "ORDER BY u.id",
                userRowMapper, userId, otherId
        );
    }

    public List<Long> getRecommendations(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        return jdbc.queryForList(
                "SELECT DISTINCT l2.user_id " +
                "FROM likes l1 " +
                "JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id = l2.user_id " +
                "WHERE l1.user_id = ? " +
                "AND l2.user_id IN (SELECT DISTINCT l3.user_id FROM likes l3 WHERE l3.user_id != ?) " +
                "AND l2.user_id NOT IN (SELECT f.friend_id FROM friendships f WHERE f.user_id = ?) " +
                "GROUP BY l2.user_id " +
                "ORDER BY COUNT(DISTINCT l2.film_id) DESC",
                Long.class, userId, userId, userId
        );
    }
}
