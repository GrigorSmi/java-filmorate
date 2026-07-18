package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipRepository;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipRepository friendshipRepository;

    public UserService(UserStorage userStorage, FriendshipRepository friendshipRepository) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
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

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .ifPresentOrElse(
                        friendship -> log.info("Запрос на дружбу уже существует: {} -> {}", userId, friendId),
                        () -> {
                            Friendship friendship = new Friendship();
                            friendship.setUser(userStorage.findById(userId).orElseThrow());
                            friendship.setFriend(userStorage.findById(friendId).orElseThrow());
                            friendship.setStatus(FriendshipStatus.UNCONFIRMED);
                            friendshipRepository.save(friendship);
                            log.info("Пользователь {} отправил запрос на дружбу пользователю {}", userId, friendId);
                        }
                );
    }

    public void removeFriend(Long userId, Long friendId) {
        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);
        log.info("Дружба между {} и {} удалена", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<Friendship> asUser = friendshipRepository.findByUserId(userId);
        List<Friendship> asFriend = friendshipRepository.findByFriendId(userId);

        return Stream.concat(asUser.stream(), asFriend.stream())
                .map(f -> f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        List<User> friends1 = getFriends(userId);
        List<User> friends2 = getFriends(otherId);

        return friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toList());
    }
}
