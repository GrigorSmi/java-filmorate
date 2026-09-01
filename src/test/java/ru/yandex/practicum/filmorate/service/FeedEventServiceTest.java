package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.exception.FeedEventException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class FeedEventServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private FeedEventService feedEventService;

    private User user;
    private Film film;

    @BeforeEach
    void setUp() {
        user = addUser("");

        film = addFilm("");
    }

    @Test
    void feedEventsForFriendship() {
        User user1 = addUser("1");

        userService.addFriend(user.getId(), user1.getId());

        List<FeedEvent> afterAdd = feedEventService.findByUserId(user.getId());
        assertTrue(afterAdd.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user1.getId())));

        userService.removeFriend(user.getId(), user1.getId());

        List<FeedEvent> afterRemove = feedEventService.findByUserId(user.getId());
        assertTrue(afterRemove.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(user1.getId())));
    }

    @Test
    void feedEventsForLikes() {
        User user1 = addUser("1");

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user1.getId());

        List<FeedEvent> userLikes = feedEventService.findByUserId(user.getId());
        assertTrue(userLikes.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(film.getId())));

        List<FeedEvent> user1Likes = feedEventService.findByUserId(user1.getId());
        assertTrue(user1Likes.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(film.getId())));

        filmService.removeLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user1.getId());

        List<FeedEvent> userRemoves = feedEventService.findByUserId(user.getId());
        assertTrue(userRemoves.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(film.getId())));

        List<FeedEvent> user1Removes = feedEventService.findByUserId(user1.getId());
        assertTrue(user1Removes.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(film.getId())));
    }

    @Test
    void feedEventsCleanupAfterUserDeletion() {
        User user1 = addUser("1");

        userService.addFriend(user.getId(), user1.getId());
        List<FeedEvent> afterAdd1 = feedEventService.findByUserId(user.getId());
        assertTrue(afterAdd1.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user1.getId())));

        userService.removeFriend(user.getId(), user1.getId());
        List<FeedEvent> afterRemove1 = feedEventService.findByUserId(user.getId());
        assertTrue(afterRemove1.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(user1.getId())));

        User user2 = addUser("2");

        userService.addFriend(user.getId(), user2.getId());
        List<FeedEvent> afterAdd2 = feedEventService.findByUserId(user.getId());
        assertTrue(afterAdd2.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user2.getId())));
        assertEquals(3, afterAdd2.size());

        userService.delete(user1.getId());

        List<FeedEvent> afterDelete = feedEventService.findByUserId(user.getId());
        assertTrue(afterDelete.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user2.getId())));
        assertEquals(3, afterDelete.size());
    }

    @Test
    void feedEventsCleanupAfterFilmDeletion() {
        User user1 = addUser("1");

        Film film1 = addFilm("1");

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user1.getId());

        filmService.addLike(film1.getId(), user.getId());
        filmService.addLike(film1.getId(), user1.getId());
        filmService.removeLike(film1.getId(), user1.getId());

        assertEquals(2, likeEvents(user.getId()).size());
        assertEquals(3, likeEvents(user1.getId()).size());

        filmService.delete(film1.getId());

        List<FeedEvent> userEventsAfter = feedEventService.findByUserId(user.getId());
        List<FeedEvent> user1EventsAfter = feedEventService.findByUserId(user1.getId());

        assertTrue(userEventsAfter.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film.getId())));
        assertTrue(user1EventsAfter.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film.getId())));

        assertEquals(2, userEventsAfter.size());
        assertEquals(3, user1EventsAfter.size());
    }

    @Test
    void addEventWithNullParamThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.addEvent(null,
                        FeedEventType.LIKE,
                        FeedEventOperation.ADD,
                        film.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.addEvent(user.getId(),
                        null,
                        FeedEventOperation.ADD,
                        film.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.addEvent(user.getId(),
                        FeedEventType.LIKE,
                        null,
                        film.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.addEvent(user.getId(),
                        FeedEventType.LIKE,
                        FeedEventOperation.ADD,
                        null));
    }

    @Test
    void addEventForNonExistentUserThrowsFeedEventException() {
        assertThrows(FeedEventException.class,
                () -> feedEventService.addEvent(9999L,
                        FeedEventType.LIKE,
                        FeedEventOperation.ADD,
                        1L));
    }

    @Test
    void findByUserIdForNonExistentUserThrowsNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> feedEventService.findByUserId(9999L));
    }

    @Test
    void deleteByEntityIdReturnsTrueWhenEventsExistAndFalseWhenNot() {
        User user1 = addUser("1");
        userService.addFriend(user.getId(), user1.getId());

        boolean first = feedEventService.deleteByEntityId(FeedEventType.FRIEND, user1.getId());
        assertTrue(first);

        boolean second = feedEventService.deleteByEntityId(FeedEventType.FRIEND, user1.getId());
        assertFalse(second);
    }

    @Test
    void deleteByEntityIdWithNullParamThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.deleteByEntityId(null, 1L));
        assertThrows(IllegalArgumentException.class,
                () -> feedEventService.deleteByEntityId(FeedEventType.LIKE, null));
    }

    @Test
    void deleteByEntityIdIsolatedByEventType() {
        Long entityId = 42L;
        feedEventService.addEvent(user.getId(),
                FeedEventType.FRIEND,
                FeedEventOperation.ADD,
                entityId);
        feedEventService.addEvent(user.getId(),
                FeedEventType.LIKE,
                FeedEventOperation.ADD,
                entityId);

        boolean deleted = feedEventService.deleteByEntityId(FeedEventType.LIKE, entityId);
        assertTrue(deleted);

        List<FeedEvent> events = feedEventService.findByUserId(user.getId());
        assertTrue(events.stream()
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getEntityId().equals(entityId)));
        assertTrue(events.stream()
                .noneMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(entityId)));
    }

    @Test
    void feedEventsOrderedByTimestampDesc() throws InterruptedException {
        User user1 = addUser("1");

        feedEventService.addEvent(user.getId(),
                FeedEventType.FRIEND,
                FeedEventOperation.ADD,
                user1.getId());
        Thread.sleep(5);
        feedEventService.addEvent(user.getId(),
                FeedEventType.LIKE,
                FeedEventOperation.ADD,
                film.getId());
        Thread.sleep(5);
        feedEventService.addEvent(user.getId(),
                FeedEventType.FRIEND,
                FeedEventOperation.REMOVE,
                user1.getId());

        List<FeedEvent> events = feedEventService.findByUserId(user.getId());

        assertEquals(3, events.size());
        assertEquals(FeedEventType.FRIEND, events.get(0).getEventType());
        assertEquals(FeedEventOperation.ADD, events.get(0).getOperation());
        assertEquals(FeedEventType.FRIEND, events.get(2).getEventType());
        assertEquals(FeedEventOperation.REMOVE, events.get(2).getOperation());
        assertTrue(events.get(0).getTimestamp() <= events.get(1).getTimestamp());
        assertTrue(events.get(1).getTimestamp() <= events.get(2).getTimestamp());
    }

    private List<FeedEvent> likeEvents(Long userId) {
        return feedEventService.findByUserId(userId)
                .stream()
                .filter(e -> e.getEventType() == FeedEventType.LIKE)
                .toList();
    }

    private Film addFilm(String suffix) {
        Film film = new Film();
        film.setName("Film" + suffix);
        film.setDescription("desc" + suffix);
        film.setReleaseDate(LocalDate.of(2010, 5, 5));
        film.setDuration(90L);
        MpaRating mpa2 = new MpaRating();
        mpa2.setId(1L);
        film.setMpa(mpa2);
        return filmService.add(film);
    }

    private User addUser(String suffix) {
        User user = new User();
        user.setEmail("user" + suffix + "@mail.com");
        user.setLogin("login" + suffix);
        user.setName("name" + suffix);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.add(user);
    }
}
