package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    @Autowired
    private UserController controller;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM friendships");
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM users");
    }

    private User createTestUser(String suffix) {
        User user = new User();
        user.setEmail("user" + suffix + "@mail.com");
        user.setLogin("login" + suffix);
        user.setName("name" + suffix);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void create_shouldUseLoginWhenNameIsNull() {
        User user = createTestUser("1");
        user.setName(null);

        User result = controller.create(user);
        assertEquals("login1", result.getName());
    }

    @Test
    void create_shouldUseLoginWhenNameIsBlank() {
        User user = createTestUser("2");
        user.setName("   ");

        User result = controller.create(user);
        assertEquals("login2", result.getName());
    }

    @Test
    void create_shouldSucceedWhenBirthdayIsNow() {
        User user = createTestUser("3");
        user.setBirthday(LocalDate.now());

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldSucceedWhenBirthdayIsInPast() {
        User user = createTestUser("4");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldSucceedWithValidUser() {
        User user = createTestUser("5");

        User result = controller.create(user);
        assertNotNull(result.getId());
        assertEquals("user5@mail.com", result.getEmail());
        assertEquals("name5", result.getName());
    }

    @Test
    void update_shouldThrowWhenIdIsNull() {
        User update = new User();
        update.setId(null);
        update.setEmail("user@mail.com");
        update.setLogin("login");

        assertThrows(ValidationException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        User update = createTestUser("6");
        update.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldUseLoginWhenNameIsBlank() {
        User user = createTestUser("7");
        User created = controller.create(user);

        User update = createTestUser("7");
        update.setId(created.getId());
        update.setName("   ");

        User result = controller.update(update);
        assertEquals("login7", result.getName());
    }

    @Test
    void update_shouldSucceedWithValidData() {
        User user = createTestUser("8");
        User created = controller.create(user);

        User update = createTestUser("8");
        update.setId(created.getId());
        update.setEmail("newemail@mail.com");
        update.setLogin("newlogin");
        update.setName("new name");
        update.setBirthday(LocalDate.of(1990, 1, 1));

        User result = controller.update(update);
        assertEquals("newemail@mail.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("new name", result.getName());
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = createTestUser("9");
        User created = controller.create(user);

        controller.delete(created.getId());

        assertThrows(NotFoundException.class, () -> controller.findById(created.getId()));
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> controller.delete(999L));
    }
}
