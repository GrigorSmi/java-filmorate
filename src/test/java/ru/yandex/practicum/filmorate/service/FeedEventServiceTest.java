package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.enums.FeedEventOperation;
import ru.yandex.practicum.filmorate.enums.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(afterAdd)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user1.getId()));

        userService.removeFriend(user.getId(), user1.getId());

        List<FeedEvent> afterRemove = feedEventService.findByUserId(user.getId());
        assertThat(afterRemove)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(user1.getId()));
    }

    @Test
    void feedEventsForLikes() {
        User user1 = addUser("1");

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user1.getId());

        List<FeedEvent> userLikes = feedEventService.findByUserId(user.getId());
        assertThat(userLikes)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(film.getId()));

        List<FeedEvent> user1Likes = feedEventService.findByUserId(user1.getId());
        assertThat(user1Likes)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(film.getId()));

        filmService.removeLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user1.getId());

        List<FeedEvent> userRemoves = feedEventService.findByUserId(user.getId());
        assertThat(userRemoves)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(film.getId()));

        List<FeedEvent> user1Removes = feedEventService.findByUserId(user1.getId());
        assertThat(user1Removes)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(film.getId()));
    }

    @Test
    void feedEventsCleanupAfterUserDeletion() {
        /* Тест корректности очистки событий из ленты при удалении пользователя, указанного в событиях типа FRIEND
        user1 дружит с user → событие FRIEND/ADD (entityId=user1).
        user1 удаляется из друзей → событие FRIEND/REMOVE (entityId=user1).
        user2 дружит с user → событие FRIEND/ADD (entityId=user2); всего событий у user ровно 3.
        удаление user1 → у user больше нет событий FRIEND с entityId=user1, но событие про user2 сохранилось.
        */
        User user1 = addUser("1");

        userService.addFriend(user.getId(), user1.getId());
        List<FeedEvent> afterAdd1 = feedEventService.findByUserId(user.getId());
        assertThat(afterAdd1)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user1.getId()));

        userService.removeFriend(user.getId(), user1.getId());
        List<FeedEvent> afterRemove1 = feedEventService.findByUserId(user.getId());
        assertThat(afterRemove1)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.REMOVE
                        && e.getEntityId().equals(user1.getId()));

        User user2 = addUser("2");

        userService.addFriend(user.getId(), user2.getId());
        List<FeedEvent> afterAdd2 = feedEventService.findByUserId(user.getId());
        assertThat(afterAdd2)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user2.getId()));
        assertThat(afterAdd2).hasSize(3);

        userService.delete(user1.getId());

        List<FeedEvent> afterDelete = feedEventService.findByUserId(user.getId());
        assertThat(afterDelete)
                .noneMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getEntityId().equals(user1.getId()));
        assertThat(afterDelete)
                .anyMatch(e -> e.getEventType() == FeedEventType.FRIEND
                        && e.getOperation() == FeedEventOperation.ADD
                        && e.getEntityId().equals(user2.getId()));
        assertThat(afterDelete).hasSize(1);
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

        assertThat(likeEvents(user.getId()))
                .hasSize(2);
        assertThat(likeEvents(user1.getId()))
                .hasSize(3);

        filmService.delete(film1.getId());

        List<FeedEvent> userEventsAfter = feedEventService.findByUserId(user.getId());
        List<FeedEvent> user1EventsAfter = feedEventService.findByUserId(user1.getId());

        // события для film1 удалены
        assertThat(userEventsAfter)
                .noneMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film1.getId()));
        assertThat(user1EventsAfter)
                .noneMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film1.getId()));

        assertThat(userEventsAfter)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film.getId()));
        assertThat(user1EventsAfter)
                .anyMatch(e -> e.getEventType() == FeedEventType.LIKE
                        && e.getEntityId().equals(film.getId()));

        assertThat(userEventsAfter).hasSize(1);
        assertThat(user1EventsAfter).hasSize(1);
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

    private List<FeedEvent> likeEvents(Long userId) {
        return feedEventService.findByUserId(userId)
                .stream()
                .filter(e -> e.getEventType() == FeedEventType.LIKE)
                .toList();
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
